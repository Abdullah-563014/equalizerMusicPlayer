package power.audio.pro.music.player.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.io.File;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 06/22/17.
 */
public class Folder implements Parcelable, Comparable<Folder> {
    private String path;
    private int count;

    public Folder(String path, int count) {
        this.path = path;
        this.count = count;
    }

    protected Folder(Parcel in) {
        path = in.readString();
        count = in.readInt();
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    public File getFile() {
        return new File(path);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public void setPath(String path) {
        this.path = path;
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
        return "Folder{" +
                "path='" + path + '\'' +
                ", count=" + count +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeInt(count);
    }

    @Override
    public int compareTo(@NonNull Folder o) {
        return getName().compareToIgnoreCase(o.getName());
    }
}
