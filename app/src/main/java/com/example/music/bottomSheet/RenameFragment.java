package com.example.music.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;


public class RenameFragment extends BottomSheetDialogFragment {
    GlobalMediaPlayer mediaPlayer;
    Button btnCancelRename, btnRename;
    EditText edtRename;
    private Song song;
    private RenameFragment() {
        // Required empty public constructor
    }

    public static RenameFragment getInstance(Song song) {
        RenameFragment renameFragment = new RenameFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song",song);
        renameFragment.setArguments(bundle);
        return renameFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaPlayer = GlobalMediaPlayer.getInstance();
        Bundle bundle = getArguments();
        this.song = (Song) bundle.getSerializable("song");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialogFragment = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.fragment_rename, null);
        mapping(view);
        initListener();
        dialogFragment.setContentView(view);

        return dialogFragment;
    }

    private void initListener() {
        btnRename.setOnClickListener(v -> {
            File fileToRename = new File(song.path);

            String renameTo = song.path.replace(song.songName, edtRename.getText().toString());
            fileToRename.renameTo(new File(renameTo));

            song.songName = edtRename.getText().toString();
            song.path = renameTo;

            GlobalListener.SongListAdapter.listener.notifySongAt(mediaPlayer.getBaseSongList().indexOf(song));

            if (GlobalListener.CurrentSongActivity.listener != null) {
                GlobalListener.CurrentSongActivity.listener.renewCurrentSong();
            }
            dismiss();
        });
        btnCancelRename.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void mapping(View view) {
        btnCancelRename = view.findViewById(R.id.btnCancelRename);
        btnRename = view.findViewById(R.id.btnRename);
        edtRename = view.findViewById(R.id.edtRename);

        edtRename.setHint(song.songName);
        edtRename.setText(song.songName);
    }
}