package com.example.music.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.music.R;
import com.example.music.listener.OnImageFragmentInteractionListener;
import com.example.music.utils.GlobalListener;

public class ImageFragment extends Fragment {
    FragmentTransaction transaction;
    LocalImageFragment localImageFrag;
    FragmentContainerView containerView;
    OnImageFragmentInteractionListener imageFragmentInteractionListener;
    public ImageFragment() {
        // Required empty public constructor
        localImageFrag = new LocalImageFragment();
    }

    public ImageFragment(LocalImageFragment localImageFrag) {
        this.localImageFrag = localImageFrag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        transaction = getChildFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
        transaction.replace(R.id.imageMainFrag, localImageFrag).commit();

        imageFragmentInteractionListener = new OnImageFragmentInteractionListener() {
            @Override
            public void addFragment(Fragment fragment) {
                addNewFrag(fragment);
            }

            @Override
            public void addFragmentWithSharedElement(View itemView, ImagePageFragment pageFragment) {
                addNewFragWithSharedElement(itemView,pageFragment);
            }

            @Override
            public FragmentManager getFragmentManager() {
                return getChildFragmentManager();
            }

        };
        GlobalListener.ImageFragment.listener = imageFragmentInteractionListener;
    }

    private void addNewFrag(Fragment fragment) {

        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.imageMainFrag, fragment).addToBackStack(null).commit();

    }
    private void addNewFragWithSharedElement(View itemView, Fragment fragment) {
        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.imageMainFrag, fragment).addToBackStack(null).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_image, container, false);
        init(v);
        return v;
    }

    private void init(View v) {
        containerView = v.findViewById(R.id.imageMainFrag);
    }
}