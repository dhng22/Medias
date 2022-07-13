package com.example.music.listener;

import androidx.fragment.app.FragmentManager;

public interface OnMusicFragmentInteractionListener {
    void toFavSongFrag();
    void toPlaylistFrag();
    void toLocalSongFrag();
    void toRecentSongFrag();

    FragmentManager getFragmentManager();
}
