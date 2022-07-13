package com.example.music.service;

import static com.example.music.database.ImageDb.TABLE_NAME;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.music.GlobalMediaPlayer;
import com.example.music.database.ImageDb;

public class RetrieveImageService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    GlobalMediaPlayer mediaPlayer;
    ImageDb db;
    public RetrieveImageService(String name) {
        super(name);
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }
    public RetrieveImageService() {
        super("IMAGE_RETRIEVE");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db = new ImageDb(this, "images.db", null, 1);
        db.execSQL("CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "'(path VARCHAR(300), thumbnail BLOB)");
        db.initImageFromSQL(getApplicationContext());
    }
}
