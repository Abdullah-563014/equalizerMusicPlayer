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
import power.audio.pro.music.player.activity.FolderActivity;
import power.audio.pro.music.player.activity.MainActivity;
import power.audio.pro.music.player.model.Folder;
import power.audio.pro.music.player.utils.FolderLoadTask;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.widget.FolderAdapter;

import java.util.List;

import butterknife.BindView;

public class FoldersFragment extends BaseFragment implements FolderLoadTask.LoadCallback, FolderAdapter.OnItemClickListener, FolderAdapter.OnItemLongClickListener {
    private AsyncTask mFolderLoadTask;

    private FolderAdapter mFolderAdapter;

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
        return R.layout.fragment_folders;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) {
            return;
        }

        mFolderAdapter = new FolderAdapter();
        mFolderAdapter.setOnItemClickListener(this);
        mFolderAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(mFolderAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshFolderList();
    }

    @Override
    public void onStop() {
        cancelFolderLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<Folder> folders) {
        mFolderLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mFolderAdapter.setFolders(folders);
        updateView();
    }

    @Override
    public void onItemClick(View view, Folder folder) {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setRefreshList(false);
            FolderActivity.start(getActivity(), folder);
        }
    }

    @Override
    public boolean onItemLongClick(View view, Folder folder) {
        onItemClick(view, folder);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mSearchMenuItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for_folders));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mFolderAdapter.filter(newText.trim());
                recyclerView.scrollToPosition(0);
                return true;
            }
        };

        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(onQueryTextListener);
                mFolderAdapter.filter(null);
                tvEmpty.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setOnQueryTextListener(null);
                mFolderAdapter.refresh();
                updateView();
                return true;
            }
        });

        mSearchMenuItem.setVisible(mFolderAdapter != null && !mFolderAdapter.isEmpty());
    }

    private void updateView() {
        recyclerView.setVisibility(mFolderAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(mFolderAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(!mFolderAdapter.isEmpty());
        }
    }

    private void cancelFolderLoadTask() {
        if (mFolderLoadTask != null) {
            if (!mFolderLoadTask.isCancelled()) {
                mFolderLoadTask.cancel(true);
            }
            mFolderLoadTask = null;
        }
    }

    private void refreshFolderList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelFolderLoadTask();
            mFolderLoadTask = new FolderLoadTask(this).execute();
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
            refreshFolderList();
        }
    }
}
