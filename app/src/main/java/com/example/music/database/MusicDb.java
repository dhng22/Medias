package com.example.music.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music.GlobalMediaPlayer;
import com.example.music.adapter.SongListAdapter;
import com.example.music.adapter.SongPlaylistAdapter;
import com.example.music.fragment.FavSongFragment;
import com.example.music.fragment.RecentSongFragment;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;

import java.io.File;
import java.util.ArrayList;

public class MusicDb extends SQLiteOpenHelper {
    GlobalMediaPlayer mediaPlayer;
    public MusicDb(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mediaPlayer = GlobalMediaPlayer.getInstance();
    }

    public Cursor getData(String tableName) {
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery("SELECT * FROM '" + tableName + "'", null);
    }

    public ArrayList<String> getTablesName() {
        SQLiteDatabase database = getReadableDatabase();
        Cursor tables = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name != 'android_metadata' AND name != 'sqlite_sequence'", null);
        ArrayList<String> tableNames = new ArrayList<>();
        while (tables.moveToNext()) {
            tableNames.add(tables.getString(0));
        }
        tables.close();
        return tableNames;
    }

    public void updateSongFromFav(Song song, String pathToUpdate) {
        MusicDb musicDb = new MusicDb(song.context, "favSong.db", null, 1);
        musicDb.queryData("UPDATE '" + FavSongFragment.TABLE_NAME + "' SET name = '" + pathToUpdate + "' WHERE name = '" + song.path + "'");
    }
    public void addFavoriteSongToTable(Song song, String tableName, Context context) {
        ArrayList<Song> favList =mediaPlayer.getFavSongList();

        if (favList.containsAll(mediaPlayer.getPlayingSongList())) {
            favList.add(song);
            mediaPlayer.setPlayingSongList(favList,false);
        } else {
            favList.add(song);
        }
        Toast.makeText(context, "Added " + song.songName + " to favorite!", Toast.LENGTH_SHORT).show();

        song.isFavorite = true;
        queryData("INSERT OR IGNORE INTO '" + tableName + "'(id,name) VALUES(null,'" + song.path + "' )");
    }

    public void removeFavSongFromTable(Song song, String tableName, Context context) {
        ArrayList<Song> favList =mediaPlayer.getFavSongList();
        if (favList.containsAll(mediaPlayer.getPlayingSongList())) {
            favList.remove(song);
            if (favList.size() > 0) {
                mediaPlayer.setPlayingSongList(favList,false);
            } else {
                mediaPlayer.resetPlayingSongList();
            }
        } else {
            favList.remove(song);
        }
        Toast.makeText(context, "Removed " + song.songName + " from favorite!", Toast.LENGTH_SHORT).show();
        song.isFavorite = false;
        queryData("DELETE FROM '" + tableName + "' WHERE name = '" + song.path + "'");
    }

    public void createPlayList(String tableName, SongPlaylistAdapter adapter) {
        mediaPlayer.getSongPlayList().add(new Playlist(tableName, new ArrayList<>()));
        adapter.notifyItemInserted(mediaPlayer.getSongPlayList().size() - 1);
        queryData("CREATE TABLE IF NOT EXISTS '" + tableName + "'(id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(200))");
    }

    public void deletePlayList(Playlist playlist, int position) {
        queryData("DROP TABLE '" + playlist.getListName() + "'");
        mediaPlayer.getSongPlayList().remove(mediaPlayer.getPlaylistByName(playlist.getListName()));
        GlobalListener.SongPlaylistAdapter.listener.notifyItemRemove(position);
    }

    public void updateSongPath(Song song,String toSongPath) {
        ArrayList<Song> favList = mediaPlayer.getFavSongList();
        ArrayList<Playlist> playList = mediaPlayer.getSongPlayList();
        if (favList.contains(song)) {
            updateSongFromFav(song,toSongPath);
        }
        for (Playlist p :
                playList) {
            if (p.getSongList().contains(song)) {
                updateSongFromPlaylist(song, toSongPath, p.getListName());
            }
        }
        song.context.getSharedPreferences("appdata", Context.MODE_PRIVATE).edit().putString("currentSong", toSongPath).apply();
    }

    private void updateSongFromPlaylist(Song song, String toSongPath, String listName) {
        queryData("UPDATE '" + listName + "' SET name = '" + toSongPath + "' WHERE name = '" + song.path + "'");
    }

