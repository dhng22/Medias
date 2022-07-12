package com.example.music.listener;

import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.music.bottomSheet.SongOptionBottomSheetFrag;
import com.example.music.models.Song;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public interface OnMainActivityInteractionListener {
    ProgressBar getNavigationProgressBar();

    FragmentManager getFragmentManager();

    int getNavigationProgressBarProgress();

    void setNavigationProgressBarMax(int progressBarMax);

    void setNavigationProgressBarProgress(int progress);

    void validatePlayPauseButton();

    void validateFavButton();

    void validateRepeatButton();

    void openCurrentSongActivity();

    void showSongBottomSheetOption(BottomSheetDialogFragment optionBottomSheetFrag, @Nullable FragmentManager fragmentManager);

    void toggleRepeatMode();

    void shuffleModeSongOn();

    void onFavButtonClicked(Song song);

    void doBackPress();

    void hideNavigationBar();

    void showNavigationBar();
}
