package com.example.music.fragment;

import static android.content.Context.MODE_PRIVATE;

import static com.example.music.fragment.FavSongFragment.TABLE_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.example.music.GlobalMediaPlayer;
import com.example.music.database.PlaylistDb;
import com.example.music.listener.OnMusicFragmentInteractionListener;
import com.example.music.models.Song;
import com.example.music.service.PlaySongService;
import com.example.music.R;
import com.example.music.adapter.SongListAdapter;
import com.example.music.utils.GlobalListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MusicFragment extends Fragment {
    public static final int STATE_UP = 1;
    public static final int STATE_DOWN = -1;
    public static CardView bottomNavWrapper;
    public static ConstraintLayout musicNavControl;
    public static BottomNavigationView bottomNav;
    float screenHeight, screenWidth, navHeight, navWidth, defaultNavY;
    RecyclerView songRecycler;
    GlobalMediaPlayer mediaPlayer;
    SongListAdapter songListAdapter;
    SearchView searchView;
    Button btnFav, btnPlaylist, btnRecent;
    CoordinatorLayout coordinatorLayoutParent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    OnMusicFragmentInteractionListener musicFragmentInteractionListener;
    View.OnTouchListener touchListener;
    Handler handler;

    public MusicFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireContext().getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mediaPlayer = GlobalMediaPlayer.getInstance();

        handler = new Handler();

        initOldSongData();

        GlobalListener.MainActivity.listener.validateRepeatButton();
    }

    private void initOldSongData() {
        try {
            mediaPlayer.initOldSong(requireContext());
        } catch (ClassCastException ignored) {
        }

        Intent intent = new Intent(getContext(), PlaySongService.class);
        requireContext().startService(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        screenHeight = requireContext().getResources().getDisplayMetrics().heightPixels;
        screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;


        mapping(view);

        songListAdapter = new SongListAdapter(requireContext(), R.layout.row_song, songRecycler,this);

        songRecycler.setAdapter(songListAdapter);

        initListener();
        Log.e("TAG", "onCreateView: "+mediaPlayer.getPlayingSongList());
        return view;
    }

    private void mapping(View view) {
        songRecycler = view.findViewById(R.id.songList);
        searchView = view.findViewById(R.id.searchView);
        btnFav = view.findViewById(R.id.btnFav);
        btnPlaylist = view.findViewById(R.id.btnPlaylist);
        btnRecent = view.findViewById(R.id.btnRecent);
        coordinatorLayoutParent = view.findViewById(R.id.layout_coordinate_parent);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListener() {
        bottomNavWrapper.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottomNavWrapper.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                navHeight = bottomNavWrapper.getHeight();
                navWidth = bottomNavWrapper.getWidth();
                defaultNavY = sharedPreferences.getFloat("defaultNavY", -1);
                if (defaultNavY == -1) {
                    defaultNavY = bottomNavWrapper.getY();
                    editor.putFloat("defaultNavY", defaultNavY);
                    editor.commit();
                }
            }
        });
        coordinatorLayoutParent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                coordinatorLayoutParent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                screenHeight = coordinatorLayoutParent.getHeight();
                screenWidth = coordinatorLayoutParent.getWidth();
            }
        });
        touchListener = new View.OnTouchListener() {
            boolean scrollable = true;
            boolean tempCheck = true;
            float y1 = 0;
            int state;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float bottomNavY = bottomNavWrapper.getY();
                state = bottomNavY == defaultNavY ? STATE_UP :  STATE_DOWN;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    y1 = 0;
                    tempCheck = true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    if (tempCheck) {
                        y1 = motionEvent.getY();
                        tempCheck = false;
                    }
                    if (state == STATE_DOWN && scrollable && (y1 - motionEvent.getY()) < -(screenHeight / 24)) {
                        scrollable = false;

                        bottomNavWrapper.animate().cancel();
                        bottomNavWrapper.animate().y(defaultNavY)
                                .withEndAction(() -> {
                                    scrollable = true;
                                    tempCheck = true;
                                })
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(200).start();
                        musicNavControl.animate().cancel();
                        musicNavControl.animate().y(defaultNavY)
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(200).start();
                    } else if (state == STATE_UP && scrollable && (y1 - motionEvent.getY()) > (screenHeight / 24)) {
                        scrollable = false;

                        bottomNavWrapper.animate().cancel();
                        bottomNavWrapper.animate().y(screenHeight - navHeight)
                                .withEndAction(() -> {
                                    scrollable = true;
                                    tempCheck = true;
                                })
                                .scaleX(screenWidth / navWidth)
                                .scaleY(screenWidth / navWidth)
                                .setDuration(200).start();
                        musicNavControl.animate().cancel();
                        musicNavControl.animate().y(screenHeight - navHeight)
                                .scaleX(screenWidth / navWidth)
                                .scaleY(screenWidth / navWidth)
                                .setDuration(200).start();

                    }
                }
                return false;
            }
        };
        musicFragmentInteractionListener = new OnMusicFragmentInteractionListener() {
            @Override
            public void addRecyclerMoveListener(RecyclerView recyclerView) {
                recyclerView.setOnTouchListener(touchListener);
            }

        };
        GlobalListener.MusicFragment.listener = musicFragmentInteractionListener;

        songRecycler.setOnTouchListener(touchListener);

        btnFav.setOnClickListener(v -> GlobalListener.MainActivity.listener.alternateForMusicFragment(new FavSongFragment()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                songListAdapter.filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("TAG", "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}