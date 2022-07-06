package com.example.music.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
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
import com.example.music.MainActivity;
import com.example.music.bottomSheet.SongOptionBottomSheetFrag;
import com.example.music.fragment.MusicFragment;
import com.example.music.listener.OnAdapterInteractionListener;
import com.example.music.service.PlaySongService;
import com.example.music.R;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;

import java.util.ArrayList;

public class SongListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    GlobalMediaPlayer mediaPlayer;
    int layout;
    RecyclerView songRecycler;
    OnAdapterInteractionListener newSongSelectedListener;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Handler handler;
    String searchKey = "";
    SongOptionBottomSheetFrag songOptionBottomSheetFrag;
    Fragment parentFragment;
    public SongListAdapter(Context context, int layout, RecyclerView songRecycler, Fragment parentFragment) {
        this.context = context;
        this.layout = layout;
        this.songRecycler = songRecycler;
        this.parentFragment = parentFragment;
        mediaPlayer = GlobalMediaPlayer.getInstance();

        sharedPreferences = context.getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        handler = new Handler();

        newSongSelectedListener = new OnAdapterInteractionListener() {
            @Override
            public void onNewSongPlaying(Song oldSong, Song newSong) {
                setBackGroundForOldSong(oldSong);
                setBackGroundForNewSong(newSong);
            }

            @Override
            public void setBackGroundForOldSong(Song oldSong) {
                if (oldSong != null) {
                    RecyclerView.ViewHolder oldSongHolder = songRecycler.findViewHolderForLayoutPosition(mediaPlayer.getVisualSongIndex());
                    if (oldSongHolder != null) {
                        try {
                            ((SongHolder) oldSongHolder).songBackground.setCardBackgroundColor(context.getColor(R.color.white));
                            ((SongHolder) oldSongHolder).imgAlbum.setImageResource(R.mipmap.ic_album);
                        } catch (ClassCastException ignore) {
                        }
                    }
                }
            }

            @Override
            public void setBackGroundForNewSong(Song newSong) {
                if (newSong != null) {
                    RecyclerView.ViewHolder newSongHolder = songRecycler.findViewHolderForLayoutPosition(mediaPlayer.getBaseSongList().indexOf(newSong));
                    if (newSongHolder != null) {
                        ((SongHolder) newSongHolder).songBackground.setCardBackgroundColor(context.getColor(R.color.color_grey));
                            if (mediaPlayer.isPlaying()) {
                                ((SongHolder) newSongHolder).imgAlbum.setImageResource(R.drawable.ic_playing);
                            } else {
                                ((SongHolder) newSongHolder).imgAlbum.setImageResource(R.drawable.ic_pausing);
                            }
                        }
                    }
            }


            @Override
            public void onItemClick(int clickedPos) {
                Song clickedSong = mediaPlayer.getSongAt(clickedPos);
                if (mediaPlayer.getPlayerState() == GlobalMediaPlayer.NULL_STATE || mediaPlayer.getCurrentSong().id != clickedSong.id) {
                    if (MainActivity.musicController.getVisibility() != View.VISIBLE) {
                        MainActivity.turnOnController();
                    }
                    editor.putInt("currentDur", -1);
                    editor.commit();
                    mediaPlayer.resetNavigationSeekBar();
                    mediaPlayer.reset();
                    mediaPlayer.playSong(clickedSong,context);

                    mediaPlayer.setCurrentSong(clickedSong);
                    GlobalListener.MainActivity.listener.openCurrentSongActivity();
                } else {

                    if (parentFragment instanceof MusicFragment) {
                        mediaPlayer.renewPlayingSongList();
                    }

                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pauseSong(context);
                    } else {
                        mediaPlayer.resumeSong(context);
                    }
                }
                GlobalListener.MainActivity.listener.validateFavButton();
            }

            @Override
            public RecyclerView getSongRecycler() {
                return songRecycler;
            }

            @Override
            public void notifySongAt(int index) {
                notifyItemChanged(index);
            }

        };
        GlobalListener.SongListAdapter.listener = newSongSelectedListener;
    }


    class SongHolder extends RecyclerView.ViewHolder {
        CardView songBackground;
        ImageView songImage, imgAlbum;
        TextView txtSongTitle, txtSongDesc, txtSongDur;
        ImageButton btnShowMore;

        public SongHolder(@NonNull View itemView) {
            super(itemView);
            songImage = itemView.findViewById(R.id.songImage);
            txtSongTitle = itemView.findViewById(R.id.txtSongTitle);
            txtSongDesc = itemView.findViewById(R.id.txtSongDesc);
            txtSongDur = itemView.findViewById(R.id.txtSongDur);
            btnShowMore = itemView.findViewById(R.id.btnShowMore);
            songBackground = itemView.findViewById(R.id.songBackground);
            imgAlbum = itemView.findViewById(R.id.imgAlbum);

            itemView.setOnClickListener(v -> newSongSelectedListener.onItemClick(getLayoutPosition()));
            btnShowMore.setOnClickListener(v -> {
                songOptionBottomSheetFrag = SongOptionBottomSheetFrag.getInstance(mediaPlayer.getSongAt(getLayoutPosition()), getLayoutPosition());
                Log.e("TAG", "SongHolder: "+getLayoutPosition());
                songOptionBottomSheetFrag.setStyle(DialogFragment.STYLE_NORMAL,R.style.TransparentDialog);
                GlobalListener.MainActivity.listener.showSongBottomSheetOption(songOptionBottomSheetFrag);
            });
        }
    }


    class EmptyHolder extends RecyclerView.ViewHolder {
        public EmptyHolder(@NonNull View itemView) {
            super(itemView);
            if (getLayoutPosition() == mediaPlayer.getBaseSongList().size() - 1) {
                itemView.setClickable(false);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Song.NORMAL_STATE || viewType == Song.PLAYING_STATE) {
            return new SongHolder(LayoutInflater.from(context).inflate(R.layout.row_song, parent, false));
        }
        return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.list_empty, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == Song.PLAYING_STATE || viewType == Song.NORMAL_STATE) {
            if (viewType == Song.PLAYING_STATE) {
                ((SongHolder) holder).songBackground.setCardBackgroundColor(context.getColor(R.color.color_grey));
                handler.postDelayed(() -> {
                    if (mediaPlayer.isPlaying()) {
                        ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_playing);
                    } else {
                        ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_pausing);
                    }
                }, 0);
            } else {
                ((SongHolder) holder).songBackground.setCardBackgroundColor(context.getColor(R.color.white));
                ((SongHolder) holder).imgAlbum.setImageResource(R.mipmap.ic_album);
            }
            Song song = mediaPlayer.getBaseSongList().get(position);

            ((SongHolder) holder).songImage.setImageBitmap(song.songImage);
            ((SongHolder) holder).txtSongTitle.setText("|  " + song.songName);
            ((SongHolder) holder).txtSongDesc.setText(song.singer + " | " + song.albumName);
            ((SongHolder) holder).txtSongDur.setText(song.txtDuration);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String key) {
        searchKey = key;
        key = key.toLowerCase();
        ArrayList<Song> songList;
        if (key.trim().isEmpty()) {
            mediaPlayer.resetVisualList();
        } else {
            songList = new ArrayList<>();
            ArrayList<Song> baseList = mediaPlayer.getBaseBaseSongList();

            for (int i = 0; i < baseList.size(); i++) {
                Song song = baseList.get(i);
                if (song.songName.toLowerCase().contains(key)) {
                    songList.add(song);
                }
            }
            mediaPlayer.setVisualSongList(songList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mediaPlayer.getBaseSongList().size() > 0 ? mediaPlayer.getBaseSongList().size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        ArrayList<Song> songList = mediaPlayer.getBaseSongList();
        if (songList.size() > 0) {
            return songList.get(position).getCurrentState();
        }else{
            return Song.EMPTY_LIST;
        }
    }
}
