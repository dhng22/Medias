package com.example.music.listener;

public interface OnVideoAdapterInteractionListener {
    void notifyVideoAdded();

    void notifySort();

    void startVideoPager(int pos);
}
