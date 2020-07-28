package power.audio.pro.music.player.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.activity.PlayerActivity;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.SongLoadTask;
import power.audio.pro.music.player.widget.SongAdapter;

import java.util.List;

import butterknife.BindView;

public class SongsFragment extends BaseFragment implements SongLoadTask.LoadCallback, SongAdapter.OnItemClickListener, SongAdapter.OnItemLongClickListener {
    private AsyncTask mSongLoadTask;

    private SongAdapter mSongAdapter;

    private boolean refreshList = true;

    private MenuItem mSearchMenuItem;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_songs;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) {
            return;
        }

        mSongAdapter = new SongAdapter();
        mSongAdapter.setOnItemClickListener(this);
        mSongAdapter.setOnItemLongClickListener(this);

        recyclerView.setAdapter(mSongAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        PlayerService.getInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshSongList();
    }

    @Override
    public void onStop() {
        cancelSongLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<SongDetail> songDetails) {
        mSongLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mSongAdapter.setSongDetails(songDetails);
        updateView();
    }

    @Override
    public void onItemClick(View view, SongDetail songDetail) {
        if (getActivity() != null) {
            if (!(mSongAdapter.getPlaying() && songDetail.equals(mSongAdapter.getPlayingSong()))) {
                PlayerService.setPlaylist(mSongAdapter.getSongDetails());
                PlayerService.setPlayingSong(songDetail);
                PlayerService.playSong();
                PlayerActivity.start(getActivity());
            }else{
                PlayerService.setPlaylist(mSongAdapter.getSongDetails());
                PlayerService.setPlayingSong(songDetail);
                PlayerService.togglePlayPause();
            }
        }
    }

    @Override
    public void onMoreOptionsClick(View view, final SongDetail songDetail) {
        if (getContext() == null || getActivity() == null) {
            return;
        }

        SongPopupFragment songPopupFragment = new SongPopupFragment();
        songPopupFragment.setSongAdapter(mSongAdapter);
        songPopupFragment.setSongDetail(songDetail);
        songPopupFragment.setCallback(new SongPopupFragment.Callback() {
            @Override
            public void disableRefreshList() {
                refreshList = false;
            }
        });
        songPopupFragment.remove(R.id.action_remove_from_playlist);
        songPopupFragment.remove(R.id.action_remove_from_queue);
        if (songDetail.equals(mSongAdapter.getPlayingSong())) {
            songPopupFragment.remove(R.id.action_add_to_queue);
            songPopupFragment.remove(R.id.action_delete);
        }
        if (SQLHelper.getInstance().isFavoriteSong(songDetail)) {
            songPopupFragment.remove(R.id.action_add_to_favorites);
        } else {
            songPopupFragment.remove(R.id.action_remove_from_favorites);
        }
        songPopupFragment.show(getChildFragmentManager());
    }

    @Override
    public boolean onItemLongClick(View view, SongDetail songDetail) {
        onMoreOptionsClick(view, songDetail);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

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
    }

    private void updateView() {
        recyclerView.setVisibility(mSongAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(mSongAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(!mSongAdapter.isEmpty());
        }
    }

    private void cancelSongLoadTask() {
        if (mSongLoadTask != null) {
            if (!mSongLoadTask.isCancelled()) {
                mSongLoadTask.cancel(true);
            }
            mSongLoadTask = null;
        }
    }

    private void refreshSongList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelSongLoadTask();
            mSongLoadTask = new SongLoadTask(this).execute();
        } else {
            refreshList = true;
        }
    }

    public void setRefreshList(boolean refreshList) {
        this.refreshList = refreshList;
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.completion,
                PlayerNotificationManager.error,
                PlayerNotificationManager.deleteSong,
                PlayerNotificationManager.playlistEmpty,
                PlayerNotificationManager.info
        };
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

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
}
