package com.example.music.listener;

import androidx.recyclerview.widget.RecyclerView;

public interface OnRecyclerItemSelectedListener {
    void setBackGroundForNewSong(int oldSongIndex, int newSongIndex);

    void onItemClick(int oldPos, int clickedPos);

    RecyclerView getSongRecycler();
}
