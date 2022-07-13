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
    public static final String ACTION_RETRIEVE_VIDEO = "action_retrieve_video";
    public static final String ACTION_RETRIEVE_IMAGE = "action_retrieve_image";
    ArrayList<String> musics,videos, images;
    Intent retrieveMusicReceiver;
    LocalBroadcastManager broadcastManager;
    ArrayList<String> fileType;
    GlobalMediaPlayer mediaPlayer;

    public RetrieveMusicService() {
        super("RETRIEVE_SERVICE");
        musics = new ArrayList<>();
        videos = new ArrayList<>();
        images = new ArrayList<>();
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        fileType = new ArrayList<>((ArrayList<String>) intent.getSerializableExtra("type"));
        if (fileType.contains(".mp3")) {
            retrieveMusicReceiver = new Intent(ACTION_RETRIEVE_MUSIC);
        } else if (fileType.contains(".mp4")) {
            retrieveMusicReceiver = new Intent(ACTION_RETRIEVE_VIDEO);
        } else if (fileType.contains(".jpg")) {
            retrieveMusicReceiver = new Intent(ACTION_RETRIEVE_IMAGE);
        }
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        retrieveMusicReference(Environment.getExternalStorageDirectory());

        if (fileType.contains(".mp3")) {
            ArrayList<Song> music = new ArrayList<>();
            for (String path : musics) {
                music.add(new Song(path, Song.NORMAL_STATE, getApplicationContext()));
            }
            retrieveMusicReceiver.putExtra("musics", music);
            broadcastManager.sendBroadcast(retrieveMusicReceiver);
        }
        if (fileType.contains(".mp4")) {
            mediaPlayer.recheckVideoList(videos, getApplicationContext());
        }
        if (fileType.contains(".jpg")) {
            mediaPlayer.recheckImageList(images, getApplicationContext());
        }
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
                    } else if (fileType.contains(file1.getAbsolutePath().substring(file1.getAbsolutePath().length() - 4)) && !Objects.requireNonNull(file1.getParent()).contains("record") && file1.length() >= 500000) {
                        String posFix = file1.getAbsolutePath().substring(file1.getAbsolutePath().length() - 4);
                        if (posFix.equals(".mp3")) {
                            musics.add(file1.getAbsolutePath());
                        } else if (posFix.equals(".mp4")) {
                            videos.add(file1.getAbsolutePath());
                        } else if (posFix.equals(".jpg") || posFix.equals(".png") || posFix.equals("jpeg")) {
                            images.add(file1.getAbsolutePath());
                        }
                    }
                }

            }
        }
    }
}
