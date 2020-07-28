package power.audio.pro.music.player.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.PresetReverb;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.widget.RemoteViews;

import com.ahihi.moreapps.util.GlideApp;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.reflect.TypeToken;
import com.nvp.util.NvpUtils;
import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.activity.MainActivity;
import power.audio.pro.music.player.activity.PlayerActivity;
import power.audio.pro.music.player.equalizer.EqualizerHelper;
import power.audio.pro.music.player.equalizer.EqualizerSetting;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.utils.AudioFocusManager;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SpUtils;
import power.audio.pro.music.player.utils.ToastUtils;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class PlayerService extends Service
        implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int NO_REPEAT = 0;
    public static final int REPEAT_ALL = 1;
    public static final int REPEAT_ONE = 2;

    private static final String EXTRA_PLAYLIST = "extra_playlist";
    private static final String EXTRA_PLAYING_SONG = "extra_playing_song";
    private static final String EXTRA_FROM_POSITION = "extra_from_position";
    private static final String EXTRA_TO_POSITION = "extra_to_position";
    private static final String EXTRA_REMOVE_SONG = "extra_remove_song";
    private static final String EXTRA_ADD_SONG = "extra_add_song";
    private static final String EXTRA_DELETE_SONG = "extra_delete_song";
    private static final String EXTRA_COMMAND = "extra_command";
    private static final String EXTRA_SET_UPDATE_TIME = "extra_set_update_time";
    private static final String EXTRA_SEEK_TO = "extra_seek_to";
    private static final String CHANNEL_ID = "power.audio.pro.music.channel";

    private static final int NOTIFICATION_ID = 13;
    private static final int COMMAND_PLAY_PAUSE = 1;
    private static final int COMMAND_PLAY_SONG = 2;
    private static final int COMMAND_NEXT = 3;
    private static final int COMMAND_PREVIOUS = 4;
    private static final int COMMAND_SHUFFLE = 5;
    private static final int COMMAND_REPEAT = 6;
    private static final int COMMAND_GET_INFO = 7;
    private static final int COMMAND_EXPAND_PLAYER = 8;
    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    private MediaSessionCompat mMediaSessionCompat;
    private AudioFocusManager mAudioFocusManager;

    private BroadcastReceiver mBecomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                if (isPlaying()) {
                    servicePauseSong();
                    updateMediaSession(false);
                    updateNotification();
                }
            }
        }
    };

    private NotificationManager mNotificationManager;

    private Handler mHandler = new Handler();
    private Runnable mTimerPause = new Runnable() {
        @Override
        public void run() {
            if (isPlaying()) {
                servicePauseSong();
                updateNotification();
                updateMediaSession(false);
                saveLastPlaylist();
                saveLastPlayingSong(getPlayingSong());
                saveLastIsShuffle(isShuffle);
                saveLastRepeatType(mRepeatType);
            }
            SpUtils.remove(MainActivity.PREF_TIMER_STOP_TIME);
        }
    };
    private Runnable mTimeUpdate = new Runnable() {
        @Override
        public void run() {
            PlayerNotificationManager.postNotificationName(PlayerNotificationManager.updateTime, mMediaPlayer.getCurrentPosition());
            mHandler.postDelayed(this, 1000);
        }
    };
    private static MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private List<SongDetail> mSongs = new ArrayList<>();
    private List<SongDetail> mShuffleSongs = new ArrayList<>();
    private int mPlayingPosition;
    private boolean isShuffle;
    private int mRepeatType;
    private boolean isForeground;
    private int lastPlayingProgress = -1;
    private boolean isError;
    private boolean pauseByAudioFocus;
    //equlizer helper
    EqualizerHelper mEqualizerHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager=(AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mAudioFocusManager = new AudioFocusManager(this, this, mHandler);

        mMediaSessionCompat = new MediaSessionCompat(this, getClass().getName());
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSessionCompat.setCallback(new MediaSessionCompatCallback());

        initAudioFX();

        try {
            applyMediaPlayerEQ();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }


        mSongs.addAll(getLastPlaylist());
        mShuffleSongs.addAll(getLastShufflePlaylist());
        isShuffle = getLastIsShuffle();
        mRepeatType = getLastRepeatType();

        SongDetail songDetail = getLastPlayingSong();
        if (songDetail != null) {
            int position = positionOf(songDetail, false);
            if (position != -1) {
                mPlayingPosition = position;
                lastPlayingProgress = getLastPlayingSongProgress();
            }
        }


        startTimerIfNeed();

        SpUtils.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Initializes the pro and audio effects for this service session.
     */
    public void initAudioFX() {
        try {
            //Instatiate the pro helper object.
            mEqualizerHelper = new EqualizerHelper(getApplicationContext(), mMediaPlayer.getAudioSessionId(), true);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void applyMediaPlayerEQ() {

        if (mEqualizerHelper==null || !mEqualizerHelper.isEqualizerSupported())
            return;

        EqualizerSetting equalizerSetting = mEqualizerHelper.getLastEquSetting();
        if(equalizerSetting==null) return;

        int fiftyHertzBandValue =  equalizerSetting.getFiftyHertz();
        int oneThirtyHertzBandValue = equalizerSetting.getOneThirtyHertz();
        int threeTwentyHertzBandValue = equalizerSetting.getThreeTwentyHertz();
        int eightHundredHertzBandValue =equalizerSetting.getEightHundredHertz();
        int twoKilohertzBandValue = equalizerSetting.getTwoKilohertz();
        int fiveKilohertzBandValue = equalizerSetting.getFiveKilohertz();
        int twelvePointFiveKilohertzBandValue = equalizerSetting.getTwelvePointFiveKilohertz();
        short enhancementValue = (short) equalizerSetting.getEnhancement();
        short virtualizerLevelValue = (short) equalizerSetting.getVirtualizer();
        short bassboostValue = (short) equalizerSetting.getBassBoost();
        short reverbValue = (short) equalizerSetting.getReverb();

        short fiftyHertzBand = mEqualizerHelper.getEqualizer().getBand(50000);
        short oneThirtyHertzBand = mEqualizerHelper.getEqualizer().getBand(130000);
        short threeTwentyHertzBand = mEqualizerHelper.getEqualizer().getBand(320000);
        short eightHundredHertzBand = mEqualizerHelper.getEqualizer().getBand(800000);
        short twoKilohertzBand = mEqualizerHelper.getEqualizer().getBand(2000000);
        short fiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(5000000);
        short twelvePointFiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(9000000);

//        Log.d("PlayerService", "fiftyHertzBandValue: " + fiftyHertzBandValue);
//        Log.d("PlayerService", "oneThirtyHertzBandValue: " + oneThirtyHertzBandValue);
//        Log.d("PlayerService", "threeTwentyHertzBandValue: " + threeTwentyHertzBandValue);
//        Log.d("PlayerService", "eightHundredHertzBandValue: " + eightHundredHertzBandValue);
//        Log.d("PlayerService", "twoKilohertzBandValue: " + twoKilohertzBandValue);
//        Log.d("PlayerService", "fiveKilohertzBandValue: " + fiveKilohertzBandValue);
//        Log.d("PlayerService", "twelvePointFiveKilohertzBandValue: " + twelvePointFiveKilohertzBandValue);
//        Log.d("PlayerService", "virtualizerLevelValue: " + virtualizerLevelValue);
//        Log.d("PlayerService", "bassboostValue: " + bassboostValue);
//        Log.d("PlayerService", "reverbValue: " + reverbValue);

        //50Hz Band.
        if (fiftyHertzBandValue==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) 0);
        } else if (fiftyHertzBandValue < 16) {

            if (fiftyHertzBandValue==0) {
                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) (-(16-fiftyHertzBandValue)*100));
            }

        } else if (fiftyHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) ((fiftyHertzBandValue-16)*100));
        }

        //130Hz Band.
        if (oneThirtyHertzBandValue==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) 0);
        } else if (oneThirtyHertzBandValue < 16) {

            if (oneThirtyHertzBandValue==0) {
                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) (-(16-oneThirtyHertzBandValue)*100));
            }

        } else if (oneThirtyHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) ((oneThirtyHertzBandValue-16)*100));
        }

        //320Hz Band.
        if (threeTwentyHertzBandValue==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) 0);
        } else if (threeTwentyHertzBandValue < 16) {

            if (threeTwentyHertzBandValue==0) {
                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) (-(16-threeTwentyHertzBandValue)*100));
            }

        } else if (threeTwentyHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) ((threeTwentyHertzBandValue-16)*100));
        }

        //800Hz Band.
        if (eightHundredHertzBandValue==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) 0);
        } else if (eightHundredHertzBandValue < 16) {

            if (eightHundredHertzBandValue==0) {
                mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) (-(16-eightHundredHertzBandValue)*100));
            }

        } else if (eightHundredHertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) ((eightHundredHertzBandValue-16)*100));
        }

        //2kHz Band.
        if (twoKilohertzBandValue==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) 0);
        } else if (twoKilohertzBandValue < 16) {

            if (twoKilohertzBandValue==0) {
                mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) (-(16-twoKilohertzBandValue)*100));
            }

        } else if (twoKilohertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) ((twoKilohertzBandValue-16)*100));
        }

        //5kHz Band.
        if (fiveKilohertzBandValue==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) 0);
        } else if (fiveKilohertzBandValue < 16) {

            if (fiveKilohertzBandValue==0) {
                mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) (-(16-fiveKilohertzBandValue)*100));
            }

        } else if (fiveKilohertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) ((fiveKilohertzBandValue-16)*100));
        }

        //12.5kHz Band.
        if (twelvePointFiveKilohertzBandValue==16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) 0);
        } else if (twelvePointFiveKilohertzBandValue < 16) {

            if (twelvePointFiveKilohertzBandValue==0) {
                mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) (-(16-twelvePointFiveKilohertzBandValue)*100));
            }

        } else if (twelvePointFiveKilohertzBandValue > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) ((twelvePointFiveKilohertzBandValue-16)*100));
        }

        //Set the audioFX values.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mEqualizerHelper.getEnhancer().setTargetGain(enhancementValue);
        }
        mEqualizerHelper.getVirtualizer().setStrength(virtualizerLevelValue);
        mEqualizerHelper.getBassBoost().setStrength(bassboostValue);

        if (reverbValue==0) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_NONE);
        } else if (reverbValue==1) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
        } else if (reverbValue==2) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
        } else if (reverbValue==3) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
        } else if (reverbValue==4) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
        } else if (reverbValue==5) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
        } else if (reverbValue==6) {
            mEqualizerHelper.getPresetReverb().setPreset(PresetReverb.PRESET_PLATE);
        }
    }

    public EqualizerHelper getEqualizerHelper() {return mEqualizerHelper;}

    private void stopPlayer() {
        try {
            mMediaPlayer.stop();
            updateMediaSession(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isForeground) {
            stopForeground(true);
            isForeground = false;
        }

        mNotificationManager.cancel(NOTIFICATION_ID);

        serviceSetUpdateTime(false);
        handlingChangesInAudioOutput(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(MainApplication.getService()==null){
            MainApplication.setService(this);
        }
        if (intent == null) {
            return START_NOT_STICKY;
        }

        if (intent.hasExtra(EXTRA_PLAYLIST)) {
            ArrayList<SongDetail> songDetails = intent.getParcelableArrayListExtra(EXTRA_PLAYLIST);
            if (songDetails != null) {
                mPlayingPosition = 0;
                setList(songDetails);
            }
        } else if (intent.hasExtra(EXTRA_PLAYING_SONG)) {
            SongDetail playingSong = intent.getParcelableExtra(EXTRA_PLAYING_SONG);
            if (playingSong != null) {
                mPlayingPosition = positionOf(playingSong, true);
            }
        } else if (intent.hasExtra(EXTRA_SET_UPDATE_TIME)) {
            serviceSetUpdateTime(isPlaying() && intent.getBooleanExtra(EXTRA_SET_UPDATE_TIME, true));
        } else if (intent.hasExtra(EXTRA_SEEK_TO)) {
            int progress = intent.getIntExtra(EXTRA_SEEK_TO, 0);
            mMediaPlayer.seekTo(progress);
            if (!isPlaying()) {
                PlayerNotificationManager.postNotificationName(PlayerNotificationManager.updateTime, progress);
            }
            if (lastPlayingProgress != -1) {
                lastPlayingProgress = progress;
                saveLastPlayingSongProgress(lastPlayingProgress);
            }
        } else if (intent.hasExtra(EXTRA_FROM_POSITION) && intent.hasExtra(EXTRA_TO_POSITION)) {
            int fromPosition = intent.getIntExtra(EXTRA_FROM_POSITION, -1);
            if (fromPosition >= 0 && fromPosition <= getCurrentPlaylist().size() - 1) {
                int toPosition = intent.getIntExtra(EXTRA_TO_POSITION, -1);
                if (toPosition >= 0 && fromPosition <= getCurrentPlaylist().size() - 1) {
                    SongDetail playingSong = getPlayingSong();
                    NvpUtils.swap(getCurrentPlaylist(), fromPosition, toPosition);
                    mPlayingPosition = positionOf(playingSong, true);
                    PlayerNotificationManager.postNotificationName(PlayerNotificationManager.swapSong, fromPosition, toPosition);
                    saveLastPlaylist();
                }
            }
        } else if (intent.hasExtra(EXTRA_REMOVE_SONG)) {
            SongDetail songDetail = intent.getParcelableExtra(EXTRA_REMOVE_SONG);
            onSongRemoveOrDelete(songDetail, PlayerNotificationManager.removeSongFromQueue);
        } else if (intent.hasExtra(EXTRA_DELETE_SONG)) {
            SongDetail songDetail = intent.getParcelableExtra(EXTRA_DELETE_SONG);
            onSongRemoveOrDelete(songDetail, PlayerNotificationManager.deleteSong);
        } else if (intent.hasExtra(EXTRA_ADD_SONG)) {
            SongDetail songDetail = intent.getParcelableExtra(EXTRA_ADD_SONG);
            if (songDetail != null && !getCurrentPlaylist().contains(songDetail)) {
                mSongs.add(songDetail);
                mShuffleSongs.add(songDetail);
                PlayerNotificationManager.postNotificationName(PlayerNotificationManager.addSongToQueue, songDetail);
                saveLastPlaylist();
            }
        } else if (intent.hasExtra(EXTRA_COMMAND)) {
            int command = intent.getIntExtra(EXTRA_COMMAND, -1);
            switch (command) {
                case COMMAND_PLAY_PAUSE:
                    serviceTogglePlayPause();
                    break;
                case COMMAND_PLAY_SONG:
                    lastPlayingProgress = -1;
                    if (isPlayingSongExists()) {
                        servicePlaySong();
                    } else {
                        serviceNextSong();
                    }
                    break;
                case COMMAND_NEXT:
                    serviceNextSong();
                    break;
                case COMMAND_PREVIOUS:
                    servicePreviousSong();
                    break;
                case COMMAND_SHUFFLE:
                    SongDetail playingSong = getPlayingSong();
                    isShuffle = !isShuffle;
                    mPlayingPosition = positionOf(playingSong, true);
                    PlayerNotificationManager.postNotificationName(PlayerNotificationManager.shuffle, isShuffle, getCurrentPlaylist());
                    saveLastIsShuffle(isShuffle);
                    break;
                case COMMAND_REPEAT:
                    switch (mRepeatType) {
                        case NO_REPEAT:
                            setRepeatType(REPEAT_ALL);
                            break;
                        case REPEAT_ALL:
                            setRepeatType(REPEAT_ONE);
                            break;
                        case REPEAT_ONE:
                            setRepeatType(NO_REPEAT);
                            break;
                    }
                    break;
                case COMMAND_GET_INFO:
                    if (isPlaylistEmpty(false) || getPlayingSong() == null) {
                        PlayerNotificationManager.postNotificationName(PlayerNotificationManager.info);
                    } else {
                        PlayerNotificationManager.postNotificationName(PlayerNotificationManager.info,
                                getCurrentPlaylist(),
                                getPlayingSong(),
                                isPlaying(),
                                isShuffle,
                                mRepeatType,
                                lastPlayingProgress == -1 ? mMediaPlayer.getCurrentPosition() : lastPlayingProgress);
                    }
                    break;
                case COMMAND_EXPAND_PLAYER:
                    Intent expandPlayerIntent = new Intent();
                    expandPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (PlayerNotificationManager.isEmptyObservers()) {
                        expandPlayerIntent.setClass(this, MainActivity.class);
                        expandPlayerIntent.setAction(MainActivity.ACTION_EXPAND_PLAYER);
                        expandPlayerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    } else {
                        expandPlayerIntent.setClass(this, PlayerActivity.class);
                    }
                    startActivity(expandPlayerIntent);
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    private void serviceTogglePlayPause() {
        if (isPlayingSongExists()) {
            if (isPlaying()) {
                servicePauseSong();
            } else if (lastPlayingProgress == -1) {
                serviceStartSong();
            } else {
                servicePlaySong();
            }
            updateMediaSession(false);
            updateNotification();
        } else {
            serviceNextSong();
        }
    }

    private void saveLastPlaylist() {
        saveLastPlaylist(mSongs);
        saveLastShufflePlaylist(mShuffleSongs);
    }

    private void onSongRemoveOrDelete(SongDetail songDetail, int observerId) {
        if (songDetail != null) {
            SongDetail playingSong = getPlayingSong();
            int removePosition = positionOf(songDetail, true);

            mSongs.remove(songDetail);
            mShuffleSongs.remove(songDetail);
            PlayerNotificationManager.postNotificationName(observerId, songDetail);

            if (mPlayingPosition > removePosition) {
                mPlayingPosition--;
            } else if (mPlayingPosition == removePosition && playingSong != null) {
                servicePlaySong();
            }

            saveLastPlaylist();
        }
    }

    private void setList(List<SongDetail> theSongs) {
        mSongs.clear();
        mSongs.addAll(theSongs);
        mShuffleSongs.clear();
        mShuffleSongs.addAll(theSongs);
        Collections.shuffle(mShuffleSongs);
        saveLastPlaylist();
    }

    private void servicePlaySong() {
        if (isPlaylistEmpty(true)) {
            return;
        }

        SongDetail playingSong = getPlayingSong();
        if (playingSong == null) {
            return;
        }

        mMediaPlayer.reset();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        try {
            mMediaPlayer.setDataSource(playingSong.getPath());
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Log.d("PlayerService", "onPrepared...........: ");
        mAudioFocusManager.requestAudioFocus();
        if (lastPlayingProgress != -1) {
            mp.seekTo(lastPlayingProgress);
            lastPlayingProgress = -1;
        }
        pauseByAudioFocus = false;
        try {
            applyMediaPlayerEQ();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        mp.start();
        SongDetail playingSong = getPlayingSong();
        if (playingSong != null) {
            PlayerNotificationManager.postNotificationName(PlayerNotificationManager.start, playingSong, false);
        }
        serviceSetUpdateTime(true);
        handlingChangesInAudioOutput(true);
        updateMediaSession(false);
        updateNotification();
        if (playingSong != null) {
            saveLastPlayingSong(playingSong);
        }
    }

    private void updateMediaSession(boolean stopped) {
        if (stopped && mMediaSessionCompat.isActive()) {
            mMediaSessionCompat.setActive(false);
            return;
        }

        if (isPlaylistEmpty(true)) {
            return;
        }

        final SongDetail songDetail = getPlayingSong();
        if (songDetail == null) {
            return;
        }

        if (!mMediaSessionCompat.isActive()) {
            mMediaSessionCompat.setActive(true);
        }

        mMediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, mPlayingPosition, 1)
                .setActions(MEDIA_SESSION_ACTIONS)
                .build());

        GlideApp.with(getApplicationContext())
                .asBitmap()
                .load(songDetail.getCoverUri())
                .centerCrop()
                .placeholder(R.drawable.default_album)
                .error(R.drawable.default_album)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mMediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songDetail.getArtist())
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songDetail.getAlbum())
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songDetail.getTitle())
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, songDetail.getDuration())
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
                                .build());
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        mMediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songDetail.getArtist())
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, songDetail.getAlbum())
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, songDetail.getTitle())
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, songDetail.getDuration())
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.drawable.default_album))
                                .build());
                    }
                });
    }

    private void updateNotification() {
        if (isPlaylistEmpty(true)) {
            return;
        }

        SongDetail playingSong = getPlayingSong();
        if (playingSong == null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Media playback controls");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mNotificationManager.createNotificationChannel(channel);
        }

        String title = playingSong.getTitle();
        String artist = playingSong.getArtist();
        String coverUri = playingSong.getCoverUri();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateNotificationModern(title, artist, coverUri);
        } else {
            updateNotificationClassic(title, artist, coverUri);
        }
    }

    private void updateNotificationModern(final String title, final String artist, String uri) {
        GlideApp.with(getApplicationContext())
                .asBitmap()
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.default_album)
                .error(R.drawable.default_album)
                .override(NvpUtils.dpToPixels(128))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        updateNotification(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        updateNotification(BitmapFactory.decodeResource(getResources(), R.drawable.default_album));
                    }

                    private void updateNotification(Bitmap bitmap) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setStyle(new MediaStyle()
                                        .setMediaSession(mMediaSessionCompat.getSessionToken())
                                        .setShowActionsInCompactView(0, 1, 2))
                                .setSmallIcon(R.drawable.ic_music_note)
                                .setLargeIcon(bitmap)
                                .setContentTitle(title)
                                .setContentText(artist)
                                .setContentIntent(notificationAction(COMMAND_EXPAND_PLAYER))
                                .setOngoing(isPlaying())
                                .setAutoCancel(!isPlaying())
                                .setShowWhen(false)
                                .addAction(new NotificationCompat.Action(R.drawable.ic_menu_previous,
                                        getString(R.string.action_previous),
                                        notificationAction(COMMAND_PREVIOUS)))
                                .addAction(new NotificationCompat.Action(isPlaying() ? R.drawable.ic_menu_pause : R.drawable.ic_menu_play,
                                        getString(R.string.action_play_pause),
                                        notificationAction(COMMAND_PLAY_PAUSE)))
                                .addAction(new NotificationCompat.Action(R.drawable.ic_menu_next,
                                        getString(R.string.action_next),
                                        notificationAction(COMMAND_NEXT)))
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

                        startForegroundIfNeed(builder.build());
                    }
                });
    }

    private void updateNotificationClassic(String title, String artist, String coverUri) {
        RemoteViews notificationSmall = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationBig = new RemoteViews(getPackageName(), R.layout.notification_big);

        updateNotificationClassic(notificationSmall, title, artist);
        updateNotificationClassic(notificationBig, title, artist);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(title)
                .setCustomContentView(notificationSmall)
                .setCustomBigContentView(notificationBig)
                .setOngoing(isPlaying())
                .setAutoCancel(!isPlaying())
                .setContentIntent(notificationAction(COMMAND_EXPAND_PLAYER))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForegroundIfNeed(notification);

        updateNotificationClassicSongArt(notificationSmall, notification, coverUri, 64);
        updateNotificationClassicSongArt(notificationBig, notification, coverUri, 128);
    }

    private void updateNotificationClassic(RemoteViews views, String title, String artist) {
        views.setOnClickPendingIntent(R.id.btn_play_pause, notificationAction(COMMAND_PLAY_PAUSE));
        views.setOnClickPendingIntent(R.id.btn_next, notificationAction(COMMAND_NEXT));
        views.setOnClickPendingIntent(R.id.btn_previous, notificationAction(COMMAND_PREVIOUS));
        views.setImageViewResource(R.id.img_song_thumb, R.drawable.default_album);
        views.setImageViewResource(R.id.btn_play_pause, isPlaying() ? R.drawable.ic_menu_pause : R.drawable.ic_menu_play);
        views.setTextViewText(R.id.song_name, title);
        views.setTextViewText(R.id.song_artist, artist);
    }

    private void updateNotificationClassicSongArt(RemoteViews views, Notification notification, String uri, int sizeDp) {
        GlideApp.with(getApplicationContext())
                .asBitmap()
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.default_album)
                .error(R.drawable.default_album)
                .override(NvpUtils.dpToPixels(sizeDp))
                .into(new NotificationTarget(getApplicationContext(), R.id.img_song_thumb, views, notification, NOTIFICATION_ID));
    }

    private PendingIntent notificationAction(int command) {
        return PendingIntent.getService(getApplicationContext(),
                command,
                new Intent(getApplicationContext(), PlayerService.class).putExtra(EXTRA_COMMAND, command),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void startForegroundIfNeed(Notification notification) {
        if (isPlaying()) {
            if (isForeground) {
                mNotificationManager.notify(NOTIFICATION_ID, notification);
            } else {
                startForeground(NOTIFICATION_ID, notification);
                isForeground = true;
            }
        } else {
            if (isForeground) {
                stopForeground(false);
                isForeground = false;
            }
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (isError) {
            isError = false;
            return;
        }
        switch (mRepeatType) {
            case NO_REPEAT:
                if (mPlayingPosition == getCurrentPlaylist().size() - 1) {
                    serviceSetUpdateTime(false);
                    handlingChangesInAudioOutput(false);
                    updateMediaSession(true);
                    updateNotification();
                    PlayerNotificationManager.postNotificationName(PlayerNotificationManager.completion);
                } else {
                    serviceNextSong();
                }
                break;
            case REPEAT_ALL:
                serviceNextSong();
                break;
            case REPEAT_ONE:
                serviceStartSong();
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        isError = true;
        mp.reset();
        PlayerNotificationManager.postNotificationName(PlayerNotificationManager.error);
        return false;
    }

    private List<SongDetail> getCurrentPlaylist() {
        return isShuffle ? mShuffleSongs : mSongs;
    }

    private boolean isPlaying() {
        if (isPlaylistEmpty(false)) {
            return false;
        }
        try {
            return mMediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    private void servicePauseSong() {
        if (isPlaylistEmpty(true)) {
            return;
        }
        mMediaPlayer.pause();
        serviceSetUpdateTime(false);
        handlingChangesInAudioOutput(false);
        PlayerNotificationManager.postNotificationName(PlayerNotificationManager.pause);
        saveLastPlayingSongProgress(mMediaPlayer.getCurrentPosition());
    }

    private void serviceStartSong() {
        if (isPlaylistEmpty(true)) {
            return;
        }
        mAudioFocusManager.requestAudioFocus();
        if (lastPlayingProgress != -1) {
            mMediaPlayer.seekTo(lastPlayingProgress);
            lastPlayingProgress = -1;
        }
        pauseByAudioFocus = false;
        mMediaPlayer.start();
        SongDetail playingSong = getPlayingSong();
        if (playingSong != null) {
            PlayerNotificationManager.postNotificationName(PlayerNotificationManager.start, playingSong, true);
        }
        serviceSetUpdateTime(true);
        handlingChangesInAudioOutput(true);
        updateMediaSession(false);
        updateNotification();
    }

    private void servicePreviousSong() {
        if (isPlaylistEmpty(true)) {
            return;
        }

        mPlayingPosition--;
        if (mPlayingPosition < 0) {
            mPlayingPosition = getCurrentPlaylist().size() - 1;
        }

        if (isPlayingSongExists()) {
            servicePlaySong();
        } else {
            servicePreviousSong();
        }
    }

    private void serviceNextSong() {
        if (isPlaylistEmpty(true)) {
            return;
        }

        mPlayingPosition++;
        if (mPlayingPosition >= getCurrentPlaylist().size()) {
            mPlayingPosition = 0;
        }

        if (isPlayingSongExists()) {
            servicePlaySong();
        } else {
            serviceNextSong();
        }
    }

    private boolean isPlaylistEmpty(boolean postNotification) {
        if (!getCurrentPlaylist().isEmpty()) {
            return false;
        }

        if (postNotification) {
            stopPlayer();

            ToastUtils.shortMsg(R.string.msg_cannot_find_this_song);

            PlayerNotificationManager.postNotificationName(PlayerNotificationManager.playlistEmpty);

            clearSavedSession();
        }

        return true;
    }

    private boolean isPlayingSongExists() {
        SongDetail playingSong = getPlayingSong();
        if (playingSong == null) {
            return false;
        }
        if (!new File(playingSong.getPath()).exists()) {
            mShuffleSongs.remove(playingSong);
            mSongs.remove(playingSong);
            saveLastPlaylist();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        SpUtils.unregisterOnSharedPreferenceChangeListener(this);
        mHandler.removeCallbacks(mTimerPause);
        mMediaSessionCompat.release();
        mMediaPlayer.release();
        mAudioFocusManager.abandonAudioFocus();
        serviceSetUpdateTime(false);
        handlingChangesInAudioOutput(false);
        PlayerNotificationManager.postNotificationName(PlayerNotificationManager.completion);
        if (isForeground) {
            stopForeground(true);
            isForeground = false;
        }
        mNotificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    private void handlingChangesInAudioOutput(boolean enabled) {
        try {
            if (enabled) {
                registerReceiver(mBecomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            } else {
                unregisterReceiver(mBecomingNoisyReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SongDetail getPlayingSong() {
        if (mPlayingPosition < 0 || mPlayingPosition > getCurrentPlaylist().size() - 1) {
            return null;
        }
        return getCurrentPlaylist().get(mPlayingPosition);
    }

    private void setRepeatType(int repeatType) {
        mRepeatType = repeatType;
        PlayerNotificationManager.postNotificationName(PlayerNotificationManager.repeat, mRepeatType);
        saveLastRepeatType(mRepeatType);
    }

    private void serviceSetUpdateTime(boolean enabled) {
        mHandler.removeCallbacks(mTimeUpdate);
        if (enabled) {
            mHandler.post(mTimeUpdate);
        }
    }

    private int positionOf(SongDetail songDetail, boolean safe) {
        for (int i = 0; i < getCurrentPlaylist().size(); i++) {
            if (getCurrentPlaylist().get(i).equals(songDetail)) {
                return i;
            }
        }
        return safe ? 0 : -1;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange <= 0) {
            if (isPlaying()) {
                servicePauseSong();
                updateMediaSession(false);
                updateNotification();
                pauseByAudioFocus = true;
            }
        } else if (pauseByAudioFocus) {
            pauseByAudioFocus = false;
            if (!isPlaying()) {
                if (lastPlayingProgress == -1) {
                    serviceStartSong();
                } else {
                    servicePlaySong();
                }
                updateMediaSession(false);
                updateNotification();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (MainActivity.PREF_TIMER_STOP_TIME.equals(key)) {
            startTimerIfNeed();
        }
    }

    private void startTimerIfNeed() {
        mHandler.removeCallbacks(mTimerPause);

        long delayTime = SpUtils.getLong(MainActivity.PREF_TIMER_STOP_TIME, -1) - Calendar.getInstance().getTimeInMillis();

        if (delayTime > 0) {
            mHandler.postDelayed(mTimerPause, delayTime);
        } else {
            SpUtils.remove(MainActivity.PREF_TIMER_STOP_TIME);
        }
    }

    public synchronized static void setPlaylist(@NonNull List<SongDetail> songDetails) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putParcelableArrayListExtra(EXTRA_PLAYLIST, new ArrayList<>(songDetails));
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void setPlayingSong(@NonNull SongDetail songDetail) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_PLAYING_SONG, songDetail);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void playSong() {
        try {
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_COMMAND, COMMAND_PLAY_SONG);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void togglePlayPause() {
        try {
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_COMMAND, COMMAND_PLAY_PAUSE);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void nextSong() {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_COMMAND, COMMAND_NEXT);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void previousSong() {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_COMMAND, COMMAND_PREVIOUS);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void updateShuffle() {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_COMMAND, COMMAND_SHUFFLE);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void updateRepeat() {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_COMMAND, COMMAND_REPEAT);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void getInfo() {
        try {
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_COMMAND, COMMAND_GET_INFO);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void setUpdateTime(boolean enabled) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_SET_UPDATE_TIME, enabled);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void seekTo(int position) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_SEEK_TO, position);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void swapSong(int fromPosition, int toPosition) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_FROM_POSITION, fromPosition);
            intent.putExtra(EXTRA_TO_POSITION, toPosition);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void removeSongFromQueue(SongDetail songDetail) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_REMOVE_SONG, songDetail);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void addSongToQueue(SongDetail songDetail) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_ADD_SONG, songDetail);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized static void deleteSong(SongDetail songDetail) {
        try{
            Intent intent = new Intent(MainApplication.getAppContext(), PlayerService.class);
            intent.putExtra(EXTRA_DELETE_SONG, songDetail);
            MainApplication.getAppContext().startService(intent);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Save play history
     */

    private static final String KEY_IS_SHUFFLE = "is_shuffle";
    private static final String KEY_REPEAT_TYPE = "repeat_type";
    private static final String KEY_PLAYING_SONG_PROGRESS = "playing_song_progress";
    private static final String KEY_PLAYING_SONG = "playing_song";
    private static final String KEY_PLAYLIST = "playlist";
    private static final String KEY_SHUFFLE_PLAYLIST = "shuffle_playlist";

    private static void clearSavedSession() {
        SpUtils.remove(KEY_IS_SHUFFLE);
        SpUtils.remove(KEY_REPEAT_TYPE);
        SpUtils.remove(KEY_PLAYING_SONG_PROGRESS);
        SpUtils.remove(KEY_PLAYING_SONG);
        SpUtils.remove(KEY_PLAYLIST);
        SpUtils.remove(KEY_SHUFFLE_PLAYLIST);
    }

    private static void saveLastIsShuffle(boolean isShuffle) {
        SpUtils.putBoolean(KEY_IS_SHUFFLE, isShuffle);
    }

    private static boolean getLastIsShuffle() {
        return SpUtils.getBoolean(KEY_IS_SHUFFLE);
    }

    private static void saveLastRepeatType(int repeatType) {
        SpUtils.putInt(KEY_REPEAT_TYPE, repeatType);
    }

    private static int getLastRepeatType() {
        return SpUtils.getInt(KEY_REPEAT_TYPE);
    }

    private static void saveLastPlayingSongProgress(int progress) {
        SpUtils.putInt(KEY_PLAYING_SONG_PROGRESS, progress);
    }

    private static int getLastPlayingSongProgress() {
        return SpUtils.getInt(KEY_PLAYING_SONG_PROGRESS);
    }

    private static void saveLastPlayingSong(SongDetail songDetail) {
        SpUtils.putString(KEY_PLAYING_SONG, MainApplication.getAppGson().toJson(songDetail));
    }

    private static SongDetail getLastPlayingSong() {
        return MainApplication.getAppGson().fromJson(SpUtils.getString(KEY_PLAYING_SONG), SongDetail.class);
    }

    private static void saveLastPlaylist(List<SongDetail> songDetails) {
        saveLastPlaylist(songDetails, KEY_PLAYLIST);
    }

    private static List<SongDetail> getLastPlaylist() {
        return getLastPlaylist(KEY_PLAYLIST);
    }

    private static void saveLastShufflePlaylist(List<SongDetail> songDetails) {
        saveLastPlaylist(songDetails, KEY_SHUFFLE_PLAYLIST);
    }

    private static List<SongDetail> getLastShufflePlaylist() {
        return getLastPlaylist(KEY_SHUFFLE_PLAYLIST);
    }

    private static void saveLastPlaylist(List<SongDetail> songDetails, String key) {
        SpUtils.putString(key, MainApplication.getAppGson().toJson(songDetails));
    }

    private static List<SongDetail> getLastPlaylist(String key) {
        try {
            Type type = new TypeToken<List<SongDetail>>() {
            }.getType();

            List<SongDetail> songDetails = MainApplication.getAppGson().<ArrayList<SongDetail>>fromJson(SpUtils.getString(key), type);

            if (songDetails == null) {
                return new ArrayList<>();
            }

            int size = songDetails.size();

            for (int i = songDetails.size() - 1; i >= 0; i--) {
                if (!new File(songDetails.get(i).getPath()).exists()) {
                    songDetails.remove(i);
                }
            }

            if (size != songDetails.size()) {
                saveLastPlaylist(songDetails, key);
            }

            return songDetails;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    class MediaSessionCompatCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            super.onPlay();
            play();
        }

        @Override
        public void onPause() {
            super.onPause();
            pause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            serviceNextSong();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            servicePreviousSong();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);

            mMediaPlayer.seekTo((int) pos);
            if (lastPlayingProgress != -1) {
                lastPlayingProgress = (int) pos;
                saveLastPlayingSongProgress(lastPlayingProgress);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            stop();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            if (Intent.ACTION_MEDIA_BUTTON.equals(mediaButtonEvent.getAction())) {
                KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event == null) {
                    return super.onMediaButtonEvent(mediaButtonEvent);
                }

                if (event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            serviceTogglePlayPause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            play();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            serviceNextSong();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            servicePreviousSong();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            stop();
                            break;
                    }
                    return true;
                }
            }
            return false;
        }

        private void stop() {
            stopPlayer();
            PlayerNotificationManager.postNotificationName(PlayerNotificationManager.completion);
        }

        private void play() {
            if (isPlayingSongExists()) {
                if (lastPlayingProgress == -1) {
                    serviceStartSong();
                } else {
                    servicePlaySong();
                }
                updateMediaSession(false);
                updateNotification();
            } else {
                serviceNextSong();
            }
        }

        private void pause() {
            if (isPlayingSongExists()) {
                if (isPlaying()) {
                    servicePauseSong();
                }
                updateMediaSession(false);
                updateNotification();
            } else {
                serviceNextSong();
            }
        }
    }

    public static class RemoteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}
