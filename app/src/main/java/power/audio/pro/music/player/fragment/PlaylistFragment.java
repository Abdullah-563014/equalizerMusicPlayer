package power.audio.pro.music.player.fragment;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.activity.MainActivity;
import power.audio.pro.music.player.activity.PlaylistActivity;
import power.audio.pro.music.player.model.Playlist;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.PlaylistLoadTask;
import power.audio.pro.music.player.utils.PlaylistUtils;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.widget.PlaylistAdapter;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class PlaylistFragment extends BaseFragment implements PlaylistLoadTask.LoadCallback, PlaylistAdapter.OnItemClickListener, PlaylistAdapter.OnItemLongClickListener {
    private AsyncTask mPlaylistLoadTask;

    private PlaylistAdapter mPlaylistAdapter;

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
        return R.layout.fragment_playlists;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) {
            return;
        }

        mPlaylistAdapter = new PlaylistAdapter();
        mPlaylistAdapter.setOnItemClickListener(this);
        mPlaylistAdapter.setOnItemLongClickListener(this);
//        mPlaylistAdapter.setAdsLoaderCallback(new PlaylistAdapter.AdsLoaderCallback() {
//            @Override
//            public boolean isLoad() {
//                return getActivity() != null && !getActivity().isFinishing();
//            }
//        });
        recyclerView.setAdapter(mPlaylistAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshPlaylistList();
    }

    @Override
    public void onStop() {
        cancelPlaylistLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<Playlist> playlists) {
        mPlaylistLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mPlaylistAdapter.setPlaylists(playlists);
        updateView();
    }

    @Override
    public void onItemClick(View view, Playlist playlist) {
        if (getActivity() != null) {
            if (playlist.isCreatePlaylist()) {
                PlaylistUtils.showAddNewPlaylistDialog(getActivity());
            } else {
                ((MainActivity) getActivity()).setRefreshList(false);
                PlaylistActivity.start(getActivity(), playlist);
            }
        }
    }

    @Override
    public void onMoreOptionsClick(View view, final Playlist playlist) {
        if (playlist.isFavoritePlaylist() || getActivity() == null) {
            return;
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog)
                .setView(R.layout.dialog_playlist_popup)
                .show();

        ((TextView) alertDialog.findViewById(R.id.tv_playlist_name)).setText(playlist.getName());
        ((TextView) alertDialog.findViewById(R.id.tv_playlist_song_count)).setText(String.format(Locale.getDefault(), "%d %s", playlist.getSongDetails().size(),
                getString(playlist.getSongDetails().size() <= 1 ? R.string.one_song : R.string.n_songs)));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

                switch (v.getId()) {
                    case R.id.action_rename:
                        showRenameDialog(playlist);
                        break;
                    case R.id.action_delete:
                        showDeleteDialog(playlist);
                        break;
                }
            }
        };

        alertDialog.findViewById(R.id.action_rename).setOnClickListener(listener);
        alertDialog.findViewById(R.id.action_delete).setOnClickListener(listener);
    }

    private void showDeleteDialog(final Playlist playlist) {
        if (getActivity() == null) {
            return;
        }

        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog)
                .setTitle(R.string.confirm)
                .setMessage(R.string.msg_confirm_delete_playlist)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLHelper.getInstance().removePlaylist(playlist);
                        mPlaylistAdapter.remove(playlist);
                        updateView();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showRenameDialog(final Playlist playlist) {
        if (getActivity() == null) {
            return;
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog)
                .setTitle(R.string.new_playlist)
                .setView(R.layout.dialog_edit_text)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();

        final TextInputLayout textInputLayout = alertDialog.findViewById(R.id.text_input_layout);
        final EditText editText = alertDialog.findViewById(R.id.edit_text);

        editText.setText(playlist.getName());
        editText.setSelection(editText.length());

        final List<String> names = SQLHelper.getInstance().getPlaylistNames();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate(textInputLayout, names, playlist.getName(), s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString().trim();
                editText.setText(text);
                editText.setSelection(text.length());

                if (!validate(textInputLayout, names, playlist.getName(), text)) {
                    return;
                }

                playlist.setName(text);
                SQLHelper.getInstance().updatePlaylist(playlist);
                mPlaylistAdapter.addOrUpdate(playlist);

                alertDialog.dismiss();
            }
        });
    }

    private boolean validate(TextInputLayout textInputLayout, List<String> names, String defaultName, String newName) {
        if (defaultName.equals(newName)) {
            return true;
        }

        if (TextUtils.isEmpty(newName)) {
            textInputLayout.setError(getString(R.string.msg_not_be_empty));
            return false;
        }

        if (getString(R.string.favorite).equals(newName) || names.contains(newName)) {
            textInputLayout.setError(getString(R.string.msg_playlist_already_exists));
            return false;
        }

        textInputLayout.setError(null);

        return true;
    }

    @Override
    public boolean onItemLongClick(View view, Playlist playlist) {
        onMoreOptionsClick(view, playlist);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mSearchMenuItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for_playlist));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mPlaylistAdapter.filter(newText.trim());
                recyclerView.scrollToPosition(0);
                return true;
            }
        };

        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(onQueryTextListener);
                mPlaylistAdapter.filter(null);
                tvEmpty.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setOnQueryTextListener(null);
                mPlaylistAdapter.refresh();
                updateView();
                return true;
            }
        });

        mSearchMenuItem.setVisible(mPlaylistAdapter != null && !mPlaylistAdapter.isEmpty());
    }

    private void updateView() {
        recyclerView.setVisibility(mPlaylistAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(mPlaylistAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(!mPlaylistAdapter.isEmpty());
        }
    }

    private void cancelPlaylistLoadTask() {
        if (mPlaylistLoadTask != null) {
            if (!mPlaylistLoadTask.isCancelled()) {
                mPlaylistLoadTask.cancel(true);
            }
            mPlaylistLoadTask = null;
        }
    }

    private void refreshPlaylistList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelPlaylistLoadTask();
            mPlaylistLoadTask = new PlaylistLoadTask(this).execute();
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
                PlayerNotificationManager.toggleFavorite,
                PlayerNotificationManager.addSongsToPlaylist,
                PlayerNotificationManager.removeSongFromPlaylist,
                PlayerNotificationManager.playlistEmpty,
                PlayerNotificationManager.deleteSong
        };
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

        if (id == PlayerNotificationManager.toggleFavorite) {
            mPlaylistAdapter.updateFavorite((SongDetail) args[0], (boolean) args[1]);
            return;
        }

        if (id == PlayerNotificationManager.addSongsToPlaylist) {
            mPlaylistAdapter.addOrUpdate((Playlist) args[0]);
            return;
        }

        if (id == PlayerNotificationManager.removeSongFromPlaylist || id == PlayerNotificationManager.deleteSong || id == PlayerNotificationManager.playlistEmpty) {
            refreshList = true;
            refreshPlaylistList();
        }
    }
}