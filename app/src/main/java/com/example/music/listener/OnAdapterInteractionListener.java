package com.example.music.listener;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.adapter.SongListAdapter;
import com.example.music.models.Song;

public interface OnAdapterInteractionListener {
    void onNewSongPlaying(Song oldSong, Song newSong);
    void setBackGroundForOldSong(Song oldSong);

    void setBackGroundForNewSong(Song newSong);

    void onItemClick(int clickedPos);

    RecyclerView getSongRecycler();

    void notifySongAt(int index);
    void notifySongAdded(int index);



    SongListAdapter getAdapter();

    void notifySongRemoved(int index);

    Fragment getParentFragment();

    void notifyDataSet();
}
