package com.example.music;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RetrieveMusicService extends IntentService {
    public static final String ACTION_RETRIEVE = "action_retrieve_music";
    ArrayList<String> musics;
    Intent retrieveMusicReceiver;
    LocalBroadcastManager broadcastManager;



    public RetrieveMusicService() {
        super("RETRIEVE_SERVICE");
        musics = new ArrayList<>();
        retrieveMusicReceiver = new Intent(ACTION_RETRIEVE);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        retrieveMusicReference(Environment.getExternalStorageDirectory());
        retrieveMusicReceiver.putExtra("musics", musics);
        broadcastManager.sendBroadcast(retrieveMusicReceiver);
    }

    private void retrieveMusicReference(File file) {
        if (file.isDirectory()) {
            File[] innerFile = file.listFiles();
            if (innerFile != null) {
                for (File file1 : innerFile) {
                    if (file1.isDirectory()) {
                        retrieveMusicReference(file1);
                    } else if (file1.getName().contains(".mp3") && !Objects.requireNonNull(file1.getParent()).contains("record") && file1.length() >= 500000) {
                        musics.add(file1.getAbsolutePath());
                    }
                }

            }
        }
    }
}
