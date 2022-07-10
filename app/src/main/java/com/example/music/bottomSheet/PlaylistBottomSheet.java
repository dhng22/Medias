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
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongPlaylistAdapter;
import com.example.music.listener.OnPlaylistBottomSheetInteraction;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlaylistBottomSheet extends BottomSheetDialogFragment {
    RecyclerView recyclerPlaylist;
    SongPlaylistAdapter playlistAdapter;
    ArrayList<Playlist> playlists;
    GlobalMediaPlayer mediaPlayer;
    OnPlaylistBottomSheetInteraction playlistBottomSheetInteraction;
    private Song song;
    private PlaylistBottomSheet() {
    }

    public static PlaylistBottomSheet getInstance(Song song) {
        PlaylistBottomSheet playlistBottomSheet = new PlaylistBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", song);
        playlistBottomSheet.setArguments(bundle);
        return playlistBottomSheet;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.song = (Song) bundle.getSerializable("song");
        }
        mediaPlayer = GlobalMediaPlayer.getInstance();
        playlists = mediaPlayer.getSongPlayList();

        playlistBottomSheetInteraction = this::dismiss;
        GlobalListener.PlaylistBottomSheet.listener = playlistBottomSheetInteraction;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_playlist_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        mapping(view);

        return bottomSheetDialog;
    }

    private void initListener() {

    }

    private void mapping(View view) {
        recyclerPlaylist = view.findViewById(R.id.recyclerPlaylist);
        playlistAdapter = new SongPlaylistAdapter(requireContext(), R.layout.item_playlist_dark, this,recyclerPlaylist, song);

        recyclerPlaylist.setAdapter(playlistAdapter);
    }
    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}