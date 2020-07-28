package power.audio.pro.music.player.utils;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.model.Album;
import power.audio.pro.music.player.model.SongDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongAlbumLoadTask extends AsyncTask<Void, Void, List<SongDetail>> {
    private final LoadCallback callback;
    private final int albumId;

    public SongAlbumLoadTask(Album album, LoadCallback callback) {
        this(album.getId(), callback);
    }

    public SongAlbumLoadTask(int albumId, LoadCallback callback) {
        this.albumId = albumId;
        this.callback = callback;
    }

    @Override
    protected List<SongDetail> doInBackground(Void... voids) {
        List<SongDetail> soundDetails = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = MainApplication.getResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.DURATION
                    },
                    MediaStore.Audio.Media.IS_MUSIC + "=1 AND " + MediaStore.Audio.Media.ALBUM_ID + "=" + albumId,
                    null,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            try {
                if (cursor.getCount() >= 1) {
                    int indexOfId = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int indexOfAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                    int indexOfArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int indexOfTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int indexOfData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int indexOfDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                    while (cursor.moveToNext()) {
                        if (isCancelled()) {
                            return soundDetails;
                        }
                        int id = cursor.getInt(indexOfId);
                        String album = cursor.getString(indexOfAlbum);
                        String artist = cursor.getString(indexOfArtist);
                        String title = cursor.getString(indexOfTitle);
                        String path = cursor.getString(indexOfData);
                        long duration = cursor.getLong(indexOfDuration);
                        soundDetails.add(new SongDetail(id, album, artist, title, path, duration));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return soundDetails;
    }

    @Override
    protected void onPostExecute(List<SongDetail> soundDetails) {
        if (callback != null) {
            callback.onLoadCompleted(soundDetails == null ? Collections.<SongDetail>emptyList() : soundDetails);
        }
    }

    public interface LoadCallback {
        void onLoadCompleted(@NonNull List<SongDetail> songDetails);
    }
}
