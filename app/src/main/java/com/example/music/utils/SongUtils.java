package com.example.music.utils;
import com.example.music.GlobalMediaPlayer;
import com.example.music.models.Song;

public class SongUtils {
    public static String getFormattedDuration(long duration) {
        return String.format("%02d:%02d", duration / 1000 / 60, duration / 1000 % 60);
    }

    public static void onSongPlayPause(Song song) {
        if (GlobalListener.SongListAdapter.listener != null) {
            GlobalListener.SongListAdapter.listener.setBackGroundForNewSong(song);
        }

        if (GlobalListener.MainActivity.listener != null) {
            GlobalListener.MainActivity.listener .validatePlayPauseButton();
        }

        if (GlobalListener.CurrentSongActivity.listener != null) {
            GlobalListener.CurrentSongActivity.listener.validateButtons();
        }

        if (GlobalListener.PlaySongService.listener != null) {
            GlobalListener.PlaySongService.listener.reloadNotificationMediaState();
        }
    }

    public static void onRepeatModeChanged() {
        if (GlobalListener.MainActivity.listener != null) {
            GlobalListener.MainActivity.listener.toggleRepeatMode();
            GlobalListener.MainActivity.listener.validateRepeatButton();
        }
        if (GlobalListener.CurrentSongActivity.listener != null) {
            GlobalListener.CurrentSongActivity.listener.validateButtons();
        }
    }

    public static void shuffleModeOn() {
        if (GlobalListener.MainActivity.listener != null) {
            GlobalListener.MainActivity.listener.shuffleModeSongOn();
            GlobalListener.MainActivity.listener.validateRepeatButton();
        }
        if (GlobalListener.CurrentSongActivity.listener != null) {
            GlobalListener.CurrentSongActivity.listener.validateButtons();
        }
        if (GlobalListener.CurrentPlayingListBottomSheet.listener != null) {
            GlobalListener.CurrentPlayingListBottomSheet.listener.validateRepMode();
        }
    }
    public static void onSongFavClicked(Song song, int pos) {
        if (GlobalListener.MainActivity.listener != null) {
            GlobalListener.MainActivity.listener.onFavButtonClicked(song);
            GlobalListener.MainActivity.listener.validateFavButton();
        }
        if (GlobalListener.CurrentSongActivity.listener != null) {
            GlobalListener.CurrentSongActivity.listener.validateButtons();
        }
        if (GlobalListener.FavSongFragment.listener != null) {
            GlobalListener.FavSongFragment.listener.notifyDataSetChange(pos);
        }
    }
}
