package com.example.music;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import com.example.music.service.RetrieveMusicService;

import java.util.ArrayList;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ArrayList<String> musics;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Intent retrieveMusicService;
    LocalBroadcastManager broadcastManager;
    BroadcastReceiver musicReceiver;
    boolean permissionAsked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.darker_grey));
        setContentView(R.layout.activity_splash);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        retrieveMusic();

    }

    private void retrieveMusic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION) == PackageManager.PERMISSION_DENIED &&!permissionAsked) {
                AlertDialog permissionDialog = new AlertDialog.Builder(this).setMessage("Allow Music to manage external storage")
                        .setPositiveButton("Allow", (dialogInterface, i) -> {
                            permissionAsked = true;
                            editor.putBoolean("permissionAsked", true);
                            askForPermission();
                        })
                        .setNegativeButton("Deny", (dialogInterface, i) -> {
                            permissionAsked = true;
                            editor.putBoolean("permissionAsked", true);
                            dialogInterface.cancel();
                            startService(retrieveMusicService);
                        }).create();
                permissionDialog.setCanceledOnTouchOutside(false);
                permissionDialog.show();
            } else if (ContextCompat.checkSelfPermission(getApplicationContext(), Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                startService(retrieveMusicService);
            }
        } else {
            startService(retrieveMusicService);
        }
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    startService(retrieveMusicService);
                }
            });
    private void askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                resultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                resultLauncher.launch(intent);
            }
        }
    }



    private void init() {

        sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        retrieveMusicService = new Intent(this, RetrieveMusicService.class);
        retrieveMusicService.putExtra("musics", musics);

        broadcastManager = LocalBroadcastManager.getInstance(this);
        musicReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                musics = (ArrayList<String>) intent.getSerializableExtra("musics");
                Intent intent1 = new Intent(SplashActivity.this, MainActivity.class);
                intent1.putExtra("musics", musics);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                broadcastManager.unregisterReceiver(this);
                startActivity(intent1);
                overridePendingTransition(R.anim.anim_nothing, R.anim.anim_fade_out);
            }
        };
        broadcastManager.registerReceiver(musicReceiver, new IntentFilter(RetrieveMusicService.ACTION_RETRIEVE));

        permissionAsked = sharedPreferences.getBoolean("permissionAsked", false);
    }
}