package com.example.music.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music.R;
import com.example.music.database.MusicDb;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeleteFragment extends BottomSheetDialogFragment {
    public static final int ACTION_DELETE_SONG = 69;
    public static final int ACTION_DELETE_LIST = 96;
    int action;
    TextView txtHeader;
    Button btnPos, btnNeg;
    private Playlist playlist;
    private Song song;
    private int pos;

    MusicDb musicDb;
    private DeleteFragment() {
        // Required empty public constructor
    }

    public static DeleteFragment getInstance(Song song, Playlist playlist, int pos) {
        DeleteFragment deleteFragment = new DeleteFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("playlist", playlist);
        bundle.putSerializable("song", song);
        bundle.putSerializable("listPost", pos);
        deleteFragment.setArguments(bundle);
        return deleteFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        musicDb = new MusicDb(requireContext(), "playlistSong.db", null, 1);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.playlist = (Playlist) bundle.getSerializable("playlist");
            this.song = (Song) bundle.getSerializable("song");
            this.pos = bundle.getInt("listPost");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_decision, null);
        dialog.setContentView(view);
        mapping(view);
        initListener();
        return dialog;
    }

    private void initListener() {
        btnNeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (action == ACTION_DELETE_LIST) {
                    musicDb.deletePlayList(playlist,pos);
                } else if (action == ACTION_DELETE_SONG) {
                    musicDb.deleteSong(song);
                }
                dismiss();
            }
        });
    }

    private void mapping(View view) {
        txtHeader = view.findViewById(R.id.txtHeaderDecision);
        btnPos = view.findViewById(R.id.btnPos);
        btnNeg = view.findViewById(R.id.btnNeg);

        if (playlist != null) {
            action = ACTION_DELETE_LIST;
            txtHeader.setText("Delete " + playlist.getListName() + "?");
        } else if (song != null) {
            action = ACTION_DELETE_SONG;
            txtHeader.setText("Delete " + song.songName + "?");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}