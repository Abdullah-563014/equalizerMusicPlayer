package power.audio.pro.music.player.utils.lastfmapi.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LastfmArtist {
    @SerializedName("artist")
    @Expose
    private ArtistInfo artist;

    public ArtistInfo getArtist() {
        return artist;
    }

    public void setArtist(ArtistInfo artist) {
        this.artist = artist;
    }
}
