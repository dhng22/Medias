package com.example.music;

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

import java.util.ArrayList;

public class PlaySongService extends Service{
    public static final int PLAYING_SONG_ID = 221;
    public static final int ACTION_PLAY_PAUSE = 11;
    public static final int ACTION_NEXT = 12;
    public static final int ACTION_PREV = 10;
    public static final int ACTION_STOP = 13;
    public static final int MODE_REPEAT_PLAYLIST = 2;
    public static final int MODE_REPEAT_ONE = 1;
    public static final int MODE_NO_REPEAT = 0;

    public static int currentSongIndex = -1;
    static Song currentSong;
    public static MediaPlayer mediaPlayer;
    public static ArrayList<Song> songList;
    public static OnNewSongSelectedListener newSongSelectedListener;
    public static Notification songNotification;
    private static Runnable updateSeekBarRunnable;
    NotificationCompat.Action actionPrevCompat, actionNextCompat, actionPlayPauseCompat, actionStopCompat;
    public static Notification.Action  actionPlay, actionPause;
    PendingIntent pendingPrev, pendingNext, pendingPlayPause, pendingStop;
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;
    int repeatMode;

    IconCompat icPrevCompat, icNextCompat, icPlayCompat, icPauseCompat, icStopCompat;
    Icon icPlay, icPause;
    public static PlaybackStateCompat playbackState;
    public static MediaSessionCompat mediaSession;
    static Handler updateSeekBar;
    static ObjectAnimator animIncreaseSeekBar;
    @Override
    public void onCreate() {
        super.onCreate();
        initActions();
        sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        repeatMode = sharedPreferences.getInt("repeatMode", MODE_NO_REPEAT);
        updateSeekBar = new Handler();

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    animIncreaseSeekBar = ObjectAnimator.ofInt(MainActivity.songProgress,
                            "progress",
                            MainActivity.songProgress.getProgress(), mediaPlayer.getCurrentPosition()).setDuration(300);
                    if (!animIncreaseSeekBar.isRunning()) {
                        animIncreaseSeekBar.start();
                        updateSeekBar.postDelayed(this, 200);
                    }
                } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    MainActivity.songProgress.setProgress(mediaPlayer.getCurrentPosition());

                } else {
                    updateSeekBar.removeCallbacks(this);
                }
            }
        };
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
        icPrevCompat = IconCompat.createWithResource(this, android.R.drawable.ic_media_previous);
        icNextCompat = IconCompat.createWithResource(this, android.R.drawable.ic_media_next);
        icPlayCompat = IconCompat.createWithResource(this, android.R.drawable.ic_media_play);
        icPauseCompat = IconCompat.createWithResource(this, android.R.drawable.ic_media_pause);
        icStopCompat = IconCompat.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel);

        icPause = Icon.createWithResource(this, android.R.drawable.ic_media_pause);
        icPlay = Icon.createWithResource(this, android.R.drawable.ic_media_play);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action", -1);


        if (sharedPreferences.getInt("currentDur", -1) > 0 && mediaPlayer == null && currentSongIndex != -1) {
            currentSong = songList.get(currentSongIndex);
            mediaPlayer = MediaPlayer.create(this, Uri.parse(currentSong.path));
            mediaPlayer.seekTo(sharedPreferences.getInt("currentDur", -1));
            updateNavigationSeekBarListener();


            editor.putInt("currentDur", -1);
            editor.commit();

            initNotification();
            startSong(getApplicationContext());

            mediaPlayer.setOnCompletionListener(mp -> {
                if (repeatMode == MODE_REPEAT_ONE) {
                    mediaPlayer.start();
                } else {
                    nextSongAutomatically(getApplicationContext());
                }
            });

        } else if (mediaPlayer == null && sharedPreferences.getInt("currentDur", -1) < 0 && currentSongIndex != -1) {
            currentSong = songList.get(currentSongIndex);
            mediaPlayer = MediaPlayer.create(this, Uri.parse(currentSong.path));


            initNotification();
            startSong(getApplicationContext());

            mediaPlayer.setOnCompletionListener(mp -> {
                if (repeatMode == MODE_REPEAT_ONE) {
                    mediaPlayer.start();
                } else {
                    nextSongAutomatically(getApplicationContext());
                }
            });

            updateNavigationSeekBarListener();
        }

        if (currentSongIndex != -1) {
            handleEvent(action);

            startForeground(PLAYING_SONG_ID, songNotification);
        }
        return START_NOT_STICKY;
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
                .setState(!playing? PlaybackStateCompat.STATE_PLAYING:PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 1, SystemClock.elapsedRealtime())
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
        currentSong.isCurrentItem = false;
        editor.putInt("currentDur", -1);
        editor.commit();
        if (currentSongIndex == 0) {
            int newSongIndex = songList.size() - 2;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong(context);
        } else if (currentSongIndex > 0) {
            int newSongIndex = currentSongIndex - 1;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong(context);
        }
    }

    public static void playPause(Context context) {
        if (mediaPlayer.isPlaying()) {
            pauseSong(context);
        } else {
            startSong(context);
        }
        newSongSelectedListener.setBackGroundForNewSong(-1,currentSongIndex);
    }

    public static void pauseSong(Context context) {
        if (mediaPlayer.isPlaying()) {
            editor.putInt("currentDur", mediaPlayer.getCurrentPosition());
            editor.commit();
            songNotification.actions[1] = actionPlay;
        }
        changeNotificationSeekBarState(context);
        mediaPlayer.pause();
    }


    public static void startSong(Context context) {
        if (!mediaPlayer.isPlaying()) {
            songNotification.actions[1] = actionPause;
        }
        changeNotificationSeekBarState(context);
        mediaPlayer.start();
        updateNavigationSeekBarListener();
    }

    private static void nextSong(Context context) {
        currentSong.isCurrentItem = false;
        editor.putInt("currentDur", -1);
        editor.commit();
        if (currentSongIndex == songList.size()-2) {
            int newSongIndex = 0;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong(context);
        } else if (currentSongIndex < songList.size() - 2) {
            int newSongIndex = currentSongIndex + 1;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong(context);
        }
    }

    private void nextSongAutomatically(Context context) {
        currentSong.isCurrentItem = false;
        editor.putInt("currentDur", -1);
        editor.commit();
        if (currentSongIndex < songList.size() - 2) {
            int newSongIndex = currentSongIndex + 1;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong(context);
        } else if (currentSongIndex == songList.size() - 2 ) {
            if (repeatMode==MODE_REPEAT_PLAYLIST) {
                int newSongIndex = 0;
                newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
                currentSongIndex = newSongIndex;
                renewSong(context);
            } else if (repeatMode==MODE_NO_REPEAT) {
                mediaPlayer.release();
                mediaPlayer = null;
                updateNavigationSeekBarListener();
            }
        }
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
        stopSelf();
    }

    private static void renewSong(Context context) {
        mediaPlayer.release();
        mediaPlayer = null;
        Intent intent = new Intent(context, PlaySongService.class);
        context.startService(intent);
    }

    private static void changeNotificationSeekBarState(Context baseContext) {
        playbackState = getPlayBackState(mediaPlayer.isPlaying());
        mediaSession.setPlaybackState(playbackState);
        Intent intent = new Intent(baseContext, PlaySongService.class);
        baseContext.startService(intent);
    }

    public static void updateNavigationSeekBarListener() {
        MainActivity.songProgress.setMax(mediaPlayer.getDuration());
        updateSeekBar.postDelayed(updateSeekBarRunnable, 200);
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
