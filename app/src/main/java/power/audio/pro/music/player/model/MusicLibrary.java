package power.audio.pro.music.player.model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import android.util.SparseArray;

import power.audio.pro.music.player.MainApplication;





//singleton class
//maintains music library

public class MusicLibrary{

    private Context context;
    private static MusicLibrary musicLibrary = new MusicLibrary();
    private ContentResolver cr;

    interface INDEX_FOR_TRACK_CURSOR{
        int _ID=0;
        int TITLE=1;
        int DATA_PATH=2;
        int ARTIST=3;
        int ALBUM=4;
        int DURATION=5;
        int ALBUM_ID=6;

    }


    public TrackItem getTrackItemFromId(int _id){

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection =  MediaStore.Audio.Media.IS_MUSIC + "!= 0" + " AND "
                + MediaStore.Audio.Media._ID  + "= '" +   _id  +"'";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID
        };
        Cursor cursor = null;
        try {
            cursor = cr.query(
                    uri,
                    projection,
                    selection,
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC");
        }catch (Exception ignored){}
        if(cursor!=null && cursor.getCount()!=0){
            cursor.moveToFirst();
            TrackItem item = new TrackItem(cursor.getString(INDEX_FOR_TRACK_CURSOR.DATA_PATH),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.TITLE),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.ARTIST),
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.ALBUM),
                    "",
                    cursor.getString(INDEX_FOR_TRACK_CURSOR.DURATION),
                    cursor.getInt(INDEX_FOR_TRACK_CURSOR.ALBUM_ID),
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                    cursor.getInt(INDEX_FOR_TRACK_CURSOR._ID));
            cursor.close();
            return  item;
        }
        return null;
    }

    //track id to track name hashmap
    //used for shuffling tracks using track name in now playing
    private SparseArray<String> trackMap= new SparseArray<>();

    private MusicLibrary(){
        this.context= MainApplication.getContext();
        this.cr = context.getContentResolver();
    }

    public static MusicLibrary getInstance(){
        if (musicLibrary==null){
            musicLibrary = new MusicLibrary();
        }
        return musicLibrary;
    }

    public Uri getAlbumArtUri(int album_id){
        Uri songCover = Uri.parse("content://media/external/audio/albumart");
        Uri uriSongCover = ContentUris.withAppendedId(songCover, album_id);
        if(uriSongCover==null){
            return null;
        }
        return uriSongCover;
    }
}


