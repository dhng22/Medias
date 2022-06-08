package com.example.music;

import static android.content.Context.MODE_PRIVATE;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int EMPTY_STATE = -1;
    public static final int EMPTY_ITEM = -2;
    public static final int PLAYING_STATE = 1;
    public static final int NORMAL_STATE = 0;
    Context context;
    ArrayList<Song> songList;
    int layout;
    public static int oldSongHolderIndex = -1;
    Song oldSongObj;
    RecyclerView songRecycler;
    OnNewSongSelectedListener newSongSelectedListener;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public SongListAdapter(Context context, ArrayList<Song> songList, int layout, RecyclerView songRecycler) {
        this.context = context;
        this.songList = songList;
        this.layout = layout;
        this.songRecycler = songRecycler;

        sharedPreferences = context.getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        newSongSelectedListener = (oldSongIndex, newSongIndex) -> {
            if (oldSongIndex != -1) {
                Song oldSong = songList.get(oldSongIndex);
                if (oldSong != null) {
                    oldSong.isCurrentItem = false;
                }
                RecyclerView.ViewHolder oldSongHolder = songRecycler.findViewHolderForLayoutPosition(oldSongIndex);
                if (oldSongHolder != null) {
                    ((SongHolder) oldSongHolder).songBackground.setCardBackgroundColor(context.getColor(R.color.white));
                    ((SongHolder) oldSongHolder).imgAlbum.setImageResource(R.mipmap.ic_album);
                }
            }
            if (newSongIndex != -1) {
                Song newSong = songList.get(newSongIndex);
                if (newSong != null) {
                    newSong.isCurrentItem = true;
                }
                RecyclerView.ViewHolder newSongHolder = songRecycler.findViewHolderForLayoutPosition(newSongIndex);
                if (newSongHolder != null) {
                    ((SongHolder) newSongHolder).songBackground.setCardBackgroundColor(context.getColor(R.color.color_grey));
                    if (PlaySongService.mediaPlayer != null) {
                        if (PlaySongService.mediaPlayer.isPlaying()) {
                            ((SongHolder) newSongHolder).imgAlbum.setImageResource(R.drawable.ic_playing);
                        } else {
                            ((SongHolder) newSongHolder).imgAlbum.setImageResource(R.drawable.ic_pausing);
                        }
                    } else {
                        ((SongHolder) newSongHolder).imgAlbum.setImageResource(R.drawable.ic_playing);
                    }
                }
                if (newSong != null) {
                    oldSongObj = newSong;
                    oldSongHolderIndex = newSongIndex;
                }
            }
        };
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

            itemView.setOnClickListener(v -> {
                onItemClick(getLayoutPosition());
                newSongSelectedListener.setBackGroundForNewSong(oldSongHolderIndex, getLayoutPosition());
            });
            btnShowMore.setOnClickListener(v -> {
                //TODO: context menu
            });

            PlaySongService.newSongSelectedListener = newSongSelectedListener;
        }
    }

    private void onItemClick(int layoutPosition) {
        if (oldSongHolderIndex != layoutPosition) {
            editor.putInt("currentDur", -1);
            editor.commit();
            if (PlaySongService.mediaPlayer != null) {
                PlaySongService.mediaPlayer.release();
                PlaySongService.mediaPlayer = null;
            }
            PlaySongService.currentSongIndex = layoutPosition;
            Intent songService = new Intent(context, PlaySongService.class);
            context.startService(songService);
        } else {
            if (PlaySongService.mediaPlayer != null) {
                if (PlaySongService.mediaPlayer.isPlaying()) {
                    PlaySongService.pauseSong(context);
                } else {
                    PlaySongService.startSong(context);
                }
            } else {
                PlaySongService.currentSongIndex = oldSongHolderIndex;
                Intent songService = new Intent(context, PlaySongService.class);
                context.startService(songService);
            }
        }
    }


    class EmptyHolder extends RecyclerView.ViewHolder {
        public EmptyHolder(@NonNull View itemView) {
            super(itemView);
            if (getLayoutPosition() == songList.size() - 1) {
                itemView.setClickable(false);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == EMPTY_ITEM) {
            return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.empty_item_row, parent, false));
        } else if (viewType == NORMAL_STATE || viewType == PLAYING_STATE) {
            return new SongHolder(LayoutInflater.from(context).inflate(R.layout.row_song, parent, false));
        }
        return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.list_empty, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == PLAYING_STATE || getItemViewType(position) == NORMAL_STATE) {
            if (getItemViewType(position) == PLAYING_STATE) {
                ((SongHolder) holder).songBackground.setCardBackgroundColor(context.getColor(R.color.color_grey));
                if (PlaySongService.mediaPlayer != null) {
                    if (PlaySongService.mediaPlayer.isPlaying()) {
                        ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_playing);
                    } else {
                        ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_pausing);
                    }
                } else {
                    if (sharedPreferences.getInt("currentDur",-1)>0) {
                        ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_pausing);
                    } else {
                        ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_playing);
                    }
                }
            } else if (getItemViewType(position) == NORMAL_STATE) {
                ((SongHolder) holder).songBackground.setCardBackgroundColor(context.getColor(R.color.white));
                ((SongHolder) holder).imgAlbum.setImageResource(R.mipmap.ic_album);
            }
            Song song = songList.get(position);
            ((SongHolder) holder).songImage.setImageBitmap(song.songImage);
            ((SongHolder) holder).txtSongTitle.setText("| " + song.songName);
            ((SongHolder) holder).txtSongDesc.setText(song.singer + " | " + song.albumName);
            ((SongHolder) holder).txtSongDur.setText(song.txtDuration);
        }
    }

    @Override
    public int getItemCount() {
        return songList.size() > 0 ? songList.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (songList.size() == 0) {
            return EMPTY_STATE;
        }
        if (songList.get(position).lastSong) {
            return EMPTY_ITEM;
        }
        if (songList.get(position).isCurrentItem) {
            return PLAYING_STATE;
        }
        return NORMAL_STATE;
    }
}
