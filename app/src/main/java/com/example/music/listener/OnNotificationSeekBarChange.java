package com.example.music.listener;

import android.support.v4.media.session.MediaSessionCompat;

import com.example.music.GlobalMediaPlayer;
import com.example.music.utils.GlobalListener;

public class OnNotificationSeekBarChange extends MediaSessionCompat.Callback {
    GlobalMediaPlayer mediaPlayer = GlobalMediaPlayer.getInstance();
    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
        mediaPlayer.seekTo((int) pos);
        GlobalListener.MainActivity.listener.setNavigationProgressBarProgress((int) pos);
        GlobalListener.PlaySongService.listener.reloadNotificationMediaState();
    }
}
