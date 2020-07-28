package power.audio.pro.music.player.utils;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.model.Artist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtistLoadTask extends AsyncTask<Void, Void, List<Artist>> {
    private final LoadCallback callback;

    public ArtistLoadTask(LoadCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List<Artist> doInBackground(Void... voids) {
        List<Artist> artists = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = MainApplication.getResolver().query(
                    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Artists._ID,
                            MediaStore.Audio.Artists.ARTIST,
                            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
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
                    int indexOfId = cursor.getColumnIndex(MediaStore.Audio.Artists._ID);
                    int indexOfArtist = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
                    int indexOfNumberOfTracks = cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);

                    while (cursor.moveToNext()) {
                        if (isCancelled()) {
                            return artists;
                        }
                        int id = cursor.getInt(indexOfId);
                        String name = cursor.getString(indexOfArtist);
                        int count = cursor.getInt(indexOfNumberOfTracks);
                        artists.add(new Artist(id, name, count));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return artists;
    }

    @Override
    protected void onPostExecute(List<Artist> artists) {
        if (callback != null) {
            callback.onLoadCompleted(artists == null ? Collections.<Artist>emptyList() : artists);
        }
    }

    public interface LoadCallback {
        void onLoadCompleted(@NonNull List<Artist> artists);
    }
}
