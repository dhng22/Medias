package com.example.music;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

public class MyApplication extends Application {

    public static final String PLAYING_SONG_CHANNEL_ID = "channel_id_playing";
    NotificationManagerCompat managerCompat;
    @Override
    public void onCreate() {
        super.onCreate();
        managerCompat = NotificationManagerCompat.from(this);
        if (managerCompat.getNotificationChannel(PLAYING_SONG_CHANNEL_ID) == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channelRetrieveSong = new NotificationChannel(PLAYING_SONG_CHANNEL_ID, "Playing Song Channel", NotificationManager.IMPORTANCE_LOW);
                channelRetrieveSong.setSound(null,null);
                managerCompat.createNotificationChannel(channelRetrieveSong);
            }
        }
    }
}
