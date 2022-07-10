package com.example.music.models;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.music.R;

import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    private String listName;
    private ArrayList<Song> songList;
    private Bitmap listImage;


    public Playlist(String listName, ArrayList<Song> songList) {
        this.listName = listName;
        this.songList = songList;
        listImage = BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.gradient);
    }

    public String getListName() {
        return listName;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public Bitmap getListImage() {

        return listImage;
    }
    public void setListName(String listName) {
        this.listName = listName;
    }
}
