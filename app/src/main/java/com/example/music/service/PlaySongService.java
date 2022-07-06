package com.example.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music.GlobalMediaPlayer;
import com.example.music.MainActivity;
import com.example.music.MyApplication;
import com.example.music.R;
import com.example.music.listener.OnNotificationSeekBarChange;
import com.example.music.listener.OnPlaySongServiceInteractionListener;
import com.example.music.models.Song;
import com.example.music.receiver.MusicActionReceiver;
import com.example.music.utils.GlobalListener;

public class PlaySongService extends Service {
    public static final int PLAYING_SONG_ID = 221;
    public static final int ACTION_PLAY_PAUSE = 11;
    public static final int ACTION_NEXT = 12;
    public static final int ACTION_PREV = 10;
    public static final int ACTION_STOP = 13;

    GlobalMediaPlayer mediaPlayer;
    public static PlaybackStateCompat playbackState;
    public static MediaSessionCompat mediaSession;
    public static Notification.Action actionPlay, actionPause;
    public static Notification songNotification;

    NotificationCompat.Action actionPrevCompat, actionNextCompat, actionPlayPauseCompat, actionStopCompat;
    IconCompat icPrevCompat, icNextCompat, icPlayCompat, icPauseCompat, icStopCompat;
    Icon icPlay, icPause;
    PendingIntent pendingPrev, pendingNext, pendingPlayPause, pendingStop;

    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    static OnPlaySongServiceInteractionListener playSongServiceInteractionListener;


    LocalBroadcastManager broadcastManager;
    Intent requestEndApp;

    @Override
    public void onCreate() {
        super.onCreate();
        initActions();
        mediaPlayer = GlobalMediaPlayer.getInstance();
        sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        requestEndApp = new Intent();
        requestEndApp.setAction(MainActivity.ACTION_STOP_ACTIVITY);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        initListener();
    }

    private void initListener() {

        playSongServiceInteractionListener = new OnPlaySongServiceInteractionListener() {
            @Override
            public void reloadNotificationMediaState() {
                resetNotificationMediaState(getApplicationContext());
            }

            @Override
            public void playOrPause(Context context) {
                playPause(context);
            }

            @Override
            public void playStateNotification() {
                playState();
            }

            @Override
            public void pauseStateNotification() {
                pauseState();
            }
        };
        GlobalListener.PlaySongService.listener = playSongServiceInteractionListener;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action", -1);

        handleEvent(action);
        if (mediaPlayer.getCurrentSong() != null) {
            initNotification(mediaPlayer.getCurrentSong());

            startForeground(PLAYING_SONG_ID, songNotification);
        }

        GlobalListener.MainActivity.listener.validatePlayPauseButton();

        return START_NOT_STICKY;
    }

    private void initNotification(Song currentSong) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putLong(MediaMetadata.METADATA_KEY_DURATION, currentSong.duration)
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentSong.songName).build();
        playbackState = getPlayBackState(mediaPlayer.getPlayerState()==GlobalMediaPlayer.PLAYING_STATE);

        OnNotificationSeekBarChange notificationSeekBarChange = new OnNotificationSeekBarChange();

        mediaSession = new MediaSessionCompat(this, "current_session");
        mediaSession.setActive(true);
        mediaSession.setMetadata(metadata);
        mediaSession.setPlaybackState(getPlayBackState(mediaPlayer.getPlayerState()==GlobalMediaPlayer.PLAYING_STATE));
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

        if (mediaPlayer.getPlayerState() == GlobalMediaPlayer.PLAYING_STATE) {
            songNotification.actions[1] = actionPause;
        } else {
            songNotification.actions[1] = actionPlay;
        }
    }

    private PlaybackStateCompat getPlayBackState(boolean playing) {
        return new PlaybackStateCompat.Builder()
                .setState(playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, mediaPlayer.getCurrentSongPlayingPosition(), 1, SystemClock.elapsedRealtime())
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

    public void playPause(Context context) {
        if (mediaPlayer.getPlayerState() == GlobalMediaPlayer.PLAYING_STATE) {
            pauseSong(context);
        } else {
            startSong(context);
        }
    }

    public void pauseSong(Context context) {
        mediaPlayer.pauseSong(context);
    }

    private void playState() {
        if (songNotification != null) {
            songNotification.actions[1] = actionPause;
        }
    }
    private void pauseState() {
        if (songNotification != null) {
            songNotification.actions[1] = actionPlay;
        }
    }
    public void startSong(Context context) {
        if (mediaPlayer.getCurrentSong() != null) {
            mediaPlayer.resumeSong(context);
        }
    }

    public void prevSong(Context context) {
        mediaPlayer.prevSong(context);
    }

    public void nextSong(Context context) {
        mediaPlayer.nextSong(context);
    }

    private void stopSongService() {
        mediaPlayer.stopSong();
        stopSelf();
        broadcastManager.sendBroadcast(requestEndApp);
    }


    public void resetNotificationMediaState(Context baseContext) {
        if (mediaPlayer.getPlayerState() == GlobalMediaPlayer.PLAYING_STATE) {
            playState();
        } else {
            pauseState();
        }
        playbackState = getPlayBackState(mediaPlayer.getPlayerState()==GlobalMediaPlayer.PLAYING_STATE);
        mediaSession.setPlaybackState(playbackState);
        Intent intent = new Intent(baseContext, PlaySongService.class);
        baseContext.startService(intent);
    }


    @Override
    public void onDestroy() {
        mediaPlayer.stopSong();
        super.onDestroy();
    }


}
