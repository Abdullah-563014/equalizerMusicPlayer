package power.audio.pro.music.player.utils;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import androidx.annotation.NonNull;

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.model.Constants;
import power.audio.pro.music.player.model.Genre;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class GenreLoadTask extends AsyncTask<Void, Void, List<Genre>> {
    private final LoadCallback callback;

    public GenreLoadTask(LoadCallback callback) {
        this.callback = callback;
    }
    private List<Integer> data = new ArrayList<Integer>();

    private List<Integer> iconData = new ArrayList<Integer>();

    HashMap<String, Integer> songCount = new HashMap<>();

    Integer [] bgImageId = {
            R.drawable.blues_back,
            R.drawable.club_back,
            R.drawable.dance_back,
            R.drawable.electronics_back,
            R.drawable.european_music_back,
            R.drawable.hip_hop_back,
            R.drawable.raggae_back,
            R.drawable.rock_img,
            R.drawable.soul_back
    };

    Integer [] icons = {
            R.drawable.pop,
            R.drawable.blues_icon,
            R.drawable.club_icon,
            R.drawable.dance_music_icon,
            R.drawable.electronic_icon,
            R.drawable.europian_music_icon,
            R.drawable.hiphop_icon,
            R.drawable.ragge_icon,
            R.drawable.rock_icon,
            R.drawable.soul_icon
    };
    @Override
    protected List<Genre> doInBackground(Void... voids) {
        List<Genre> genres = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = MainApplication.getResolver().query(
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Genres._ID,
                            MediaStore.Audio.Genres.NAME
                    },
                    null,
                    null,
                    MediaStore.Audio.Genres.NAME + " ASC"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(cursor!=null && cursor.getCount()>0){
            try {
                int i = 0;
                int indexOfId = cursor.getColumnIndex(MediaStore.Audio.Genres._ID);
                data = Arrays.asList(bgImageId);
                Collections.shuffle(data);

                iconData = Arrays.asList(icons);
                Collections.shuffle(iconData);

                while (cursor.moveToNext()) {

                    if (isCancelled()) {
                        return genres;
                    }

                    ArrayList<Integer> songList = getSongListFromGenreIdNew(cursor.getInt(indexOfId) , Constants.SORT_ORDER.ASC);
                    if (songList==null || songList.size() == 0)
                        continue;

                    int id = cursor.getInt(indexOfId);
                    int indexOfGenre = cursor.getColumnIndex(MediaStore.Audio.Genres.NAME);
                    String genre_name = cursor.getString(indexOfGenre);
                    if(genre_name==null) continue;

                    int cover = data.get(i);
                    int icon = iconData.get(i);
                    genres.add(new Genre(id, genre_name, cover, icon,songList.size()));
                    i ++ ;
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }

        return genres;
    }

    public ArrayList<Integer> getSongListFromGenreIdNew(int genre_id,int sort){
        ArrayList<Integer> songList=new ArrayList<>();
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genre_id);
        String[] projection = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID};
        String sortOrder="";
        if(sort== Constants.SORT_ORDER.ASC) {
            sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        }else{
            sortOrder = MediaStore.Audio.Media.TITLE + " DESC";
        }
        Cursor cursor = null;
        try {
            cursor=MainApplication.getResolver().query(uri, projection, null, null, sortOrder);
        }catch (Exception ignored){}
        if(cursor!=null){
            while (cursor.moveToNext()){
                songList.add(cursor.getInt(2));
            }
            cursor.close();
            return songList;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Genre> artists) {
        if (callback != null) {
            callback.onLoadCompleted(artists == null ? Collections.<Genre>emptyList() : artists);
        }
    }

    public interface LoadCallback {
        void onLoadCompleted(@NonNull List<Genre> artists);
    }
}
