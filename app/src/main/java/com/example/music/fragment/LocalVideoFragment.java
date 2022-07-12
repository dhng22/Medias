package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.VideoAdapter;
import com.example.music.utils.GlobalListener;


public class LocalVideoFragment extends Fragment {
    RecyclerView recyclerView;
    VideoAdapter adapter;
    GlobalMediaPlayer mediaPlayer;
    public LocalVideoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = GlobalMediaPlayer.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_video, container, false);
        mapping(view);
        return view;
    }

    private void mapping(View view) {
        recyclerView = view.findViewById(R.id.recyclerVideo);
        adapter = new VideoAdapter(requireContext(), R.layout.item_video, mediaPlayer.getBaseVideoList(), recyclerView, LocalVideoFragment.this);
        recyclerView.setAdapter(adapter);
        GlobalListener.LocalSongFragment.listener.addRecyclerMoveListener(recyclerView);
    }

}