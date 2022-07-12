package com.example.music.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.music.GlobalMediaPlayer;
import com.example.music.fragment.FragmentParticularVideo;
import com.example.music.models.Video;

import java.util.ArrayList;

public class VideoViewPagerAdapter extends FragmentStateAdapter {
    ArrayList<Video> videos;
    GlobalMediaPlayer mediaPlayer;

    public VideoViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        videos = mediaPlayer.getBaseVideoList();
    }
    public VideoViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        videos = mediaPlayer.getBaseVideoList();
    }
    public VideoViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        videos = mediaPlayer.getBaseVideoList();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Video video = videos.get(position);

        return FragmentParticularVideo.getInstance(video);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }
}
