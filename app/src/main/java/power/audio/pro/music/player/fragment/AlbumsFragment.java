package power.audio.pro.music.player.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.activity.AlbumActivity;
import power.audio.pro.music.player.activity.MainActivity;
import power.audio.pro.music.player.model.Album;
import power.audio.pro.music.player.utils.AlbumLoadTask;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.widget.AlbumAdapter;

import java.util.List;

import butterknife.BindView;

public class AlbumsFragment extends BaseFragment implements AlbumLoadTask.LoadCallback, AlbumAdapter.OnItemClickListener, AlbumAdapter.OnItemLongClickListener {
    private AsyncTask mAlbumLoadTask;

    private AlbumAdapter mAlbumAdapter;

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
        return R.layout.fragment_albums;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) {
            return;
        }

        mAlbumAdapter = new AlbumAdapter();
        mAlbumAdapter.setOnItemClickListener(this);
        mAlbumAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(mAlbumAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshAlbumList();
    }

    @Override
    public void onStop() {
        cancelAlbumLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<Album> albums) {
        mAlbumLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mAlbumAdapter.setAlbums(albums);
        updateView();
    }

    @Override
    public void onItemClick(View view, Album album) {
        if (mSearchMenuItem != null && mSearchMenuItem.isActionViewExpanded()) {
            mSearchMenuItem.collapseActionView();
        }
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setRefreshList(false);
            AlbumActivity.start(getActivity(), album);
        }
    }

    @Override
    public boolean onItemLongClick(View view, Album album) {
        onItemClick(view, album);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mSearchMenuItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for_albums));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAlbumAdapter.filter(newText.trim());
                recyclerView.scrollToPosition(0);
                return true;
            }
        };

        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(onQueryTextListener);
                mAlbumAdapter.filter(null);
                tvEmpty.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setOnQueryTextListener(null);
                mAlbumAdapter.refresh();
                updateView();
                return true;
            }
        });

        mSearchMenuItem.setVisible(mAlbumAdapter != null && !mAlbumAdapter.isEmpty());
    }

    private void updateView() {
        recyclerView.setVisibility(mAlbumAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(mAlbumAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(!mAlbumAdapter.isEmpty());
        }
    }

    private void cancelAlbumLoadTask() {
        if (mAlbumLoadTask != null) {
            if (!mAlbumLoadTask.isCancelled()) {
                mAlbumLoadTask.cancel(true);
            }
            mAlbumLoadTask = null;
        }
    }

    private void refreshAlbumList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelAlbumLoadTask();
            mAlbumLoadTask = new AlbumLoadTask(this).execute();
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
            refreshAlbumList();
        }
    }
}
