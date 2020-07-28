package power.audio.pro.music.player.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 06/20/17.
 */
public class Artist implements Parcelable, Comparable<Artist> {
    private int id;
    private String name;
    private int count;

    public Artist(int id, String name, int count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    protected Artist(Parcel in) {
        id = in.readInt();
        name = in.readString();
        count = in.readInt();
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
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

    @NonNull
    @Override
    public String toString() {
        return "Artist{" +
                "id=" + id +
                ", name='" + name + '\'' +
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
        dest.writeInt(count);
    }

    @Override
    public int compareTo(@NonNull Artist o) {
        return name.compareToIgnoreCase(o.name);
    }
}
