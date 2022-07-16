package com.example.music.service;

import static com.example.music.database.VideoDb.TABLE_NAME;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import androidx.annotation.Nullable;

import com.example.music.GlobalMediaPlayer;
import com.example.music.database.VideoDb;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RetrieveVideoService extends IntentService {
    ArrayList<String> videos;

    GlobalMediaPlayer mediaPlayer;
    VideoDb db;
    public RetrieveVideoService(String name) {
        super(name);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        videos = new ArrayList<>();
    }
    public RetrieveVideoService() {
        super("VIDEO_RETRIEVE");
        mediaPlayer = GlobalMediaPlayer.getInstance();
        videos = new ArrayList<>();
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db = new VideoDb(this, "videos.db", null, 1);
        db.execSQL("CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "'(path VARCHAR(300), thumbnail BLOB, fullImage BLOB, duration VARCHAR(20))");
        db.initVideoFromSQL(getApplicationContext());
        retrieveVideoReference(Environment.getExternalStorageDirectory());

        mediaPlayer.recheckVideoList(videos, getApplicationContext());
    }

    private void retrieveVideoReference(File file) {
        if (file.isDirectory()) {
            File[] innerFile = file.listFiles();
            if (innerFile != null) {
                for (File file1 : innerFile) {
                    String name = file1.getName().toLowerCase();
                    if (file1.isDirectory()) {
                        if (!name.contains("sys")
                                && !name.contains("droid")
                                && !name.contains(".")
                                && !name.contains("ui")
                                && !name.contains("vendor")
                                && !name.contains("back")
                                && !name.contains("record")
                        ) {
                            retrieveVideoReference(file1);
                        }
                    } else if (file1.getAbsolutePath().contains(".mp4") && !Objects.requireNonNull(file1.getParent()).contains("record") && file1.length() >= 500000) {
                        videos.add(file1.getAbsolutePath());
                    }
                }

            }
        }
    }
}
