package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {
    ArrayList<String> musics;
    ArrayList<Song> songList;
    ConstraintLayout layoutParentScroll, musicNavControl;
    CardView bottomNavWrapper;
    ViewPager2 viewPagerTabs;
    ViewPagerAdapter viewPagerAdapter;
    BottomNavigationView bottomNavigation;
    float defaultNavY,screenHeight, screenWidth;
    public static ProgressBar songProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(-1,-1);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;

        mapping();
        initSongList();

        defaultNavY = bottomNavWrapper.getY();

        MusicFragment.bottomNavWrapper = bottomNavWrapper;
        MusicFragment.musicNavControl = musicNavControl;
        MusicFragment.songList = songList;
        MusicFragment.bottomNav = bottomNavigation;
        PlaySongService.songList = songList;

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerTabs.setAdapter(viewPagerAdapter);
        viewPagerTabs.setUserInputEnabled(false);
        viewPagerTabs.setCurrentItem(1, false);

        bottomNavigation.setSelectedItemId(R.id.btnMusicTab);
        initListener();
        initSeekBarProg();
    }

    private void mapping() {
        layoutParentScroll = findViewById(R.id.layout_scroll_parent);
        bottomNavWrapper = findViewById(R.id.bottomNavWrapper);
        viewPagerTabs = findViewById(R.id.viewpager_tab);
        bottomNavigation = findViewById(R.id.bottomNav);
        songProgress = findViewById(R.id.songProgress);
        musicNavControl = findViewById(R.id.musicNavControl);
    }

    private void initSongList() {
        musics = (ArrayList<String>) getIntent().getSerializableExtra("musics");

        LinkedList<Song> songsLinkedList = new LinkedList<>();

        for (String music : musics) {
            songsLinkedList.addFirst(new Song(music,false,this));
        }
        songList = new ArrayList<>(songsLinkedList);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            songList.sort(Comparator.naturalOrder());
        }
        if (musics.size() > 0) {
            songList.add(new Song(musics.get(0),true,this));
        }
    }

    private void initListener() {

        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                    viewPagerTabs.setCurrentItem(1,true);
                    break;
                case R.id.btnVideoTab:
                    viewPagerTabs.setCurrentItem(0, true);
                    break;
                case R.id.btnFavTab:
                    viewPagerTabs.setCurrentItem(2, true);
                    break;
            }
            return true;
        });
    }


    private void initSeekBarProg() {
        if (PlaySongService.mediaPlayer != null) {
            PlaySongService.updateNavigationSeekBarListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();



    }
}