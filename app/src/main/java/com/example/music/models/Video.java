package com.example.music.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;

import com.example.music.utils.SongUtils;

import java.io.File;

public class Video implements Comparable<Video>{
    long duration;
    long date;
    public String path;
    public String txtDuration;
    public Bitmap thumbnail;
    public Context context;
    public String videoName;
    private static final MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    public Video(String path, Context context) {
        this.path = path;
        this.context = context;

        duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        videoName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        thumbnail = retriever.getFrameAtTime((duration / 4) * 1000);
        date = new File(path).lastModified();
        txtDuration = SongUtils.getFormattedDuration(duration);
    }

    @Override
    public int compareTo(Video o) {
        if (this.date < o.date) {
            return 1;
        } else if (this.date > o.date) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "videoName='" + videoName + "\n";
    }
}
