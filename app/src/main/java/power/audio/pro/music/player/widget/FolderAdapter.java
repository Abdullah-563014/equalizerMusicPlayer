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
import power.audio.pro.music.player.model.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private SortedList<Folder> mFolderList = new SortedList<>(Folder.class, new SortedList.Callback<Folder>() {
        @Override
        public int compare(Folder o1, Folder o2) {
            return o1.compareTo(o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Folder oldItem, Folder newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Folder item1, Folder item2) {
            return item1.equals(item2);
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
    private List<Folder> mFoldersClone = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    private String mConstraint;
    private TextAppearanceSpan highlight;

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FolderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final FolderViewHolder holder, int position) {
        final Folder folder = getItem(position);

        holder.tvFolderTitle.setText(getSpannable(folder.getName()));
        holder.tvFolderSongCount.setText(String.format(Locale.getDefault(), "%d %s", folder.getCount(),
                MainApplication.getAppContext().getString(folder.getCount() <= 1 ? R.string.one_song : R.string.n_songs)));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, folder);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mOnItemLongClickListener != null && mOnItemLongClickListener.onItemLongClick(view, folder);
            }
        });
    }

    public Folder getItem(int position) {
        return mFolderList.get(position);
    }

    @Override
    public int getItemCount() {
        return mFolderList.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void setFolders(List<Folder> folders) {
        mFolderList.beginBatchedUpdates();
        mFolderList.clear();
        mFolderList.addAll(folders);
        mFolderList.endBatchedUpdates();
        mFoldersClone.clear();
        mFoldersClone.addAll(folders);
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
        mFolderList.beginBatchedUpdates();
        mFolderList.clear();
        if (!TextUtils.isEmpty(newText)) {
            for (Folder folder : mFoldersClone) {
                if (folder.getName().toUpperCase().contains(newText.toUpperCase())) {
                    mFolderList.add(folder);
                }
            }
        }
        mFolderList.endBatchedUpdates();
    }

    public void refresh() {
        mConstraint = null;
        mFolderList.beginBatchedUpdates();
        mFolderList.clear();
        mFolderList.addAll(mFoldersClone);
        mFolderList.endBatchedUpdates();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Folder folder);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, Folder folder);
    }

    class FolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_folder_art)
        ImageView ivFolderArt;
        @BindView(R.id.tv_folder_title)
        TextView tvFolderTitle;
        @BindView(R.id.tv_folder_song_count)
        TextView tvFolderSongCount;

        FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
