package com.example.music.service;

import static com.example.music.database.ImageDb.TABLE_NAME;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.music.database.ImageDb;


public class RetrieveImagePathService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    ImageDb db;
    public RetrieveImagePathService(String name) {
        super(name);
    }
    public RetrieveImagePathService() {
        super("RETRIEVE_IMG_PATH");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db = new ImageDb(this, "images.db", null, 1);
        db.execSQL("CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "'(path VARCHAR(300), thumbnail BLOB)");
        db.initImagePath();
    }
}
