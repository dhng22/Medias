package com.example.music.fragment;

import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.R;
import com.example.music.listener.OnVideoFragmentInteractionListener;
import com.example.music.utils.GlobalListener;

public class VideoFragment extends Fragment {
    FragmentTransaction transaction;
    LocalVideoFragment localVideoFrag;
    FragmentContainerView containerView;
    OnVideoFragmentInteractionListener videoFragmentInteractionListener;
    public VideoFragment() {
        // Required empty public constructor
        localVideoFrag = new LocalVideoFragment();
    }

    public VideoFragment(LocalVideoFragment localVideoFrag) {
        this.localVideoFrag = localVideoFrag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        transaction = getChildFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
        transaction.replace(R.id.videoMainFrag, localVideoFrag).commit();

        videoFragmentInteractionListener = new OnVideoFragmentInteractionListener() {
            @Override
            public void addFragment(Fragment fragment) {
                addNewFrag(fragment);
            }

            @Override
            public void addFragmentWithSharedElement(View itemView, VideoPageFragment pageFragment) {
                addNewFragWithSharedElement(itemView,pageFragment);
            }

            @Override
            public FragmentManager getFragmentManager() {
                return getChildFragmentManager();
            }

        };
        GlobalListener.VideoFragment.listener = videoFragmentInteractionListener;
    }

    private void addNewFrag(Fragment fragment) {

        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.videoMainFrag, fragment).addToBackStack(null).commit();

    }
    private void addNewFragWithSharedElement(View itemView, Fragment fragment) {
        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.videoMainFrag, fragment).addToBackStack(null).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_video, container, false);
        init(v);
        return v;
    }

    private void init(View v) {
        containerView = v.findViewById(R.id.videoMainFrag);
    }
}