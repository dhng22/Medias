package com.example.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.music.CurrentSongActivity;
import com.example.music.service.PlaySongService;

public class TimerReceiver extends BroadcastReceiver {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = context.getSharedPreferences("appdata", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        int action = intent.getIntExtra("action", -1);
        Intent service = new Intent(context, PlaySongService.class);
        service.putExtra("action",action);
        context.startService(service);

        editor.putInt("timerMode", CurrentSongActivity.TIMER_OFF);
        editor.commit();
    }
}