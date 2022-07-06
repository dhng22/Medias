package com.example.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music.bottomSheet.SongOptionBottomSheetFrag;
import com.example.music.listener.OnCurrentSongActivityInteractionListener;
import com.example.music.models.Song;
import com.example.music.receiver.TimerReceiver;
import com.example.music.service.PlaySongService;
import com.example.music.utils.GlobalListener;
import com.example.music.utils.SongUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.StringTokenizer;

public class CurrentSongActivity extends AppCompatActivity {
    public static final int TIMER_ON = 1;
    public static final int TIMER_OFF = -1;
    GlobalMediaPlayer mediaPlayer;
    ImageButton btnRepMode, btnPrev, btnPlayPause, btnNext, btnTimer, btnFav, btnBack, btnShowMore;
    TextView txtSongTitle, txtSongProgress, txtSongDuration;
    SeekBar seekBarSong;
    Handler handler;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Runnable runnable;
    AlarmManager alarmManager;
    PendingIntent pendingTimer;
    Button btnCancelTimer, btnStartTimer;
    NumberPicker timerPicker;
    OnCurrentSongActivityInteractionListener currentSongActivityInteractionListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_song);

        getWindow().setStatusBarColor(getColor(R.color.sea_foam));
        mapping();
        initVar();
        initListener();
        initCurrentSong();
    }

    private void initCurrentSong() {
        if (mediaPlayer.getCurrentSong()!=null) {
            GlobalListener.CurrentSongActivity.listener.renewCurrentSong();
            GlobalListener.CurrentSongActivity.listener.validateButtons();
        }
    }

    private void initVar() {
        mediaPlayer = GlobalMediaPlayer.getInstance();
        handler = new Handler();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        sharedPreferences = getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Intent intent = new Intent(this, TimerReceiver.class);
        intent.putExtra("action", PlaySongService.ACTION_STOP);
        pendingTimer = PendingIntent.getBroadcast(this, PlaySongService.ACTION_STOP, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initListener() {
        seekBarSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer.getPlayerState() != GlobalMediaPlayer.NULL_STATE) {
                    handler.removeCallbacks(runnable);
                    txtSongProgress.setText(SongUtils.getFormattedDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.post(runnable);
                if (mediaPlayer.getPlayerState() != GlobalMediaPlayer.NULL_STATE) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                    GlobalListener.PlaySongService.listener.reloadNotificationMediaState();
                }
            }
        });
        btnFav.setOnClickListener(v -> {
            if (mediaPlayer.getCurrentSong() != null) {
                SongUtils.onSongFavClicked(mediaPlayer.getCurrentSong(),-1);
            }
        });
        btnPlayPause.setOnClickListener(v -> {
            GlobalListener.PlaySongService.listener.playOrPause(CurrentSongActivity.this);
        });
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.anim_nothing, R.anim.activity_anim_slide_down);
        });
        btnNext.setOnClickListener(v -> mediaPlayer.nextSong(CurrentSongActivity.this));
        btnPrev.setOnClickListener(v -> mediaPlayer.prevSong(CurrentSongActivity.this));
        btnRepMode.setOnClickListener(v -> {
            SongUtils.onRepeatModeChanged();
        });
        btnTimer.setOnClickListener(v -> {
            int mode = sharedPreferences.getInt("timerMode", TIMER_OFF);
            if (mode == TIMER_ON) {
                editor.putInt("timerMode", TIMER_OFF);
                editor.commit();
                alarmManager.cancel(pendingTimer);
                Toast.makeText(this, "Timer off", Toast.LENGTH_SHORT).show();
            } else if (mode == TIMER_OFF) {
                Calendar cal = Calendar.getInstance();
                setAlarm(cal);
            }
            validateTimer();
        });
        btnShowMore.setOnClickListener(v -> {
            SongOptionBottomSheetFrag optionBottomSheetFrag = SongOptionBottomSheetFrag.getInstance(mediaPlayer.getCurrentSong(),-1);
            optionBottomSheetFrag.setStyle(DialogFragment.STYLE_NORMAL,R.style.TransparentDialog);
            optionBottomSheetFrag.show(getSupportFragmentManager(),optionBottomSheetFrag.getTag());
        });
        runnable = new Runnable() {
            @Override
            public void run() {
                txtSongProgress.setText(SongUtils.getFormattedDuration(mediaPlayer.getPlayerState()!=GlobalMediaPlayer.NULL_STATE ? mediaPlayer.getCurrentSongPlayingPosition() : 0));
                seekBarSong.setProgress(mediaPlayer.getCurrentSongPlayingPosition());
                handler.postDelayed(this, 200);
            }
        };
        currentSongActivityInteractionListener = new OnCurrentSongActivityInteractionListener() {
            @Override
            public void setSongSeekBarMax(int max) {
                seekBarSong.setMax(max);
            }

            @Override
            public void setSongSeekBarProgress(int progress) {
                seekBarSong.setProgress(progress);
            }

            @Override
            public void setTxtSongDuration(int duration) {
                txtSongDuration.setText(SongUtils.getFormattedDuration(duration));
            }

            @Override
            public void renewCurrentSong() {
                Song song = mediaPlayer.getCurrentSong();
                txtSongTitle.setText(song.songName);
                seekBarSong.setMax((int) song.duration);
                seekBarSong.setProgress(mediaPlayer.getCurrentSongPlayingPosition());
                validateDuration(song);
            }

            @Override
            public void validateButtons() {
                validateRepMode();
                validateTimer();
                validatePlayPause();
                validateRepMode();
                validateFav();
            }
        };
        GlobalListener.CurrentSongActivity.listener = this.currentSongActivityInteractionListener;
    }

    private void setAlarm(Calendar cal) {
        View view = getLayoutInflater().inflate(R.layout.timer_picker_layout, null);

        btnCancelTimer = view.findViewById(R.id.btnCancel);
        btnStartTimer = view.findViewById(R.id.btnStart);
        timerPicker = view.findViewById(R.id.numberPicker);
        String[] timerVal = getTimerValues();
        timerPicker.setDisplayedValues(timerVal);
        timerPicker.setMinValue(0);
        timerPicker.setMaxValue(timerVal.length-1);
        timerPicker.setWrapSelectorWheel(false);
        timerPicker.setValue(0);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this,R.style.TransparentDialog);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
        bottomSheetDialog.setCancelable(false);


        btnCancelTimer.setOnClickListener(v -> bottomSheetDialog.dismiss());
        btnStartTimer.setOnClickListener(v -> {
            String val =timerPicker.getDisplayedValues()[timerPicker.getValue()];
            int time = Integer.parseInt(new StringTokenizer(val," ", false).nextToken());
            cal.add(Calendar.MINUTE,time);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingTimer);
            Toast.makeText(CurrentSongActivity.this, "Timer on, app's stopping in "+time+" minutes", Toast.LENGTH_SHORT).show();
            editor.putInt("timerMode", TIMER_ON);
            editor.commit();
            bottomSheetDialog.dismiss();
            validateTimer();
        });

    }

    private String[] getTimerValues() {
        return new String[]{"15 minutes", "20 minutes", "30 minutes", "40 minutes", "50 minutes", "60 minutes"};
    }


    private void mapping() {
        btnRepMode = findViewById(R.id.btnReplayMode);
        btnPrev = findViewById(R.id.btnPrev);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnTimer = findViewById(R.id.btnTimer);
        btnFav = findViewById(R.id.btnFav);
        btnBack = findViewById(R.id.btnBack);
        btnShowMore = findViewById(R.id.btnShowMore);
        txtSongTitle = findViewById(R.id.txtSongTitle);
        seekBarSong = findViewById(R.id.seekBarSong);
        txtSongProgress = findViewById(R.id.txtSongProgress);
        txtSongDuration = findViewById(R.id.txtSongDuration);
    }

    private void validateDuration(Song song) {
        txtSongDuration.setText(song.txtDuration);
    }

    private void validateTimer() {
        int mode = sharedPreferences.getInt("timerMode", TIMER_OFF);
        if (mode == TIMER_ON) {
            btnTimer.setImageResource(R.drawable.ic_timer_on);

        }else if (mode == TIMER_OFF) {
            btnTimer.setImageResource(R.drawable.ic_timer_off);
        }
    }

    private void validateRepMode() {
        int repMode = sharedPreferences.getInt("repeatMode", -1);
        if (repMode == GlobalMediaPlayer.MODE_REPEAT_ONE) {
            btnRepMode.setImageResource(R.drawable.ic_rep_one);
        } else if (repMode == GlobalMediaPlayer.MODE_REPEAT_PLAYLIST) {
            btnRepMode.setImageResource(R.drawable.ic_rep_all);
        } else if (repMode == GlobalMediaPlayer.MODE_SHUFFLE) {
            btnRepMode.setImageResource(R.drawable.ic_shuffle);
        }
    }

    private void validatePlayPause() {
        if (mediaPlayer.getPlayerState() != GlobalMediaPlayer.NULL_STATE) {
            if (mediaPlayer.isPlaying()) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play);
            }
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        }
    }

    private void validateFav() {
        if (mediaPlayer.getCurrentSong() != null) {
            Song song = mediaPlayer.getCurrentSong();
            if (song.isFavorite) {
                btnFav.setColorFilter(Color.RED);
            } else {
                btnFav.setColorFilter(getColor(R.color.dark_grey));
            }
        }
    }
    @Override
    public void onBackPressed() {
        btnBack.performClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(() -> txtSongTitle.setSelected(true), 1200);
        handler.postDelayed(runnable, 200);
    }

    @Override
    protected void onPause() {
        super.onPause();
        txtSongTitle.setSelected(false);
        handler.removeCallbacks(runnable);
    }
}