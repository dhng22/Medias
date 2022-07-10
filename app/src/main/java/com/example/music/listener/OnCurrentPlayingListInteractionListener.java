package com.example.music.listener;

import androidx.recyclerview.widget.RecyclerView;

public interface OnCurrentPlayingListInteractionListener {
    void validateRepMode();

    void onHolderDrag(RecyclerView.ViewHolder viewHolderForAdapterPosition);
}
