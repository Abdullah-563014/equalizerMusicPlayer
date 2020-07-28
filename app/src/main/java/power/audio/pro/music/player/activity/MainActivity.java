package power.audio.pro.music.player.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ahihi.moreapps.MoreAppDialog;
import com.ahihi.moreapps.util.GlideApp;
import com.ahihi.ratedialog.RateDialog;
import com.nvp.easypermissions.NvpPermission;
import com.nvp.easypermissions.PermissionListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.fragment.AlbumsFragment;
import power.audio.pro.music.player.fragment.ArtistsFragment;
import power.audio.pro.music.player.fragment.FoldersFragment;
import power.audio.pro.music.player.fragment.GenresFragment;
import power.audio.pro.music.player.fragment.PlaylistFragment;
import power.audio.pro.music.player.fragment.SongsFragment;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SpUtils;
import power.audio.pro.music.player.utils.ThemeUtils;
import power.audio.pro.music.player.widget.BottomPlayer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity
        implements
        ViewPager.OnPageChangeListener,
        PermissionListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String ACTION_EXPAND_PLAYER = "power.audio.pro.music.player.activity.EXPAND_PLAYER";

    public static final String PREF_TIMER_STOP_TIME = "timer_stop_time";

    private static final String PREF_OPEN_COUNT = "open_count";
    private static final String PREF_LAST_FRAGMENT = "last_fragment";

    private MyFragmentPagerAdapter mPagerAdapter;

    private MenuItem mSearchMenuItem;


    private MoreAppDialog mMoreAppDialog;

    private BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTimeRemaining();
        }
    };
    private BottomPlayer mBottomPlayer;

    private AlertDialog mTimerDialog;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_header)
    ImageView navHeader;
    @BindView(R.id.tv_time_remaining)
    TextView tvTimeRemaining;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    SmartTabLayout mTabLayout;

    //tab sequence
    int[] savedTabSeqInt = {0,1,2,3,4,5};

    private static final String TAG = MainActivity.class.getSimpleName();
    DrawerLayout rl;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setSupportActionBar(mToolbar);
        setTitle("Music Equalizer");
        mBottomPlayer = new BottomPlayer(this);
        mBottomPlayer.setBeforePlayerActivityShown(new BottomPlayer.BeforePlayerActivityShown() {
            @Override
            public void beforePlayerActivityShown() {
                setRefreshList(false);
            }
        });

        String lang = "KR";//Locale.getDefault().getLanguage();

        NvpPermission.with(this)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .setPermissionListener(this)
                .check();

        if (SpUtils.getInt("selected_theme_item") > 0) {
            Random rnd = new Random();
            int random = getRandomWithExclusion(rnd, 1, 7, SpUtils.getInt("selected_theme_item"));
            SpUtils.putInt("selected_theme_item", random);
        } else {
            Random rnd = new Random();
            int random = getRandomWithExclusion(rnd, 1, 7, -1);
            SpUtils.putInt("selected_theme_item", random);
        }
    }

    public int getRandomWithExclusion(Random rnd, int start, int end, int... exclude) {
        int random = start + rnd.nextInt(end - start + 1 - exclude.length);
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }


    @Override
    protected void onResume() {
        super.onResume();

        relativeLayout = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    public void onPermissionGranted() {
        mPagerAdapter = new MyFragmentPagerAdapter();
        mPagerAdapter.add(new SongsFragment(), R.string.songs);
        mPagerAdapter.add(new PlaylistFragment(), R.string.playlist);
        mPagerAdapter.add(new GenresFragment(), R.string.genres);
        mPagerAdapter.add(new AlbumsFragment(), R.string.albums);
        mPagerAdapter.add(new FoldersFragment(), R.string.folders);
        mPagerAdapter.add(new ArtistsFragment(), R.string.artists);


        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);
        mViewPager.setCurrentItem(SpUtils.getInt(PREF_LAST_FRAGMENT));

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        mViewPager.setPageTransformer(true, new CubeTransformer());
        viewPagerTab.setViewPager(mViewPager);

        if (ACTION_EXPAND_PLAYER.equals(getIntent().getAction())) {
            PlayerActivity.start(this);
        }
    }

    @Override
    public void onPermissionDenied(@NonNull ArrayList<String> deniedPermissions) {
        finish();
    }

    @Override
    protected void onDestroy() {
        SpUtils.putInt(PREF_LAST_FRAGMENT, mViewPager.getCurrentItem());
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (item.getItemId() == android.R.id.home) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                    return true;
                }
                break;
            case R.id.action_equ:
                try {
                    startActivity( new Intent(this,ActivityEqualizer.class));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_sleep_timer:
                showTimerDialog();
                break;

            case R.id.action_theme:
                try {
                    startActivity( new Intent(this,SelectThemeActivity.class));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (!showRateDialog()) {
            super.onBackPressed();
        }
    }

    private boolean showRateDialog() {
        if (!SpUtils.getBoolean(RateDialog.PREF_SHOW_RATE, true)) {
            return false;
        }

        int openCount = SpUtils.getInt(PREF_OPEN_COUNT);
        if (openCount < 4) {
            SpUtils.putInt(PREF_OPEN_COUNT, openCount + 1);
            return false;
        }

        return new Random().nextInt(2) == 0 &&
                new RateDialog(this).setButtonVisibility(RateDialog.BUTTON_NOT_GOOD, View.GONE).show();

    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.completion,
                PlayerNotificationManager.updateTime,
                PlayerNotificationManager.playlistEmpty,
                PlayerNotificationManager.info
        };
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (mSearchMenuItem != null && mSearchMenuItem.isActionViewExpanded()) {
            mSearchMenuItem.collapseActionView();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @OnClick({R.id.nav_songs, R.id.nav_playlist,
            R.id.nav_folders, R.id.nav_albums,
            R.id.nav_artists, R.id.nav_privacy_policy,
            R.id.nav_rate_app, R.id.nav_more_apps, R.id.nav_equalizer,
            R.id.nav_share_app, R.id.nav_timer})
    void onNavigationItemSelected(View view) {
        mDrawerLayout.closeDrawer(GravityCompat.START);

        switch (view.getId()) {
            case R.id.nav_privacy_policy:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://bd-super.blogspot.com/p/music-player.html")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.nav_more_apps:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id="+getPackageName())));
                } catch (ActivityNotFoundException e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id="+getPackageName())));
                    } catch (ActivityNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
                break;
            case R.id.nav_rate_app:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (Exception e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                break;
            case R.id.nav_share_app:
                try {
                    startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                            .putExtra(Intent.EXTRA_TEXT, "Hey check out my app at: https://play.google.com/store/apps/details?id=" + getPackageName())
                            .setType("text/plain"), getString(R.string.share)));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.nav_timer:
                showTimerDialog();
                break;
            case R.id.nav_songs:
                if (mViewPager.getCurrentItem() != 0) {
                    mViewPager.setCurrentItem(0);
                }
                break;
            case R.id.nav_playlist:
                if (mViewPager.getCurrentItem() != 1) {
                    mViewPager.setCurrentItem(1);
                }
                break;
            case R.id.nav_folders:
                if (mViewPager.getCurrentItem() != 4) {
                    mViewPager.setCurrentItem(4);
                }
                break;
            case R.id.nav_albums:
                if (mViewPager.getCurrentItem() != 3) {
                    mViewPager.setCurrentItem(3);
                }
                break;
            case R.id.nav_artists:
                if (mViewPager.getCurrentItem() != 5) {
                    mViewPager.setCurrentItem(5);
                }
                break;
            case R.id.nav_equalizer:
                try {
                    startActivity( new Intent(this,ActivityEqualizer.class));
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void showTimerDialog() {
        if (mTimerDialog == null) {
            mTimerDialog = new AlertDialog.Builder(this, R.style.MyAlertDialog)
                    .setView(R.layout.dialog_timer)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (((SwitchCompat) mTimerDialog.findViewById(R.id.timer_switch)).isChecked()) {
                                SpUtils.putLong(PREF_TIMER_STOP_TIME, Calendar.getInstance().getTimeInMillis() + (((SeekBar) mTimerDialog.findViewById(R.id.sb_timer)).getProgress() + 5) * 60 * 1000);
                            } else {
                                SpUtils.remove(PREF_TIMER_STOP_TIME);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            mTimerDialog.show();
        }
        updateTimeDialog();
    }

    private void updateTimeDialog() {
        if (mTimerDialog == null || !mTimerDialog.isShowing()) {
            return;
        }

        final SwitchCompat timerSwitch = mTimerDialog.findViewById(R.id.timer_switch);
        final SeekBar sbTimer = mTimerDialog.findViewById(R.id.sb_timer);
        final TextView tvTimeRemaining = mTimerDialog.findViewById(R.id.tv_time_remaining);
        final TextView tvSelectedTime = mTimerDialog.findViewById(R.id.tv_selected_time);
        final TextView tvMaxTime = mTimerDialog.findViewById(R.id.tv_max_time);

        int maxTime = 115;

        sbTimer.setProgress(0);
        sbTimer.setMax(maxTime);
        sbTimer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvSelectedTime.setText((progress + 5) + "'");
                    tvTimeRemaining.setText((progress + 5) + "'");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                timerSwitch.setChecked(true);
            }
        });

        tvSelectedTime.setText((sbTimer.getProgress() + 5) + "'");
        tvMaxTime.setText((maxTime + 5) + "'");

        final long stopTimeMillis = SpUtils.getLong(PREF_TIMER_STOP_TIME, -1);
        final long now = Calendar.getInstance().getTimeInMillis();
        final long timeRemaining = (stopTimeMillis - now) / 60 / 1000;

        timerSwitch.setChecked(stopTimeMillis > now);
        timerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvTimeRemaining.setText((sbTimer.getProgress() + 5) + "'");
                    tvTimeRemaining.setVisibility(View.VISIBLE);
                } else {
                    tvTimeRemaining.setVisibility(View.GONE);
                }
            }
        });

        tvTimeRemaining.setText(String.valueOf(timeRemaining) + "'");
        tvTimeRemaining.setVisibility(stopTimeMillis > now ? View.VISIBLE : View.GONE);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PREF_TIMER_STOP_TIME.equals(key)) {
            updateTimeRemaining();
            updateTimeDialog();
        }
    }

    private void updateTimeRemaining() {
        long timeRemaining = SpUtils.getLong(PREF_TIMER_STOP_TIME, -1) - Calendar.getInstance().getTimeInMillis();
        if (timeRemaining > 0) {
            tvTimeRemaining.setText(String.valueOf(timeRemaining / 60 / 1000) + "'");
            tvTimeRemaining.setVisibility(View.VISIBLE);
        } else {
            tvTimeRemaining.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateTimeRemaining();
        SpUtils.registerOnSharedPreferenceChangeListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(mTimeChangedReceiver, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mTimeChangedReceiver);
        SpUtils.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    public void setRefreshList(boolean refreshList) {
        ((SongsFragment) mPagerAdapter.getItem(0)).setRefreshList(refreshList);
        ((PlaylistFragment) mPagerAdapter.getItem(1)).setRefreshList(refreshList);
        ((GenresFragment) mPagerAdapter.getItem(2)).setRefreshList(refreshList);
        ((AlbumsFragment) mPagerAdapter.getItem(3)).setRefreshList(refreshList);
        ((FoldersFragment) mPagerAdapter.getItem(4)).setRefreshList(refreshList);
        ((ArtistsFragment) mPagerAdapter.getItem(5)).setRefreshList(refreshList);
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

        mBottomPlayer.onReceivedPlayerNotification(id, args);

        if (id == PlayerNotificationManager.start) {
            if (!(boolean) args[1]) {
                GlideApp.with(this)
                        .load(((SongDetail) args[0]).getCoverUri())
                        .centerCrop()
                        .error(R.drawable.toolbar_bg)
                        .placeholder(R.drawable.toolbar_bg)
                        .into(navHeader);
            }
            return;
        }

        if (id == PlayerNotificationManager.info) {
            if (args.length > 0 && args[0] != null && ((List<SongDetail>) args[0]).size() > 0) {
                GlideApp.with(this)
                        .load(((SongDetail) args[1]).getCoverUri())
                        .centerCrop()
                        .error(R.drawable.toolbar_bg)
                        .placeholder(R.drawable.toolbar_bg)
                        .into(navHeader);
            }
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        MyFragmentPagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        public void add(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        public void add(Fragment fragment, @StringRes int titleRes) {
            add(fragment, getString(titleRes));
        }
    }

    public class CubeTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.65f;
        private static final float MIN_ALPHA = 0.3f;

        @Override
        public void transformPage(View page, float position) {

            if (position < -1) {  // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                page.setScaleX(Math.max(MIN_SCALE, 1 - Math.abs(position)));
                page.setScaleY(Math.max(MIN_SCALE, 1 - Math.abs(position)));
                page.setAlpha(Math.max(MIN_ALPHA, 1 - Math.abs(position)));
            } else {  // (1,+Infinity]
                // This page is way off-screen to the right.
                page.setAlpha(0);

            }
        }
    }
}
