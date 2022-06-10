package com.example.music.listener;

import android.widget.ProgressBar;

public interface OnMainActivityInteractionListener {
    ProgressBar getNavigationProgressBar();

    int getNavigationProgressBarProgress();

    void setNavigationProgressBarMax(int progressBarMax);

    void setNavigationProgressBarProgress(int progress);

    void validatePlayPauseButton();

    void validateFavButton();
}
