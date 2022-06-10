package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.music.adapter.SongListAdapter;
import com.example.music.adapter.ViewPagerAdapter;
import com.example.music.fragment.MusicFragment;
import com.example.music.listener.OnMainActivityInteractionListener;
import com.example.music.listener.OnNotificationSeekBarChange;
import com.example.music.listener.OnPlaySongServiceInteractionListener;
import com.example.music.models.Song;
import com.example.music.service.PlaySongService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {
    public static final String ACTION_STOP_ACTIVITY = "stop";
    ArrayList<String> musics;
    ArrayList<Song> songList;
    ConstraintLayout layoutParentScroll;
    public static ConstraintLayout musicController, musicNavControl;
    CardView bottomNavWrapper;
    ViewPager2 viewPagerTabs;
    ViewPagerAdapter viewPagerAdapter;
    BottomNavigationView bottomNavigation;
    ImageButton btnCloseController, btnReplayMode, btnFavController, btnPrevSong, btnPlayPauseSong, btnNextSong;
    float defaultNavY, screenHeight, screenWidth;
    ProgressBar songProgress;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static OnPlaySongServiceInteractionListener playSongServiceInteractionListener;
    BroadcastReceiver stopActivityReceiver;
    LocalBroadcastManager broadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(-1, -1);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void init() {
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;


        broadcastManager = LocalBroadcastManager.getInstance(this);
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
        musicController = findViewById(R.id.musicController);
        btnCloseController = findViewById(R.id.btnCloseController);
        btnReplayMode = findViewById(R.id.btnReplayMode);
        btnFavController = findViewById(R.id.btnFavController);
        btnPrevSong = findViewById(R.id.btnPrevSong);
        btnPlayPauseSong = findViewById(R.id.btnPlayPauseSong);
        btnNextSong = findViewById(R.id.btnNextSong);
    }

    private void initSongList() {
        musics = (ArrayList<String>) getIntent().getSerializableExtra("musics");

        LinkedList<Song> songsLinkedList = new LinkedList<>();

        for (String music : musics) {
            songsLinkedList.addFirst(new Song(music, false, this));
        }
        songList = new ArrayList<>(songsLinkedList);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            songList.sort(Comparator.naturalOrder());
        }
        if (musics.size() > 0) {
            songList.add(new Song(musics.get(0), true, this));
        }
    }

    private void initListener() {
        OnMainActivityInteractionListener listener = new OnMainActivityInteractionListener() {
            @Override
            public ProgressBar getNavigationProgressBar() {
                return songProgress;
            }

            @Override
            public int getNavigationProgressBarProgress() {
                return songProgress.getProgress();
            }

            @Override
            public void setNavigationProgressBarMax(int progressBarMax) {
                songProgress.setMax(progressBarMax);
            }

            @Override
            public void setNavigationProgressBarProgress(int progress) {
                songProgress.setProgress(progress);
            }

            @Override
            public void validatePlayPauseButton() {
                MainActivity.this.invalidatePlayPause();
            }

            @Override
            public void validateFavButton() {
                MainActivity.this.invalidateFav();
            }

            @Override
            public void validateRepeatButton() {
                invalidateRepeatMode();
            }
        };
        PlaySongService.mainActivityInteractionListener = listener;
        OnNotificationSeekBarChange.mainActivityInteractionListener = listener;
        SongListAdapter.mainActivityInteractionListener = listener;
        MusicFragment.mainActivityInteractionListener = listener;

        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                    musicNavControl.setVisibility(View.VISIBLE);

                    viewPagerTabs.setCurrentItem(1, true);
                    break;
                case R.id.btnVideoTab:
                    if (PlaySongService.mediaPlayer != null && PlaySongService.mediaPlayer.isPlaying()) {
                        turnOffController();
                    } else {
                        musicNavControl.setVisibility(View.GONE);
                    }
                    viewPagerTabs.setCurrentItem(0, true);
                    break;
                case R.id.btnFavTab:
                    if (PlaySongService.mediaPlayer != null && PlaySongService.mediaPlayer.isPlaying()) {
                        turnOffController();
                    } else {
                        musicNavControl.setVisibility(View.GONE);
                    }
                    viewPagerTabs.setCurrentItem(2, true);
                    break;
            }
            return true;
        });
        bottomNavigation.findViewById(R.id.btnMusicTab).setOnLongClickListener(v -> {
            toggleMusicController();
            return false;
        });

        btnCloseController.setOnClickListener(v -> toggleMusicController());
        btnPlayPauseSong.setOnClickListener(v -> {
            PlaySongService.playPause(this);
        });

        btnFavController.setOnClickListener(v -> {
            if (PlaySongService.currentSongIndex != -1) {
                Song song = songList.get(PlaySongService.currentSongIndex);
                song.isFavorite = !song.isFavorite;
                invalidateFav();
            }
        });
        btnNextSong.setOnClickListener(v -> {
            if (PlaySongService.mediaPlayer != null) {
                PlaySongService.nextSong(this);
            }
            invalidatePlayPause();
        });
        btnPrevSong.setOnClickListener(v -> {
            if (PlaySongService.mediaPlayer != null) {
                PlaySongService.prevSong(this);
            }
            invalidatePlayPause();
        });

        btnReplayMode.setOnClickListener(v -> {
            if (sharedPreferences.getInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST) == PlaySongService.MODE_REPEAT_PLAYLIST) {
                editor.putInt("repeatMode", PlaySongService.MODE_REPEAT_ONE);
                editor.commit();
            } else if (sharedPreferences.getInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST) == PlaySongService.MODE_REPEAT_ONE) {
                editor.putInt("repeatMode", PlaySongService.MODE_SHUFFLE);
                editor.commit();
            } else if (sharedPreferences.getInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST) == PlaySongService.MODE_SHUFFLE) {
                editor.putInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST);
                editor.commit();
            }
            invalidateRepeatMode();
        });
        stopActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        broadcastManager.registerReceiver(stopActivityReceiver,new IntentFilter(ACTION_STOP_ACTIVITY));
    }


    public void toggleMusicController() {
        if (musicController.getVisibility() == View.VISIBLE) {
            turnOffController();
        } else {
            turnOnController();
        }
    }

    public static void turnOnController() {
        musicController.setVisibility(View.VISIBLE);
        musicController.setAlpha(0);
        musicController.animate().alpha(1).setDuration(200).start();
    }

    public static void turnOffController() {
        musicController.animate().alpha(0).withEndAction(() -> {
            musicController.setVisibility(View.GONE);
        }).setDuration(200).start();
    }

    public void invalidatePlayPause() {
        if (PlaySongService.mediaPlayer != null) {
            if (PlaySongService.mediaPlayer.isPlaying()) {
                btnPlayPauseSong.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayPauseSong.setImageResource(R.drawable.ic_play);
            }
        } else {
            btnPlayPauseSong.setImageResource(R.drawable.ic_play);
        }
    }

    public void invalidateFav() {
        if (PlaySongService.currentSongIndex != -1) {
            Song song = songList.get(PlaySongService.currentSongIndex);
            if (song.isFavorite) {
                btnFavController.setColorFilter(Color.RED);
            } else {
                btnFavController.setColorFilter(getColor(R.color.dark_grey));
            }
        }
    }

    public void invalidateRepeatMode() {
        if (sharedPreferences.getInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST) == PlaySongService.MODE_REPEAT_PLAYLIST) {
            btnReplayMode.setImageResource(R.drawable.ic_rep_oall);
        } else if (sharedPreferences.getInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST) == PlaySongService.MODE_REPEAT_ONE) {
            btnReplayMode.setImageResource(R.drawable.ic_rep_one);
        } else if (sharedPreferences.getInt("repeatMode", PlaySongService.MODE_REPEAT_PLAYLIST) == PlaySongService.MODE_SHUFFLE) {
            btnReplayMode.setImageResource(R.drawable.ic_shuffle);
        }
    }
    private void initSeekBarProg() {
        if (PlaySongService.mediaPlayer != null) {
            PlaySongService.updateNavigationSeekBarListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        invalidateFav();
        invalidatePlayPause();
    }

    @Override
    protected void onPause() {
        super.onPause();


    }
}