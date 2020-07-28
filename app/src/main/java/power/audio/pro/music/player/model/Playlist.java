package power.audio.pro.music.player.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.utils.SQLHelper;

import java.util.ArrayList;
import java.util.List;

public class Playlist implements Parcelable, Comparable<Playlist> {
    private static final int FAVORITE_PLAYLIST_ID = -1;
    private static final int ADS_PLAYLIST_ID = -2;
    private static final int CREATE_PLAYLIST_ID = -3;

    private int id;
    private String name;
    private ArrayList<SongDetail> songDetails;

    public Playlist() {
    }

    public Playlist(int id, String name, ArrayList<SongDetail> songDetails) {
        this.id = id;
        this.name = name;
        this.songDetails = songDetails;
    }

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

    public ArrayList<SongDetail> getSongDetails() {
        return songDetails;
    }

    public void setSongDetails(ArrayList<SongDetail> songDetails) {
        this.songDetails = songDetails;
    }

    public void setSongDetails(List<SongDetail> songDetails) {
        this.songDetails = new ArrayList<>(songDetails);
    }

    public String getSongIdsString() {
        if (songDetails == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (SongDetail songDetail : songDetails) {
            builder.append(songDetail.getId()).append(" ");
        }
        return builder.toString().trim();
    }

    public boolean isEmpty() {
        return songDetails == null || songDetails.isEmpty();
    }

    protected Playlist(Parcel in) {
        id = in.readInt();
        name = in.readString();
        songDetails = in.createTypedArrayList(SongDetail.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeTypedList(songDetails);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Playlist playlist = (Playlist) o;

        if (!TextUtils.equals(name, playlist.name)) {
            return false;
        }

        if (songDetails == null && playlist.songDetails == null) {
            return true;
        }

        if (songDetails == null || playlist.songDetails == null) {
            return false;
        }

        if (songDetails.size() != playlist.songDetails.size()) {
            return false;
        }

        for (int i = 0; i < songDetails.size(); i++) {
            if (!songDetails.get(i).equals(playlist.songDetails.get(i))) {
                return false;
            }
        }

        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songDetails=" + songDetails +
                '}';
    }

    @Override
    public int compareTo(@NonNull Playlist o) {
        if (isCreatePlaylist()) {
            return -1;
        }
        if (o.isCreatePlaylist()) {
            return 1;
        }
        if (isFavoritePlaylist()) {
            return -1;
        }
        if (o.isFavoritePlaylist()) {
            return 1;
        }
        if (isAdsPlaylist()) {
            return -1;
        }
        if (o.isAdsPlaylist()) {
            return 1;
        }
        return name.compareToIgnoreCase(o.name);
    }

    public boolean isFavoritePlaylist() {
        return id == FAVORITE_PLAYLIST_ID;
    }

    public boolean isAdsPlaylist() {
        return id == ADS_PLAYLIST_ID;
    }

    public boolean isCreatePlaylist() {
        return id == CREATE_PLAYLIST_ID;
    }

    public static Playlist ads() {
        return new Playlist(ADS_PLAYLIST_ID, "", null);
    }

    public static Playlist create() {
        return new Playlist(CREATE_PLAYLIST_ID, "", null);
    }

    public static Playlist favorite(SQLHelper.Callback callback) {
        return new Playlist(FAVORITE_PLAYLIST_ID, MainApplication.getAppContext().getString(R.string.favorite), SQLHelper.getInstance().getFavoriteSongsList(callback));
    }
}
