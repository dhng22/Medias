package com.example.music.listener;

public interface OnCurrentSongActivityInteractionListener {
    void setSongSeekBarMax(int max);

    void setSongSeekBarProgress(int progress);

    void setTxtSongDuration(int duration);

    void renewCurrentSong();

    void validateButtons();
}
