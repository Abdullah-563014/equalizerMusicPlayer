package power.audio.pro.music.player.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.nvp.util.NvpUtils;
import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.model.Playlist;
import power.audio.pro.music.player.model.SongDetail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "music.db";
    private static final int DB_VER = 1;

    private static SQLHelper sSQLHelper;

    public static SQLHelper getInstance() {
        if (null == sSQLHelper) {
            sSQLHelper = new SQLHelper();
        }
        return sSQLHelper;
    }

    private SQLHelper() {
        super(MainApplication.getAppContext(), DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                ArtistImageUrlColumns.TABLE_NAME + "(" +
                ArtistImageUrlColumns.ARTIST_NAME + " TEXT, " +
                ArtistImageUrlColumns.ARTIST_URL + " TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                PlaylistColumns.TABLE_NAME + "(" +
                PlaylistColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PlaylistColumns.NAME + " TEXT, " +
                PlaylistColumns.SONG_IDS + " TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                FavoriteSongColumns.TABLE_NAME + "(" +
                FavoriteSongColumns._ID + " INTEGER PRIMARY KEY, " +
                FavoriteSongColumns.ALBUM + " TEXT, " +
                FavoriteSongColumns.ARTIST + " TEXT, " +
                FavoriteSongColumns.TITLE + " TEXT, " +
                FavoriteSongColumns.DATA + " TEXT, " +
                FavoriteSongColumns.DURATION + " BIGINTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /*
     * Artist methods
     * */

    public synchronized void insertArtistUrl(String name, String url) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(ArtistImageUrlColumns.ARTIST_NAME, name);
            values.put(ArtistImageUrlColumns.ARTIST_URL, url);
            db.insert(ArtistImageUrlColumns.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public synchronized String getArtistUrl(String name) {
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(ArtistImageUrlColumns.TABLE_NAME, null, ArtistImageUrlColumns.ARTIST_NAME + " like ?", new String[]{name}, null, null, null);
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(ArtistImageUrlColumns.ARTIST_URL));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return null;
    }


    /*
     * Favorite methods
     * */
    public synchronized ArrayList<SongDetail> getFavoriteSongsList(Callback callback) {
        ArrayList<SongDetail> result = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(FavoriteSongColumns.TABLE_NAME, null, null, null, null, null, null);

            int indexOfId = cursor.getColumnIndexOrThrow(FavoriteSongColumns._ID);
            int indexOfAlbum = cursor.getColumnIndexOrThrow(FavoriteSongColumns.ALBUM);
            int indexOfArtist = cursor.getColumnIndexOrThrow(FavoriteSongColumns.ARTIST);
            int indexOfTitle = cursor.getColumnIndexOrThrow(FavoriteSongColumns.TITLE);
            int indexOfData = cursor.getColumnIndexOrThrow(FavoriteSongColumns.DATA);
            int indexOfDuration = cursor.getColumnIndexOrThrow(FavoriteSongColumns.DURATION);

            while (cursor.moveToNext()) {
                if (callback != null && callback.isCancelled()) {
                    return result;
                }
                String path = cursor.getString(indexOfData);
                int id = cursor.getInt(indexOfId);
                if (new File(path).exists()) {
                    String album = cursor.getString(indexOfAlbum);
                    String artist = cursor.getString(indexOfArtist);
                    String title = cursor.getString(indexOfTitle);
                    long duration = cursor.getLong(indexOfDuration);
                    result.add(new SongDetail(id, album, artist, title, path, duration));
                } else {
                    onSongDelete(id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return result;
    }

    public synchronized void insertFavoriteSong(SongDetail songDetail) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(FavoriteSongColumns._ID, songDetail.getId());
            values.put(FavoriteSongColumns.ALBUM, songDetail.getAlbum());
            values.put(FavoriteSongColumns.ARTIST, songDetail.getArtist());
            values.put(FavoriteSongColumns.TITLE, songDetail.getTitle());
            values.put(FavoriteSongColumns.DATA, songDetail.getPath());
            values.put(FavoriteSongColumns.DURATION, songDetail.getDuration());
            db.insert(FavoriteSongColumns.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public synchronized void updateFavoriteSong(SongDetail songDetail) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(FavoriteSongColumns.ALBUM, songDetail.getAlbum());
            values.put(FavoriteSongColumns.ARTIST, songDetail.getArtist());
            values.put(FavoriteSongColumns.TITLE, songDetail.getTitle());
            values.put(FavoriteSongColumns.DATA, songDetail.getPath());
            values.put(FavoriteSongColumns.DURATION, songDetail.getDuration());
            db.update(FavoriteSongColumns.TABLE_NAME, values, FavoriteSongColumns._ID + "=" + songDetail.getId(), null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public synchronized void removeFavoriteSong(SongDetail songDetail) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(FavoriteSongColumns.TABLE_NAME, FavoriteSongColumns._ID + "=" + songDetail.getId(), null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public synchronized void removeFavoriteSongsList() {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.execSQL("DELETE FROM " + FavoriteSongColumns.TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public synchronized boolean isFavoriteSong(SongDetail songDetail) {
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(FavoriteSongColumns.TABLE_NAME, null, FavoriteSongColumns._ID + "=" + songDetail.getId(), null, null, null, null);
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            closeCursor(cursor);
        }
    }

    public synchronized int getFavoriteSongsCount() {
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(FavoriteSongColumns.TABLE_NAME, null, null, null, null, null, null);
            return cursor.getCount();
        } catch (Exception e) {
            return 0;
        } finally {
            closeCursor(cursor);
        }
    }


    /**
     * Playlist method
     */

    public synchronized List<Playlist> getPlaylists(Callback callback) {
        List<Playlist> result = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(PlaylistColumns.TABLE_NAME, null, null, null, null, null, null);

            int indexOfId = cursor.getColumnIndexOrThrow(PlaylistColumns._ID);
            int indexOfName = cursor.getColumnIndexOrThrow(PlaylistColumns.NAME);
            int indexOfSongIds = cursor.getColumnIndexOrThrow(PlaylistColumns.SONG_IDS);

            while (cursor.moveToNext()) {
                if (callback != null && callback.isCancelled()) {
                    return result;
                }
                result.add(new Playlist(cursor.getInt(indexOfId), cursor.getString(indexOfName), getSongDetails(cursor.getString(indexOfSongIds), callback)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        result.add(Playlist.favorite(callback));
        result.add(Playlist.create());

        return result;
    }

    public synchronized Playlist getPlaylist(int id) {
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(PlaylistColumns.TABLE_NAME, null, PlaylistColumns._ID + "=" + id, null, null, null, null);

            if (cursor.moveToFirst()) {
                return new Playlist(cursor.getInt(cursor.getColumnIndexOrThrow(PlaylistColumns._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(PlaylistColumns.NAME)),
                        getSongDetails(cursor.getString(cursor.getColumnIndexOrThrow(PlaylistColumns.SONG_IDS)), null));
            }

            return null;
        } catch (Exception e) {
            return null;
        } finally {
            closeCursor(cursor);
        }
    }

    public synchronized HashMap<String, Integer> getPlaylistNameId() {
        HashMap<String, Integer> nameIds = new HashMap<>();
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(PlaylistColumns.TABLE_NAME, null, null, null, null, null, null);

            int indexOfId = cursor.getColumnIndexOrThrow(PlaylistColumns._ID);
            int indexOfName = cursor.getColumnIndexOrThrow(PlaylistColumns.NAME);

            while (cursor.moveToNext()) {
                nameIds.put(cursor.getString(indexOfName), cursor.getInt(indexOfId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return nameIds;
    }

    public synchronized List<String> getPlaylistNames() {
        List<String> names = new ArrayList<>();
        Cursor cursor = null;
        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.query(PlaylistColumns.TABLE_NAME, null, null, null, null, null, null);
            int indexOfName = cursor.getColumnIndexOrThrow(PlaylistColumns.NAME);

            while (cursor.moveToNext()) {
                names.add(cursor.getString(indexOfName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return names;
    }

    public synchronized void insertPlaylist(Playlist playlist) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(PlaylistColumns.NAME, playlist.getName());
            values.put(PlaylistColumns.SONG_IDS, playlist.getSongIdsString());
            playlist.setId((int) db.insert(PlaylistColumns.TABLE_NAME, null, values));
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public synchronized void updatePlaylist(Playlist playlist) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(PlaylistColumns.NAME, playlist.getName());
            values.put(PlaylistColumns.SONG_IDS, playlist.getSongIdsString());
            db.update(PlaylistColumns.TABLE_NAME, values, PlaylistColumns._ID + "=" + playlist.getId(), null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public synchronized void removePlaylist(Playlist playlist) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(PlaylistColumns.TABLE_NAME, PlaylistColumns._ID + "=" + playlist.getId(), null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public synchronized void onSongDelete(SongDetail songDetail) {
        onSongDelete(songDetail.getId());
    }

    private synchronized void onSongDelete(int id) {
        onSongDelete(String.valueOf(id));
    }

    private synchronized void onSongDelete(String songId) {
        Cursor cursor = null;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(FavoriteSongColumns.TABLE_NAME, FavoriteSongColumns._ID + "=" + songId, null);

            cursor = db.query(PlaylistColumns.TABLE_NAME, null, null, null, null, null, null);

            int indexOfId = cursor.getColumnIndexOrThrow(PlaylistColumns._ID);
            int indexOfSongIds = cursor.getColumnIndexOrThrow(PlaylistColumns.SONG_IDS);

            while (cursor.moveToNext()) {
                List<String> songIdList = NvpUtils.toArrayList(cursor.getString(indexOfSongIds).split(" "));

                if (songIdList.contains(songId)) {
                    int originalSize = songIdList.size();
                    int playlistId = cursor.getInt(indexOfId);

                    songIdList.remove(songId);

                    if (songIdList.size() < originalSize) {
                        StringBuilder builder = new StringBuilder();
                        for (String id : songIdList) {
                            builder.append(id).append(" ");
                        }
                        ContentValues values = new ContentValues(1);
                        values.put(PlaylistColumns.SONG_IDS, builder.toString().trim());
                        db.update(PlaylistColumns.TABLE_NAME, values, PlaylistColumns._ID + "=" + playlistId, null);
                    }
                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
            db.endTransaction();
            db.close();
        }
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<SongDetail> getSongDetails(String songIdsString, Callback callback) {
        ArrayList<SongDetail> result = new ArrayList<>();
        Cursor cursor = null;

        try {
            List<String> idList = NvpUtils.toArrayList(songIdsString.split(" "));
            cursor = MainApplication.getResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.DURATION
                    },
                    MediaStore.Audio.Media.IS_MUSIC + "=1",
                    null,
                    null
            );

            if (cursor != null && cursor.getCount() >= 1) {
                int indexOfId = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int indexOfAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int indexOfArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int indexOfTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int indexOfData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int indexOfDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    if (callback != null && callback.isCancelled()) {
                        return result;
                    }
                    int id = cursor.getInt(indexOfId);
                    if (idList.contains(String.valueOf(id))) {
                        String path = cursor.getString(indexOfData);
                        if (new File(path).exists()) {
                            String album = cursor.getString(indexOfAlbum);
                            String artist = cursor.getString(indexOfArtist);
                            String title = cursor.getString(indexOfTitle);
                            long duration = cursor.getLong(indexOfDuration);
                            result.add(new SongDetail(id, album, artist, title, path, duration));
                        } else {
                            onSongDelete(id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(cursor);
        }

        return result;
    }

    /*
     * Columns
     * */
    public static class ArtistImageUrlColumns implements BaseColumns {
        public static final String TABLE_NAME = "artistImageUrl";
        public static final String ARTIST_NAME = "artistName";
        public static final String ARTIST_URL = "artistUrl";
    }

    public static class FavoriteSongColumns implements BaseColumns {
        public static final String TABLE_NAME = "favoriteSongs";
        public static final String ALBUM = MediaStore.Audio.Media.ALBUM;
        public static final String ARTIST = MediaStore.Audio.Media.ARTIST;
        public static final String TITLE = MediaStore.Audio.Media.TITLE;
        public static final String DATA = MediaStore.Audio.Media.DATA;
        public static final String DURATION = MediaStore.Audio.Media.DURATION;
    }

    public static class PlaylistColumns implements BaseColumns {
        public static final String TABLE_NAME = "playlists";
        public static final String NAME = "name";
        public static final String SONG_IDS = "song_ids";
    }

    public interface Callback {
        boolean isCancelled();
    }
}
