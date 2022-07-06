package com.example.music.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SongOptionBottomSheetFrag extends BottomSheetDialogFragment {
    private Song song;
    private int pos;
    TextView btnPlayPause, btnAddToList, btnDelete, btnRename,txtSongName, btnFav;
    GlobalMediaPlayer mediaPlayer;
    private SongOptionBottomSheetFrag() {
    }

    public static SongOptionBottomSheetFrag getInstance(Song song,int pos) {
        SongOptionBottomSheetFrag songOptionBottomSheetFrag = new SongOptionBottomSheetFrag();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", song);
        bundle.putInt("pos",pos);
        songOptionBottomSheetFrag.setArguments(bundle);
        return songOptionBottomSheetFrag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaPlayer = GlobalMediaPlayer.getInstance();
        Bundle bundle = getArguments();
        this.song = (Song) bundle.getSerializable("song");
        this.pos = bundle.getInt("pos");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_song_option, null);
        mapping(view);
        initListener();
        bottomSheetDialog.setContentView(view);

        return bottomSheetDialog;
    }

    private void initListener() {
        btnAddToList.setOnClickListener(v -> {

        });
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongUtils.onSongFavClicked(song,pos);
                dismiss();
            }
        });
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.getPlayerState() == GlobalMediaPlayer.PLAYING_STATE || mediaPlayer.getPlayerState() == GlobalMediaPlayer.PAUSING_STATE) {
                if (song.id == mediaPlayer.getCurrentSong().id) {
                    GlobalListener.PlaySongService.listener.playOrPause(requireContext());
                } else {
                    mediaPlayer.reset();
                    mediaPlayer.playSong(song, requireContext());
                }
            } else {
                mediaPlayer.playSong(song, requireContext());
            }
            validatePlayPause();
            dismiss();
        });
        btnRename.setOnClickListener(v -> {
            RenameFragment renameFragment = RenameFragment.getInstance(song);
            renameFragment.setStyle(STYLE_NORMAL, R.style.TransparentDialog);
            renameFragment.show(getParentFragmentManager(),renameFragment.getTag());

            dismiss();
        });
    }

    private void mapping(View view) {
        btnPlayPause = view.findViewById(R.id.txtPlay);
        btnAddToList = view.findViewById(R.id.txtAddToList);
        btnDelete = view.findViewById(R.id.txtDelete);
        btnRename = view.findViewById(R.id.txtRename);
        txtSongName = view.findViewById(R.id.txtSongName);
        btnFav = view.findViewById(R.id.txtFav);

        txtSongName.setText(song.songName);

        validatePlayPause();
        validateFav();
    }

    private void validateFav() {
        if (song.isFavorite) {
            btnFav.setText("Remove from favorite");
        } else {
            btnFav.setText("Add to favorite");
        }
    }

    private void validatePlayPause() {
        if (song.id == mediaPlayer.getCurrentSong().id) {
            if (mediaPlayer.getPlayerState() == GlobalMediaPlayer.PLAYING_STATE) {
                btnPlayPause.setText("Pause");
            } else {
                btnPlayPause.setText("Play");
            }
        } else {
            btnPlayPause.setText("Play");
        }
    }
}
