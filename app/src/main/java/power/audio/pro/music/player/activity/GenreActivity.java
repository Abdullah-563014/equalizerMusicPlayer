package power.audio.pro.music.player.activity;

import android.content.Context;
import android.content.Intent;
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

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.fragment.SongPopupFragment;
import power.audio.pro.music.player.model.Genre;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.SongGenreLoadTask;
import power.audio.pro.music.player.utils.ThemeUtils;
import power.audio.pro.music.player.widget.BottomPlayer;
import power.audio.pro.music.player.widget.SongInGenreAdapter;

import java.util.List;

import butterknife.BindView;

public class GenreActivity extends ToolbarActivity implements SongGenreLoadTask.LoadCallback, SongInGenreAdapter.OnItemClickListener, SongInGenreAdapter.OnItemLongClickListener {
    private static final String EXTRA_GENRE = "extra_genre";

    private AsyncTask mSongFolderLoadTask;

    private Genre mFolder;

    private SongInGenreAdapter mSongAdapter;

    private boolean refreshList = true;

    private MenuItem mSearchMenuItem;

    private BottomPlayer mBottomPlayer;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_album_title)
    TextView tvAlbumTitle;
    @BindView(R.id.iv_album_art)
    ImageView imgGenre;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    RelativeLayout relativeLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);

        mBottomPlayer = new BottomPlayer(this);
        mBottomPlayer.setBeforePlayerActivityShown(new BottomPlayer.BeforePlayerActivityShown() {
            @Override
            public void beforePlayerActivityShown() {
                refreshList = false;
            }
        });

        mFolder = getIntent().getParcelableExtra(EXTRA_GENRE);

        setTitle("");
        tvAlbumTitle.setText(mFolder.getName());
        Drawable myImage = getResources().getDrawable(mFolder.getCover());
        GlideApp.with(this)
                .load(myImage)
                .circleCrop()
                .error(R.drawable.default_album_circle)
                .placeholder(R.drawable.default_album_circle)
                .into(imgGenre);
        mSongAdapter = new SongInGenreAdapter();
        mSongAdapter.setShowArtistName(false);
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

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_genre);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.error,
                PlayerNotificationManager.deleteSong,
                PlayerNotificationManager.completion,
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
        cancelSongFolderLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<SongDetail> songDetails) {
        mSongFolderLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mSongAdapter.setSongDetails(songDetails);
        updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchMenuItem = menu.findItem(R.id.action_search);

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
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setOnQueryTextListener(null);
                mSongAdapter.refresh();
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
        songPopupFragment.setSongInGenreAdapter(mSongAdapter);
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

    private void cancelSongFolderLoadTask() {
        if (mSongFolderLoadTask != null) {
            if (!mSongFolderLoadTask.isCancelled()) {
                mSongFolderLoadTask.cancel(true);
            }
            mSongFolderLoadTask = null;
        }
    }

    private void refreshSongList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelSongFolderLoadTask();
            mSongFolderLoadTask = new SongGenreLoadTask(mFolder, this).execute();
        } else {
            refreshList = true;
        }
    }

    public static void start(@NonNull Context context, Genre folder) {
        Intent starter = new Intent(context, GenreActivity.class);
        starter.putExtra(EXTRA_GENRE, folder);
        context.startActivity(starter);
    }
}
