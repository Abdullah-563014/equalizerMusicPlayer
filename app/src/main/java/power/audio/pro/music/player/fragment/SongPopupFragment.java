package power.audio.pro.music.player.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahihi.moreapps.util.GlideApp;
import com.nvp.util.NvpUtils;
import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.model.Playlist;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.PlaylistUtils;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.SafUtils;
import power.audio.pro.music.player.utils.SpUtils;
import power.audio.pro.music.player.utils.ToastUtils;
import power.audio.pro.music.player.widget.SongAdapter;
import power.audio.pro.music.player.widget.SongInAlbumAdapter;
import power.audio.pro.music.player.widget.SongInGenreAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongPopupFragment extends DialogFragment implements DialogInterface.OnShowListener {
    private static final int REQUEST_CODE_WRITE_SETTINGS = 13;
    private static final int REQUEST_CODE_STORAGE_ACCESS = 14;

    private SongDetail songDetail;
    private Playlist playlist;
    private SongAdapter mSongAdapter;
    private SongInAlbumAdapter mSongInAlbumAdapter;
    private SongInGenreAdapter mSongInGenreAdapter;

    private List<Integer> removeIds = new ArrayList<>();
    private List<Integer> actionIds = new ArrayList<>();

    private Callback callback;
    boolean fChanged=false;

    public SongPopupFragment() {
        actionIds.add(R.id.action_add_to_queue);
        actionIds.add(R.id.action_remove_from_queue);
        actionIds.add(R.id.action_add_to_playlist);
        actionIds.add(R.id.action_remove_from_playlist);
        actionIds.add(R.id.action_add_to_favorites);
        actionIds.add(R.id.action_remove_from_favorites);
        actionIds.add(R.id.action_set_ringtone);
        actionIds.add(R.id.action_delete);
        actionIds.add(R.id.action_details);
        actionIds.add(R.id.action_edit_tag);
        actionIds.add(R.id.action_share);
    }

    public  void setSongAdapter(SongAdapter mSongAdapter){this.mSongAdapter = mSongAdapter;}
    public  void setSongInAlbumAdapter(SongInAlbumAdapter mSongInAlbumAdapter){this.mSongInAlbumAdapter = mSongInAlbumAdapter;}
    public  void setSongInGenreAdapter(SongInGenreAdapter mSongInGenreAdapter){this.mSongInGenreAdapter = mSongInGenreAdapter;}

    public void setSongDetail(SongDetail songDetail) {
        this.songDetail = songDetail;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public void remove(int id) {
        removeIds.add(id);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog)
                .setView(R.layout.dialog_song_popup)
                .create();
        alertDialog.setOnShowListener(this);
        return alertDialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (songDetail == null) {
            dismiss();
            return;
        }

        GlideApp.with(MainApplication.getAppContext())
                .load(songDetail.getCoverUri())
                .circleCrop()
                .error(R.drawable.default_song_cover)
                .placeholder(R.drawable.default_song_cover)
                .into((ImageView) getDialog().findViewById(R.id.img_song_thumb));

        ((TextView) getDialog().findViewById(R.id.song_name)).setText(songDetail.getTitle());
        ((TextView) getDialog().findViewById(R.id.song_artist_and_duration)).setText(NvpUtils.formatTimeDuration(songDetail.getDuration()) + " - " + songDetail.getArtist());

        for (int id : removeIds) {
            if (actionIds.contains(id)) {
                getDialog().findViewById(id).setVisibility(View.GONE);
            }
        }

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.action_add_to_favorites:
                        if (!SQLHelper.getInstance().isFavoriteSong(songDetail)) {
                            SQLHelper.getInstance().insertFavoriteSong(songDetail);
                            PlayerNotificationManager.postNotificationName(PlayerNotificationManager.toggleFavorite, songDetail, true);
                        }
                        break;
                    case R.id.action_remove_from_favorites:
                        if (SQLHelper.getInstance().isFavoriteSong(songDetail)) {
                            SQLHelper.getInstance().removeFavoriteSong(songDetail);
                            PlayerNotificationManager.postNotificationName(PlayerNotificationManager.toggleFavorite, songDetail, false);
                        }
                        break;
                    case R.id.action_add_to_queue:
                        PlayerService.addSongToQueue(songDetail);
                        break;
                    case R.id.action_remove_from_queue:
                        PlayerService.removeSongFromQueue(songDetail);
                        break;
                    case R.id.action_add_to_playlist:
                        PlaylistUtils.addSongToPlaylist(getActivity(), Collections.singletonList(songDetail));
                        break;
                    case R.id.action_remove_from_playlist:
                        if (playlist == null) {
                            dismiss();
                        } else {
                            if (playlist.getSongDetails().contains(songDetail)) {
                                playlist.getSongDetails().remove(songDetail);
                                SQLHelper.getInstance().updatePlaylist(playlist);
                                PlayerNotificationManager.postNotificationName(PlayerNotificationManager.removeSongFromPlaylist, songDetail);
                            }
                        }
                        break;
                    case R.id.action_set_ringtone:
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                        getContext().getContentResolver().update(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songDetail.getId()), values, null, null);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.System.canWrite(getContext())) {
                                if (callback != null) {
                                    callback.disableRefreshList();
                                }
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
                                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
                            } else {
                                SafUtils.SetRingtone(getActivity(), songDetail.getPath(), songDetail.getId());
                            }
                        } else {
                            SafUtils.SetRingtone(getActivity(), songDetail.getPath(), songDetail.getId());
                        }

                        return;
                    case R.id.action_delete:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (SafUtils.isOnExtSdCard(new File(songDetail.getPath()).getParentFile())) {
                                if (checkWriteAccess()) {
                                    confirmDeleteSong();
                                }
                            } else {
                                confirmDeleteSong();
                            }
                        } else {
                            confirmDeleteSong();
                        }
                        return;
                    case R.id.action_details:
                        showDetailsDialog();
                        break;
                    case R.id.action_edit_tag:
                        showEditTagDialog(getActivity());
                        break;
                    case R.id.action_share:
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(songDetail.getUri()));
                        shareIntent.setType("audio/*");
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                        break;
                }

                dismiss();
            }
        };

        for (int id : actionIds) {
            if (!removeIds.contains(id)) {
                getDialog().findViewById(id).setOnClickListener(listener);
            }
        }
    }

    private void setRingtone() {
        RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_RINGTONE, Uri.parse(songDetail.getUri()));
        ToastUtils.shortMsg(R.string.msg_change_ringtone_successful);
        dismiss();
    }

    private void showDetailsDialog() {
        String detail = getString(R.string.title) + ": " + songDetail.getTitle() + "\n" +
                getString(R.string.album) + ": " + songDetail.getAlbum() + "\n" +
                getString(R.string.artist) + ": " + songDetail.getArtist() + "\n" +
                getString(R.string.duration) + ": " + NvpUtils.formatTimeDuration(songDetail.getDuration()) + "\n" +
                getString(R.string.size) + ": " + Formatter.formatFileSize(getContext(), new File(songDetail.getPath()).length()) + "\n" +
                getString(R.string.path) + ": " + songDetail.getPath() + "\n";

        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog)
                .setTitle(R.string.detail)
                .setMessage(detail)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void confirmDeleteSong() {
        new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog)
                .setTitle(R.string.confirm)
                .setMessage(R.string.msg_confirm_delete_song)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (SafUtils.deleteFile(new File(songDetail.getPath()))) {
                            SQLHelper.getInstance().onSongDelete(songDetail);
                            getContext().getContentResolver().delete(Uri.parse(songDetail.getUri()), null, null);
                            MediaScannerConnection.scanFile(getContext(), new String[]{songDetail.getPath()}, null, null);
                            PlayerService.deleteSong(songDetail);
                        }
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }


    private void showEditTagDialog(final Context context) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.MyAlertDialog)
                .setTitle(R.string.tag_edit)
                .setView(R.layout.dialog_edit_tag)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();

        final TextInputLayout titleInputLayout = alertDialog.findViewById(R.id.title_input_layout);
        final EditText editTitle = alertDialog.findViewById(R.id.edit_title);
        final TextInputLayout artistInputLayout = alertDialog.findViewById(R.id.artist_input_layout);
        final EditText editArtist = alertDialog.findViewById(R.id.edit_artist);
        final TextInputLayout albumInputLayout = alertDialog.findViewById(R.id.album_input_layout);
        final EditText editAlbum = alertDialog.findViewById(R.id.edit_album);

        editTitle.setText(songDetail.getTitle());
        editArtist.setText(songDetail.getArtist());
        editAlbum.setText(songDetail.getAlbum());

        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate(context, titleInputLayout, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate(context, artistInputLayout, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editAlbum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate(context, albumInputLayout, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editTitle.getText().toString().trim();
                String artist = editArtist.getText().toString().trim();
                String album = editAlbum.getText().toString().trim();

                if(title.isEmpty() || artist.isEmpty() || album.isEmpty()){
                    ToastUtils.shortMsg(R.string.te_error_empty_field);
                    return;
                }

                if(!title.equals(songDetail.getTitle()) ||
                        !artist.equals(songDetail.getArtist()) ||
                        !album.equals(songDetail.getAlbum())
                ){
                    fChanged = true;
                }

                if(fChanged){
                    try {
                        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Audio.Media.TITLE, title);
                        values.put(MediaStore.Audio.Media.ARTIST, artist);
                        values.put(MediaStore.Audio.Media.ALBUM, album);
                        context.getContentResolver().update(uri, values, MediaStore.Audio.Media.TITLE +"=?", new String[] {songDetail.getTitle()});

                        songDetail.setTitle(title);
                        songDetail.setArtist(artist);
                        songDetail.setAlbum(album);

                        if(mSongAdapter != null && !mSongAdapter.isEmpty()){
                            mSongAdapter.addOrUpdate(songDetail);
                            mSongAdapter.notifyDataSetChanged();
                        }

                        if(mSongInAlbumAdapter != null && !mSongInAlbumAdapter.isEmpty()){
                            mSongInAlbumAdapter.addOrUpdate(songDetail);
                            mSongInAlbumAdapter.notifyDataSetChanged();
                        }

                        if(mSongInGenreAdapter != null && !mSongInGenreAdapter.isEmpty()){
                            mSongInGenreAdapter.addOrUpdate(songDetail);
                            mSongInGenreAdapter.notifyDataSetChanged();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtils.shortMsg(R.string.te_error_saving_tags);
                    }
                }
                alertDialog.dismiss();
            }
        });
    }

    private static boolean validate(final Context context, TextInputLayout textInputLayout, String text) {
        if (TextUtils.isEmpty(text)) {
            textInputLayout.setError(context.getString(R.string.msg_not_be_empty));
            return false;
        }
        textInputLayout.setError(null);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_WRITE_SETTINGS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(getContext())) {
                    SafUtils.SetRingtone(getActivity(), songDetail.getPath(), songDetail.getId());
                }
                break;
            case REQUEST_CODE_STORAGE_ACCESS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (data != null) {
                        Uri treeUri = data.getData();
                        if (treeUri != null) {
                            getContext().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            SpUtils.putString(SafUtils.PREF_MICRO_SD_URI, treeUri.toString());
                        }
                    }
                    if (checkWriteAccess()) {
                        confirmDeleteSong();
                    }
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean checkWriteAccess() {
        int mode = SafUtils.checkFolder(new File(songDetail.getPath()).getParentFile());

        if (mode == SafUtils.CAN_CREATE_FILES) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setPadding(0, getResources().getDimensionPixelSize(R.dimen.default_margin), 0, getResources().getDimensionPixelSize(R.dimen.default_margin));
            imageView.setLayoutParams(new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setImageResource(R.drawable.sd_operate_step);

            new AlertDialog.Builder(getActivity(), R.style.MyAlertDialog)
                    .setTitle(R.string.needs_access_title)
                    .setMessage(R.string.needs_access_msg)
                    .setView(imageView)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (callback != null) {
                                callback.disableRefreshList();
                            }
                            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), REQUEST_CODE_STORAGE_ACCESS);
                        }
                    })
                    .show();

            return false;
        }

        return mode == SafUtils.DOES_NOT_EXIST || mode == SafUtils.WRITABLE_OR_ON_SDCARD;
    }

    public void show(FragmentManager manager) {
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag("SongPopupFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        show(ft, "SongPopupFragment");
    }

    public interface Callback {
        void disableRefreshList();
    }
}
