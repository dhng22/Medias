package com.example.music.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.music.fragment.MusicFragment;
import com.example.music.fragment.VideoFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            default:
                return new VideoFragment();
            case 1:
                return new MusicFragment();
            case 2:
                return new VideoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
