package com.example.music.listener;

import androidx.fragment.app.FragmentManager;

public interface OnCurrentSongActivityInteractionListener {
    void setSongSeekBarMax(int max);

    void setSongSeekBarProgress(int progress);

    void setTxtSongDuration(int duration);

    void renewCurrentSong();

    void validateButtons();

    FragmentManager getFragmentManager();

    void destroy();
}
