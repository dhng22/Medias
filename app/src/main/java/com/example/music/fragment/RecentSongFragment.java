package com.example.music.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongListAdapter;
import com.example.music.listener.OnFavSongFragmentInteractionListener;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;

import java.util.ArrayList;

public class RecentSongFragment extends Fragment {
    public static final String TABLE_NAME = "recentSong";
    ImageButton btnBack;
    TextView btnPlayRandomly;
    RecyclerView recyclerFavSong;
    ArrayList<Song> recentSongList;
    GlobalMediaPlayer mediaPlayer;
    OnFavSongFragmentInteractionListener favSongFragmentInteractionListener;
    SongListAdapter adapter;
    public RecentSongFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        initRecentSongList();

    }

    private void initRecentSongList() {
        recentSongList = mediaPlayer.getRecentSongList(requireContext());
        mediaPlayer.setVisualSongList(recentSongList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_song, container, false);
        // Inflate the layout for this fragment
        mapping(view);
        initListener();
        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initListener() {
        GlobalListener.LocalSongFragment.listener.addRecyclerMoveListener(recyclerFavSong);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalListener.MainActivity.listener.doBackPress();
            }
        });
    }

    private void mapping(View view) {
        btnBack = view.findViewById(R.id.btnBackFromPlaylistFrag);
        btnPlayRandomly = view.findViewById(R.id.btnPlayRandomlyFavSong);
        recyclerFavSong = view.findViewById(R.id.recyclerFavSong);

        adapter = new SongListAdapter(requireContext(), R.layout.row_song, recyclerFavSong,this,null);
        recyclerFavSong.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.resetVisualList();
    }
}
