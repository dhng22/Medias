package com.example.music.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.music.GlobalMediaPlayer;
import com.example.music.fragment.FragmentParticularImage;
import com.example.music.models.Image;
import java.util.ArrayList;

public class ImageViewPagerAdapter extends FragmentStateAdapter {
    ArrayList<Image> images;
    GlobalMediaPlayer mediaPlayer;

    public ImageViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        images = mediaPlayer.getBaseImageList();
    }
    public ImageViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        images = mediaPlayer.getBaseImageList();
    }
    public ImageViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        images = mediaPlayer.getBaseImageList();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Image image = images.get(position);
        return FragmentParticularImage.getInstance(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
