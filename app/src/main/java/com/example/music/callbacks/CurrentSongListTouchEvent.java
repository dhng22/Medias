package com.example.music.callbacks;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.models.Song;

import java.util.ArrayList;
import java.util.Collections;

public class CurrentSongListTouchEvent extends ItemTouchHelper.Callback {
    GlobalMediaPlayer mediaPlayer;
    ArrayList<Song> currentSongList;
    RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;
    Context context;
    public CurrentSongListTouchEvent(Context context, ArrayList<Song> currentSongList, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        this.currentSongList = currentSongList;
        this.adapter = adapter;
        this.context = context;
        mediaPlayer = GlobalMediaPlayer.getInstance();

    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlag = ItemTouchHelper.DOWN | ItemTouchHelper.UP;
        int swipeFlag = ItemTouchHelper.RIGHT;

        return makeMovementFlags(dragFlag, swipeFlag);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        Collections.swap(currentSongList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
        adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

        return false;
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (mediaPlayer.getSongAt(viewHolder.getAdapterPosition()) == mediaPlayer.getCurrentSong()) {
            mediaPlayer.nextSong(context);
        }
        currentSongList.remove(currentSongList.get(viewHolder.getAdapterPosition()));
        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }
}
