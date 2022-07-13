package com.example.music.listener;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.music.fragment.ImagePageFragment;
import com.example.music.fragment.VideoPageFragment;

public interface OnImageFragmentInteractionListener {
    void addFragment(Fragment pageFragment);

    void addFragmentWithSharedElement(View itemView, ImagePageFragment pageFragment);

    FragmentManager getFragmentManager();
}
