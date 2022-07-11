package com.example.music.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.music.listener.OnVideoAdapterInteractionListener;
import com.example.music.models.Video;
import com.example.music.utils.GlobalListener;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int EMPTY_LIST = -1;
    public static final int NORMAL_ITEM = 1;
    OnVideoAdapterInteractionListener videoAdapterInteractionListener;
    Context context;
    int layout;
    RecyclerView videoRecycler;
    Fragment parentCall;
    GlobalMediaPlayer mediaPlayer;
    ArrayList<Video> videoList;

    public VideoAdapter(Context context, int layout,ArrayList<Video> videoList, RecyclerView videoRecycler, Fragment parentCall) {
        this.context = context;
        this.layout = layout;
        this.videoRecycler = videoRecycler;
        this.parentCall = parentCall;

        mediaPlayer = GlobalMediaPlayer.getInstance();
        this.videoList = videoList;

        videoAdapterInteractionListener = new OnVideoAdapterInteractionListener() {
            @Override
            public void notifyVideoAdded() {
                notifyItemInserted(videoList.size() - 1);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void notifySort() {
                notifyDataSetChanged();
            }
        };
        GlobalListener.VideoAdapter.listener = videoAdapterInteractionListener;
    }


    class VideoHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView txtVidDur;
        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            txtVidDur = itemView.findViewById(R.id.txtVidDur);
        }
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
        return new VideoHolder(LayoutInflater.from(context).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == NORMAL_ITEM) {
            ((VideoHolder) holder).imgThumbnail.setImageBitmap(videoList.get(position).getThumbnail());
            ((VideoHolder) holder).txtVidDur.setText(" "+videoList.get(position).getFormattedDuration()+" ");
        }
    }

    @Override
    public int getItemCount() {
        return videoList.size() > 0 ? videoList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (videoList.size() == 0) {
            return EMPTY_LIST;
        }
        return NORMAL_ITEM;
    }

    public ArrayList<Video> getVideoList() {
        return videoList;
    }

    public void setVideoList(ArrayList<Video> videoList) {
        this.videoList = videoList;
    }
}
