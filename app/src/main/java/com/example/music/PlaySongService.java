package com.example.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;

public class PlaySongService extends Service {
    public static final int PLAYING_SONG_ID = 221;
    public static final int ACTION_PLAY_PAUSE = 11;
    public static final int ACTION_NEXT = 12;
    public static final int ACTION_PREV = 10;
    public static final int ACTION_STOP = 13;
    public static final int MODE_REPEAT_PLAYLIST = 2;
    public static final int MODE_REPEAT_ONE = 1;
    public static final int MODE_NO_REPEAT = 0;

    public static int currentSongIndex;
    Song currentSong;
    public static MediaPlayer mediaPlayer;
    public static ArrayList<Song> songList;
    public static OnNewSongSelectedListener newSongSelectedListener;
    Notification notification;
    NotificationCompat.Action actionPrevCompat, actionNextCompat, actionPlayPauseCompat, actionStopCompat;
    Notification.Action  actionPlayPause;
    PendingIntent pendingPrev, pendingNext, pendingPlayPause, pendingStop;
    SharedPreferences sharedPreferences;
    int repeatMode;

    IconCompat icPrevCompat, icNextCompat, icPlayCompat, icPauseCompat, icStopCompat;
    Icon icPlay, icPause;

    @Override
    public void onCreate() {
        super.onCreate();
        initActions();
        sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);
        repeatMode = sharedPreferences.getInt("repeatMode", MODE_NO_REPEAT);
    }

    private void initActions() {
        initIntent();
        initIcons();
        actionPrevCompat = new NotificationCompat.Action.Builder(icPrevCompat, "Previous", pendingPrev).build();
        actionNextCompat = new NotificationCompat.Action.Builder(icNextCompat, "Next", pendingNext).build();
        actionPlayPauseCompat = new NotificationCompat.Action.Builder(icPauseCompat, "Pause", pendingPlayPause).build();
        actionStopCompat = new NotificationCompat.Action.Builder(icStopCompat, "Stop", pendingStop).build();

        actionPlayPause = new Notification.Action.Builder(icPause, "Pause", pendingPlayPause).build();
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
        if (mediaPlayer == null) {
            currentSong = songList.get(currentSongIndex);
            mediaPlayer = MediaPlayer.create(this, Uri.parse(currentSong.path));

            MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, currentSong.duration)
                    .putString(MediaMetadata.METADATA_KEY_TITLE, currentSong.songName).build();
            PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO).build();

            MediaSessionCompat mediaSession = new MediaSessionCompat(this, "current_session");
            mediaSession.setActive(true);
            mediaSession.setMetadata(metadata);
            mediaSession.setPlaybackState(playbackState);


            androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle();
            mediaStyle.setMediaSession(mediaSession.getSessionToken());

            notification = new NotificationCompat.Builder(this, MyApplication.PLAYING_SONG_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(currentSong.songImage)
                    .setContentTitle(currentSong.songName)
                    .setContentText(currentSong.singer)
                    .addAction(actionPrevCompat)
                    .addAction(actionPlayPauseCompat)
                    .addAction(actionNextCompat)
                    .addAction(actionStopCompat)
                    .setStyle(mediaStyle).build();

            startSong();

            mediaPlayer.setOnCompletionListener(mp -> {
                if (repeatMode == MODE_REPEAT_ONE) {
                    mediaPlayer.start();
                } else {
                    nextSongAutomatically();
                }
            });
        }

        handleEvent(action);

        startForeground(PLAYING_SONG_ID,notification);
        return START_NOT_STICKY;
    }

    private void handleEvent(int action) {
        switch (action) {
            case ACTION_PREV:
                prevSong();
                break;
            case ACTION_PLAY_PAUSE:
                playPause();
                break;
            case ACTION_NEXT:
                nextSong();
                break;
            case ACTION_STOP:
                stopSongService();
                break;
        }
    }

    private void prevSong() {
        currentSong.isCurrentItem = false;
        if (currentSongIndex == 0) {
            int newSongIndex = songList.size() - 2;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong();
        } else if (currentSongIndex > 0) {
            int newSongIndex = currentSongIndex - 1;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong();
        }
    }

    private void playPause() {
        if (mediaPlayer.isPlaying()) {
            pauseSong();
        } else {
            startSong();
        }
    }

    private void pauseSong() {
        if (mediaPlayer.isPlaying()) {
            actionPlayPause = new Notification.Action.Builder(icPlay, "Play", pendingPlayPause).build();
            notification.actions[1] = actionPlayPause;
        }
        mediaPlayer.pause();
    }


    private void startSong() {
        if (!mediaPlayer.isPlaying()) {
            actionPlayPause= new Notification.Action.Builder(icPause, "Pause", pendingPlayPause).build();
            notification.actions[1] = actionPlayPause;
        }
        mediaPlayer.start();
    }

    private void nextSong() {
        currentSong.isCurrentItem = false;
        if (currentSongIndex == songList.size()-2) {
            int newSongIndex = 0;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong();
        } else if (currentSongIndex < songList.size() - 2) {
            int newSongIndex = currentSongIndex + 1;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong();
        }
    }

    private void nextSongAutomatically() {
        currentSong.isCurrentItem = false;
        if (currentSongIndex < songList.size() - 2) {
            int newSongIndex = currentSongIndex + 1;
            newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
            currentSongIndex = newSongIndex;
            renewSong();
        } else if (currentSongIndex == songList.size() - 2 ) {
            if (repeatMode==MODE_REPEAT_PLAYLIST) {
                int newSongIndex = 0;
                newSongSelectedListener.setBackGroundForNewSong(currentSongIndex,newSongIndex);
                currentSongIndex = newSongIndex;
                renewSong();
            } else if (repeatMode==MODE_NO_REPEAT) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
    }

    private void stopSongService() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopSelf();
    }

    private void renewSong() {
        mediaPlayer.release();
        mediaPlayer = null;
        Intent intent = new Intent(this, PlaySongService.class);
        getApplicationContext().startService(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
