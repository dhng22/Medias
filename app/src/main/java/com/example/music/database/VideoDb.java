package com.example.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import com.example.music.GlobalMediaPlayer;
import com.example.music.models.Video;
import com.example.music.utils.GlobalListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class VideoDb extends SQLiteOpenHelper {
    GlobalMediaPlayer mediaPlayer;
    public static final String TABLE_NAME = "videos";

    public VideoDb(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    public Cursor getData() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM '" + TABLE_NAME + "'", null);
    }


    public void initVideoFromSQL(Context context) {
        ArrayList<Video> videos = new ArrayList<>();
        mediaPlayer.initVideoList(videos);
        Cursor data = getData();
        while (data.moveToNext()) {
            String path = data.getString(0);
            byte[] img = data.getBlob(1);
            byte[] imgFull = data.getBlob(2);
            String txtDur = data.getString(3);
            Video video;
            try {
                video = new Video(path, BitmapFactory.decodeByteArray(img, 0, img.length), BitmapFactory.decodeByteArray(imgFull, 0, imgFull.length), txtDur);

            } catch (FileNotFoundException e) {
                removeVideoFromTable(path);
                continue;
            }
            videos.add(video);
            if (GlobalListener.VideoAdapter.listener != null) {
                GlobalListener.VideoAdapter.listener.notifyVideoAdded();
            }
        }

    }
    public void initVideoPath() {
        ArrayList<String> videos = new ArrayList<>();
        Cursor data = getData();
        while (data.moveToNext()) {
            String path = data.getString(0);
            videos.add(path);
        }
        mediaPlayer.setBaseVideoListPath(videos);
    }
    public void removeVideoFromTable(String path) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM '" + TABLE_NAME + "' WHERE path = '"+path+"'");
        sqLiteDatabase.close();
    }

    public void addVideoToTable(String path, Bitmap thumbnail, Bitmap fullImage, String txtDur) {
        byte[] thumbnailBlob, fullImageBlob;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        thumbnailBlob = outputStream.toByteArray();
        fullImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        fullImageBlob = outputStream.toByteArray();

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("thumbnail", thumbnailBlob);
        values.put("fullImage", fullImageBlob);
        values.put("duration", txtDur);
        sqLiteDatabase.insertWithOnConflict(TABLE_NAME, null, values,SQLiteDatabase.CONFLICT_IGNORE);
        sqLiteDatabase.close();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void execSQL(String s) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(s);
        database.close();
    }


}
