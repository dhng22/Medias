package com.example.music.listener;

import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.example.music.bottomSheet.SongOptionBottomSheetFrag;
import com.example.music.models.Song;

public interface OnMainActivityInteractionListener {
    ProgressBar getNavigationProgressBar();

    int getNavigationProgressBarProgress();

    void setNavigationProgressBarMax(int progressBarMax);

    void setNavigationProgressBarProgress(int progress);

    void validatePlayPauseButton();

    void validateFavButton();

    void validateRepeatButton();

    void openCurrentSongActivity();

    void showSongBottomSheetOption(SongOptionBottomSheetFrag optionBottomSheetFrag);

    void alternateForMusicFragment(Fragment fragment);

    void hideFavSongFragment();

    void toggleRepeatMode();

    void shuffleModeSongOn();

    void onFavButtonClicked(Song song);
}
