package power.audio.pro.music.player.utils;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.model.Folder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 11/13/17.
 */
public class FolderLoadTask extends AsyncTask<Void, Void, List<Folder>> {
    private final LoadCallback callback;

    public FolderLoadTask(LoadCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List<Folder> doInBackground(Void... voids) {
        ArrayList<Folder> folders = new ArrayList<>();
        HashMap<String, Integer> songCount = new HashMap<>();
        Cursor cursor = null;

        try {
            cursor = MainApplication.getResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Media.DATA},
                    MediaStore.Audio.Media.IS_MUSIC + "=1",
                    null,
                    null
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            try {
                if (cursor.getCount() >= 1) {
                    int indexOfData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                    while (cursor.moveToNext()) {
                        if (isCancelled()) {
                            return folders;
                        }
                        String file = cursor.getString(indexOfData);
                        if (new File(file).exists()) {
                            String folder = file.substring(0, file.lastIndexOf("/"));
                            Integer count = songCount.get(folder);
                            songCount.put(folder, count == null ? 1 : count + 1);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        for (String folder : songCount.keySet()) {
            folders.add(new Folder(folder, songCount.get(folder)));
        }

        return folders;
    }

    @Override
    protected void onPostExecute(List<Folder> folders) {
        if (callback != null) {
            callback.onLoadCompleted(folders == null ? Collections.<Folder>emptyList() : folders);
        }
    }

    public interface LoadCallback {
        void onLoadCompleted(@NonNull List<Folder> folders);
    }
}
