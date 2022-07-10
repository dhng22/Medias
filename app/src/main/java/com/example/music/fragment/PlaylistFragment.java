package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongPlaylistAdapter;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {
    RecyclerView recyclerPlaylist;
    SongPlaylistAdapter playlistAdapter;
    ArrayList<Playlist> playlists;
    ImageButton btnBackFromPlaylistFrag;
    TextView txtIntroPlaylistFrag,btnPlayRandomlyPlaylist;
    GlobalMediaPlayer mediaPlayer;
    public PlaylistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = GlobalMediaPlayer.getInstance();

    }

    @Override
    public void onResume() {
        super.onResume();
        playlists = mediaPlayer.getSongPlayList();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        mapping(view);
        initListener();
        // Inflate the layout for this fragment
        return view;
    }

    private void initListener() {
        btnBackFromPlaylistFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalListener.MainActivity.listener.doBackPress();
            }
        });
        GlobalListener.LocalSongFragment.listener.addRecyclerMoveListener(recyclerPlaylist);
        btnPlayRandomlyPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Playlist p :
                        playlists) {
                    if (p.getSongList().size() >0) {
                        break;
                    }
                    Toast.makeText(requireContext(), "All list are empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<Song> songs = new ArrayList<>();
                while (songs.size() == 0) {
                    songs = playlists.get((int) (Math.random() * (playlists.size()))).getSongList();
                }
                mediaPlayer.setPlayingSongList(songs,true);
                mediaPlayer.playSong(songs.get(0), requireContext());
            }
        });

    }

    private void mapping(View view) {
        recyclerPlaylist = view.findViewById(R.id.recyclerPlaylist);
        btnBackFromPlaylistFrag = view.findViewById(R.id.btnBackFromPlaylistFrag);
        txtIntroPlaylistFrag = view.findViewById(R.id.txtIntroPlaylistFrag);
        btnPlayRandomlyPlaylist = view.findViewById(R.id.btnPlayRandomlyPlaylist);

        playlistAdapter = new SongPlaylistAdapter(requireContext(), R.layout.item_playlist_dark, this,recyclerPlaylist,null);
        recyclerPlaylist.setAdapter(playlistAdapter);
    }
}