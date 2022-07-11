package com.example.music.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.music.fragment.LocalSongFragment;
import com.example.music.fragment.MusicFragment;
import com.example.music.fragment.VideoFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    Fragment musicTabFrag = new MusicFragment();
    VideoFragment videoFragTab = new VideoFragment();


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            default:
                return videoFragTab;
            case 1:
                return musicTabFrag;
            case 2:
                return new VideoFragment();
        }
    }

    public void setMusicTabFrag(Fragment musicTabFrag) {
        this.musicTabFrag = musicTabFrag;
    }

    public VideoFragment getVideoFragTab() {
        return videoFragTab;
    }
    @Override
    public int getItemCount() {

        return 3;
    }
}
