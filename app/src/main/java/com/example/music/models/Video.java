package com.example.music.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.example.music.utils.SongUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.LinkOption;

public class Video implements Comparable<Video>, Serializable {
    private long duration;
    long date;
    public String path;
    private String txtDuration;
    private Bitmap thumbnail, fullImage;
    public String videoName;
    int videoHeight, videoWidth;
    private static final MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    public Video(String path, Bitmap thumbnail,Bitmap fullImage, String txtDuration) throws FileNotFoundException {
        this.path = path;
        File video = new File(path);
        if (!video.exists()) {
            throw new FileNotFoundException("not found");
        }
        this.thumbnail = thumbnail;
        this.fullImage = fullImage;
        this.txtDuration = txtDuration;
        date = video.lastModified();
    }
    public Video(String path) throws FileNotFoundException {
        this.path = path;

        retriever.setDataSource(path);
        File video = new File(path);
        if (!video.exists()) {
            throw new FileNotFoundException("not found");
        }

        videoHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))/10;
        videoWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))/10;
        fullImage = Bitmap.createScaledBitmap(retriever.getFrameAtTime(0), videoWidth * 10, videoHeight * 10, false);
        thumbnail = Bitmap.createScaledBitmap(retriever.getFrameAtTime(0), videoWidth, videoHeight, false);
        duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        txtDuration = SongUtils.getFormattedDuration(duration);

        date = video.lastModified();
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

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public Bitmap getFullImage() {
        return fullImage;
    }

    public String getFormattedDuration() {
        return txtDuration;
    }

    @Override
    public String toString() {
        return "'" + path + "\n";
    }
}
