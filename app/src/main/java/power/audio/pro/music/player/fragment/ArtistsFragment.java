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
import power.audio.pro.music.player.activity.ArtistActivity;
import power.audio.pro.music.player.activity.MainActivity;
import power.audio.pro.music.player.model.Artist;
import power.audio.pro.music.player.utils.ArtistLoadTask;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.widget.ArtistAdapter;

import java.util.List;

import butterknife.BindView;

public class ArtistsFragment extends BaseFragment implements ArtistLoadTask.LoadCallback, ArtistAdapter.OnItemClickListener, ArtistAdapter.OnItemLongClickListener {
    private AsyncTask mArtistLoadTask;

    private ArtistAdapter mArtistAdapter;

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
        return R.layout.fragment_artists;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) {
            return;
        }

        mArtistAdapter = new ArtistAdapter();
        mArtistAdapter.setOnItemClickListener(this);
        mArtistAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(mArtistAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshArtistList();
    }

    @Override
    public void onStop() {
        cancelArtistLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<Artist> artists) {
        mArtistLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mArtistAdapter.setArtists(artists);
        updateView();
    }

    @Override
    public void onItemClick(View view, Artist artist) {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setRefreshList(false);
            ArtistActivity.start(getActivity(), artist);
        }
    }

    @Override
    public boolean onItemLongClick(View view, Artist artist) {
        onItemClick(view, artist);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mSearchMenuItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for_artists));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mArtistAdapter.filter(newText.trim());
                recyclerView.scrollToPosition(0);
                return true;
            }
        };

        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(onQueryTextListener);
                mArtistAdapter.filter(null);
                tvEmpty.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setOnQueryTextListener(null);
                mArtistAdapter.refresh();
                updateView();
                return true;
            }
        });

        mSearchMenuItem.setVisible(mArtistAdapter != null && !mArtistAdapter.isEmpty());
    }

    private void updateView() {
        recyclerView.setVisibility(mArtistAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(mArtistAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(!mArtistAdapter.isEmpty());
        }
    }

    private void cancelArtistLoadTask() {
        if (mArtistLoadTask != null) {
            if (!mArtistLoadTask.isCancelled()) {
                mArtistLoadTask.cancel(true);
            }
            mArtistLoadTask = null;
        }
    }

    private void refreshArtistList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelArtistLoadTask();
            mArtistLoadTask = new ArtistLoadTask(this).execute();
        } else {
            refreshList = true;
        }
    }

    public void setRefreshList(boolean refreshList) {
        this.refreshList = refreshList;
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{PlayerNotificationManager.deleteSong, PlayerNotificationManager.playlistEmpty};
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

        if (id == PlayerNotificationManager.deleteSong || id == PlayerNotificationManager.playlistEmpty) {
            refreshList = true;
            refreshArtistList();
        }
    }
}
