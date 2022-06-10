package com.example.music.listener;

import android.support.v4.media.session.MediaSessionCompat;

import com.example.music.service.PlaySongService;

public class OnNotificationSeekBarChange extends MediaSessionCompat.Callback {
    public static OnMainActivityInteractionListener mainActivityInteractionListener;
    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
        PlaySongService.mediaPlayer.seekTo((int) pos);
        mainActivityInteractionListener.setNavigationProgressBarProgress((int) pos);
    }
}
