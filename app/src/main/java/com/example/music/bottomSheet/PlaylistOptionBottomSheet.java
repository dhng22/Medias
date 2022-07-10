package com.example.music.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongPlaylistAdapter;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlaylistOptionBottomSheet extends BottomSheetDialogFragment {
    Playlist playlist;
    int listPos;
    TextView txtPlay,txtDelete, txtRename, txtPlaylistName;
    GlobalMediaPlayer mediaPlayer;
    private PlaylistOptionBottomSheet() {
        // Required empty public constructor
    }

    public static PlaylistOptionBottomSheet getInstance(Playlist playlist, int listPost) {
        PlaylistOptionBottomSheet playlistOptionBottomSheet = new PlaylistOptionBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putSerializable("playlist", playlist);
        bundle.putSerializable("listPost", listPost);
        playlistOptionBottomSheet.setArguments(bundle);
        return playlistOptionBottomSheet;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaPlayer = GlobalMediaPlayer.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.playlist = (Playlist) bundle.getSerializable("playlist");
            this.listPos = bundle.getInt("listPost");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_playlist_option_bottom_sheet, null);
        dialog.setContentView(view);
        mapping(view);
        initListener();
        return dialog;
    }

    private void initListener() {
        txtPlay.setOnClickListener(v -> {
            ArrayList<Song> songs = playlist.getSongList();
            if (songs.size() > 0) {
                mediaPlayer.setPlayingSongList(songs,true);
                mediaPlayer.playSong(songs.get(0), requireContext());
            }
            dismiss();
        });
        txtRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EdittextBottomFragment bottomFragment = EdittextBottomFragment.getInstance(null, playlist, EdittextBottomFragment.ACTION_RENAME, null);
                GlobalListener.MainActivity.listener.showSongBottomSheetOption(bottomFragment, getParentFragmentManager());
                dismiss();
            }
        });
        txtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteFragment deleteFragment = DeleteFragment.getInstance(null, playlist, mediaPlayer.getListIndexFromVisualPlaylist(playlist));
                GlobalListener.MainActivity.listener.showSongBottomSheetOption(deleteFragment, getParentFragmentManager());
                dismiss();
            }
        });
    }

    private void mapping(View view) {
        txtDelete = view.findViewById(R.id.txtDelete);
        txtPlay = view.findViewById(R.id.txtPlay);
        txtRename = view.findViewById(R.id.txtRename);
        txtPlaylistName = view.findViewById(R.id.txtPlaylistName);

        txtPlaylistName.setText(playlist.getListName());
    }
    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}