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

public class Image implements Comparable<Image>, Serializable {
    long date;
    public String path;
    private Bitmap thumbnail, fullImage;
    int imageHeight, imageWidth;

    public Image(String path, Bitmap thumbnail) throws FileNotFoundException {
        this.path = path;
        File image = new File(path);
        if (!image.exists()) {
            throw new FileNotFoundException("not found");
        }
        this.thumbnail = thumbnail;
        this.fullImage = BitmapFactory.decodeFile(path);
        date = image.lastModified();
        Log.e("TAG", "Image: created first");
    }
    public Image(String path) throws FileNotFoundException {
        this.path = path;

        File image = new File(path);
        if (!image.exists()) {
            throw new FileNotFoundException("not found");
        }

        Bitmap thumb = BitmapFactory.decodeFile(path);
        imageHeight = thumb.getHeight()/10;
        imageWidth = thumb.getWidth()/10;
        fullImage = BitmapFactory.decodeFile(path);
        thumbnail = Bitmap.createScaledBitmap(thumb, imageWidth, imageHeight, false);

        date = image.lastModified();
        Log.e("TAG", "Image: created second");
    }

    @Override
    public int compareTo(Image o) {
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

    @Override
    public String toString() {
        return "'" + path + "\n";
    }
}
