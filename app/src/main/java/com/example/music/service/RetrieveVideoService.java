package com.example.music.service;

import static com.example.music.database.VideoDb.TABLE_NAME;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.music.GlobalMediaPlayer;
import com.example.music.database.VideoDb;

public class RetrieveVideoService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    GlobalMediaPlayer mediaPlayer;
    VideoDb db;
    public RetrieveVideoService(String name) {
        super(name);
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }
    public RetrieveVideoService() {
        super("VIDEO_RETRIEVE");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        db = new VideoDb(this, "videos.db", null, 1);
        db.execSQL("CREATE TABLE IF NOT EXISTS '" + TABLE_NAME + "'(path VARCHAR(300), thumbnail BLOB, fullImage BLOB, duration VARCHAR(20))");
        db.initVideoFromSQL(getApplicationContext());
    }
}
