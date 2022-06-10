package com.example.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.music.service.PlaySongService;

public class MusicActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra("action", -1);
        Intent service = new Intent(context, PlaySongService.class);
        service.putExtra("action",action);
        context.startService(service);
    }
}