    public void deleteSong(Song song) {
        File file = new File(song.path);
        if (mediaPlayer.getVisualSongList().contains(song)) {
            int index = mediaPlayer.getSongIndexFromVisualList(song);
            mediaPlayer.getBaseSongList().remove(song);
            mediaPlayer.getVisualSongList().remove(song);
            mediaPlayer.getPlayingSongList().remove(song);
            if (mediaPlayer.getPlayingSongList().size() > 0) {
                mediaPlayer.playSong(mediaPlayer.getPlayingSongList().get(0), song.context);
            }
            GlobalListener.SongListAdapter.listener.getAdapter().notifyItemRemoved(index);
        } else {
            mediaPlayer.getBaseSongList().remove(song);
            mediaPlayer.getPlayingSongList().remove(song);
            if (mediaPlayer.getPlayingSongList().size() > 0) {
                mediaPlayer.nextSong(song.context);
            } else {
                GlobalListener.CurrentSongActivity.listener.destroy();
            }
        }
        file.delete();
        mediaPlayer.initFavSong(song.context);
        mediaPlayer.initPlayList(song.context);

    }
    public void renamePlaylist(Playlist playlist, String renameTo, int position) {
        queryData("ALTER TABLE '"+playlist.getListName()+"' RENAME TO '"+renameTo+"'");
        playlist.setListName(renameTo);
        GlobalListener.SongPlaylistAdapter.listener.notifyItemChange(position);
    }
    public void addSongToPlaylist(Song song, Playlist playlist, Context context) {
        if (playlist != null) {
            if (!playlist.getSongList().contains(song)) {
                if (playlist.getSongList().containsAll(mediaPlayer.getPlayingSongList())) {
                    playlist.getSongList().add(song);
                    mediaPlayer.setPlayingSongList(playlist.getSongList(),false);
                } else {
                    playlist.getSongList().add(song);
                }
                Toast.makeText(context, "Added " + song.songName + " to " + playlist.getListName(), Toast.LENGTH_SHORT).show();
                queryData("INSERT OR IGNORE INTO '" + playlist.getListName() + "'(id,name) VALUES(null,'" + song.path + "')");
            } else {
                Toast.makeText(context, "Song's already in " + playlist.getListName(), Toast.LENGTH_SHORT).show();
            }
        }


    }
    public void removeSongFromPlaylist(Song song, Playlist playlist ,SongListAdapter songListAdapter,int songPos, Context context) {
        if (playlist != null) {
            if (GlobalListener.SongPlaylistAdapter.listener != null) {
                GlobalListener.SongPlaylistAdapter.listener.notifyItemChange(mediaPlayer.getSongPlayList().indexOf(playlist));
            }
            if (playlist.getSongList().containsAll(mediaPlayer.getPlayingSongList())) {
                playlist.getSongList().remove(song);
                if (playlist.getSongList().size() > 0) {
                    mediaPlayer.setPlayingSongList(playlist.getSongList(),false);
                } else {
                    mediaPlayer.resetPlayingSongList();
                }
            } else {
                playlist.getSongList().remove(song);
            }
            if (songListAdapter != null) {
                songListAdapter.notifyItemRemoved(songPos);
            }
            if (context != null) {
                Toast.makeText(context, "Removed " + song.songName + " from " + playlist.getListName(), Toast.LENGTH_SHORT).show();
            }
            queryData("DELETE FROM '" + playlist.getListName() + "' WHERE name ='" + song.path + "'");
        }
    }

    public void addSongToRecent(Song song, Fragment parentCall) {
        ArrayList<Song> recentList = mediaPlayer.getRecentSongList(song.context);
        if (recentList.size() >= 20) {
            Song lastSong = recentList.get(recentList.size() - 1);
            recentList.remove(lastSong);
            removeSongFromRecent(lastSong,parentCall);
        }

        if (recentList.containsAll(mediaPlayer.getVisualSongList())) {
            recentList.add(0,song);
            if (parentCall instanceof RecentSongFragment) {
                GlobalListener.SongListAdapter.listener.notifySongAdded(0);
            }
        } else {
            recentList.add(0,song);
        }
        queryData("INSERT OR IGNORE INTO '" + RecentSongFragment.TABLE_NAME + "' (id,name) VALUES(null,'" + song.path + "')");
    }

    public void removeSongFromRecent(Song song, Fragment parentCall) {
        if (mediaPlayer.getRecentSongList(song.context).containsAll(mediaPlayer.getVisualSongList())) {
            mediaPlayer.getRecentSongList(song.context).remove(song);
            if (mediaPlayer.getVisualSongList().size() > 0) {
                if (parentCall instanceof RecentSongFragment) {
                    GlobalListener.SongListAdapter.listener.notifySongRemoved(mediaPlayer.getVisualSongList().size() - 1);
                }
            }
        } else {
            mediaPlayer.getRecentSongList(song.context).remove(song);
        }
        queryData("DELETE FROM '" + RecentSongFragment.TABLE_NAME + "' WHERE name='" + song.path + "'");
    }
    public void queryData(String sql) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
