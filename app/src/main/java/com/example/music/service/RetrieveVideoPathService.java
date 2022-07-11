package com.example.music.service;

import static com.example.music.database.VideoDb.TABLE_NAME;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.music.database.VideoDb;

public class RetrieveVideoPathService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    VideoDb db;
    public RetrieveVideoPathService(String name) {
        super(name);
    }
    public RetrieveVideoPathService() {
        super("RETRIEVE_VID_PATH");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db = new VideoDb(this, "videos.db", null, 1);
        db.execSQL("CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "'(path VARCHAR(300), thumbnail BLOB, fullImage BLOB, duration VARCHAR(20))");
        db.initVideoPath();
    }
}
