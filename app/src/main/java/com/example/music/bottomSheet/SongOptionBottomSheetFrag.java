package com.example.music.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongListAdapter;
import com.example.music.database.PlaylistDb;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SongOptionBottomSheetFrag extends BottomSheetDialogFragment {
    private Song song;
    private Playlist playlist;
    private int pos;
    TextView btnPlayPause, btnAddToList, btnDelete, btnRename,txtSongName, btnFav;
    GlobalMediaPlayer mediaPlayer;
    public static Fragment fragment;
    PlaylistDb playlistDb;

    private SongOptionBottomSheetFrag() {
    }

    public static SongOptionBottomSheetFrag getInstance(Song song, Playlist playlist, int songPos, Fragment parentFragment) {
        fragment = parentFragment;
        SongOptionBottomSheetFrag songOptionBottomSheetFrag = new SongOptionBottomSheetFrag();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", song);
        bundle.putSerializable("playlist", playlist);
        bundle.putInt("songPos", songPos);
        songOptionBottomSheetFrag.setArguments(bundle);

        return songOptionBottomSheetFrag;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.song = (Song) bundle.getSerializable("song");
            this.pos = bundle.getInt("songPos");
            this.playlist = (Playlist) bundle.getSerializable("playlist");
        }
        playlistDb = new PlaylistDb(requireContext(), "playlistSong.db", null, 1);
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
            if (fragment instanceof ParticularPlaylistBottomSheet) {
                playlistDb.removeSongFromPlaylist(song,playlist,GlobalListener.SongListAdapter.listener.getAdapter(),mediaPlayer.getSongIndexFromVisualList(song), requireContext());
            } else {
                PlaylistBottomSheet playlistBottomSheet = PlaylistBottomSheet.getInstance(song);
                GlobalListener.MainActivity.listener.showSongBottomSheetOption(playlistBottomSheet,getParentFragmentManager());
            }
            dismiss();
        });

        btnFav.setOnClickListener(v -> {
            SongUtils.onSongFavClicked(song,pos);
            dismiss();
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
            EdittextBottomFragment edittextBottomFragment = EdittextBottomFragment.getInstance(song, null, EdittextBottomFragment.ACTION_RENAME, null);
            edittextBottomFragment.setStyle(STYLE_NORMAL, R.style.TransparentDialog);
            edittextBottomFragment.show(getParentFragmentManager(), edittextBottomFragment.getTag());

            dismiss();
        });
        btnDelete.setOnClickListener(v -> {
            DeleteFragment deleteFragment = DeleteFragment.getInstance(song, playlist, pos);
            GlobalListener.MainActivity.listener.showSongBottomSheetOption(deleteFragment, getParentFragmentManager());
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
        validateAddList();
    }

    private void validateAddList() {
        if (fragment instanceof ParticularPlaylistBottomSheet) {
            if (playlist.getSongList().contains(song)) {
                btnAddToList.setText("Remove from this list");
            }
        }

    }

    private void validateFav() {
        if (song.isFavorite) {
            btnFav.setText("Remove from favorite");
        } else {
            btnFav.setText("Add to favorite");
        }
    }

    private void validatePlayPause() {
        if (song == null) {
            return;
        }
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
    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
