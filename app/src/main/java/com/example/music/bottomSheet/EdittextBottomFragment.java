package com.example.music.bottomSheet;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongPlaylistAdapter;
import com.example.music.database.MusicDb;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;


public class EdittextBottomFragment extends BottomSheetDialogFragment {
    public static final int ACTION_RENAME = 10;
    public static final int ACTION_ADD_PLAYLIST = 11;
    GlobalMediaPlayer mediaPlayer;
    Button btnCancelRename, btnRename;
    EditText edtRename;
    TextView txtHeaderEdtFrag;
    private Song song;
    private int action;
    MusicDb musicDb;
    SongPlaylistAdapter songListAdapter;
    Playlist playlist;

    private EdittextBottomFragment() {
        // Required empty public constructor
    }

    public static EdittextBottomFragment getInstance(Song song, Playlist playlist, int action, SongPlaylistAdapter songListAdapter) {
        EdittextBottomFragment edittextBottomFragment = new EdittextBottomFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song", song);
        bundle.putSerializable("playlist", playlist);
        bundle.putSerializable("songListAdapter", songListAdapter);
        bundle.putInt("action", action);
        edittextBottomFragment.setArguments(bundle);
        return edittextBottomFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaPlayer = GlobalMediaPlayer.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.song = (Song) bundle.getSerializable("song");
            this.playlist = (Playlist) bundle.getSerializable("playlist");
            this.action = bundle.getInt("action");
            this.songListAdapter = (SongPlaylistAdapter) bundle.getSerializable("songListAdapter");
        }
        musicDb = new MusicDb(requireContext(), "playlistSong.db", null, 1);
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
            if (action == ACTION_RENAME) {
                if (song != null) {
                    File fileToRename = new File(song.path);

                    String toPath = song.path.replace(song.songName, edtRename.getText().toString().trim());
                    musicDb.updateSongPath(song, toPath);

                    File renameToTemp = new File( song.path.replace(song.songName, "yziscdqweqwzxcs"));
                    fileToRename.renameTo(renameToTemp);

                    File renameToDes = new File(toPath);
                    renameToTemp.renameTo(renameToDes);

                    song.songName = edtRename.getText().toString().trim();
                    song.path = toPath;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("TAG", "initListener: " + song.path);
                        }
                    }, 300);
                    if (mediaPlayer.getCurrentSong().equals(song)) {
                        SongUtils.setCurrentSong(requireContext(), song);
                    }
                    GlobalListener.SongListAdapter.listener.notifySongAt(mediaPlayer.getVisualSongList().indexOf(song));

                    if (GlobalListener.CurrentSongActivity.listener != null) {
                        GlobalListener.CurrentSongActivity.listener.renewCurrentSong();
                    }
                } else {
                    if (playlist != null) {
                        String renameTo = edtRename.getText().toString().trim();
                        if (renameTo.isEmpty()) {
                            Toast.makeText(requireContext(), "Playlist's name must not be empty", Toast.LENGTH_SHORT).show();
                        } else if (!mediaPlayer.isThisTableExist(renameTo)) {
                            musicDb.renamePlaylist(playlist, renameTo, mediaPlayer.getListIndexFromVisualPlaylist(playlist));
                            dismiss();
                        } else {
                            edtRename.setText("");
                            Toast.makeText(requireContext(), "Playlist already existed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else if (action == ACTION_ADD_PLAYLIST) {
                String playlistName = edtRename.getText().toString().trim();
                if (playlistName.isEmpty()) {
                    Toast.makeText(requireContext(), "Playlist's name must not be empty", Toast.LENGTH_SHORT).show();
                } else if (!mediaPlayer.isThisTableExist(playlistName)) {
                    musicDb.createPlayList(playlistName, songListAdapter);
                    dismiss();
                } else {
                    edtRename.setText("");
                    Toast.makeText(requireContext(), "Playlist already existed", Toast.LENGTH_SHORT).show();
                }
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
        txtHeaderEdtFrag = view.findViewById(R.id.txtHeaderDecision);

        if (action == ACTION_RENAME) {
            btnRename.setText("Rename");
            txtHeaderEdtFrag.setText("Rename to");
            if (song != null) {
                edtRename.setHint(song.songName);
                edtRename.setText(song.songName);
            } else if (playlist != null) {
                edtRename.setHint(playlist.getListName());
                edtRename.setText(playlist.getListName());
            }
        } else if (action == ACTION_ADD_PLAYLIST) {
            btnRename.setText("Create");
            txtHeaderEdtFrag.setText("Create new playlist");
            edtRename.setHint("Playlist name");
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}