package power.audio.pro.music.player.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahihi.moreapps.util.GlideApp;
import com.nvp.util.NvpUtils;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.fragment.SongPopupFragment;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.ThemeUtils;
import power.audio.pro.music.player.widget.CircularSeekBar;

import butterknife.BindView;
import butterknife.OnClick;

public class PlayerActivity extends BaseActivity implements CircularSeekBar.OnCircularSeekBarChangeListener {
    private SongDetail mPlayingSong;

    @BindView(R.id.song_name)
    TextView songName;
    @BindView(R.id.song_artist)
    TextView songArtist;
    @BindView(R.id.btn_play_pause)
    ImageButton btnPlayPause;
    @BindView(R.id.btn_shuffle)
    ImageButton btnShuffle;
    @BindView(R.id.btn_repeat)
    ImageButton btnRepeat;
    @BindView(R.id.btn_favorite)
    ImageButton btnFavorite;
    @BindView(R.id.player_seek_bar)
    CircularSeekBar playerSeekBar;
    @BindView(R.id.tv_current_progress)
    TextView tvCurrentProgress;
    @BindView(R.id.tv_max_progress)
    TextView tvMaxProgress;
    @BindView(R.id.iv_album_art)
    ImageView ivAlbumArt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        tvCurrentProgress.setText(NvpUtils.formatTimeDuration(0));
        tvMaxProgress.setText(NvpUtils.formatTimeDuration(0));

