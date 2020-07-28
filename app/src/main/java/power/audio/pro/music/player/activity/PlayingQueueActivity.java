package power.audio.pro.music.player.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.fragment.SongPopupFragment;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.PlaylistUtils;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.SimpleItemTouchHelper;
import power.audio.pro.music.player.utils.ThemeUtils;
import power.audio.pro.music.player.widget.PlayingQueueAdapter;

import java.util.List;

import butterknife.BindView;

public class PlayingQueueActivity extends ToolbarActivity
        implements
        PlayingQueueAdapter.OnItemClickListener,
        PlayingQueueAdapter.OnItemLongClickListener,
        SimpleItemTouchHelper.OnStartDragListener,
        SimpleItemTouchHelper.ItemTouchHelperAdapter {

    private PlayingQueueAdapter mPlayingQueueAdapter;

    private ItemTouchHelper mItemTouchHelper;
    private SimpleItemTouchHelper mSimpleItemTouchHelper;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_queue);

        mPlayingQueueAdapter = new PlayingQueueAdapter();
        mPlayingQueueAdapter.setOnItemClickListener(this);
        mPlayingQueueAdapter.setOnItemLongClickListener(this);
        mPlayingQueueAdapter.setDragStartListener(this);
        recyclerView.setAdapter(mPlayingQueueAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        mSimpleItemTouchHelper = new SimpleItemTouchHelper(this);
        mSimpleItemTouchHelper.setItemViewSwipeEnabled(mPlayingQueueAdapter.getItemCount() > 1);
        mItemTouchHelper = new ItemTouchHelper(mSimpleItemTouchHelper);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        PlayerService.getInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_playing_queue);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.playing_queue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save_queue) {
            PlaylistUtils.addSongToPlaylist(this, mPlayingQueueAdapter.getPlayingList());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        PlayerService.swapSong(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        PlayerService.removeSongFromQueue(mPlayingQueueAdapter.getItem(position));
    }

    @Override
    public void onItemClick(View view, SongDetail songDetail) {
        PlayerService.setPlayingSong(songDetail);
        PlayerService.playSong();
    }

    @Override
    public void onMoreOptionsClick(View view, SongDetail songDetail) {
        SongPopupFragment songPopupFragment = new SongPopupFragment();
        songPopupFragment.setSongDetail(songDetail);
        songPopupFragment.remove(R.id.action_add_to_queue);
        songPopupFragment.remove(R.id.action_remove_from_playlist);
        songPopupFragment.remove(R.id.action_delete);
        if (mPlayingQueueAdapter.getItemCount() <= 1) {
            songPopupFragment.remove(R.id.action_remove_from_queue);
        }
        if (SQLHelper.getInstance().isFavoriteSong(songDetail)) {
            songPopupFragment.remove(R.id.action_add_to_favorites);
        } else {
            songPopupFragment.remove(R.id.action_remove_from_favorites);
        }
        songPopupFragment.show(getSupportFragmentManager());
    }

    @Override
    public boolean onItemLongClick(View view, SongDetail songDetail) {
        onMoreOptionsClick(view, songDetail);
        return true;
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.completion,
                PlayerNotificationManager.error,
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.shuffle,
                PlayerNotificationManager.swapSong,
                PlayerNotificationManager.removeSongFromQueue,
                PlayerNotificationManager.deleteSong,
                PlayerNotificationManager.addSongToQueue,
                PlayerNotificationManager.playlistEmpty,
                PlayerNotificationManager.info
        };
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

        if (id == PlayerNotificationManager.start) {
            mPlayingQueueAdapter.setPlayingSong((SongDetail) args[0]);
            mPlayingQueueAdapter.setPlaying(true);
            scrollToPlayingPosition();
            return;
        }

        if (id == PlayerNotificationManager.pause) {
            mPlayingQueueAdapter.setPlaying(false);
            scrollToPlayingPosition();
            return;
        }

        if (id == PlayerNotificationManager.completion) {
            mPlayingQueueAdapter.setPlayingSong(null);
            mPlayingQueueAdapter.setPlaying(false);
            return;
        }

        if (id == PlayerNotificationManager.error) {
            mPlayingQueueAdapter.setPlayingSong(null);
            mPlayingQueueAdapter.setPlaying(false);
            return;
        }

        if (id == PlayerNotificationManager.playlistEmpty) {
            finish();
            return;
        }

        if (id == PlayerNotificationManager.shuffle) {
            mPlayingQueueAdapter.setPlayingList((List<SongDetail>) args[1]);
            mSimpleItemTouchHelper.setItemViewSwipeEnabled(mPlayingQueueAdapter.getItemCount() > 1);
            return;
        }

        if (id == PlayerNotificationManager.swapSong) {
            mPlayingQueueAdapter.swap((int) args[0], (int) args[1]);
            return;
        }

        if (id == PlayerNotificationManager.removeSongFromQueue || id == PlayerNotificationManager.deleteSong) {
            mPlayingQueueAdapter.remove((SongDetail) args[0]);
            mSimpleItemTouchHelper.setItemViewSwipeEnabled(mPlayingQueueAdapter.getItemCount() > 1);
            return;
        }

        if (id == PlayerNotificationManager.addSongToQueue) {
            mPlayingQueueAdapter.add((SongDetail) args[0]);
            mSimpleItemTouchHelper.setItemViewSwipeEnabled(mPlayingQueueAdapter.getItemCount() > 1);
            return;
        }

        if (id == PlayerNotificationManager.info) {
            if (args.length > 0 && args[0] != null) {
                mPlayingQueueAdapter.setPlayingList((List<SongDetail>) args[0]);
                mPlayingQueueAdapter.setPlayingSong((SongDetail) args[1]);
                mPlayingQueueAdapter.setPlaying((boolean) args[2]);
                mSimpleItemTouchHelper.setItemViewSwipeEnabled(mPlayingQueueAdapter.getItemCount() > 1);

                scrollToPlayingPosition();
            }
        }
    }

    private void scrollToPlayingPosition() {
        int playingPosition = mPlayingQueueAdapter.getPlayingPosition();
        if (mPlayingQueueAdapter.isCorrectPosition(playingPosition)) {
            recyclerView.smoothScrollToPosition(playingPosition);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left);
    }

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, PlayingQueueActivity.class));
        activity.overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
    }
}
