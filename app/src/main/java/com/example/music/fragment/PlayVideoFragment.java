package com.example.music.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.utils.GlobalListener;

public class PlayVideoFragment extends Fragment {

    // TODO: Rename and change types of parameters
    private String path;
    VideoView videoView;
    MediaSession sessionCompat;
    MediaController controller;
    ImageButton btnFullScreen;
    GlobalMediaPlayer mediaPlayer;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public PlayVideoFragment() {
        // Required empty public constructor
    }

    public static PlayVideoFragment getInstance(String path) {
        PlayVideoFragment fragment = new PlayVideoFragment();
        Bundle args = new Bundle();
        args.putString("path",path);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString("path");
        }
        mediaPlayer = GlobalMediaPlayer.getInstance();
        sessionCompat = new MediaSession(requireContext(), "video_session");
        controller = new MediaController(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_play_video, container, false);
        init(v);
        return v;
    }

    private void init(View view) {
        sharedPreferences = requireContext().getSharedPreferences("appdata", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        btnFullScreen = view.findViewById(R.id.btnFullScreen);
        videoView = view.findViewById(R.id.videoPlayer);
        videoView.setVideoPath(path);
        GlobalListener.MainActivity.listener.hideNavigationBar();
        videoView.setVisibility(View.VISIBLE);
        videoView.setMediaController(controller);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                GlobalListener.MainActivity.listener.doBackPress();
            }
        });
        videoView.start();
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putLong("duration", videoView.getCurrentPosition());
                editor.commit();
                if (requireActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE) {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                    btnFullScreen.setImageResource(R.drawable.ic_full_screen);
                } else {
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                    btnFullScreen.setImageResource(R.drawable.ic_normal_screen);

                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GlobalListener.VideoAdapter.listener.startVideoPager(mediaPlayer.getVideoIndexByPath(path));
                        GlobalListener.FragmentParticularVideo.listener.btnPlayPerform();
                        videoView.seekTo((int) sharedPreferences.getLong("duration", 0));
                        editor.putLong("duration", 0);
                        editor.commit();
                    }
                }, 150);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sharedPreferences.getLong("duration", 0) == 0) {
            if (videoView.isPlaying()) {
                videoView.stopPlayback();
                videoView.suspend();
                btnFullScreen.setImageResource(R.drawable.ic_full_screen);
            }
            videoView.setVisibility(View.GONE);
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);

        }
        GlobalListener.MainActivity.listener.showNavigationBar();
    }
}