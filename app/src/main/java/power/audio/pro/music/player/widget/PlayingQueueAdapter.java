package power.audio.pro.music.player.widget;

import android.graphics.drawable.AnimationDrawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahihi.moreapps.util.GlideApp;
import com.nvp.util.NvpUtils;
import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.utils.SimpleItemTouchHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayingQueueAdapter extends RecyclerView.Adapter<PlayingQueueAdapter.PlayingQueueViewHolder> {
    private List<SongDetail> mPlayingList = new ArrayList<>();
    private SongDetail mPlayingSong;
    private boolean isPlaying;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private SimpleItemTouchHelper.OnStartDragListener mDragStartListener;

    private int textColorPrimary = ContextCompat.getColor(MainApplication.getAppContext(), R.color.textColorPrimary);
    private int textColorSecondary = ContextCompat.getColor(MainApplication.getAppContext(), R.color.textColorSecondary);

    @NonNull
    @Override
    public PlayingQueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayingQueueViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playing_song, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlayingQueueViewHolder holder, int position) {
        final SongDetail songDetail = getItem(position);

        if (mPlayingSong != null && mPlayingSong.equals(songDetail)) {
            holder.songThumb.setVisibility(View.GONE);
            holder.equalizer.setVisibility(View.VISIBLE);
            if (isPlaying) {
                holder.equalizer.setImageResource(R.drawable.ic_equalizer);
                ((AnimationDrawable) holder.equalizer.getDrawable()).start();
            } else {
                holder.equalizer.setImageResource(R.drawable.ic_equalizer_paused);
            }
            holder.songName.setTextColor(0xFFFE720C);
            holder.songArtistAndDuration.setTextColor(0xFFFE720C);
        } else {
            holder.equalizer.setVisibility(View.GONE);
            holder.songThumb.setVisibility(View.VISIBLE);
            GlideApp.with(MainApplication.getAppContext())
                    .load(songDetail.getCoverUri())
                    .circleCrop()
                    .error(R.drawable.default_song_cover)
                    .placeholder(R.drawable.default_song_cover)
                    .into(holder.songThumb);
            holder.songName.setTextColor(textColorPrimary);
            holder.songArtistAndDuration.setTextColor(textColorSecondary);
        }

        holder.songName.setText(songDetail.getTitle());
        holder.songArtistAndDuration.setText(NvpUtils.formatTimeDuration(songDetail.getDuration()) + " - " + songDetail.getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, songDetail);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mOnItemLongClickListener != null && mOnItemLongClickListener.onItemLongClick(view, songDetail);
            }
        });
        holder.moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onMoreOptionsClick(view, songDetail);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlayingList.size();
    }

    public SongDetail getItem(int position) {
        return mPlayingList.get(position);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public void setDragStartListener(SimpleItemTouchHelper.OnStartDragListener listener) {
        mDragStartListener = listener;
    }

    public SongDetail getPlayingSong() {
        return mPlayingSong;
    }

    public List<SongDetail> getPlayingList() {
        return mPlayingList;
    }

    public int getPlayingPosition() {
        return positionOf(mPlayingSong);
    }

    public void setPlayingList(List<SongDetail> songDetails) {
        mPlayingList.clear();
        mPlayingList.addAll(songDetails);
        notifyDataSetChanged();
    }

    public void setPlayingSong(SongDetail songDetail) {
        int oldPlayingPosition = positionOf(mPlayingSong);
        mPlayingSong = songDetail;
        int playingPosition = positionOf(mPlayingSong);
        if (oldPlayingPosition != playingPosition) {
            if (isCorrectPosition(oldPlayingPosition)) {
                notifyItemChanged(oldPlayingPosition);
            }
            if (isCorrectPosition(playingPosition)) {
                notifyItemChanged(playingPosition);
            }
        }
    }

    public boolean isCorrectPosition(int position) {
        return position >= 0 && position <= getItemCount() - 1;
    }

    public int positionOf(SongDetail songDetail) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).equals(songDetail)) {
                return i;
            }
        }
        return -1;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        int playingPosition = positionOf(mPlayingSong);
        if (isCorrectPosition(playingPosition)) {
            notifyItemChanged(playingPosition);
        }
    }

    public void swap(int fromPosition, int toPosition) {
        NvpUtils.swap(mPlayingList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void remove(SongDetail songDetail) {
        int position = positionOf(songDetail);
        if (isCorrectPosition(position)) {
            mPlayingList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void add(SongDetail songDetail) {
        int position = positionOf(songDetail);
        if (position == -1) {
            mPlayingList.add(songDetail);
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, SongDetail songDetail);

        void onMoreOptionsClick(View view, SongDetail songDetail);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, SongDetail songDetail);
    }

    class PlayingQueueViewHolder extends RecyclerView.ViewHolder implements SimpleItemTouchHelper.ItemTouchHelperViewHolder {
        private int defaultItemDrawableId = -1;

        @BindView(R.id.img_song_thumb)
        ImageView songThumb;
        @BindView(R.id.song_name)
        TextView songName;
        @BindView(R.id.song_artist_and_duration)
        TextView songArtistAndDuration;
        @BindView(R.id.img_more_icon)
        ImageView moreOptions;
        @BindView(R.id.iv_equalizer)
        ImageView equalizer;
        @BindView(R.id.iv_drag_handle)
        ImageView dragHandle;

        PlayingQueueViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(PlayingQueueViewHolder.this);
                        return true;
                    }
                    return false;
                }
            });
        }

        public int getDefaultItemDrawableId() {
            if (defaultItemDrawableId == -1) {
                defaultItemDrawableId = NvpUtils.selectableItemBackgroundThemeDark(MainApplication.getAppContext());
            }
            return defaultItemDrawableId;
        }

        @Override
        public void onItemDrag() {
            itemView.setBackgroundColor(0xff3C525D);
        }

        @Override
        public void onItemSwipe() {
            itemView.setBackgroundColor(0xff3C525D);
        }

        @Override
        public void onSwipeClear() {
            itemView.setBackgroundResource(getDefaultItemDrawableId());
        }

        @Override
        public void onDragClear() {
            itemView.setBackgroundResource(getDefaultItemDrawableId());
        }
    }
}
