package com.example.music.listener;

import com.example.music.models.Song;

public interface OnPlaySongServiceInteractionListener {
    Song getCurrentSong();

    void reloadNotificationMediaState();

    void shuffleModeOn();

    void shuffleModeOff();

    int getSongIndexAt(int itemOnRecycler);

}
