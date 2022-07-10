package com.example.music.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.R;
import com.example.music.bottomSheet.EdittextBottomFragment;
import com.example.music.bottomSheet.ParticularPlaylistBottomSheet;
import com.example.music.bottomSheet.PlaylistBottomSheet;
import com.example.music.bottomSheet.PlaylistOptionBottomSheet;
import com.example.music.database.PlaylistDb;
import com.example.music.fragment.PlaylistFragment;
import com.example.music.listener.OnSongPlaylistInteractionListener;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;

import java.io.Serializable;
import java.util.ArrayList;

public class SongPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Serializable {
    public static final int ADD_LIST_TYPE = 9;
    public static final int LIST_TYPE = 10;
    private final PlaylistDb playlistDb;
    Context context;
    int layout;
    Fragment parentCall;
    GlobalMediaPlayer mediaPlayer;
    ImageButton btnMorePlaylist;
    ArrayList<Playlist> playlists;
    RecyclerView recyclerView;
    Song songToAdd;
    OnSongPlaylistInteractionListener songPlaylistInteractionListener;

    public SongPlaylistAdapter(Context context, int layout, Fragment parentCall, RecyclerView recyclerView, Song songToAdd) {
        this.context = context;
        this.layout = layout;
        this.parentCall = parentCall;
        this.recyclerView = recyclerView;
        this.songToAdd = songToAdd;
        mediaPlayer = GlobalMediaPlayer.getInstance();
        playlists = mediaPlayer.getSongPlayList();
        playlistDb = new PlaylistDb(context, "playlistSong.db", null, 1);

        songPlaylistInteractionListener = new OnSongPlaylistInteractionListener() {
            @Override
            public void notifyItemChange(int pos) {
                notifyItemChanged(pos);
            }

            @Override
            public void notifyItemRemove(int pos) {
                notifyItemRemoved(pos);
            }
        };
        GlobalListener.SongPlaylistAdapter.listener = songPlaylistInteractionListener;
    }

    private class AddPlayListHolder extends RecyclerView.ViewHolder {
        CardView btnAddList;

        public AddPlayListHolder(@NonNull View itemView) {
            super(itemView);

            btnAddList = itemView.findViewById(R.id.btnAddList);
            btnAddList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EdittextBottomFragment edittextBottomFragment = EdittextBottomFragment.getInstance(null,null, EdittextBottomFragment.ACTION_ADD_PLAYLIST, SongPlaylistAdapter.this);
                    GlobalListener.MainActivity.listener.showSongBottomSheetOption(edittextBottomFragment, parentCall.getParentFragmentManager());
                }
            });

        }
    }

    private class PlayListHolder extends RecyclerView.ViewHolder {
        ImageView listImage;
        TextView txtListName, txtListSize;
        ImageButton btnPlayThisList;


        public PlayListHolder(@NonNull View itemView) {
            super(itemView);

            listImage = itemView.findViewById(R.id.imgPlaylist);
            txtListName = itemView.findViewById(R.id.txtPlaylistName);
            txtListSize = itemView.findViewById(R.id.txtListSize);
            btnPlayThisList = itemView.findViewById(R.id.btnPlayThisList);
            btnMorePlaylist = itemView.findViewById(R.id.btnMorePlaylist);

            btnPlayThisList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Song> songs = playlists.get(getLayoutPosition()).getSongList();
                    if (songs.size() > 0) {
                        mediaPlayer.setPlayingSongList(songs,true);
                        mediaPlayer.playSong(songs.get(0), context);
                    }
                }
            });
            listImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (parentCall instanceof PlaylistBottomSheet) {
                        playlistDb.addSongToPlaylist(songToAdd, playlists.get(getLayoutPosition()), context);
                        GlobalListener.PlaylistBottomSheet.listener.hideDialog();
                    } else if (parentCall instanceof PlaylistFragment) {
                        ParticularPlaylistBottomSheet playlistBottomSheet = ParticularPlaylistBottomSheet.getInstance(playlists.get(getLayoutPosition()));
                        GlobalListener.MainActivity.listener.showSongBottomSheetOption(playlistBottomSheet, null);
                    }
                }
            });
            btnMorePlaylist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlaylistOptionBottomSheet playlistOptionBottomSheet = PlaylistOptionBottomSheet.getInstance(playlists.get(getLayoutPosition()), getLayoutPosition());
                    GlobalListener.MainActivity.listener.showSongBottomSheetOption(playlistOptionBottomSheet,null);
                }
            });
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ADD_LIST_TYPE) {
            return new AddPlayListHolder(LayoutInflater.from(context).inflate(R.layout.item_playlist_add_dark, parent, false));
        } else if (parentCall instanceof PlaylistBottomSheet) {
            return new PlayListHolder(LayoutInflater.from(context).inflate(R.layout.item_playlist_dark, parent, false));
        }
        return new PlayListHolder(LayoutInflater.from(context).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == LIST_TYPE) {
            Playlist playlist = playlists.get(position);
            ((PlayListHolder) holder).listImage.setImageBitmap(playlist.getListImage());
            ((PlayListHolder) holder).txtListName.setText(playlist.getListName());
            ((PlayListHolder) holder).txtListSize.setText(playlist.getSongList().size() + ((playlist.getSongList().size() > 1) ? " songs" : " song"));
            if (parentCall instanceof PlaylistBottomSheet) {
                ((PlayListHolder) holder).btnPlayThisList.setVisibility(View.GONE);

            }
        }
    }

    @Override
    public int getItemCount() {
        return playlists.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == playlists.size()) {
            return ADD_LIST_TYPE;
        }
        return LIST_TYPE;
    }
}
