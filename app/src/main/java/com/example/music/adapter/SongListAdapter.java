package com.example.music.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.GlobalMediaPlayer;
import com.example.music.MainActivity;
import com.example.music.bottomSheet.CurrentPlayingListBottomSheet;
import com.example.music.bottomSheet.ParticularPlaylistBottomSheet;
import com.example.music.bottomSheet.SongOptionBottomSheetFrag;
import com.example.music.fragment.FavSongFragment;
import com.example.music.fragment.LocalSongFragment;
import com.example.music.fragment.RecentSongFragment;
import com.example.music.listener.OnAdapterInteractionListener;
import com.example.music.R;
import com.example.music.models.Playlist;
import com.example.music.models.Song;
import com.example.music.utils.GlobalListener;

import java.util.ArrayList;

public class SongListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
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
    Playlist playlist;
    public SongListAdapter(Context context, int layout, RecyclerView songRecycler, Fragment parentFragment, @Nullable Playlist playlist) {
        this.context = context;
        this.layout = layout;
        this.songRecycler = songRecycler;
        this.parentFragment = parentFragment;
        this.playlist = playlist;
        mediaPlayer = GlobalMediaPlayer.getInstance();

        sharedPreferences = context.getSharedPreferences("appdata", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        handler = new Handler();

        newSongSelectedListener = new OnAdapterInteractionListener() {
            @Override
            public void onNewSongPlaying(Song oldSong, Song newSong) {
                if (!(parentFragment instanceof RecentSongFragment)) {
                    setBackGroundForOldSong(oldSong);
                    setBackGroundForNewSong(newSong);
                }
            }

            @Override
            public void setBackGroundForOldSong(Song oldSong) {
                if (oldSong != null) {
                    RecyclerView.ViewHolder oldSongHolder = songRecycler.findViewHolderForLayoutPosition(mediaPlayer.getVisualSongIndex());
                    if (oldSongHolder != null) {
                        try {
                            ((SongHolder) oldSongHolder).btnShowMore.setColorFilter(context.getColor(R.color.color_grey));
                            ((SongHolder) oldSongHolder).imgAlbum.setColorFilter(context.getColor(android.R.color.transparent));
                            ((SongHolder) oldSongHolder).imgAlbum.setImageResource(R.drawable.ic_album);
                            ((SongHolder) oldSongHolder).songBackground.setCardBackgroundColor(context.getColor(R.color.darker_grey));
                            ((SongHolder) oldSongHolder).txtSongTitle.setTextColor(context.getColor(R.color.color_grey));
                            ((SongHolder) oldSongHolder).txtSongDesc.setTextColor(context.getColor(R.color.color_grey));

                            if (!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                                ((SongHolder) oldSongHolder).txtSongDur.setTextColor(context.getColor(R.color.color_grey));
                            }else{
                                ((SongHolder) oldSongHolder).txtSongDesc.setText("");

                            }
                        } catch (ClassCastException ignore) {
                        }
                    }
                }
            }

            @Override
            public void setBackGroundForNewSong(Song newSong) {
                if (newSong != null) {
                    RecyclerView.ViewHolder newSongHolder = songRecycler.findViewHolderForLayoutPosition(mediaPlayer.getVisualSongList().indexOf(newSong));
                    if (newSongHolder != null) {
                        ((SongHolder) newSongHolder).btnShowMore.setColorFilter(context.getColor(R.color.darker_grey));
                        ((SongHolder) newSongHolder).imgAlbum.setColorFilter(context.getColor(R.color.deep_aqua));
                        ((SongHolder) newSongHolder).songBackground.setCardBackgroundColor(context.getColor(R.color.color_grey));
                        ((SongHolder) newSongHolder).txtSongTitle.setTextColor(context.getColor(R.color.darker_grey));
                        ((SongHolder) newSongHolder).txtSongDesc.setTextColor(context.getColor(R.color.darker_grey));

                        if (!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                            ((SongHolder) newSongHolder).txtSongDur.setTextColor(context.getColor(R.color.darker_grey));
                        }
                        if (mediaPlayer.isPlaying()) {
                            if (parentFragment instanceof CurrentPlayingListBottomSheet) {
                                ((SongHolder) newSongHolder).txtSongDesc.setText(" Playing");
                            }
                            ((SongHolder) newSongHolder).imgAlbum.setImageResource(R.drawable.ic_playing);
                        } else {
                            if (parentFragment instanceof CurrentPlayingListBottomSheet) {
                                ((SongHolder) newSongHolder).txtSongDesc.setText(" Paused");
                            }
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
                    if (!(parentFragment instanceof ParticularPlaylistBottomSheet) && !(parentFragment instanceof RecentSongFragment) &&!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                        GlobalListener.MainActivity.listener.openCurrentSongActivity();
                    }

                    if (parentFragment instanceof LocalSongFragment) {
                        if (!mediaPlayer.getPlayingSongList().containsAll(mediaPlayer.getVisualSongList())) {
                            mediaPlayer.renewPlayingSongList();
                        }
                    }
                    if (parentFragment instanceof FavSongFragment) {
                        mediaPlayer.setPlayingSongList(mediaPlayer.getFavSongList(),true);
                    }

                    if (parentFragment instanceof CurrentPlayingListBottomSheet) {
                        GlobalListener.CurrentSongActivity.listener.renewCurrentSong();
                    }
                } else {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pauseSong(context);
                    } else {
                        mediaPlayer.resumeSong(context);
                    }
                }
                GlobalListener.MainActivity.listener.validateFavButton();
                if (GlobalListener.CurrentSongActivity.listener != null) {
                    GlobalListener.CurrentSongActivity.listener.renewCurrentSong();
                }
            }

            @Override
            public RecyclerView getSongRecycler() {
                return songRecycler;
            }

            @Override
            public void notifySongAt(int index) {
                notifyItemChanged(index);
            }

            @Override
            public void notifySongAdded(int index) {
                notifyItemInserted(index);
            }

            @Override
            public SongListAdapter getAdapter() {
                return SongListAdapter.this;
            }

            @Override
            public void notifySongRemoved(int index) {
                notifyItemRemoved(index);
            }

            @Override
            public Fragment getParentFragment() {
                return parentFragment;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void notifyDataSet() {
                notifyDataSetChanged();
            }

        };
        GlobalListener.SongListAdapter.listener = newSongSelectedListener;
    }


    class SongHolder extends RecyclerView.ViewHolder {
        CardView songBackground;
        ImageView songImage, imgAlbum;
        TextView txtSongTitle, txtSongDesc, txtSongDur;
        ImageButton btnShowMore;

        @SuppressLint("ClickableViewAccessibility")
        public SongHolder(@NonNull View itemView) {
            super(itemView);
            songImage = itemView.findViewById(R.id.songImage);
            txtSongTitle = itemView.findViewById(R.id.txtSongTitle);
            txtSongDesc = itemView.findViewById(R.id.txtSongDesc);
            if (!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                txtSongDur = itemView.findViewById(R.id.txtSongDur);
            }
            btnShowMore = itemView.findViewById(R.id.btnShowMore);
            songBackground = itemView.findViewById(R.id.songBackground);
            imgAlbum = itemView.findViewById(R.id.imgAlbum);

            itemView.setOnClickListener(v -> newSongSelectedListener.onItemClick(getLayoutPosition()));
            if (!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                btnShowMore.setOnClickListener(v -> {
                    songOptionBottomSheetFrag = SongOptionBottomSheetFrag.getInstance(mediaPlayer.getSongAt(getLayoutPosition()), playlist, getLayoutPosition(), parentFragment);
                    GlobalListener.MainActivity.listener.showSongBottomSheetOption(songOptionBottomSheetFrag, null);
                });
            } else {
                btnShowMore.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        GlobalListener.CurrentPlayingListBottomSheet.listener.onHolderDrag(songRecycler.findViewHolderForAdapterPosition(getAdapterPosition()));
                    }
                    return false;
                });
            }
        }
    }


    class EmptyHolder extends RecyclerView.ViewHolder {
        public EmptyHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Song.NORMAL_STATE || viewType == Song.PLAYING_STATE) {
            return new SongHolder(LayoutInflater.from(context).inflate(layout, parent, false));
        }
        return new EmptyHolder(LayoutInflater.from(context).inflate(R.layout.list_empty, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == Song.PLAYING_STATE || viewType == Song.NORMAL_STATE) {
            if (!(parentFragment instanceof RecentSongFragment)) {
                if (viewType == Song.PLAYING_STATE) {
                    ((SongHolder) holder).songBackground.setCardBackgroundColor(context.getColor(R.color.color_grey));
                    ((SongHolder) holder).txtSongTitle.setTextColor(context.getColor(R.color.darker_grey));
                    ((SongHolder) holder).btnShowMore.setColorFilter(context.getColor(R.color.darker_grey));
                    ((SongHolder) holder).imgAlbum.setColorFilter(context.getColor(R.color.deep_aqua));
                    ((SongHolder) holder).txtSongDesc.setTextColor(context.getColor(R.color.darker_grey));

                    if (!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                        ((SongHolder) holder).txtSongDur.setTextColor(context.getColor(R.color.darker_grey));
                    }
                    handler.postDelayed(() -> {
                        if (mediaPlayer.isPlaying()) {
                            if (parentFragment instanceof CurrentPlayingListBottomSheet) {
                                ((SongHolder) holder).txtSongDesc.setText(" Playing");
                            }
                            ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_playing);
                        } else {
                            if (parentFragment instanceof CurrentPlayingListBottomSheet) {
                                ((SongHolder) holder).txtSongDesc.setText(" Paused");
                            }
                            ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_pausing);
                        }
                    }, 0);
                } else {
                    ((SongHolder) holder).songBackground.setCardBackgroundColor(context.getColor(R.color.darker_grey));
                    ((SongHolder) holder).imgAlbum.setImageResource(R.drawable.ic_album);

                    ((SongHolder) holder).txtSongTitle.setTextColor(context.getColor(R.color.color_grey));
                    ((SongHolder) holder).txtSongDesc.setTextColor(context.getColor(R.color.color_grey));
                    ((SongHolder) holder).btnShowMore.setColorFilter(context.getColor(R.color.color_grey));
                    ((SongHolder) holder).imgAlbum.setColorFilter(context.getColor(android.R.color.transparent));

                    if (!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                        ((SongHolder) holder).txtSongDur.setTextColor(context.getColor(R.color.color_grey));
                    } else {
                        ((SongHolder) holder).txtSongDesc.setText("");
                    }
                }
            }
            Song song = mediaPlayer.getVisualSongList().get(position);

            ((SongHolder) holder).songImage.setImageBitmap(song.songImage);
            ((SongHolder) holder).txtSongTitle.setText("|  " + song.songName);
            ((SongHolder) holder).txtSongDesc.setText(song.singer + " | " + song.albumName);
            if (!(parentFragment instanceof CurrentPlayingListBottomSheet)) {
                ((SongHolder) holder).txtSongDur.setText(song.txtDuration);
            }
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
            ArrayList<Song> baseList = mediaPlayer.getBaseSongList();

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
        return mediaPlayer.getVisualSongList().size() > 0 ? mediaPlayer.getVisualSongList().size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        ArrayList<Song> songList = mediaPlayer.getVisualSongList();
        if (songList.size() > 0) {
            return songList.get(position).getCurrentState();
        }else{
            return Song.EMPTY_LIST;
        }
    }
}
