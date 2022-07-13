package com.example.music.fragment;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.music.R;
import com.example.music.adapter.ImageViewPagerAdapter;



public class ImagePageFragment extends Fragment {
    ViewPager2 imagePager;
    ImageViewPagerAdapter adapter;
    int pos;
    public ImagePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_page, container, false);
        adapter = new ImageViewPagerAdapter(this);
        init(view);
        return view;
    }

    private void init(View view) {
        imagePager = view.findViewById(R.id.imagePager);
        imagePager.setAdapter(adapter);
        imagePager.setCurrentItem(pos,false);
    }

    public void setPage(int pos) {
        this.pos = pos;
    }
}