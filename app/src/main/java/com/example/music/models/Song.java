package com.example.music.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import com.example.music.R;

import java.io.File;
import java.io.Serializable;

public class Song implements Serializable, Comparable<Song> {
    public static final int EMPTY_LIST = -1;
    public static final int PLAYING_STATE = 1;
    public static final int NORMAL_STATE = 0;

    public int id;
    long date;
    private int currentState;
    public long duration;
    public boolean isFavorite;

    public Context context;
    public String songName;
    public String albumName;
    public String singer;
    public String path;
    public String txtDuration;
    public Bitmap songImage;
    private static final MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    public Song(String songPath) {
        this.path = songPath;
    }
    public Song(String uri, int songState, Context context) {
        this.currentState = songState;

        isFavorite = false;
        this.context = context;

        path = uri;
        retriever.setDataSource(uri);
        singer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        validateSinger();
        albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        validateAlbumName();
        songName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        validateSongName();

        date = new File(path).lastModified();


        int min = (int) (Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000 / 60);
        int sec = (int) (Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000 % 60);
        duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        txtDuration = String.format("%02d:%02d", min, sec);

        byte[] songImgArr = retriever.getEmbeddedPicture();
        validateSongImage(songImgArr);
    }


    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public int getCurrentState() {
        return currentState;
    }

    private void validateSongName() {
        if (songName == null) {
            String s = new File(path).getName();
            songName = s.substring(0, s.length() - 4);
        }
    }

    private void validateAlbumName() {
        if (albumName == null) {
            albumName = "Unknown Album";
        }
    }

    private void validateSinger() {
        if (singer == null) {
            singer = "Unknown Artist";
        }
    }

    private void validateSongImage(byte[] songImgArr) {
        if (songImgArr != null) {
            songImage = BitmapFactory.decodeByteArray(songImgArr, 0, songImgArr.length);
        } else {
            songImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.gradient);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        return path.equals(song.path);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public int compareTo(Song o) {
        if (this.date < o.date) {
            return 1;
        } else if (this.date > o.date) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return songName + '\n';
    }
}
