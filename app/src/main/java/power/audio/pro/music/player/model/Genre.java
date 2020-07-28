package power.audio.pro.music.player.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 06/20/17.
 */
public class Genre implements Parcelable, Comparable<Genre> {
    private int id;
    private String name;
    private int cover;
    private int icon;
    private int count;

    public Genre(int id, String name, int cover, int icon, int count) {
        this.id = id;
        this.name = name;
        this.cover = cover;
        this.icon = icon;
        this.count = count;
    }

    protected Genre(Parcel in) {
        id = in.readInt();
        name = in.readString();
        cover = in.readInt();
        icon = in.readInt();
        count = in.readInt();
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel in) {
            return new Genre(in);
        }

        @Override
        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    public int getCover() {
        return cover;
    }

    public void setCover(int cover) {
        this.cover = cover;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
    @NonNull
    @Override
    public String toString() {
        return "Genre{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", icon='" + icon + '\'' +
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
        dest.writeString(name);
        dest.writeInt(cover);
        dest.writeInt(icon);
        dest.writeInt(count);
    }

    @Override
    public int compareTo(@NonNull Genre o) {
        return name.compareToIgnoreCase(o.name);
    }
}
