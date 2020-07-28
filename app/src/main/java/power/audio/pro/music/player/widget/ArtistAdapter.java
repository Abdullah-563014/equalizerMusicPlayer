package power.audio.pro.music.player.widget;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
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

import com.ahihi.moreapps.util.GlideApp;
import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.model.Artist;
import power.audio.pro.music.player.utils.lastfmapi.LastFmClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {
    private SortedList<Artist> mArtistsList = new SortedList<>(Artist.class, new SortedList.Callback<Artist>() {
        @Override
        public int compare(Artist o1, Artist o2) {
            return o1.compareTo(o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Artist oldItem, Artist newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Artist item1, Artist item2) {
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
    private List<Artist> mArtistClone = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private String mConstraint;
    private TextAppearanceSpan highlight;

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ArtistViewHolder holder, int position) {
        final Artist artist = getItem(position);

        LastFmClient.getArtistUrl(artist.getName(), new LastFmClient.Callback() {
            @Override
            public void onArtistUrlResult(String url) {
                GlideApp.with(MainApplication.getAppContext())
                        .load(url)
                        .circleCrop()
                        .error(R.drawable.default_artist)
                        .placeholder(R.drawable.default_artist)
                        .into(holder.ivArtistArt);
            }
        });

        holder.tvArtistTitle.setText(getSpannable(artist.getName()));
        holder.tvArtistSongCount.setText(String.format(Locale.getDefault(), "%d %s", artist.getCount(),
                MainApplication.getAppContext().getString(artist.getCount() <= 1 ? R.string.one_song : R.string.n_songs)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, artist);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mOnItemLongClickListener != null && mOnItemLongClickListener.onItemLongClick(view, artist);
            }
        });
    }

    public Artist getItem(int position) {
        return mArtistsList.get(position);
    }

    @Override
    public int getItemCount() {
        return mArtistsList.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setArtists(List<Artist> artists) {
        mArtistsList.beginBatchedUpdates();
        mArtistsList.clear();
        mArtistsList.addAll(artists);
        mArtistsList.endBatchedUpdates();
        mArtistClone.clear();
        mArtistClone.addAll(artists);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Artist artist);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, Artist artist);
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
        mArtistsList.beginBatchedUpdates();
        mArtistsList.clear();
        if (!TextUtils.isEmpty(newText)) {
            for (Artist artist : mArtistClone) {
                if (artist.getName().toUpperCase().contains(newText.toUpperCase())) {
                    mArtistsList.add(artist);
                }
            }
        }
        mArtistsList.endBatchedUpdates();
    }

    public void refresh() {
        mConstraint = null;
        mArtistsList.beginBatchedUpdates();
        mArtistsList.clear();
        mArtistsList.addAll(mArtistClone);
        mArtistsList.endBatchedUpdates();
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_artist_art)
        ImageView ivArtistArt;
        @BindView(R.id.tv_artist_title)
        TextView tvArtistTitle;
        @BindView(R.id.tv_artist_song_count)
        TextView tvArtistSongCount;

        ArtistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
