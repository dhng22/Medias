package com.example.music.listener;

public interface OnRecyclerItemSelectedListener {
    void setBackGroundForNewSong(int oldSongIndex, int newSongIndex);

    void onItemClick(int oldPos, int clickedPos);
}
