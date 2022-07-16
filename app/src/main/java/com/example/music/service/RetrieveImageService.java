package com.example.music.service;

import static com.example.music.database.ImageDb.TABLE_NAME;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.music.GlobalMediaPlayer;
import com.example.music.database.ImageDb;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RetrieveImageService extends IntentService {
    ArrayList<String> images;

    GlobalMediaPlayer mediaPlayer;
    ImageDb db;
    public RetrieveImageService(String name) {
        super(name);
        mediaPlayer = GlobalMediaPlayer.getInstance();
        images = new ArrayList<>();
    }
    public RetrieveImageService() {
        super("IMAGE_RETRIEVE");
        mediaPlayer = GlobalMediaPlayer.getInstance();
        images = new ArrayList<>();
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db = new ImageDb(this, "images.db", null, 1);
        db.execSQL("CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "'(path VARCHAR(300), thumbnail BLOB)");
        db.initImageFromSQL(getApplicationContext());
        retrieveImageReference(Environment.getExternalStorageDirectory());

        mediaPlayer.recheckImageList(images, getApplicationContext());
    }
    private void retrieveImageReference(File file) {
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
                            retrieveImageReference(file1);
                        }
                    } else if ((file1.getAbsolutePath().contains(".jpg")||file1.getAbsolutePath().contains(".png")||file1.getAbsolutePath().contains("jpeg")) && !Objects.requireNonNull(file1.getParent()).contains("record") && file1.length() >= 500000) {
                        images.add(file1.getAbsolutePath());
                    }
                }

            }
        }
    }
}
