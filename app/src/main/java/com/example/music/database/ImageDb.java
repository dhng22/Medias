package com.example.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.music.GlobalMediaPlayer;
import com.example.music.models.Image;
import com.example.music.utils.GlobalListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ImageDb extends SQLiteOpenHelper {
    GlobalMediaPlayer mediaPlayer;
    public static final String TABLE_NAME = "images";

    public ImageDb(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    public Cursor getData() {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        return sqLiteDatabase.rawQuery("SELECT * FROM '" + TABLE_NAME + "'", null);
    }


    public void initImageFromSQL(Context context) {
        ArrayList<Image> images = new ArrayList<>();
        mediaPlayer.initImageList(images);
        synchronized (mediaPlayer.getBaseImageList()) {
            Handler handler = new Handler(Looper.getMainLooper());
            Cursor data = getData();
            while (data.moveToNext()) {
                String path = data.getString(0);
                byte[] img = data.getBlob(1);
                Image image;
                try {
                    image = new Image(path, BitmapFactory.decodeByteArray(img, 0, img.length));
                } catch (FileNotFoundException e) {
                    removeImageFromTable(path);
                    continue;
                }
                images.add(image);
                if (GlobalListener.ImageAdapter.listener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            GlobalListener.ImageAdapter.listener.notifyImageAdded();
                        }
                    });
                }
            }
        }
    }

    public void initImagePath() {
        ArrayList<String> images = new ArrayList<>();
        Cursor data = getData();
        while (data.moveToNext()) {
            String path = data.getString(0);
            images.add(path);
        }
        mediaPlayer.setBaseImageListPath(images);
    }
    public void removeImageFromTable(String path) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM '" + TABLE_NAME + "' WHERE path = '" + path);
        sqLiteDatabase.close();
    }

    public void addImageToTable(String path, Bitmap thumbnail, Bitmap fullImage) {
        byte[] thumbnailBlob;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        thumbnailBlob = outputStream.toByteArray();
        fullImage.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);


        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("thumbnail", thumbnailBlob);
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
