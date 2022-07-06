package com.example.music.listener;

import android.content.Context;

public interface OnPlaySongServiceInteractionListener {
    void reloadNotificationMediaState();

    void playOrPause(Context context);

    void playStateNotification();

    void pauseStateNotification();
}
