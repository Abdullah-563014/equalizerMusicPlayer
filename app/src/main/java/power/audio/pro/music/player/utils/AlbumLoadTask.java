package power.audio.pro.music.player.utils;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.model.Album;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlbumLoadTask extends AsyncTask<Void, Void, List<Album>> {
    private final LoadCallback callback;

    public AlbumLoadTask(LoadCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List<Album> doInBackground(Void... voids) {
        List<Album> albums = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = MainApplication.getResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.Albums.ARTIST,
                            MediaStore.Audio.Albums.ALBUM,
                            MediaStore.Audio.Albums.NUMBER_OF_SONGS
                    },
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            try {
                if (cursor.getCount() >= 1) {
                    int indexOfId = cursor.getColumnIndex(MediaStore.Audio.Albums._ID);
                    int indexOfArtist = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
                    int indexOfAlbum = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
                    int indexOfNumberOfSongs = cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);

                    while (cursor.moveToNext()) {
                        if (isCancelled()) {
                            return albums;
                        }
                        int id = cursor.getInt(indexOfId);
                        String title = cursor.getString(indexOfAlbum);
                        String artist = cursor.getString(indexOfArtist);
                        String cover = "content://media/external/audio/albumart/" + cursor.getLong(0);
                        int count = cursor.getInt(indexOfNumberOfSongs);
                        albums.add(new Album(id, title, artist, cover, count));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return albums;
    }

    @Override
    protected void onPostExecute(List<Album> albums) {
        if (callback != null) {
            callback.onLoadCompleted(albums == null ? Collections.<Album>emptyList() : albums);
        }
    }

    public interface LoadCallback {
        void onLoadCompleted(@NonNull List<Album> albums);
    }
}
