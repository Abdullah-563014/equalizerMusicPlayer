package power.audio.pro.music.player.widget;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahihi.moreapps.util.GlideApp;
import com.bumptech.glide.request.RequestOptions;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.activity.PlayerActivity;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.PlayerNotificationManager;

import java.util.List;

public class BottomPlayer {
    private Activity activity;
    private TextView songName;
    private TextView songArtist;
    private ImageButton btnPlayPause;
    private ImageView songThumb;
    private CustomProgressBar customProgressBar;
    private RelativeLayout bottomPlayer;

    public interface BeforePlayerActivityShown {
        void beforePlayerActivityShown();
    }

    private BeforePlayerActivityShown beforePlayerActivityShown;

    public BottomPlayer(final Activity activity) {
        this.activity = activity;

        songName = activity.findViewById(R.id.song_name);
        songArtist = activity.findViewById(R.id.song_artist);
        btnPlayPause = activity.findViewById(R.id.btn_play_pause);
        songThumb = activity.findViewById(R.id.img_song_thumb);
        customProgressBar = activity.findViewById(R.id.custom_progress_bar);
        bottomPlayer = activity.findViewById(R.id.bottom_player);

        activity.findViewById(R.id.btn_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerService.previousSong();
            }
        });
        activity.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerService.nextSong();
            }
        });
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayerService.togglePlayPause();
            }
        });

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (beforePlayerActivityShown != null) {
                    beforePlayerActivityShown.beforePlayerActivityShown();
                }
                PlayerActivity.start(activity);
            }
        };

        bottomPlayer.setOnClickListener(listener);
        songArtist.setOnClickListener(listener);
        songName.setOnClickListener(listener);
        songThumb.setOnClickListener(listener);
        customProgressBar.setOnClickListener(listener);
    }

    public void setBeforePlayerActivityShown(BeforePlayerActivityShown beforePlayerActivityShown) {
        this.beforePlayerActivityShown = beforePlayerActivityShown;
    }

    public void onReceivedPlayerNotification(int id, Object... args) {
        if (id == PlayerNotificationManager.start) {
            SongDetail songDetail = (SongDetail) args[0];

            bottomPlayer.setVisibility(View.VISIBLE);
            btnPlayPause.setSelected(true);

            if (!(boolean) args[1]) {
                GlideApp.with(activity)
                        .load(songDetail.getCoverUri())
                        .apply(RequestOptions.circleCropTransform()
                                .error(R.drawable.default_song_cover)
                                .placeholder(R.drawable.default_song_cover))
                        .into(songThumb);
            }

            songName.setText(songDetail.getTitle());
            songArtist.setText(songDetail.getArtist());

            customProgressBar.setMax(songDetail.getDuration());

            return;
        }

        if (id == PlayerNotificationManager.updateTime) {
            customProgressBar.setProgress((int) args[0]);
            return;
        }

        if (id == PlayerNotificationManager.completion) {
            btnPlayPause.setSelected(false);
            customProgressBar.setProgress(0);
            return;
        }

        if (id == PlayerNotificationManager.pause) {
            btnPlayPause.setSelected(false);
            return;
        }

        if (id == PlayerNotificationManager.playlistEmpty) {
            bottomPlayer.setVisibility(View.GONE);
            return;
        }

        if (id == PlayerNotificationManager.info) {
            if (args.length > 0 && args[0] != null && ((List<SongDetail>) args[0]).size() > 0) {
                SongDetail songDetail = (SongDetail) args[1];

                bottomPlayer.setVisibility(View.VISIBLE);
                btnPlayPause.setSelected((boolean) args[2]);

                GlideApp.with(activity)
                        .load(songDetail.getCoverUri())
                        .circleCrop()
                        .error(R.drawable.default_song_cover)
                        .placeholder(R.drawable.default_song_cover)
                        .into(songThumb);

                songName.setText(songDetail.getTitle());
                songArtist.setText(songDetail.getArtist());

                customProgressBar.setMax(songDetail.getDuration());
                customProgressBar.setProgress((int) args[5]);
            } else {
                bottomPlayer.setVisibility(View.GONE);
            }
        }
    }
}
