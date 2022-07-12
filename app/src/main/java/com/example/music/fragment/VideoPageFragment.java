package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.MainActivity;
import com.example.music.R;
import com.example.music.adapter.VideoViewPagerAdapter;


public class VideoPageFragment extends Fragment {
    ViewPager2 videoPager;
    VideoViewPagerAdapter adapter;
    int pos;
    public VideoPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_page, container, false);
        adapter = new VideoViewPagerAdapter(this);
        init(view);
        return view;
    }

    private void init(View view) {
        videoPager = view.findViewById(R.id.videoPager);
        videoPager.setAdapter(adapter);
        videoPager.setCurrentItem(pos,false);
    }

    public void setPage(int pos) {
        this.pos = pos;
    }
}