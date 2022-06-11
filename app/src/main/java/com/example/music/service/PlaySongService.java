package com.example.music.service;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music.MainActivity;
import com.example.music.MyApplication;
import com.example.music.R;
import com.example.music.adapter.SongListAdapter;
import com.example.music.fragment.MusicFragment;
import com.example.music.models.Song;
import com.example.music.listener.OnMainActivityInteractionListener;
import com.example.music.listener.OnNotificationSeekBarChange;
import com.example.music.listener.OnPlaySongServiceInteractionListener;
import com.example.music.listener.OnRecyclerItemSelectedListener;
import com.example.music.receiver.MusicActionReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaySongService extends Service{
    public static final int PLAYING_SONG_ID = 221;
    public static final int ACTION_PLAY_PAUSE = 11;
    public static final int ACTION_NEXT = 12;
    public static final int ACTION_PREV = 10;
    public static final int ACTION_STOP = 13;
    public static final int MODE_REPEAT_PLAYLIST = 2;
    public static final int MODE_REPEAT_ONE = 1;
    public static final int MODE_SHUFFLE = 3;

    public static int currentSongIndex = -1;
    public static int currentSongDuration = -1;
    Song currentSong;
    public static MediaPlayer mediaPlayer;
    public static ArrayList<Song> songList;
    public static OnRecyclerItemSelectedListener newSongSelectedListener;
    public static Notification songNotification;
    private static Runnable updateSeekBarRunnable;
    NotificationCompat.Action actionPrevCompat, actionNextCompat, actionPlayPauseCompat, actionStopCompat;
    public static Notification.Action  actionPlay, actionPause;
    PendingIntent pendingPrev, pendingNext, pendingPlayPause, pendingStop;
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    public static OnMainActivityInteractionListener mainActivityInteractionListener;
    IconCompat icPrevCompat, icNextCompat, icPlayCompat, icPauseCompat, icStopCompat;
    Icon icPlay, icPause;
    public static PlaybackStateCompat playbackState;
    public static MediaSessionCompat mediaSession;
    static Handler updateSeekBar;
    static ObjectAnimator animIncreaseSeekBar, animResetSeekBar;
    static OnPlaySongServiceInteractionListener playSongServiceInteractionListener;
    public static OnRecyclerItemSelectedListener recyclerItemSelectedListener;
    LocalBroadcastManager broadcastManager;
    Intent requestEndApp;
    public static ArrayList<Integer> songIndexArr, baseSongIndexArr;
    @Override
    public void onCreate() {
        super.onCreate();
        initActions();
        sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        updateSeekBar = new Handler();
        requestEndApp = new Intent();
        requestEndApp.setAction(MainActivity.ACTION_STOP_ACTIVITY);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        initListener();
        initSongIndexArray();
    }

    private void initListener() {
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && (animResetSeekBar == null || !animResetSeekBar.isRunning())) {
                    animIncreaseSeekBar = ObjectAnimator.ofInt(mainActivityInteractionListener.getNavigationProgressBar(),
                            "progress",
                            mainActivityInteractionListener.getNavigationProgressBarProgress(), mediaPlayer.getCurrentPosition()).setDuration(300);
                    if (!animIncreaseSeekBar.isRunning()) {
                        animIncreaseSeekBar.start();
                        updateSeekBar.postDelayed(this, 200);
                    }
                } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mainActivityInteractionListener.setNavigationProgressBarProgress(mediaPlayer.getCurrentPosition());
                } else {
                    updateSeekBar.removeCallbacks(this);
                }
            }
        };
        playSongServiceInteractionListener = new OnPlaySongServiceInteractionListener() {
            @Override
            public Song getCurrentSong() {
                return currentSong;
            }

            @Override
            public void reloadNotificationMediaState() {
                resetNotificationMediaState(getApplicationContext());
            }

            @Override
            public void shuffleModeOn() {
                if (sharedPreferences.getInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST) == PlaySongService.MODE_SHUFFLE) {
                    int tempIndex = currentSongIndex;
                    songIndexArr = new ArrayList<>(baseSongIndexArr);
                    Collections.shuffle(songIndexArr);
                    for (int i = 0; i < songIndexArr.size(); i++) {
                        if (songIndexArr.get(i) == tempIndex) {
                            songIndexArr.set(i, songIndexArr.get(tempIndex));
                            songIndexArr.set(tempIndex, tempIndex);
                        }
                    }
                }
            }

            @Override
            public void shuffleModeOff() {
                currentSongIndex = baseSongIndexArr.get(songIndexArr.get(currentSongIndex));
                songIndexArr = baseSongIndexArr;
            }

            @Override
            public int getSongIndexAt(int itemOnRecycler) {
                for (int i = 0; i < songIndexArr.size(); i++) {
                    if (songIndexArr.get(i) == itemOnRecycler) {
                        return i;
                    }
                }
                return -1;
            }
        };
        OnNotificationSeekBarChange.playSongServiceInteractionListener = playSongServiceInteractionListener;
        MainActivity.playSongServiceInteractionListener = playSongServiceInteractionListener;
        SongListAdapter.playSongServiceInteractionListener = playSongServiceInteractionListener;
        MusicFragment.playSongServiceInteractionListener = playSongServiceInteractionListener;
    }


    private void initActions() {
        initIntent();
        initIcons();
        actionPrevCompat = new NotificationCompat.Action.Builder(icPrevCompat, "Previous", pendingPrev).build();
        actionNextCompat = new NotificationCompat.Action.Builder(icNextCompat, "Next", pendingNext).build();
        actionPlayPauseCompat = new NotificationCompat.Action.Builder(icPauseCompat, "Pause", pendingPlayPause).build();
        actionStopCompat = new NotificationCompat.Action.Builder(icStopCompat, "Stop", pendingStop).build();

        actionPause = new Notification.Action.Builder(icPause, "Pause", pendingPlayPause).build();
        actionPlay = new Notification.Action.Builder(icPlay, "Play", pendingPlayPause).build();
    }
    private void initIntent() {
        Intent intentPrev = new Intent(this, MusicActionReceiver.class);
        intentPrev.putExtra("action", ACTION_PREV);
        pendingPrev = PendingIntent.getBroadcast(this, ACTION_PREV, intentPrev, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(this, MusicActionReceiver.class);
        intentNext.putExtra("action", ACTION_NEXT);
        pendingNext = PendingIntent.getBroadcast(this, ACTION_NEXT, intentNext, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlayPause = new Intent(this, MusicActionReceiver.class);
        intentPlayPause.putExtra("action", ACTION_PLAY_PAUSE);
        pendingPlayPause = PendingIntent.getBroadcast(this, ACTION_PLAY_PAUSE, intentPlayPause, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentStop = new Intent(this, MusicActionReceiver.class);
        intentStop.putExtra("action", ACTION_STOP);
        pendingStop = PendingIntent.getBroadcast(this, ACTION_STOP, intentStop, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

    }
    private void initIcons() {
        icPrevCompat = IconCompat.createWithResource(this, R.drawable.ic_prev);
        icNextCompat = IconCompat.createWithResource(this, R.drawable.ic_next);
        icPlayCompat = IconCompat.createWithResource(this, R.drawable.ic_play);
        icPauseCompat = IconCompat.createWithResource(this, R.drawable.ic_pause);
        icStopCompat = IconCompat.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel);

        icPause = Icon.createWithResource(this, R.drawable.ic_pause);
        icPlay = Icon.createWithResource(this, R.drawable.ic_play);
    }


    private void initSongIndexArray() {
        baseSongIndexArr = new ArrayList<>();
        for (int i = 0; i < songList.size()-1; i++) {
            baseSongIndexArr.add(i);
        }
        songIndexArr = baseSongIndexArr;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action", -1);


        if (currentSongDuration > 0 && mediaPlayer == null && currentSongIndex != -1) {
            currentSong = songList.get(songIndexArr.get(currentSongIndex));
            mediaPlayer = MediaPlayer.create(this, Uri.parse(currentSong.path));
            mediaPlayer.seekTo(currentSongDuration);


            editor.putInt("currentDur", -1);
            editor.commit();
            currentSongDuration = -1;

            initNotification();
//            startSong(getApplicationContext());

            mediaEndListening();

        } else if (mediaPlayer == null && currentSongDuration < 0 && currentSongIndex != -1) {
            currentSong = songList.get(songIndexArr.get(currentSongIndex));
            mediaPlayer = MediaPlayer.create(this, Uri.parse(currentSong.path));


            initNotification();
            startSong(getApplicationContext());

            mediaEndListening();


        }

        if (currentSongIndex != -1) {
            handleEvent(action);

            startForeground(PLAYING_SONG_ID, songNotification);
            mainActivityInteractionListener.validatePlayPauseButton();
        }
        return START_NOT_STICKY;
    }

    private void mediaEndListening() {
        mediaPlayer.setOnCompletionListener(mp -> {
            resetNavigationSeekBar();
            if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == MODE_REPEAT_ONE) {
                repeatSong();
            } else {
                nextSong(getApplicationContext());
            }
        });
    }

    private void initNotification() {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putLong(MediaMetadata.METADATA_KEY_DURATION, currentSong.duration)
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentSong.songName).build();
        playbackState = getPlayBackState(mediaPlayer.isPlaying());

        OnNotificationSeekBarChange notificationSeekBarChange = new OnNotificationSeekBarChange();

        mediaSession = new MediaSessionCompat(this, "current_session");
        mediaSession.setActive(true);
        mediaSession.setMetadata(metadata);
        mediaSession.setPlaybackState(getPlayBackState(mediaPlayer.isPlaying()));
        mediaSession.setCallback(notificationSeekBarChange);

        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
        mediaStyle.setMediaSession(mediaSession.getSessionToken());

        songNotification = new NotificationCompat.Builder(this, MyApplication.PLAYING_SONG_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(currentSong.songImage)
                .setContentTitle(currentSong.songName)
                .setContentText(currentSong.singer)
                .addAction(actionPrevCompat)
                .addAction(actionPlayPauseCompat)
                .addAction(actionNextCompat)
                .addAction(actionStopCompat)
                .setStyle(mediaStyle).build();

    }

    private static PlaybackStateCompat getPlayBackState(boolean playing) {
        return new PlaybackStateCompat.Builder()
                .setState(playing? PlaybackStateCompat.STATE_PLAYING:PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1, SystemClock.elapsedRealtime())
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO).build();
    }

    private void handleEvent(int action) {
        switch (action) {
            case ACTION_PREV:
                prevSong(getApplicationContext());
                break;
            case ACTION_PLAY_PAUSE:
                playPause(getApplicationContext());
                break;
            case ACTION_NEXT:
                nextSong(getApplicationContext());
                break;
            case ACTION_STOP:
                stopSongService();
                break;
        }
    }

    public static void prevSong(Context context) {
        playSongServiceInteractionListener.getCurrentSong().isCurrentItem = false;
        editor.putInt("currentDur", -1);
        editor.commit();
        currentSongDuration = -1;
        resetNavigationSeekBar();
        if (currentSongIndex == 0) {
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex, currentSongIndex = songList.size() - 2);
            renewSong(context);

        } else if (currentSongIndex > 0) {
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex, --currentSongIndex);
            renewSong(context);

        }
        editor.putInt("currentSong", PlaySongService.songIndexArr.get(PlaySongService.currentSongIndex));
        editor.commit();
        mainActivityInteractionListener.validateFavButton();
    }

    public static void playPause(Context context) {
        if (mediaPlayer == null) {
            Intent service = new Intent(context, PlaySongService.class);
            service.putExtra("action", ACTION_PLAY_PAUSE);
            context.startService(service);
        } else {
            if (mediaPlayer.isPlaying()) {
                pauseSong(context);
            } else {
                startSong(context);
            }
        }

        newSongSelectedListener.setBackGroundForNewSong(-1, currentSongIndex);
        mainActivityInteractionListener.validatePlayPauseButton();
    }
    public static void pauseSong(Context context) {

        editor.putInt("currentDur", mediaPlayer.getCurrentPosition());
        editor.commit();

        if (mediaPlayer.isPlaying()) {
            songNotification.actions[1] = actionPlay;
            mediaPlayer.pause();
        }
        editor.putInt("currentSong", PlaySongService.songIndexArr.get(PlaySongService.currentSongIndex));
        editor.commit();
        resetNotificationMediaState(context);
    }
    public static void startSong(Context context) {
        if (!mediaPlayer.isPlaying()) {
            songNotification.actions[1] = actionPause;
            mediaPlayer.start();
        }
        resetNotificationMediaState(context);
        updateNavigationSeekBarListener();
    }
    public static void nextSong(Context context) {
        playSongServiceInteractionListener.getCurrentSong().isCurrentItem = false;
        editor.putInt("currentDur", -1);
        editor.commit();
        currentSongDuration = -1;
        resetNavigationSeekBar();
        if (currentSongIndex == songList.size()-2) {
            playSongServiceInteractionListener.shuffleModeOn();
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex, currentSongIndex = 0);
            renewSong(context);

        } else if (currentSongIndex < songList.size() - 2) {
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex, ++currentSongIndex);
            renewSong(context);

        }
        editor.putInt("currentSong", PlaySongService.songIndexArr.get(PlaySongService.currentSongIndex));
        editor.commit();
        mainActivityInteractionListener.validateFavButton();
    }

    public void repeatSong() {
        editor.putInt("currentDur", -1);
        editor.commit();
        resetNavigationSeekBar();
        newSongSelectedListener.setBackGroundForNewSong(currentSongIndex, currentSongIndex);
        renewSong(getApplicationContext());
        editor.putInt("currentSong", PlaySongService.songIndexArr.get(PlaySongService.currentSongIndex));
        editor.commit();
    }
    private void stopSongService() {
        if (mediaPlayer != null) {
            editor.putInt("currentDur", mediaPlayer.getCurrentPosition());
            editor.commit();
            mediaPlayer.pause();
            newSongSelectedListener.setBackGroundForNewSong(-1, currentSongIndex);
            mediaPlayer.release();
            mediaPlayer = null;
        }
        editor.putInt("currentSong", PlaySongService.songIndexArr.get(PlaySongService.currentSongIndex));
        editor.commit();
        stopSelf();
        broadcastManager.sendBroadcast(requestEndApp);
    }

    private static void renewSong(Context context) {
        mediaPlayer.release();
        mediaPlayer = null;
        Intent intent = new Intent(context, PlaySongService.class);
        context.startService(intent);
    }

    public static void resetNotificationMediaState(Context baseContext) {
        playbackState = getPlayBackState(mediaPlayer.isPlaying());
        mediaSession.setPlaybackState(playbackState);
        Intent intent = new Intent(baseContext, PlaySongService.class);
        baseContext.startService(intent);
    }

    public static void updateNavigationSeekBarListener() {
        if (animResetSeekBar == null || !animResetSeekBar.isRunning()) {
            mainActivityInteractionListener.setNavigationProgressBarMax(mediaPlayer.getDuration());
            updateSeekBar.postDelayed(updateSeekBarRunnable, 200);
        }
    }

    public static void resetNavigationSeekBar() {
        if (mainActivityInteractionListener.getNavigationProgressBar() == null) {
            return;
        }
        float progress = mainActivityInteractionListener.getNavigationProgressBarProgress();
        float max = mainActivityInteractionListener.getNavigationProgressBar().getMax();
        int time = (int) ((progress / max) * 2000f);


            animResetSeekBar = ObjectAnimator.ofInt(mainActivityInteractionListener.getNavigationProgressBar(),
                    "progress",
                    (int) progress,
                    0).setDuration(Math.max(time, 500));
            animResetSeekBar.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animResetSeekBar = null;
                    if (mediaPlayer != null) {
                        mainActivityInteractionListener.setNavigationProgressBarMax(mediaPlayer.getDuration());
                        updateSeekBar.postDelayed(updateSeekBarRunnable, 200);
                    }
                }
            });
            animResetSeekBar.start();

    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }


}
