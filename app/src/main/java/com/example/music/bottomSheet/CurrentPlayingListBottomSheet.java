package com.example.music.bottomSheet;

import static com.example.music.GlobalMediaPlayer.MODE_REPEAT_PLAYLIST;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.adapter.SongListAdapter;
import com.example.music.callbacks.CurrentSongListTouchEvent;
import com.example.music.listener.OnCurrentPlayingListInteractionListener;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class CurrentPlayingListBottomSheet extends BottomSheetDialogFragment {
    SongListAdapter adapter;
    RecyclerView recyclerCurrentSongs;
    ImageButton btnReplayMode;
    GlobalMediaPlayer mediaPlayer;
    CurrentSongListTouchEvent currentSongListTouchEvent;
    ItemTouchHelper itemTouchHelper;
    OnCurrentPlayingListInteractionListener currentPlayingListInteractionListener;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    TextView txtRepMode;
    private CurrentPlayingListBottomSheet() {
        // Required empty public constructor
    }

    public static CurrentPlayingListBottomSheet getInstance() {
        return new CurrentPlayingListBottomSheet();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        mediaPlayer.setVisualSongList(mediaPlayer.getPlayingSongList());

        sharedPreferences = requireContext().getSharedPreferences("appdata", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_current_playing_list_bottom_sheet, null);
        mapping(view);

        listener();
        bottomSheetDialog.setContentView(view);
        validateRepeatMode();
        return bottomSheetDialog;
    }
    @SuppressLint("NotifyDataSetChanged")
    private void listener() {
        currentPlayingListInteractionListener = new OnCurrentPlayingListInteractionListener() {
            @Override
            public void validateRepMode() {
                validateRepeatMode();
            }

            @Override
            public void onHolderDrag(RecyclerView.ViewHolder viewHolderForAdapterPosition) {
                onDrag(viewHolderForAdapterPosition);
            }
        };
        GlobalListener.CurrentPlayingListBottomSheet.listener = currentPlayingListInteractionListener;


        btnReplayMode.setOnClickListener(v -> {
            SongUtils.onRepeatModeChanged();
            validateRepeatMode();
            mediaPlayer.shuffleModeOn();
            adapter.notifyDataSetChanged();
        });
        txtRepMode.setOnClickListener(v -> btnReplayMode.performClick());
    }

    private void validateRepeatMode() {
        if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == MODE_REPEAT_PLAYLIST) {
            btnReplayMode.setImageResource(R.drawable.ic_rep_all);
        } else if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == GlobalMediaPlayer.MODE_REPEAT_ONE) {
            btnReplayMode.setImageResource(R.drawable.ic_rep_one);
        } else if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == GlobalMediaPlayer.MODE_SHUFFLE) {
            btnReplayMode.setImageResource(R.drawable.ic_shuffle);
            mediaPlayer.setVisualSongList(mediaPlayer.getPlayingSongList());
        }
    }

    private void mapping(View view) {
        recyclerCurrentSongs = view.findViewById(R.id.recyclerCurrentSongs);
        btnReplayMode = view.findViewById(R.id.btnReplayMode);
        txtRepMode = view.findViewById(R.id.txtRepMode);

        adapter = new SongListAdapter(requireContext(), R.layout.row_current_song, recyclerCurrentSongs, this, null);
        recyclerCurrentSongs.setAdapter(adapter);

        currentSongListTouchEvent = new CurrentSongListTouchEvent(requireContext(),mediaPlayer.getPlayingSongList(), adapter);
        itemTouchHelper = new ItemTouchHelper(currentSongListTouchEvent);
        itemTouchHelper.attachToRecyclerView(recyclerCurrentSongs);
    }
    public void onDrag(RecyclerView.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);
    }
    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.resetVisualList();
    }
}