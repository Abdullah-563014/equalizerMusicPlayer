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

import power.audio.pro.music.player.MainApplication;
import power.audio.pro.music.player.R;
import power.audio.pro.music.player.model.Playlist;
import power.audio.pro.music.player.model.SongDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PlaylistAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_PLAYLIST = 0;
    private static final int VIEW_TYPE_ADS = 1;

    private SortedList<Playlist> mPlaylists = new SortedList<>(Playlist.class, new SortedList.Callback<Playlist>() {
        @Override
        public int compare(Playlist o1, Playlist o2) {
            return o1.compareTo(o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Playlist oldItem, Playlist newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Playlist item1, Playlist item2) {
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
    private List<Playlist> mPlaylistsClone = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private AdsLoaderCallback mAdsLoaderCallback;

    private String mConstraint;
    private TextAppearanceSpan highlight;

    @Override
    public int getItemViewType(int position) {
        Playlist playlist = getItem(position);
        return playlist.isAdsPlaylist() ? VIEW_TYPE_ADS : VIEW_TYPE_PLAYLIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PlaylistViewHolder(inflater.inflate(R.layout.item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Playlist playlist = getItem(position);

        bindPlaylist((PlaylistViewHolder) holder, playlist);
    }

    private void bindPlaylist(PlaylistViewHolder holder, final Playlist playlist) {
        if (playlist.isCreatePlaylist()) {
            holder.ivPlaylist.setImageResource(R.drawable.ic_playlist_create);
            holder.tvPlaylistName.setText(R.string.new_playlist);
            holder.tvPlaylistCount.setVisibility(View.GONE);
            holder.moreOptions.setVisibility(View.GONE);
        } else {
            holder.ivPlaylist.setImageResource(R.drawable.ic_playlist);
            holder.tvPlaylistName.setText(getSpannable(playlist.getName()));
            holder.tvPlaylistCount.setVisibility(View.VISIBLE);
            holder.tvPlaylistCount.setText(String.format(Locale.getDefault(), "%d %s", playlist.getSongDetails().size(),
                    MainApplication.getAppContext().getString(playlist.getSongDetails().size() <= 1 ? R.string.one_song : R.string.n_songs)));

            holder.moreOptions.setVisibility(playlist.isFavoritePlaylist() ? View.GONE : View.VISIBLE);
            holder.moreOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onMoreOptionsClick(view, playlist);
                    }
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, playlist);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mOnItemLongClickListener != null && mOnItemLongClickListener.onItemLongClick(view, playlist);
            }
        });

    }

    public Playlist getItem(int position) {
        return mPlaylists.get(position);
    }

    @Override
    public int getItemCount() {
        return mPlaylists.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setPlaylists(List<Playlist> playlists) {
        mPlaylists.beginBatchedUpdates();
        mPlaylists.clear();
        mPlaylists.addAll(playlists);
//        loadNativeAds();
        mPlaylists.endBatchedUpdates();
        mPlaylistsClone.clear();
        mPlaylistsClone.addAll(playlists);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

//    public void setAdsLoaderCallback(AdsLoaderCallback callback) {
//        mAdsLoaderCallback = callback;
//    }

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
        mPlaylists.beginBatchedUpdates();
        mPlaylists.clear();
        if (!TextUtils.isEmpty(newText)) {
            for (Playlist playlist : mPlaylistsClone) {
                if (playlist.getName().toUpperCase().contains(newText.toUpperCase()) &&
                        !playlist.isAdsPlaylist() &&
                        !playlist.isFavoritePlaylist() &&
                        !playlist.isCreatePlaylist()) {
                    mPlaylists.add(playlist);
                }
            }
        }
        mPlaylists.endBatchedUpdates();
    }

    public void refresh() {
        mConstraint = null;
        mPlaylists.beginBatchedUpdates();
        mPlaylists.clear();
        mPlaylists.addAll(mPlaylistsClone);
//        loadNativeAds();
        mPlaylists.endBatchedUpdates();
    }

    public void updateFavorite(SongDetail songDetail, boolean isFavorite) {
        Playlist playlist = getItem(1);
        if (isFavorite) {
            playlist.getSongDetails().add(songDetail);
        } else {
            playlist.getSongDetails().remove(songDetail);
        }
        update(playlist);
    }

    public void addOrUpdate(Playlist playlist) {
        if (playlist == null) {
            return;
        }

        if (!update(playlist)) {
            mPlaylists.add(playlist);
            mPlaylistsClone.add(playlist);
        }

//        loadNativeAds();
    }

    private boolean update(Playlist playlist) {
        for (int i = mPlaylistsClone.size() - 1; i >= 0; i--) {
            if (mPlaylistsClone.get(i).getId() == playlist.getId()) {
                mPlaylistsClone.remove(i);
                mPlaylistsClone.add(playlist);
                break;
            }
        }
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).getId() == playlist.getId()) {
                mPlaylists.updateItemAt(i, playlist);
                return true;
            }
        }
        return false;
    }

    public void remove(Playlist playlist) {
        mPlaylists.remove(playlist);
        mPlaylistsClone.remove(playlist);
//        loadNativeAds();
    }

//    private void loadNativeAds() {
//        int positionOfAds = positionOfAds();
//
//        if (getItemCount() <= 2) {
//            if (positionOfAds == -1) {
//                mPlaylists.add(Playlist.ads());
//            }
//        } else if (positionOfAds != -1) {
//            mPlaylists.removeItemAt(positionOfAds);
//        }
//    }

    private int positionOfAds() {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).isAdsPlaylist()) {
                return i;
            }
        }
        return -1;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Playlist playlist);

        void onMoreOptionsClick(View view, Playlist playlist);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, Playlist playlist);
    }

    public interface AdsLoaderCallback {
        boolean isLoad();
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_playlist)
        ImageView ivPlaylist;
        @BindView(R.id.tv_playlist_name)
        TextView tvPlaylistName;
        @BindView(R.id.tv_playlist_song_count)
        TextView tvPlaylistCount;
        @BindView(R.id.img_more_icon)
        ImageView moreOptions;

        PlaylistViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

//    class AdsViewHolder extends RecyclerView.ViewHolder {
//        @BindView(R.id.admob_app_icon)
//        ImageView adMobAppIcon;
//        @BindView(R.id.admob_headline)
//        TextView adMobHeadline;
//
//        AdsViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//    }
}
