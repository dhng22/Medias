package com.example.music.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.music.fragment.ImageFragment;
import com.example.music.fragment.MusicFragment;
import com.example.music.fragment.LocalVideoFragment;
import com.example.music.fragment.VideoFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    Fragment musicTabFrag = new MusicFragment();
    LocalVideoFragment localVideoFragTab = new LocalVideoFragment();
    VideoFragment videoFragTab = new VideoFragment(localVideoFragTab);
    ImageFragment imageFragment = new ImageFragment();


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            default:
                return videoFragTab;
            case 1:
                return musicTabFrag;
            case 2:
                return imageFragment;
        }
    }

    public void setMusicTabFrag(Fragment musicTabFrag) {
        this.musicTabFrag = musicTabFrag;
    }

    public LocalVideoFragment getVideoFragTab() {
        return localVideoFragTab;
    }
    @Override
    public int getItemCount() {

        return 3;
    }
}
