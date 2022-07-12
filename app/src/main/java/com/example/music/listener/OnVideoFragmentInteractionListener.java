package com.example.music.listener;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.music.fragment.VideoPageFragment;

public interface OnVideoFragmentInteractionListener {
    void addFragment(Fragment fragment);

    void addFragmentWithSharedElement(View itemView, VideoPageFragment pageFragment);

    FragmentManager getFragmentManager();
}
