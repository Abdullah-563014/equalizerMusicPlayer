package power.audio.pro.music.player.utils;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.model.Playlist;
import power.audio.pro.music.player.model.SongDetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PlaylistUtils {
    public static void addSongToPlaylist(@NonNull final Context context, @NonNull final List<SongDetail> songDetails) {
        if (songDetails.isEmpty()) {
            return;
        }

        final HashMap<String, Integer> nameIds = SQLHelper.getInstance().getPlaylistNameId();
        final String[] names = nameIds.keySet().toArray(new String[nameIds.size()]);

        new AlertDialog.Builder(context, R.style.MyAlertDialog)
                .setTitle(R.string.add_to_playlist)
                .setItems(names, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Playlist playlist = SQLHelper.getInstance().getPlaylist(nameIds.get(names[which]));
                        if (playlist != null) {
                            boolean added = false;
                            for (SongDetail songDetail : songDetails) {
                                if (!playlist.getSongDetails().contains(songDetail)) {
                                    playlist.getSongDetails().add(songDetail);
                                    added = true;
                                }
                            }
                            if (added) {
                                SQLHelper.getInstance().updatePlaylist(playlist);
                                PlayerNotificationManager.postNotificationName(PlayerNotificationManager.addSongsToPlaylist, playlist, songDetails);
                            }
                        }
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.new_playlist, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showAddNewPlaylistDialog(context, Arrays.asList(names), songDetails);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private static void showAddNewPlaylistDialog(final Context context, final List<String> playlistNames, final List<SongDetail> songDetails) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.MyAlertDialog)
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

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate(context, playlistNames, textInputLayout, s.toString());
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

                if (!validate(context, playlistNames, textInputLayout, text)) {
                    return;
                }

                Playlist playlist = new Playlist();
                playlist.setName(text);
                playlist.setSongDetails(songDetails);

                SQLHelper.getInstance().insertPlaylist(playlist);
                PlayerNotificationManager.postNotificationName(PlayerNotificationManager.addSongsToPlaylist, playlist, songDetails);

                alertDialog.dismiss();
            }
        });
    }

    public static void showAddNewPlaylistDialog(final Context context) {
        final HashMap<String, Integer> nameIds = SQLHelper.getInstance().getPlaylistNameId();
        showAddNewPlaylistDialog(context, Arrays.asList(nameIds.keySet().toArray(new String[nameIds.size()])), new ArrayList<SongDetail>());
    }

    private static boolean validate(final Context context, List<String> names, TextInputLayout textInputLayout, String text) {
        if (TextUtils.isEmpty(text)) {
            textInputLayout.setError(context.getString(R.string.msg_not_be_empty));
            return false;
        }

        if (context.getString(R.string.favorite).equals(text) || names.contains(text)) {
            textInputLayout.setError(context.getString(R.string.msg_playlist_already_exists));
            return false;
        }

        textInputLayout.setError(null);

        return true;
    }
}
