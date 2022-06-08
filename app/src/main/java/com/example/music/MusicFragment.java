package com.example.music;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class MusicFragment extends Fragment {
    public static final int STATE_UP = 1;
    public static final int STATE_DOWN = -1;
    public static CardView bottomNavWrapper;
    public static ConstraintLayout musicNavControl;
    public static BottomNavigationView bottomNav;
    public static ArrayList<Song> songList;
    float screenHeight, screenWidth, navHeight, navWidth, defaultNavY;
    RecyclerView songRecycler;
    SongListAdapter songListAdapter;
    SearchView searchView;
    Button btnFav, btnPlaylist, btnRecent;
    CoordinatorLayout coordinatorLayoutParent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public MusicFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getContext().getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        int currentSong = sharedPreferences.getInt("currentSong", -1);
        SongListAdapter.oldSongHolderIndex = currentSong;
        songList.get(currentSong).isCurrentItem = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;


        mapping(view);

        songListAdapter = new SongListAdapter(getContext(), songList, R.layout.row_song,songRecycler);
        songRecycler.setAdapter(songListAdapter);
        defaultNavY = bottomNavWrapper.getY();
        initListener();
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
        songRecycler.setOnTouchListener(new View.OnTouchListener() {
            boolean scrollable = true;
            boolean tempCheck = true;
            float y1 = 0;
            int state = STATE_UP;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    y1 = 0;
                    tempCheck = true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    if (tempCheck) {
                        y1 = motionEvent.getY();
                        tempCheck = false;
                    }
                    if (state != STATE_UP && scrollable && (y1 - motionEvent.getY()) < -(screenHeight / 24)) {
                        scrollable = false;
                        state = STATE_UP;
                        bottomNavWrapper.animate().cancel();
                        bottomNavWrapper.animate().translationY(defaultNavY)
                                .withEndAction(() -> {
                                    scrollable = true;
                                    tempCheck = true;
                                })
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(200).start();
                        musicNavControl.animate().cancel();
                        musicNavControl.animate().translationY(defaultNavY)
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(200).start();
                    } else if (state != STATE_DOWN && scrollable && (y1 - motionEvent.getY()) > (screenHeight / 24)) {
                        scrollable = false;
                        state = STATE_DOWN;
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
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        editor.putInt("currentSong", SongListAdapter.oldSongHolderIndex);
        editor.commit();
    }
}