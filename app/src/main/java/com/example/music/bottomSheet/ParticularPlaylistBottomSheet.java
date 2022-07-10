package com.example.music.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongListAdapter;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class ParticularPlaylistBottomSheet extends BottomSheetDialogFragment {
    ImageView imgSelectedPlaylist;
    TextView txtSelectedPlaylist;
    Button btnPlaySelectedPlaylist;
    RecyclerView selectedListRecycler;
    private Playlist playlist;
    SongListAdapter adapter;
    GlobalMediaPlayer mediaPlayer;
    private ParticularPlaylistBottomSheet() {
        // Required empty public constructor
    }

    public static ParticularPlaylistBottomSheet getInstance(Playlist playlist) {
        ParticularPlaylistBottomSheet playlistBottomSheet = new ParticularPlaylistBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putSerializable("playlist", playlist);
        playlistBottomSheet.setArguments(bundle);

        return playlistBottomSheet;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.playlist = (Playlist) getArguments().getSerializable("playlist");
        }
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_particurlar_playlist_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        mapping(view);
        initListener();
        return bottomSheetDialog;
    }

    private void initListener() {
        btnPlaySelectedPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Song> songs = playlist.getSongList();
                if (songs.size() > 0) {
                    mediaPlayer.setPlayingSongList(songs,true);
                    mediaPlayer.playSong(songs.get(0), requireContext());
                }
            }
        });
    }

    private void mapping(View view) {
        mediaPlayer.setVisualSongList(playlist.getSongList());
        imgSelectedPlaylist = view.findViewById(R.id.imgSelectedPlaylist);
        txtSelectedPlaylist = view.findViewById(R.id.txtSelectedPlaylist);
        btnPlaySelectedPlaylist = view.findViewById(R.id.btnPlaySelectedPlaylist);
        selectedListRecycler = view.findViewById(R.id.selectedListRecycler);

        adapter = new SongListAdapter(requireContext(), R.layout.row_song, selectedListRecycler, this,playlist);
        selectedListRecycler.setAdapter(adapter);

        txtSelectedPlaylist.setText(playlist.getListName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.resetVisualList();
    }
    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}