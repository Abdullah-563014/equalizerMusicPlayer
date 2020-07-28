package power.audio.pro.music.player.widget;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nvp.util.NvpUtils;
import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.model.SongDetail;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongInAlbumAdapter extends RecyclerView.Adapter<SongInAlbumAdapter.SongViewHolder> {
    private SortedList<SongDetail> mSongDetails = new SortedList<>(SongDetail.class, new SortedList.Callback<SongDetail>() {
        @Override
        public int compare(SongDetail o1, SongDetail o2) {
            return o1.compareTo(o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(SongDetail oldItem, SongDetail newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(SongDetail item1, SongDetail item2) {
            return item1.getId() == item2.getId();
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    });
    private List<SongDetail> mSongsClone = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private String mConstraint;
    private TextAppearanceSpan highlight;

    private SongDetail mPlayingSong;
    private boolean isPlaying;

    private boolean isShowArtistName = true;

    private int textColorPrimary = ContextCompat.getColor(MainApplication.getAppContext(), R.color.textColorPrimary);
    private int textColorSecondary = ContextCompat.getColor(MainApplication.getAppContext(), R.color.textColorSecondary);

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SongViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_in_album, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        final SongDetail songDetail = getItem(position);

        if (mPlayingSong != null && mPlayingSong.equals(songDetail)) {
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
            holder.songName.setTextColor(textColorPrimary);
            holder.songArtistAndDuration.setTextColor(textColorSecondary);
        }

        holder.songName.setText(getSpannable((position + 1) + ". " + songDetail.getTitle()));
        holder.songArtistAndDuration.setText(getSpannable(NvpUtils.formatTimeDuration(songDetail.getDuration()) + (isShowArtistName ? " - " + songDetail.getArtist() : "")));

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

    public SongDetail getItem(int position) {
        return mSongDetails.get(position);
    }

    @Override
    public int getItemCount() {
        return mSongDetails.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setSongDetails(List<SongDetail> songs) {
        mSongDetails.beginBatchedUpdates();
        mSongDetails.clear();
        mSongDetails.addAll(songs);
        mSongDetails.endBatchedUpdates();
        mSongsClone.clear();
        mSongsClone.addAll(songs);
    }

    public List<SongDetail> getSongDetails() {
        List<SongDetail> songDetails = new ArrayList<>(getItemCount());
        for (int i = 0; i < getItemCount(); i++) {
            songDetails.add(getItem(i));
        }
        return songDetails;
    }

    public void setShowArtistName(boolean showArtistName) {
        isShowArtistName = showArtistName;
        notifyDataSetChanged();
    }

    public int positionOf(SongDetail songDetail) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).equals(songDetail)) {
                return i;
            }
        }
        return -1;
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

    public SongDetail getPlayingSong() {
        return mPlayingSong;
    }

    public boolean isCorrectPosition(int position) {
        return position >= 0 && position <= getItemCount() - 1;
    }

    public void remove(SongDetail songDetail) {
        int position = positionOf(songDetail);
        if (isCorrectPosition(position)) {
            mSongDetails.removeItemAt(position);
            mSongsClone.remove(songDetail);
        }
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
        int playingPosition = positionOf(mPlayingSong);
        if (isCorrectPosition(playingPosition)) {
            notifyItemChanged(playingPosition);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    private Spannable getSpannable(String s) {
        Spannable spannable = new SpannableString(TextUtils.isEmpty(s) ? "" : s);

        if (!TextUtils.isEmpty(mConstraint)) {
            int index = s.toUpperCase().lastIndexOf(mConstraint.toUpperCase());
            if (index != -1) {
                spannable.setSpan(getHighlight(), index, index + mConstraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return spannable;
    }

    private TextAppearanceSpan getHighlight() {
        if (highlight == null) {
            ColorStateList color = new ColorStateList(new int[][]{new int[]{}}, new int[]{ContextCompat.getColor(MainApplication.getAppContext(), R.color.colorAccent)});
            highlight = new TextAppearanceSpan(null, Typeface.BOLD, -1, color, null);
        }
        return highlight;
    }

    public void filter(String newText) {
        mConstraint = newText;
        mSongDetails.beginBatchedUpdates();
        mSongDetails.clear();
        if (!TextUtils.isEmpty(newText)) {
            for (SongDetail songDetail : mSongsClone) {
                if (songDetail.getTitle().toUpperCase().contains(newText.toUpperCase())
                        || songDetail.getArtist().toUpperCase().contains(newText.toUpperCase())) {
                    mSongDetails.add(songDetail);
                }
            }
        }
        mSongDetails.endBatchedUpdates();
    }

    public void refresh() {
        mConstraint = null;
        mSongDetails.beginBatchedUpdates();
        mSongDetails.clear();
        mSongDetails.addAll(mSongsClone);
        mSongDetails.endBatchedUpdates();
    }

    public void add(SongDetail songDetail) {
        if (positionOf(songDetail) == -1) {
            mSongDetails.add(songDetail);
            mSongsClone.add(songDetail);
        }
    }

    public void addOrUpdate(SongDetail songDetail) {
        if (songDetail == null) {
            return;
        }

        if (!update(songDetail)) {
            mSongDetails.add(songDetail);
            mSongsClone.add(songDetail);
        }
    }

    private boolean update(SongDetail songDetail) {
        for (int i = mSongsClone.size() - 1; i >= 0; i--) {
            if (mSongsClone.get(i).getId() == songDetail.getId()) {
                mSongsClone.remove(i);
                mSongsClone.add(songDetail);
                break;
            }
        }
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).getId() == songDetail.getId()) {
                mSongDetails.updateItemAt(i, songDetail);
                return true;
            }
        }
        return false;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, SongDetail songDetail);

        void onMoreOptionsClick(View view, SongDetail songDetail);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, SongDetail songDetail);
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.song_name)
        TextView songName;
        @BindView(R.id.song_artist_and_duration)
        TextView songArtistAndDuration;
        @BindView(R.id.img_more_icon)
        ImageView moreOptions;
        @BindView(R.id.iv_equalizer)
        ImageView equalizer;

        SongViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
