package com.example.music;

import static com.example.music.GlobalMediaPlayer.MODE_REPEAT_PLAYLIST;
import static com.example.music.GlobalMediaPlayer.MODE_SHUFFLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.music.adapter.ViewPagerAdapter;
import com.example.music.database.MusicDb;
import com.example.music.fragment.FavSongFragment;
import com.example.music.fragment.LocalSongFragment;
import com.example.music.listener.OnMainActivityInteractionListener;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class MainActivity extends AppCompatActivity {
    public static final String ACTION_STOP_ACTIVITY = "stop";
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
    BroadcastReceiver stopActivityReceiver;
    LocalBroadcastManager broadcastManager;
    GlobalMediaPlayer mediaPlayer;
    OnMainActivityInteractionListener listener;
    MusicDb favSongDb, recentSongDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(-1, -1);
        setContentView(R.layout.activity_main);
        init();
        if (sharedPreferences.getInt("repeatMode", GlobalMediaPlayer.MODE_REPEAT_PLAYLIST) == GlobalMediaPlayer.MODE_SHUFFLE) {
            new Handler().postDelayed(() -> mediaPlayer.shuffleModeOn(), 300) ;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void init() {
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        mediaPlayer = GlobalMediaPlayer.getInstance();


        sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        initDb();
        mapping();

        initFavList();
        initPlayList();
        defaultNavY = bottomNavWrapper.getY();

        LocalSongFragment.bottomNavWrapper = bottomNavWrapper;
        LocalSongFragment.musicNavControl = musicNavControl;
        LocalSongFragment.bottomNav = bottomNavigation;

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerTabs.setAdapter(viewPagerAdapter);
        viewPagerTabs.setUserInputEnabled(false);
        viewPagerTabs.setCurrentItem(1, false);
        viewPagerTabs.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        bottomNavigation.setSelectedItemId(R.id.btnMusicTab);

        initListener();
    }

    private void initFavList() {
        mediaPlayer.initFavSong(this);
    }
    private void initPlayList() {
        mediaPlayer.initPlayList(this);
    }

    private void initDb() {
        favSongDb = new MusicDb(this, "favSong.db", null, 1);
        favSongDb.queryData("CREATE TABLE IF NOT EXISTS favList(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(400))");

        recentSongDb = new MusicDb(this, "recentSong.db", null, 1);
        recentSongDb.queryData("CREATE TABLE IF NOT EXISTS recentSong(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(400))");
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


    private void initListener() {
        listener = new OnMainActivityInteractionListener() {
            @Override
            public ProgressBar getNavigationProgressBar() {
                return songProgress;
            }

            @Override
            public FragmentManager getFragmentManager() {
                return getSupportFragmentManager();
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

            @Override
            public void openCurrentSongActivity() {
                Intent intent = new Intent(MainActivity.this, CurrentSongActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_activity_slide_up, R.anim.anim_nothing);
            }

            @Override
            public void showSongBottomSheetOption(BottomSheetDialogFragment optionBottomSheetFrag, FragmentManager fragmentManager) {
                optionBottomSheetFrag.setStyle(DialogFragment.STYLE_NORMAL, R.style.TransparentDialog);
                if (fragmentManager != null) {
                    optionBottomSheetFrag.show(fragmentManager, optionBottomSheetFrag.getTag());

                } else {
                    optionBottomSheetFrag.show(getSupportFragmentManager(), optionBottomSheetFrag.getTag());
                }
            }

            @Override
            public void toggleRepeatMode() {
                toggleRepMode();
            }

            @Override
            public void shuffleModeSongOn() {
                turnOnShuffleMode();
            }

            @Override
            public void onFavButtonClicked(Song song) {
                onFavClicked(song);
            }

            @Override
            public void doBackPress() {
                onBackPressed();
            }

            @Override
            public void hideNavigationBar() {
                bottomNavWrapper.setVisibility(View.GONE);
            }

            @Override
            public void showNavigationBar() {
                bottomNavWrapper.setVisibility(View.VISIBLE);
            }
        };

        GlobalListener.MainActivity.listener = listener;

        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                default:
                    musicNavControl.setVisibility(View.VISIBLE);
                    if (viewPagerTabs.getCurrentItem() == 1 && mediaPlayer.getCurrentSong() != null) {
                        listener.openCurrentSongActivity();
                    } else {
                        viewPagerTabs.setCurrentItem(1, true);
                    }
                    break;
                case R.id.btnVideoTab:
                    if (mediaPlayer.isPlaying()) {
                        turnOffController();
                    } else {
                        musicNavControl.setVisibility(View.GONE);
                    }
                    viewPagerTabs.setCurrentItem(0, true);
                    break;
                case R.id.btnFavTab:
                    if (mediaPlayer.isPlaying()) {
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
            GlobalListener.PlaySongService.listener.playOrPause(this);
        });

        btnFavController.setOnClickListener(v -> {
            if (mediaPlayer.getCurrentSong() != null) {
                Song song = mediaPlayer.getCurrentSong();
                SongUtils.onSongFavClicked(song,-1);
            }
        });
        btnNextSong.setOnClickListener(v -> {
            if (mediaPlayer.getPlayerState() != GlobalMediaPlayer.NULL_STATE) {
                mediaPlayer.nextSong(this);
                GlobalListener.SongListAdapter.listener.getSongRecycler().smoothScrollToPosition(mediaPlayer.getVisualSongIndex());
            }
            invalidatePlayPause();
        });
        btnPrevSong.setOnClickListener(v -> {
            if (mediaPlayer.getPlayerState() != GlobalMediaPlayer.NULL_STATE) {
                mediaPlayer.prevSong(this);
                GlobalListener.SongListAdapter.listener.getSongRecycler().smoothScrollToPosition(mediaPlayer.getVisualSongIndex());
            }
            invalidatePlayPause();
        });

        btnReplayMode.setOnClickListener(v -> {
            SongUtils.onRepeatModeChanged();
        });
        stopActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finishAndRemoveTask();
            }
        };
        broadcastManager.registerReceiver(stopActivityReceiver, new IntentFilter(ACTION_STOP_ACTIVITY));
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

    public void onFavClicked(Song song) {
        if (song.isFavorite) {
            unFavThisSong(song);
        } else {
            favThisSong(song);
        }
    }
    public void favThisSong(Song song) {
        favSongDb.addFavoriteSongToTable(song, FavSongFragment.TABLE_NAME, this);
    }
    public void unFavThisSong(Song song) {
        favSongDb.removeFavSongFromTable(song, FavSongFragment.TABLE_NAME, this);
    }
    public void toggleRepMode() {
        if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == MODE_REPEAT_PLAYLIST) {
            editor.putInt("repeatMode", GlobalMediaPlayer.MODE_REPEAT_ONE);
            editor.commit();
            Toast.makeText(this, "Repeat one song mode turned on", Toast.LENGTH_SHORT).show();
        } else if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == GlobalMediaPlayer.MODE_REPEAT_ONE) {
            turnOnShuffleMode();
        } else if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == GlobalMediaPlayer.MODE_SHUFFLE) {
            editor.putInt("repeatMode", MODE_REPEAT_PLAYLIST);
            editor.commit();
            mediaPlayer.shuffleModeOff();
            Toast.makeText(this, "Repeat all song mode turned on", Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOnShuffleMode() {
        if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) != MODE_SHUFFLE) {
            mediaPlayer.shuffleModeOn();
            editor.putInt("repeatMode", GlobalMediaPlayer.MODE_SHUFFLE);
            editor.commit();
            Toast.makeText(this, "Shuffle song mode turned on", Toast.LENGTH_SHORT).show();
        }
    }

    public void invalidatePlayPause() {
        if (mediaPlayer.getPlayerState() != GlobalMediaPlayer.NULL_STATE) {
            if (mediaPlayer.isPlaying()) {
                btnPlayPauseSong.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayPauseSong.setImageResource(R.drawable.ic_play);
            }
        } else {
            btnPlayPauseSong.setImageResource(R.drawable.ic_play);
        }
    }

    public void invalidateFav() {
        if (mediaPlayer.getCurrentSong() != null) {
            Song song = mediaPlayer.getCurrentSong();
            if (song.isFavorite) {
                btnFavController.setColorFilter(getColor(R.color.red));
            } else {
                btnFavController.setColorFilter(getColor(R.color.dark_grey));
            }
        }
    }

    public void invalidateRepeatMode() {
        if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == MODE_REPEAT_PLAYLIST) {
            btnReplayMode.setImageResource(R.drawable.ic_rep_all);
        } else if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == GlobalMediaPlayer.MODE_REPEAT_ONE) {
            btnReplayMode.setImageResource(R.drawable.ic_rep_one);
        } else if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == GlobalMediaPlayer.MODE_SHUFFLE) {
            btnReplayMode.setImageResource(R.drawable.ic_shuffle);
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

    @Override
    public void onBackPressed() {
        if (viewPagerTabs.getCurrentItem() == 0) {
            if (GlobalListener.VideoFragment.listener.getFragmentManager().getBackStackEntryCount() > 0) {
                GlobalListener.VideoFragment.listener.getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        } else if (viewPagerTabs.getCurrentItem() == 1) {
            if (GlobalListener.MusicFragment.listener.getFragmentManager().getBackStackEntryCount() > 0) {
                GlobalListener.MusicFragment.listener.getFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        } else if (viewPagerTabs.getCurrentItem() == 2) {
            if (GlobalListener.ImageFragment.listener.getFragmentManager().getBackStackEntryCount() > 0) {
                GlobalListener.ImageFragment.listener.getFragmentManager().popBackStack();
            } else super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}