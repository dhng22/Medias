package com.example.music.listener;

import android.annotation.SuppressLint;

public interface OnImageAdapterInteractionListener {

    void notifyImageAdded();
    @SuppressLint("NotifyDataSetChanged")
    void notifySort();

    void startImagePager(int pos);
}
