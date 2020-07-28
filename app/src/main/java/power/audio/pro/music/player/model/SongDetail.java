package power.audio.pro.music.player.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import power.audio.pro.music.player.MainApplication;

import java.io.IOException;

public class SongDetail implements Parcelable, Comparable<SongDetail> {
    @SerializedName("id")
    private int id;
    @SerializedName("album")
    private String album;
    @SerializedName("artist")
    private String artist;
    @SerializedName("title")
    private String title;
    @SerializedName("duration")
    private long duration;
    @SerializedName("path")
    private String path;

    public SongDetail(int id, String album, String artist, String title, String path, long duration) {
        this.id = id;
        this.album = album;
        this.artist = artist;
        this.title = title;
        this.path = path;
        this.duration = duration;
    }

    protected SongDetail(Parcel in) {
        id = in.readInt();
        album = in.readString();
        artist = in.readString();
        title = in.readString();
        duration = in.readLong();
        path = in.readString();
    }

    public static final Creator<SongDetail> CREATOR = new Creator<SongDetail>() {
        @Override
        public SongDetail createFromParcel(Parcel in) {
            return new SongDetail(in);
        }

        @Override
        public SongDetail[] newArray(int size) {
            return new SongDetail[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCoverUri() {
        return getUri() + "/albumart";
    }

    public String getUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id;
    }

    public Bitmap getSmallCover() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap curThumb = null;
        try {
            Uri uri = Uri.parse(getCoverUri());
            ParcelFileDescriptor pfd = MainApplication.getResolver().openFileDescriptor(uri, "r");
            if (pfd != null) {
                curThumb = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return curThumb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongDetail that = (SongDetail) o;
        return duration == that.duration &&
                TextUtils.equals(album, that.album) &&
                TextUtils.equals(artist, that.artist) &&
                TextUtils.equals(title, that.title) &&
                TextUtils.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (album != null ? album.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "SongDetail{" +
                "id=" + id +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", duration=" + duration +
                ", path='" + path + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeLong(duration);
        dest.writeString(path);
    }

    @Override
    public int compareTo(@NonNull SongDetail o) {
        return title.compareToIgnoreCase(o.title);
    }
}
