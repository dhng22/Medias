package com.example.music.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.fragment.ImagePageFragment;
import com.example.music.listener.OnImageAdapterInteractionListener;
import com.example.music.models.Image;
import com.example.music.utils.GlobalListener;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int EMPTY_LIST = -1;
    public static final int NORMAL_ITEM = 1;
    OnImageAdapterInteractionListener imageAdapterInteractionListener;
    Context context;
    int layout;
    RecyclerView imageRecycler;
    Fragment parentCall;
    GlobalMediaPlayer mediaPlayer;
    ArrayList<Image> imageList;
    ImagePageFragment pageFragment;
    public ImageAdapter(Context context, int layout, ArrayList<Image> imageList, RecyclerView imageRecycler, Fragment parentCall) {
        pageFragment = new ImagePageFragment();
        this.context = context;
        this.layout = layout;
        this.imageRecycler = imageRecycler;
        this.parentCall = parentCall;

        mediaPlayer = GlobalMediaPlayer.getInstance();
        this.imageList = imageList;

        imageAdapterInteractionListener = new OnImageAdapterInteractionListener() {
            @Override
            public void notifyImageAdded() {
                notifyItemInserted(mediaPlayer.getBaseImageList().size() - 1);
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void notifySort() {
                notifyDataSetChanged();
            }
            @Override
            public void startImagePager(int pos) {
                startImagePage(pos);
            }
        };
        GlobalListener.ImageAdapter.listener = imageAdapterInteractionListener;
    }


    class ImageHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        public ImageHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startImagePage(getLayoutPosition());
                }
            });
        }
    }

    private void startImagePage(int pos) {
        pageFragment.setPage(pos);
        GlobalListener.ImageFragment.listener.addFragment(pageFragment);
    }

    class EmptyHolder extends RecyclerView.ViewHolder {
        public EmptyHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EMPTY_LIST) {
            return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.list_empty, parent, false));
        }
        return new ImageHolder(LayoutInflater.from(context).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == NORMAL_ITEM) {
            ((ImageHolder) holder).imgThumbnail.setImageBitmap(imageList.get(position).getThumbnail());
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size() > 0 ? imageList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mediaPlayer.getBaseImageList().size() == 0) {
            return EMPTY_LIST;
        }
        return NORMAL_ITEM;
    }

    public ArrayList<Image> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<Image> imageList) {
        this.imageList = imageList;
    }
}
