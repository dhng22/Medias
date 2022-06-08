package com.example.music;

import android.support.v4.media.session.MediaSessionCompat;

public class OnNotificationSeekBarChange extends MediaSessionCompat.Callback {
    @Override
    public void onSeekTo(long pos) {
        super.onSeekTo(pos);
        PlaySongService.mediaPlayer.seekTo((int) pos);
    }
}
