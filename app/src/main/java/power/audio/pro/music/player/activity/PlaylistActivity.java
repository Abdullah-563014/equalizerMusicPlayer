package power.audio.pro.music.player.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.fragment.SongPopupFragment;
import power.audio.pro.music.player.model.Playlist;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.ThemeUtils;
import power.audio.pro.music.player.widget.BottomPlayer;
import power.audio.pro.music.player.widget.SongAdapter;

import java.io.File;
import java.util.List;

import butterknife.BindView;

public class PlaylistActivity extends ToolbarActivity implements SongAdapter.OnItemClickListener, SongAdapter.OnItemLongClickListener {
    private static final String EXTRA_FOLDER = "extra_playlist";

    private Playlist mPlaylist;

    private SongAdapter mSongAdapter;

    private MenuItem mSearchMenuItem;

    private BottomPlayer mBottomPlayer;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        mBottomPlayer = new BottomPlayer(this);

        mPlaylist = getIntent().getParcelableExtra(EXTRA_FOLDER);

        setTitle(mPlaylist.getName());

        mSongAdapter = new SongAdapter();
        mSongAdapter.setSongDetails(mPlaylist.getSongDetails());
        mSongAdapter.setOnItemClickListener(this);
        mSongAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(mSongAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        updateView();

        PlayerService.getInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_playlist);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.error,
                PlayerNotificationManager.completion,
                PlayerNotificationManager.toggleFavorite,
                PlayerNotificationManager.deleteSong,
                PlayerNotificationManager.updateTime,
                PlayerNotificationManager.removeSongFromPlaylist,
                PlayerNotificationManager.addSongsToPlaylist,
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
            refreshSongList();
            return;
        }

        if (id == PlayerNotificationManager.toggleFavorite && mPlaylist.isFavoritePlaylist()) {
            SongDetail songDetail = (SongDetail) args[0];
            if ((boolean) args[1]) {
                if (!mPlaylist.getSongDetails().contains(songDetail)) {
                    mPlaylist.getSongDetails().add(songDetail);
                    SQLHelper.getInstance().updatePlaylist(mPlaylist);
                    mSongAdapter.add(songDetail);
                }
            } else {
                onSongRemove(songDetail);
            }
            return;
        }

        if (id == PlayerNotificationManager.addSongsToPlaylist) {
            Playlist playlist = (Playlist) args[0];
            if (playlist.getId() == mPlaylist.getId()) {
                mSongAdapter.add((List<SongDetail>) args[1]);
            }
            return;
        }

        if (id == PlayerNotificationManager.removeSongFromPlaylist || id == PlayerNotificationManager.deleteSong) {
            onSongRemove((SongDetail) args[0]);
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
    public void onMoreOptionsClick(View view, SongDetail songDetail) {
        SongPopupFragment songPopupFragment = new SongPopupFragment();
        songPopupFragment.setSongAdapter(mSongAdapter);
        songPopupFragment.setSongDetail(songDetail);
        songPopupFragment.setPlaylist(mPlaylist);
        songPopupFragment.remove(R.id.action_remove_from_queue);
        if (songDetail.equals(mSongAdapter.getPlayingSong())) {
            songPopupFragment.remove(R.id.action_add_to_queue);
            songPopupFragment.remove(R.id.action_delete);
        }
        if (mPlaylist.isFavoritePlaylist()) {
            songPopupFragment.remove(R.id.action_add_to_favorites);
            songPopupFragment.remove(R.id.action_remove_from_playlist);
        } else if (SQLHelper.getInstance().isFavoriteSong(songDetail)) {
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

    private void refreshSongList() {
        List<SongDetail> songDetails = mPlaylist.getSongDetails();
        boolean updatePlaylist = false;
        for (int i = songDetails.size() - 1; i >= 0; i--) {
            SongDetail songDetail = songDetails.get(i);
            if (!new File(songDetail.getPath()).exists()) {
                updatePlaylist = true;
                mSongAdapter.remove(songDetail);
                songDetails.remove(i);
            }
        }
        if (updatePlaylist) {
            SQLHelper.getInstance().updatePlaylist(mPlaylist);
            updateView();
        }
    }

    private void onSongRemove(SongDetail songDetail) {
        mSongAdapter.remove(songDetail);
        updateView();
    }

    public static void start(@NonNull Context context, Playlist playlist) {
        Intent starter = new Intent(context, PlaylistActivity.class);
        starter.putExtra(EXTRA_FOLDER, playlist);
        context.startActivity(starter);
    }
}
