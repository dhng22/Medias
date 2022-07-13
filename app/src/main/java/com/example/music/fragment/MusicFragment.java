package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.R;
import com.example.music.listener.OnMusicFragmentInteractionListener;
import com.example.music.utils.GlobalListener;


public class MusicFragment extends Fragment {
    FragmentContainerView mainFragment;
    FragmentTransaction fragmentManager;
    LocalSongFragment localSongFragment;
    PlaylistFragment playlistFragment;
    FavSongFragment favSongFragment;
    RecentSongFragment recentSongFrag;
    OnMusicFragmentInteractionListener musicFragmentInteractionListener;

    public MusicFragment() {
        // Required empty public constructor
        localSongFragment = new LocalSongFragment();
        favSongFragment = new FavSongFragment();
        playlistFragment = new PlaylistFragment();
        recentSongFrag = new RecentSongFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getChildFragmentManager().beginTransaction();
        fragmentManager.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
        fragmentManager.replace(R.id.mainFragment, localSongFragment).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        init(view);

        initListener();
        return view;
    }

    private void initListener() {
        musicFragmentInteractionListener = new OnMusicFragmentInteractionListener() {
            @Override
            public void toFavSongFrag() {
                fragmentManager = getChildFragmentManager().beginTransaction();
                fragmentManager.setCustomAnimations(R.anim.anim_slide_right, R.anim.anim_fade_out, R.anim.anim_fade_in, R.anim.anim_slide_left);
                fragmentManager.replace(R.id.mainFragment, favSongFragment).addToBackStack(null).commit();
            }

            @Override
            public void toPlaylistFrag() {
                fragmentManager = getChildFragmentManager().beginTransaction();
                fragmentManager.setCustomAnimations(R.anim.anim_slide_right, R.anim.anim_fade_out, R.anim.anim_fade_in, R.anim.anim_slide_left);

                fragmentManager.replace(R.id.mainFragment, playlistFragment).addToBackStack(null).commit();
            }

            @Override
            public void toLocalSongFrag() {
                fragmentManager = getChildFragmentManager().beginTransaction();
                fragmentManager.setCustomAnimations(R.anim.anim_slide_right, R.anim.anim_fade_out, R.anim.anim_fade_in, R.anim.anim_slide_left);

                fragmentManager.replace(R.id.mainFragment, localSongFragment).commit();
            }

            @Override
            public void toRecentSongFrag() {
                fragmentManager = getChildFragmentManager().beginTransaction();
                fragmentManager.setCustomAnimations(R.anim.anim_slide_right, R.anim.anim_fade_out, R.anim.anim_fade_in, R.anim.anim_slide_left);
                fragmentManager.replace(R.id.mainFragment, recentSongFrag).addToBackStack(null).commit();

            }

            @Override
            public FragmentManager getFragmentManager() {
                return getChildFragmentManager();
            }
        };
        GlobalListener.MusicFragment.listener = musicFragmentInteractionListener;
    }

    private void init(View view) {
        mainFragment = view.findViewById(R.id.mainFragment);
    }
}