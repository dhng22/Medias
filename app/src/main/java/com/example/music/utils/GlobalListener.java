package com.example.music.utils;
import com.example.music.listener.OnAdapterInteractionListener;
import com.example.music.listener.OnCurrentPlayingListInteractionListener;
import com.example.music.listener.OnCurrentSongActivityInteractionListener;
import com.example.music.listener.OnFavSongFragmentInteractionListener;
import com.example.music.listener.OnImageAdapterInteractionListener;
import com.example.music.listener.OnImageFragmentInteractionListener;
import com.example.music.listener.OnMainActivityInteractionListener;
import com.example.music.listener.OnLocalSongFragmentInteractionListener;
import com.example.music.listener.OnMusicFragmentInteractionListener;
import com.example.music.listener.OnParticularVideoInteractionListener;
import com.example.music.listener.OnPlaySongServiceInteractionListener;
import com.example.music.listener.OnPlaylistBottomSheetInteraction;
import com.example.music.listener.OnSongPlaylistInteractionListener;
import com.example.music.listener.OnVideoAdapterInteractionListener;
import com.example.music.listener.OnVideoFragmentInteractionListener;

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

    public static class LocalSongFragment {
        public static OnLocalSongFragmentInteractionListener listener;
    }
    public static class FavSongFragment {
        public static OnFavSongFragmentInteractionListener listener;
    }

    public static class PlaylistBottomSheet {
        public static OnPlaylistBottomSheetInteraction listener;
    }
    public static class SongPlaylistAdapter {
        public static OnSongPlaylistInteractionListener listener;
    }
    public static class MusicFragment {
        public static OnMusicFragmentInteractionListener listener;
    }
    public static class CurrentPlayingListBottomSheet {
        public static OnCurrentPlayingListInteractionListener listener;
    }

    public static class VideoAdapter {
        public static OnVideoAdapterInteractionListener listener;
    }

    public static class VideoFragment {
        public static OnVideoFragmentInteractionListener listener;
    }

    public static class FragmentParticularVideo {
        public static OnParticularVideoInteractionListener listener;
    }

    public static class ImageAdapter {
        public static OnImageAdapterInteractionListener listener;
    }

    public static class ImageFragment {
        public static OnImageFragmentInteractionListener listener;

    }
}
