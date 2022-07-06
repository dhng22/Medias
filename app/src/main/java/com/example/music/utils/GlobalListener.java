package com.example.music.utils;
import com.example.music.listener.OnAdapterInteractionListener;
import com.example.music.listener.OnCurrentSongActivityInteractionListener;
import com.example.music.listener.OnFavSongFragmentInteractionListener;
import com.example.music.listener.OnMainActivityInteractionListener;
import com.example.music.listener.OnMusicFragmentInteractionListener;
import com.example.music.listener.OnPlaySongServiceInteractionListener;

public class GlobalListener {
    public static class MainActivity {
        public static OnMainActivityInteractionListener listener;
    }

    public static class SongListAdapter {
        public static OnAdapterInteractionListener listener;
    }

    public static class CurrentSongActivity {
        public static OnCurrentSongActivityInteractionListener listener;
    }

    public static class PlaySongService {
        public static OnPlaySongServiceInteractionListener listener;
    }

    public static class MusicFragment {
        public static OnMusicFragmentInteractionListener listener;
    }
    public static class FavSongFragment {
        public static OnFavSongFragmentInteractionListener listener;
    }
}
