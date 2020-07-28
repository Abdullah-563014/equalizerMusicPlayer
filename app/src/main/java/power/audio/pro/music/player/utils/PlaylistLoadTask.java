package power.audio.pro.music.player.utils;

import android.os.AsyncTask;
import androidx.annotation.NonNull;

import power.audio.pro.music.player.model.Playlist;

import java.util.Collections;
import java.util.List;

public class PlaylistLoadTask extends AsyncTask<Void, Void, List<Playlist>> {
    private final LoadCallback callback;

    public PlaylistLoadTask(LoadCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List<Playlist> doInBackground(Void... voids) {
        return SQLHelper.getInstance().getPlaylists(new SQLHelper.Callback() {
            @Override
            public boolean isCancelled() {
                return PlaylistLoadTask.this.isCancelled();
            }
        });
    }

    @Override
    protected void onPostExecute(List<Playlist> playlists) {
        if (callback != null) {
            callback.onLoadCompleted(playlists == null ? Collections.<Playlist>emptyList() : playlists);
        }
    }

    public interface LoadCallback {
        void onLoadCompleted(@NonNull List<Playlist> playlists);
    }
}
