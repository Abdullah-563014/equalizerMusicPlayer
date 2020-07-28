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
import power.audio.pro.music.player.model.Album;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private SortedList<Album> mAlbumList = new SortedList<>(Album.class, new SortedList.Callback<Album>() {
        @Override
        public int compare(Album o1, Album o2) {
            return o1.compareTo(o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Album oldItem, Album newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Album item1, Album item2) {
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
    private List<Album> mAlbumsClone = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private String mConstraint;
    private TextAppearanceSpan highlight;

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlbumViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        final Album album = getItem(position);

        GlideApp.with(MainApplication.getAppContext())
                .load(album.getCover())
                .centerCrop()
                .error(R.drawable.default_album)
                .placeholder(R.drawable.default_album)
                .into(holder.ivAlbumArt);

        holder.tvAlbumTitle.setText(getSpannable(album.getTitle()));
        holder.tvAlbumArtist.setText(getSpannable(album.getArtist()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, album);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mOnItemLongClickListener != null && mOnItemLongClickListener.onItemLongClick(view, album);
            }
        });
    }

    public Album getItem(int position) {
        return mAlbumList.get(position);
    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setAlbums(List<Album> albums) {
        mAlbumList.beginBatchedUpdates();
        mAlbumList.clear();
        mAlbumList.addAll(albums);
        mAlbumList.endBatchedUpdates();
        mAlbumsClone.clear();
        mAlbumsClone.addAll(albums);
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
        mAlbumList.beginBatchedUpdates();
        mAlbumList.clear();
        if (!TextUtils.isEmpty(newText)) {
            for (Album album : mAlbumsClone) {
                if (album.getTitle().toUpperCase().contains(newText.toUpperCase())
                        || album.getArtist().toUpperCase().contains(newText.toUpperCase())) {
                    mAlbumList.add(album);
                }
            }
        }
        mAlbumList.endBatchedUpdates();
    }

    public void refresh() {
        mConstraint = null;
        mAlbumList.beginBatchedUpdates();
        mAlbumList.clear();
        mAlbumList.addAll(mAlbumsClone);
        mAlbumList.endBatchedUpdates();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Album album);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, Album album);
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_album_art)
        ImageView ivAlbumArt;
        @BindView(R.id.tv_album_title)
        TextView tvAlbumTitle;
        @BindView(R.id.tv_album_artist)
        TextView tvAlbumArtist;

        AlbumViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
