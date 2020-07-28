package power.audio.pro.music.player.utils.lastfmapi;

import power.audio.pro.music.player.utils.lastfmapi.model.LastfmArtist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 12/14/17.
 */
public interface SOService {
    @GET("?method=artist.getinfo&api_key=" + LastFmClient.API_KEY + "&format=json")
    Call<LastfmArtist> getArtistInfo(@Query("artist") String name);
}
