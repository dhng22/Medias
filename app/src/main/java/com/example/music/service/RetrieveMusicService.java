package com.example.music.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music.GlobalMediaPlayer;
import com.example.music.models.Song;
import com.example.music.models.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RetrieveMusicService extends IntentService {
    public static final String ACTION_RETRIEVE_MUSIC = "action_retrieve_music";
    ArrayList<String> musics;
    Intent retrieveMusicReceiver;
    LocalBroadcastManager broadcastManager;
    GlobalMediaPlayer mediaPlayer;

    public RetrieveMusicService() {
        super("RETRIEVE_SERVICE");
        musics = new ArrayList<>();
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        retrieveMusicReceiver = new Intent(ACTION_RETRIEVE_MUSIC);
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        retrieveMusicReference(Environment.getExternalStorageDirectory());

        ArrayList<Song> music = new ArrayList<>();
        for (String path : musics) {
            music.add(new Song(path, Song.NORMAL_STATE, getApplicationContext()));
        }
        retrieveMusicReceiver.putExtra("musics", music);
        broadcastManager.sendBroadcast(retrieveMusicReceiver);
    }

    private void retrieveMusicReference(File file) {
        if (file.isDirectory()) {
            File[] innerFile = file.listFiles();
            if (innerFile != null) {
                for (File file1 : innerFile) {
                    String name = file1.getName().toLowerCase();
                    if (file1.isDirectory()) {
                        if (!name.contains("sys")
                                && !name.contains("droid")
                                && !name.contains(".")
                                && !name.contains("com")
                                && !name.contains("ui")
                                && !name.contains("vendor")
                                && !name.contains("back")
                                && !name.contains("record")
                                ) {
                            retrieveMusicReference(file1);
                        }
                    } else if (file1.getAbsolutePath().contains(".mp3") && !Objects.requireNonNull(file1.getParent()).contains("record") && file1.length() >= 500000) {
                        musics.add(file1.getAbsolutePath());
                    }
                }

            }
        }
    }
}
