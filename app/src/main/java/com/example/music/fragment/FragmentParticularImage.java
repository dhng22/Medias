package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.models.Image;


public class FragmentParticularImage extends Fragment {
    Image image;
    ImageView imgFullImage;
    GlobalMediaPlayer mediaPlayer;
    public FragmentParticularImage() {
        // Required empty public constructor
    }

    public static FragmentParticularImage getInstance(Image image) {
        FragmentParticularImage fragment = new FragmentParticularImage();
        Bundle args = new Bundle();
        args.putSerializable("image", image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.image = (Image) getArguments().getSerializable("image");
        }
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_particular_image, container, false);
        init(view);

        initListener();
        return view;
    }

    private void initListener() {
    }

    private void init(View view) {
        imgFullImage = view.findViewById(R.id.imgFullImage);
        imgFullImage.setImageBitmap(image.getFullImage());
    }
}