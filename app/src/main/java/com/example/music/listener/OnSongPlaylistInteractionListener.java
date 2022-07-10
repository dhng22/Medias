package com.example.music.listener;

public interface OnSongPlaylistInteractionListener {
    void notifyItemChange(int pos);
    void notifyItemRemove(int pos);
}
