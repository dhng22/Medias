package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.listener.OnParticularVideoInteractionListener;
import com.example.music.models.Video;
import com.example.music.utils.GlobalListener;


public class FragmentParticularVideo extends Fragment {
    Video video;

    ImageButton btnPlay;
    ImageView imgFullImage;

    GlobalMediaPlayer mediaPlayer;
    OnParticularVideoInteractionListener particularVideoInteractionListener;
    PlayVideoFragment playVideoFragment;
    public FragmentParticularVideo() {
        // Required empty public constructor
    }

    public static FragmentParticularVideo getInstance(Video video) {
        FragmentParticularVideo fragment = new FragmentParticularVideo();
        Bundle args = new Bundle();
        args.putSerializable("video", video);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.video = (Video) getArguments().getSerializable("video");
        }
        mediaPlayer = GlobalMediaPlayer.getInstance();
        playVideoFragment = PlayVideoFragment.getInstance(video.path);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_particular_video, container, false);
        init(view);

        initListener();
        return view;
    }

    private void initListener() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pauseSong(requireContext());
                }
                GlobalListener.VideoFragment.listener.addFragment(playVideoFragment);
            }
        });
    }

    private void init(View view) {
        btnPlay = view.findViewById(R.id.btnPlay);
        imgFullImage = view.findViewById(R.id.imgFullImage);
        imgFullImage.setImageBitmap(video.getFullImage());

        particularVideoInteractionListener = new OnParticularVideoInteractionListener() {
            @Override
            public void btnPlayPerform() {
                btnPlay.performClick();
            }
        };
        GlobalListener.FragmentParticularVideo.listener = particularVideoInteractionListener;
    }

}