        PlayerService.getInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_player);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.updateTime,
                PlayerNotificationManager.completion,
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.shuffle,
                PlayerNotificationManager.repeat,
                PlayerNotificationManager.toggleFavorite,
                PlayerNotificationManager.playlistEmpty,
                PlayerNotificationManager.info
        };
    }

    private void loadAlbumArt(SongDetail songDetail) {
        GlideApp.with(this)
                .load(songDetail.getCoverUri())
                .circleCrop()
                .error(R.drawable.player_default_song_cover)
                .placeholder(R.drawable.player_default_song_cover)
                .into(ivAlbumArt);
    }

    @OnClick(R.id.btn_more)
    void onBtnMoreClicked() {
        if (mPlayingSong == null) {
            return;
        }
        SongPopupFragment songPopupFragment = new SongPopupFragment();
        songPopupFragment.setSongDetail(mPlayingSong);
        songPopupFragment.remove(R.id.action_remove_from_playlist);
        songPopupFragment.remove(R.id.action_remove_from_queue);
        songPopupFragment.remove(R.id.action_add_to_queue);
        songPopupFragment.remove(R.id.action_delete);
        songPopupFragment.remove(R.id.action_add_to_favorites);
        songPopupFragment.remove(R.id.action_remove_from_favorites);
        songPopupFragment.show(getSupportFragmentManager());
    }

    @OnClick(R.id.btn_playlist)
    void onBtnPlaylistClicked() {
        PlayingQueueActivity.start(this);
    }

    @OnClick(R.id.btn_volume)
    void onBtnVolumeClicked() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        }
    }

    @OnClick(R.id.btn_previous)
    void onBtnPreviousClicked() {
        PlayerService.previousSong();
    }

    @OnClick(R.id.btn_play_pause)
    void onBtnPlayPauseClicked() {
        PlayerService.togglePlayPause();
    }

    @OnClick(R.id.btn_next)
    void onBtnNextClicked() {
        PlayerService.nextSong();
    }

    @OnClick(R.id.btn_favorite)
    void onBtnFavoriteClicked() {
        if (mPlayingSong != null) {
            if (SQLHelper.getInstance().isFavoriteSong(mPlayingSong)) {
                SQLHelper.getInstance().removeFavoriteSong(mPlayingSong);
                PlayerNotificationManager.postNotificationName(PlayerNotificationManager.toggleFavorite, mPlayingSong, false);
            } else {
                SQLHelper.getInstance().insertFavoriteSong(mPlayingSong);
                PlayerNotificationManager.postNotificationName(PlayerNotificationManager.toggleFavorite, mPlayingSong, true);
            }
        }
    }

    @OnClick(R.id.btn_shuffle)
    void onBtnShuffleClicked() {
        PlayerService.updateShuffle();
    }

    @OnClick(R.id.btn_repeat)
    void onBtnRepeatClicked() {
        PlayerService.updateRepeat();
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if (fromUser) {
            tvCurrentProgress.setText(NvpUtils.formatTimeDuration(progress));
        }
    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar seekBar) {
        PlayerService.setUpdateTime(false);
    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar seekBar) {
        PlayerService.seekTo(seekBar.getProgress());
        PlayerService.setUpdateTime(true);
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

        if (id == PlayerNotificationManager.start) {
            mPlayingSong = (SongDetail) args[0];

            btnPlayPause.setSelected(true);
            if (!(boolean) args[1]) {
                loadAlbumArt(mPlayingSong);
            }
            songName.setText(mPlayingSong.getTitle());
            songArtist.setText(mPlayingSong.getArtist());
            tvMaxProgress.setText(NvpUtils.formatTimeDuration(mPlayingSong.getDuration()));
            playerSeekBar.setMax((int) mPlayingSong.getDuration());
            playerSeekBar.setOnSeekBarChangeListener(this);
            btnFavorite.setSelected(SQLHelper.getInstance().isFavoriteSong(mPlayingSong));
            return;
        }

        if (id == PlayerNotificationManager.updateTime) {
            int currentPosition = (int) args[0];
            playerSeekBar.setProgress(currentPosition);
            tvCurrentProgress.setText(NvpUtils.formatTimeDuration(currentPosition));
            return;
        }

        if (id == PlayerNotificationManager.pause) {
            btnPlayPause.setSelected(false);
            return;
        }

        if (id == PlayerNotificationManager.completion) {
            tvCurrentProgress.setText(NvpUtils.formatTimeDuration(0));
            playerSeekBar.setProgress(0);
            btnPlayPause.setSelected(false);
            return;
        }

        if (id == PlayerNotificationManager.playlistEmpty) {
            finish();
            return;
        }

        if (id == PlayerNotificationManager.shuffle) {
            updateShuffleButton((boolean) args[0]);
            return;
        }

        if (id == PlayerNotificationManager.repeat) {
            updateRepeatButton((int) args[0]);
            return;
        }

        if (id == PlayerNotificationManager.toggleFavorite) {
            if (mPlayingSong != null && mPlayingSong.equals(args[0])) {
                btnFavorite.setSelected((boolean) args[1]);
                animateHeartButton();
            }
            return;
        }

        if (id == PlayerNotificationManager.info) {
            if (args.length > 0 && args[0] != null) {
                mPlayingSong = (SongDetail) args[1];
                int currentPosition = (int) args[5];

                if ((boolean) args[2]) {
                    btnPlayPause.setSelected(true);
                } else {
                    btnPlayPause.setSelected(false);
                }
                songName.setText(mPlayingSong.getTitle());
                songArtist.setText(mPlayingSong.getArtist());
                tvMaxProgress.setText(NvpUtils.formatTimeDuration(mPlayingSong.getDuration()));
                tvCurrentProgress.setText(NvpUtils.formatTimeDuration(currentPosition));
                playerSeekBar.setMax((int) mPlayingSong.getDuration());
                playerSeekBar.setProgress(currentPosition);
                playerSeekBar.setOnSeekBarChangeListener(this);
                btnFavorite.setSelected(SQLHelper.getInstance().isFavoriteSong(mPlayingSong));
                updateShuffleButton((boolean) args[3]);
                updateRepeatButton((int) args[4]);
                loadAlbumArt(mPlayingSong);
            }
        }
    }

    private void animateHeartButton() {
        AnimatorSet animatorSet = new AnimatorSet();
        AccelerateInterpolator interpolator = new AccelerateInterpolator();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(btnFavorite, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(interpolator);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(btnFavorite, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(interpolator);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(btnFavorite, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(interpolator);

        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.start();
    }

    private void updateRepeatButton(int repeatType) {
        switch (repeatType) {
            case PlayerService.REPEAT_ALL:
                btnRepeat.setImageResource(R.drawable.btn_repeat);
                btnRepeat.setAlpha(1.0f);
                break;
            case PlayerService.REPEAT_ONE:
                btnRepeat.setImageResource(R.drawable.btn_repeat_one);
                btnRepeat.setAlpha(1.0f);
                break;
            default:
                btnRepeat.setImageResource(R.drawable.btn_repeat);
                btnRepeat.setAlpha(0.5f);
                break;
        }
    }

    private void updateShuffleButton(boolean isShuffle) {
        btnShuffle.setAlpha(isShuffle ? 1.0f : 0.5f);
    }

    @OnClick(R.id.btn_back)
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_bottom);
    }

    public static void start(@NonNull Activity activity) {
        Intent starter = new Intent(activity, PlayerActivity.class);
        activity.startActivity(starter);
        activity.overridePendingTransition(R.anim.slide_in_top, android.R.anim.fade_out);
    }
}
