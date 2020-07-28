package power.audio.pro.music.player.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahihi.moreapps.util.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.fragment.SongPopupFragment;
import power.audio.pro.music.player.model.Album;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.FastBlur;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.SongAlbumLoadTask;
import power.audio.pro.music.player.utils.ThemeUtils;
import power.audio.pro.music.player.widget.BottomPlayer;
import power.audio.pro.music.player.widget.SongInAlbumAdapter;

import java.util.List;

import butterknife.BindView;

public class AlbumActivity extends power.audio.pro.music.player.activity.ToolbarActivity implements SongAlbumLoadTask.LoadCallback, SongInAlbumAdapter.OnItemClickListener, SongInAlbumAdapter.OnItemLongClickListener {
    private static final String EXTRA_ALBUM = "extra_album";

    private AsyncTask mSongAlbumLoadTask;

    private Album mAlbum;

    private SongInAlbumAdapter mSongAdapter;

    private boolean refreshList = true;

    private MenuItem mSearchMenuItem;

    private BottomPlayer mBottomPlayer;

    @BindView(R.id.iv_album_art)
    ImageView mAlbumArt;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.tv_album_title)
    TextView tvAlbumTitle;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.iv_blur_bg)
    ImageView ivBlurBg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        mBottomPlayer = new BottomPlayer(this);
        mBottomPlayer.setBeforePlayerActivityShown(new BottomPlayer.BeforePlayerActivityShown() {
            @Override
            public void beforePlayerActivityShown() {
                refreshList = false;
            }
        });

        mAlbum = getIntent().getParcelableExtra(EXTRA_ALBUM);

        setTitle("");
        tvAlbumTitle.setText(mAlbum.getTitle());

        GlideApp.with(this)
                .load(mAlbum.getCover())
                .circleCrop()
                .error(R.drawable.default_album_circle)
                .placeholder(R.drawable.default_album_circle)
                .into(mAlbumArt);

        GlideApp.with(this)
                .asBitmap()
                .centerCrop()
                .error(R.drawable.bg_activity)
                .placeholder(R.drawable.bg_activity)
                .load(mAlbum.getCover())
                .into(new SimpleTarget<Bitmap>() {
                          @Override
                          public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                              ivBlurBg.setImageBitmap(FastBlur.doBlur(resource, 50, true));
                          }

                          @Override
                          public void onLoadFailed(@Nullable Drawable errorDrawable) {
                              Bitmap bitmap = null;

                              if (errorDrawable != null) {
                                  if (errorDrawable instanceof BitmapDrawable) {
                                      bitmap = ((BitmapDrawable) errorDrawable).getBitmap();
                                  } else {
                                      int intrinsicWidth = errorDrawable.getIntrinsicWidth();
                                      int intrinsicHeight = errorDrawable.getIntrinsicHeight();
                                      if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                                          bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
                                          Canvas canvas = new Canvas(bitmap);
                                          errorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                          errorDrawable.draw(canvas);
                                      }
                                  }
                              }

                              if (bitmap != null) {
                                  ivBlurBg.setImageBitmap(bitmap);
                              }
                          }
                      }
                );

        mSongAdapter = new SongInAlbumAdapter();
        mSongAdapter.setOnItemClickListener(this);
        mSongAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(mSongAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        PlayerService.getInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_album);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.error,
                PlayerNotificationManager.completion,
                PlayerNotificationManager.deleteSong,
                PlayerNotificationManager.updateTime,
                PlayerNotificationManager.playlistEmpty,
                PlayerNotificationManager.info
        };
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

        mBottomPlayer.onReceivedPlayerNotification(id, args);

        if (id == PlayerNotificationManager.start) {
            mSongAdapter.setPlayingSong((SongDetail) args[0]);
            mSongAdapter.setPlaying(true);
            return;
        }

        if (id == PlayerNotificationManager.pause) {
            mSongAdapter.setPlaying(false);
            return;
        }

        if (id == PlayerNotificationManager.completion || id == PlayerNotificationManager.error) {
            mSongAdapter.setPlayingSong(null);
            mSongAdapter.setPlaying(false);
            return;
        }

        if (id == PlayerNotificationManager.playlistEmpty) {
            mSongAdapter.setPlayingSong(null);
            mSongAdapter.setPlaying(false);
            refreshList = true;
            refreshSongList();
            return;
        }

        if (id == PlayerNotificationManager.deleteSong) {
            mSongAdapter.remove((SongDetail) args[0]);
            updateView();
            return;
        }

        if (id == PlayerNotificationManager.info) {
            if (args.length > 0 && args[0] != null) {
                mSongAdapter.setPlayingSong((SongDetail) args[1]);
                mSongAdapter.setPlaying((boolean) args[2]);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshSongList();
    }

    @Override
    public void onStop() {
        cancelSongAlbumLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<SongDetail> songDetails) {
        mSongAlbumLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mSongAdapter.setSongDetails(songDetails);
        updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchMenuItem.setVisible(mSongAdapter != null && !mSongAdapter.isEmpty());

        final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for_songs));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSongAdapter.filter(newText.trim());
                recyclerView.scrollToPosition(0);
                return true;
            }
        };

        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(onQueryTextListener);
                mSongAdapter.filter(null);
                tvEmpty.setVisibility(View.GONE);
                tvAlbumTitle.setText("");
                mAlbumArt.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setOnQueryTextListener(null);
                mSongAdapter.refresh();
                tvAlbumTitle.setText(mAlbum.getTitle());
                mAlbumArt.setVisibility(View.VISIBLE);
                updateView();
                return true;
            }
        });

        mSearchMenuItem.setVisible(mSongAdapter != null && !mSongAdapter.isEmpty());

        return true;
    }

    @Override
    public void onItemClick(View view, SongDetail songDetail) {
        PlayerService.setPlaylist(mSongAdapter.getSongDetails());
        PlayerService.setPlayingSong(songDetail);
        PlayerService.playSong();
    }

    @Override
    public void onMoreOptionsClick(View view, final SongDetail songDetail) {
        SongPopupFragment songPopupFragment = new SongPopupFragment();
        songPopupFragment.setSongInAlbumAdapter(mSongAdapter);
        songPopupFragment.setSongDetail(songDetail);
        songPopupFragment.setCallback(new SongPopupFragment.Callback() {
            @Override
            public void disableRefreshList() {
                refreshList = false;
            }
        });
        songPopupFragment.remove(R.id.action_remove_from_queue);
        songPopupFragment.remove(R.id.action_remove_from_playlist);
        if (songDetail.equals(mSongAdapter.getPlayingSong())) {
            songPopupFragment.remove(R.id.action_add_to_queue);
            songPopupFragment.remove(R.id.action_delete);
        }
        if (SQLHelper.getInstance().isFavoriteSong(songDetail)) {
            songPopupFragment.remove(R.id.action_add_to_favorites);
        } else {
            songPopupFragment.remove(R.id.action_remove_from_favorites);
        }
        songPopupFragment.show(getSupportFragmentManager());
    }

    @Override
    public boolean onItemLongClick(View view, SongDetail songDetail) {
        onMoreOptionsClick(view, songDetail);
        return true;
    }

    private void updateView() {
        recyclerView.setVisibility(mSongAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(mSongAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(!mSongAdapter.isEmpty());
        }
    }

    private void cancelSongAlbumLoadTask() {
        if (mSongAlbumLoadTask != null) {
            if (!mSongAlbumLoadTask.isCancelled()) {
                mSongAlbumLoadTask.cancel(true);
            }
            mSongAlbumLoadTask = null;
        }
    }

    private void refreshSongList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelSongAlbumLoadTask();
            mSongAlbumLoadTask = new SongAlbumLoadTask(mAlbum, this).execute();
        } else {
            refreshList = true;
        }
    }

    public static void start(@NonNull Context context, Album album) {
        Intent starter = new Intent(context, AlbumActivity.class);
        starter.putExtra(EXTRA_ALBUM, album);
        context.startActivity(starter);
    }
}
