package power.audio.pro.music.player.utils.lastfmapi;

import androidx.annotation.NonNull;

import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.lastfmapi.model.ArtistInfo;
import power.audio.pro.music.player.utils.lastfmapi.model.Image;
import power.audio.pro.music.player.utils.lastfmapi.model.LastfmArtist;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 12/14/17.
 */
public class LastFmClient {
    public static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/";
    public static final String API_KEY = "5278b21f806f494d06d22a09d588bcaa";

    private static Retrofit retrofit = null;

    private static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static SOService getSOService() {
        return LastFmClient.getClient(BASE_URL).create(SOService.class);
    }

    public static void getArtistUrl(final String name, final Callback callback) {
        if (callback == null) {
            return;
        }

        String url = SQLHelper.getInstance().getArtistUrl(name);
        if (url != null) {
            callback.onArtistUrlResult(url);
            return;
        }

        LastFmClient.getSOService().getArtistInfo(name).enqueue(new retrofit2.Callback<LastfmArtist>() {
            @Override
            public void onResponse(Call<LastfmArtist> call, Response<LastfmArtist> response) {
                if (response == null) {
                    onTaskCompleted("");
                    return;
                }

                LastfmArtist lastfmArtist = response.body();
                if (lastfmArtist == null) {
                    onTaskCompleted("");
                    return;
                }

                ArtistInfo artistInfo = lastfmArtist.getArtist();
                if (artistInfo == null) {
                    onTaskCompleted("");
                    return;
                }

                Image image = artistInfo.getBestImage();
                if (image == null) {
                    onTaskCompleted("");
                    return;
                }

                String url = image.getText();
                if (url == null) {
                    onTaskCompleted("");
                    return;
                }

                onTaskCompleted(url);
            }

            @Override
            public void onFailure(Call<LastfmArtist> call, Throwable t) {
                callback.onArtistUrlResult("");
            }

            private void onTaskCompleted(@NonNull String url) {
                SQLHelper.getInstance().insertArtistUrl(name, url);
                callback.onArtistUrlResult(url);
            }
        });
    }

    public interface Callback {
        void onArtistUrlResult(String url);
    }
}
