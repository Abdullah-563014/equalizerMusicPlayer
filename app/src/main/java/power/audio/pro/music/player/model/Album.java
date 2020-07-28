package power.audio.pro.music.player.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 06/20/17.
 */
public class Album implements Parcelable, Comparable<Album> {
    private int id;
    private String title;
    private String artist;
    private String cover;
    private int count;

    public Album(int id, String title, String artist, String cover, int count) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.cover = cover;
        this.count = count;
    }

    protected Album(Parcel in) {
        id = in.readInt();
        title = in.readString();
        artist = in.readString();
        cover = in.readString();
        count = in.readInt();
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @NonNull
    @Override
    public String toString() {
        return "Album{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", cover='" + cover + '\'' +
                ", count=" + count +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(cover);
        dest.writeInt(count);
    }

    @Override
    public int compareTo(@NonNull Album o) {
        return title.compareToIgnoreCase(o.title);
    }
}
