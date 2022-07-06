package com.example.music;

import static com.example.music.fragment.FavSongFragment.TABLE_NAME;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.example.music.database.PlaylistDb;
import com.example.music.models.Song;
import com.example.music.service.PlaySongService;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GlobalMediaPlayer {
    public static final int NULL_STATE = -1;
    public static final int PAUSING_STATE = 1;
    public static final int PLAYING_STATE = 2;

    public static final int MODE_REPEAT_PLAYLIST = 2;
    public static final int MODE_REPEAT_ONE = 1;
    public static final int MODE_SHUFFLE = 3;

    private static final GlobalMediaPlayer globalMediaPlayer = new GlobalMediaPlayer();
    private int currentSongPlayingPosition = -1;
    private static ArrayList<Song> playingSongList, visualSongList, baseSongList, favSongList;

    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private Runnable updateSeekBarRunnable;
    ObjectAnimator animIncreaseSeekBar, animResetSeekBar;
    Handler updateSeekBarHandler, helperHandler;
    private boolean isGoingUp;

    private GlobalMediaPlayer() {
        initVar();
        initListener();
    }

    public void init(ArrayList<String> songUri, Context context) {

        sharedPreferences = context.getSharedPreferences("appdata", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        visualSongList = new ArrayList<>();

        for (String music : songUri) {
            visualSongList.add(new Song(music, Song.NORMAL_STATE, context));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            visualSongList.sort(Comparator.naturalOrder());
        }

        for (int i = 0; i < visualSongList.size(); i++) {
            visualSongList.get(i).id = i;
        }
        baseSongList = new ArrayList<>(visualSongList);
        renewPlayingSongList();
    }

    private void initVar() {
        updateSeekBarHandler = new Handler();
        helperHandler = new Handler();
    }

    private void initListener() {
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPlaying() && (animResetSeekBar == null || !animResetSeekBar.isRunning())) {
                    animIncreaseSeekBar = ObjectAnimator.ofInt(GlobalListener.MainActivity.listener.getNavigationProgressBar(),
                            "progress",
                            GlobalListener.MainActivity.listener.getNavigationProgressBarProgress(), mediaPlayer.getCurrentPosition()).setDuration(300);
                    if (!animIncreaseSeekBar.isRunning()) {
                        animIncreaseSeekBar.start();
                        updateSeekBarHandler.postDelayed(this, 200);
                    }
                } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    GlobalListener.MainActivity.listener.setNavigationProgressBarProgress(mediaPlayer.getCurrentPosition());
                } else {
                    updateSeekBarHandler.removeCallbacks(this);
                }
            }
        };
    }

    public void initFavSong(Context context) {
        favSongList = new ArrayList<>();
        PlaylistDb favSongDb = new PlaylistDb(context, "favSong.db", null, 1);
        favSongDb.queryData("CREATE TABLE IF NOT EXISTS favList(id INTEGER PRIMARY KEY AUTOINCREMENT,name VARCHAR(200))");

        Cursor data = favSongDb.getData(TABLE_NAME);
        while (data.moveToNext()) {
            Song song = getSongByKeyword(data.getString(1));
            song.isFavorite = true;
            favSongList.add(song);
        }
    }
    public void initOldSong(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("appdata", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        int currentSongId = sharedPreferences.getInt("currentSong", -1);
        setCurrentSongById(currentSongId);

        int currentDur = sharedPreferences.getInt("currentDur", -1);
        setCurrentSongPlayingPosition(currentDur);

        if (mediaPlayer == null) {
            if (currentSong != null) {
                mediaPlayer = MediaPlayer.create(context, Uri.parse(currentSong.path));
                addMediaPlayerListener(context);
                mediaPlayer.seekTo(currentSongPlayingPosition);
                if (currentSong.getCurrentState() == Song.NORMAL_STATE) {
                    currentSong.setCurrentState(Song.PLAYING_STATE);
                }
                editor.putInt("currentDur", -1);
                currentSongPlayingPosition = -1;

                if (GlobalListener.MainActivity.listener.getNavigationProgressBarProgress() == 0) {
                    GlobalListener.MainActivity.listener.setNavigationProgressBarMax((int) getCurrentSongDuration());
                    GlobalListener.MainActivity.listener.setNavigationProgressBarProgress(currentDur);
                }
                helperHandler.postDelayed(() -> GlobalListener.PlaySongService.listener.reloadNotificationMediaState(), 100);
            }
        } else {
            if (currentSong.getCurrentState() == Song.NORMAL_STATE) {
                currentSong.setCurrentState(Song.PLAYING_STATE);
            }
            GlobalListener.SongListAdapter.listener.setBackGroundForNewSong(currentSong);
            if (GlobalListener.MainActivity.listener.getNavigationProgressBarProgress() == 0) {
                GlobalListener.MainActivity.listener.setNavigationProgressBarMax((int) getCurrentSongDuration());
                GlobalListener.MainActivity.listener.setNavigationProgressBarProgress(currentDur);
            }
        }
    }


    public void playSong(Song songToPlay, Context context) {
        reset();
        int currentSongIn = getVisualSongIndex();

        if (currentSong != null) {
            if (currentSong.getCurrentState() == Song.PLAYING_STATE) {
                currentSong.setCurrentState(Song.NORMAL_STATE);
            }
        }
        if (songToPlay.getCurrentState() == Song.NORMAL_STATE) {
            songToPlay.setCurrentState(Song.PLAYING_STATE);
        }

        mediaPlayer = MediaPlayer.create(context, Uri.parse(songToPlay.path));
        addMediaPlayerListener(context);
        mediaPlayer.start();
        editor.putInt("currentSong", songToPlay.id);
        editor.commit();

        GlobalListener.SongListAdapter.listener.onNewSongPlaying(currentSong, songToPlay);
        currentSong = songToPlay;

        int newSongIn = getVisualSongIndex();
        isGoingUp = (newSongIn - currentSongIn) < 0;
        Intent intent = new Intent(context, PlaySongService.class);
        context.startService(intent);

    }

    public void repeatSong(Context context) {
        playSong(currentSong, context);
    }

    public void pauseSong(Context context) {
        if (mediaPlayer != null) {
            editor.putInt("currentDur", getCurrentSongPlayingPosition());
            editor.putInt("currentSong", getCurrentSong().id);
            editor.commit();
            mediaPlayer.pause();

            SongUtils.onSongPlayPause(currentSong);
            Intent intent = new Intent(context, PlaySongService.class);
            context.startService(intent);
        }
    }

    public void resumeSong(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, Uri.parse(currentSong.path));
            addMediaPlayerListener(context);
        }
        mediaPlayer.start();
        SongUtils.onSongPlayPause(currentSong);
        Intent intent = new Intent(context, PlaySongService.class);
        context.startService(intent);
        updateNavigationSeekBarListener();
    }

    public void prevSong(Context context) {
        resetNavigationSeekBar();
        editor.putInt("currentDur", -1);
        editor.commit();
        currentSongPlayingPosition = -1;

        int currentSongIndex = getCurrentSongIndex();
        Song newSong;
        if (currentSongIndex == 0) {
            newSong = playingSongList.get(playingSongList.size() - 1);
            reset();
            playSong(newSong, context);
        } else if (currentSongIndex > 0) {
            newSong = playingSongList.get(currentSongIndex - 1);
            reset();
            playSong(newSong, context);
        }

        editor.putInt("currentSong", currentSong.id);
        editor.commit();
        GlobalListener.MainActivity.listener.validateFavButton();
        if (GlobalListener.CurrentSongActivity.listener != null) {
            GlobalListener.CurrentSongActivity.listener.renewCurrentSong();
            GlobalListener.CurrentSongActivity.listener.validateButtons();
        }
    }

    public void nextSong(Context context) {
        resetNavigationSeekBar();
        editor.putInt("currentDur", -1);
        editor.commit();
        currentSongPlayingPosition = -1;

        int currentSongIndex = getCurrentSongIndex();
        Song newSong;

        if (currentSongIndex == playingSongList.size() - 1) {
            if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == MODE_SHUFFLE) {
                shuffleModeOn();
            }
            newSong = playingSongList.get(0);
            reset();
            playSong(newSong, context);

        } else if (currentSongIndex < playingSongList.size() - 1) {
            newSong = playingSongList.get(currentSongIndex + 1);
            reset();
            playSong(newSong, context);
        }

        editor.putInt("currentSong", currentSong.id);
        editor.commit();
        GlobalListener.MainActivity.listener.validateFavButton();
        if (GlobalListener.CurrentSongActivity.listener != null) {
            GlobalListener.CurrentSongActivity.listener.renewCurrentSong();
            GlobalListener.CurrentSongActivity.listener.validateButtons();
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            editor.putInt("currentDur", getCurrentSongPlayingPosition());
            editor.putInt("currentSong", currentSong.id);
            editor.commit();
            mediaPlayer.pause();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void seekTo(int timeStamp) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(timeStamp);
        }
    }

    public void reset() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void resetVisualList() {
        visualSongList = new ArrayList<>(baseSongList);
    }

    public void renewPlayingSongList() {
        playingSongList = new ArrayList<>(visualSongList);
    }

    public void shuffleModeOn() {
        if (visualSongList.size() > 0) {
            if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == MODE_SHUFFLE) {
                Collections.shuffle(playingSongList);
            }
        }
    }

    public void shuffleModeOff() {
        renewPlayingSongList();
    }

    public void resetNavigationSeekBar() {
        if (GlobalListener.MainActivity.listener.getNavigationProgressBar() == null) {
            return;
        }
        float progress = GlobalListener.MainActivity.listener.getNavigationProgressBarProgress();
        float max = GlobalListener.MainActivity.listener.getNavigationProgressBar().getMax();
        int time = (int) ((progress / max) * 2000f);


        animResetSeekBar = ObjectAnimator.ofInt(GlobalListener.MainActivity.listener.getNavigationProgressBar(),
                "progress",
                (int) progress,
                0).setDuration(Math.max(time, 500));
        animResetSeekBar.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animResetSeekBar = null;
                if (mediaPlayer != null) {
                    GlobalListener.MainActivity.listener.setNavigationProgressBarMax(mediaPlayer.getDuration());
                    if (GlobalListener.CurrentSongActivity.listener != null) {
                        GlobalListener.CurrentSongActivity.listener.setSongSeekBarMax(mediaPlayer.getDuration());
                        GlobalListener.CurrentSongActivity.listener.setTxtSongDuration(mediaPlayer.getDuration());
                    }
                    updateSeekBarHandler.postDelayed(updateSeekBarRunnable, 200);
                }
            }
        });
        animResetSeekBar.start();
    }

    public void updateNavigationSeekBarListener() {
        if (animResetSeekBar == null || !animResetSeekBar.isRunning()) {
            GlobalListener.MainActivity.listener.setNavigationProgressBarMax(mediaPlayer.getDuration());
            if (GlobalListener.CurrentSongActivity.listener != null) {
                GlobalListener.CurrentSongActivity.listener.setSongSeekBarMax(mediaPlayer.getDuration());
                GlobalListener.CurrentSongActivity.listener.setTxtSongDuration(mediaPlayer.getDuration());
            }
            updateSeekBarHandler.postDelayed(updateSeekBarRunnable, 200);
        }
    }

    private void addMediaPlayerListener(Context context) {
        mediaPlayer.setOnCompletionListener(mp -> {
            resetNavigationSeekBar();
            if (sharedPreferences.getInt("repeatMode", MODE_REPEAT_PLAYLIST) == MODE_REPEAT_ONE) {
                repeatSong(context);
            } else {
                nextSong(context);
            }
        });
    }


    public boolean isGoingUp() {
        return isGoingUp;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getPlayerState() {
        if (mediaPlayer == null) {
            return NULL_STATE;
        } else if (mediaPlayer.isPlaying()) {
            return PLAYING_STATE;
        } else {
            return PAUSING_STATE;
        }
    }

    public int getVisualSongIndex() {
        if (currentSong != null) {
            for (int i = 0; i < visualSongList.size(); i++) {
                if (currentSong.id == visualSongList.get(i).id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getCurrentSongIndex() {
        if (currentSong != null) {
            for (int i = 0; i < playingSongList.size(); i++) {
                if (currentSong.id == playingSongList.get(i).id) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getCurrentSongPlayingPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public long getCurrentSongDuration() {
        return currentSong != null ? currentSong.duration : 0;
    }

    public Song getSongById(int id) {
        for (int i = 0; i < visualSongList.size(); i++) {
            Song song = visualSongList.get(i);
            if (id == song.id) {
                return song;
            }
        }
        return null;
    }

    public Song getSongByKeyword(String key) {
        for (Song song :
                baseSongList) {
            if (song.songName.contains(key)) {
                return song;
            }
        }
        return null;
    }

    public Song getSongAt(int index) {
        return visualSongList.get(index);
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public ArrayList<Song> getPlayingSongList() {
        return playingSongList;
    }

    public ArrayList<Song> getFavSongList(Context context) {
        initFavSong(context);
        return favSongList;
    }
    public ArrayList<Song> getBaseSongList() {
        return visualSongList;
    }

    public ArrayList<Song> getBaseBaseSongList() {
        return baseSongList;
    }

    public static GlobalMediaPlayer getInstance() {
        return globalMediaPlayer;
    }

    public void setCurrentSongPlayingPosition(int currentSongPlayingPosition) {
        this.currentSongPlayingPosition = currentSongPlayingPosition;
    }

    public void setCurrentSongById(int id) {
        currentSong = getSongById(id);
    }

    public void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public void setPlayingSongList(@Nullable ArrayList<Song> currentSongList) {
        if (currentSongList != null) {
            GlobalMediaPlayer.playingSongList = currentSongList;
        }
    }

    public void setVisualSongList(ArrayList<Song> baseSongList) {
        GlobalMediaPlayer.visualSongList = baseSongList;
    }
}
