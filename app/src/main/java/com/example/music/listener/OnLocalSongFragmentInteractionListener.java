package com.example.music.listener;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.example.music.models.Song;

import java.util.ArrayList;

public interface OnLocalSongFragmentInteractionListener {
    void addRecyclerMoveListener(RecyclerView recyclerView);
}
