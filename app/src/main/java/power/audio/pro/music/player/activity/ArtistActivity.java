package power.audio.pro.music.player.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahihi.moreapps.util.GlideApp;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.fragment.SongPopupFragment;
import power.audio.pro.music.player.model.Artist;
import power.audio.pro.music.player.model.SongDetail;
import power.audio.pro.music.player.service.PlayerService;
import power.audio.pro.music.player.utils.FastBlur;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.SQLHelper;
import power.audio.pro.music.player.utils.SongArtistLoadTask;
import power.audio.pro.music.player.utils.ThemeUtils;
import power.audio.pro.music.player.utils.lastfmapi.LastFmClient;
import power.audio.pro.music.player.widget.BottomPlayer;
import power.audio.pro.music.player.widget.SongInAlbumAdapter;

import java.util.List;

import butterknife.BindView;

public class ArtistActivity extends ToolbarActivity implements SongArtistLoadTask.LoadCallback, SongInAlbumAdapter.OnItemClickListener, SongInAlbumAdapter.OnItemLongClickListener {
    private static final String EXTRA_ARTIST = "extra_artist";

    private AsyncTask mSongArtistLoadTask;

    private Artist mArtist;

    private SongInAlbumAdapter mSongAdapter;

    private boolean refreshList = true;

    private MenuItem mSearchMenuItem;

    private BottomPlayer mBottomPlayer;

    @BindView(R.id.iv_artist_art)
    ImageView mArtistArt;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.tv_artist_title)
    TextView tvArtistTitle;
    @BindView(R.id.iv_blur_bg)
    ImageView ivBlurBg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        mBottomPlayer = new BottomPlayer(this);
        mBottomPlayer.setBeforePlayerActivityShown(new BottomPlayer.BeforePlayerActivityShown() {
            @Override
            public void beforePlayerActivityShown() {
                refreshList = false;
            }
        });

        mArtist = getIntent().getParcelableExtra(EXTRA_ARTIST);

        setTitle("");
        tvArtistTitle.setText(mArtist.getName());

        LastFmClient.getArtistUrl(mArtist.getName(), new LastFmClient.Callback() {
            @Override
            public void onArtistUrlResult(String url) {
                GlideApp.with(getApplicationContext())
                        .load(url)
                        .circleCrop()
                        .error(R.drawable.default_album_circle)
                        .placeholder(R.drawable.default_album_circle)
                        .into(mArtistArt);

                GlideApp.with(getApplicationContext())
                        .asBitmap()
                        .centerCrop()
                        .error(R.drawable.bg_activity)
                        .placeholder(R.drawable.bg_activity)
                        .load(url)
                        .into(new SimpleTarget<Bitmap>() {
                                  @Override
                                  public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                      try{
                                          ivBlurBg.setImageBitmap(FastBlur.doBlur(resource, 50, true));
                                      }catch (Exception e){
                                      }
                                  }

                                  @Override
                                  public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                      Bitmap bitmap = null;

                                      if (errorDrawable != null) {
                                          if (errorDrawable instanceof BitmapDrawable) {
                                              bitmap = ((BitmapDrawable) errorDrawable).getBitmap();
                                          } else {
                                              int intrinsicWidth = errorDrawable.getIntrinsicWidth();
                                              int intrinsicHeight = errorDrawable.getIntrinsicHeight();
                                              if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                                                  bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
                                                  Canvas canvas = new Canvas(bitmap);
                                                  errorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                                  errorDrawable.draw(canvas);
                                              }
                                          }
                                      }

                                      if (bitmap != null) {
                                          ivBlurBg.setImageBitmap(bitmap);
                                      }
                                  }
                              }
                        );
            }
        });

        mSongAdapter = new SongInAlbumAdapter();
        mSongAdapter.setShowArtistName(false);
        mSongAdapter.setOnItemClickListener(this);
        mSongAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(mSongAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        PlayerService.getInfo();
    }


    @Override
    protected void onResume() {
        super.onResume();

        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.activity_artist);
        ThemeUtils.showTheme(relativeLayout);
    }

    @Override
    protected int[] getObserverIds() {
        return new int[]{
                PlayerNotificationManager.start,
                PlayerNotificationManager.pause,
                PlayerNotificationManager.error,
                PlayerNotificationManager.deleteSong,
                PlayerNotificationManager.completion,
                PlayerNotificationManager.playlistEmpty,
                PlayerNotificationManager.updateTime,
                PlayerNotificationManager.info
        };
    }

    @Override
    protected void onReceivedPlayerNotification(int id, Object... args) {
        super.onReceivedPlayerNotification(id, args);

        mBottomPlayer.onReceivedPlayerNotification(id, args);

        if (id == PlayerNotificationManager.start) {
            mSongAdapter.setPlayingSong((SongDetail) args[0]);
            mSongAdapter.setPlaying(true);
            return;
        }

        if (id == PlayerNotificationManager.pause) {
            mSongAdapter.setPlaying(false);
            return;
        }

        if (id == PlayerNotificationManager.completion || id == PlayerNotificationManager.error) {
            mSongAdapter.setPlayingSong(null);
            mSongAdapter.setPlaying(false);
            return;
        }

        if (id == PlayerNotificationManager.playlistEmpty) {
            mSongAdapter.setPlayingSong(null);
            mSongAdapter.setPlaying(false);
            refreshList = true;
            refreshSongList();
            return;
        }

        if (id == PlayerNotificationManager.deleteSong) {
            mSongAdapter.remove((SongDetail) args[0]);
            updateView();
            return;
        }

        if (id == PlayerNotificationManager.info) {
            if (args.length > 0 && args[0] != null) {
                mSongAdapter.setPlayingSong((SongDetail) args[1]);
                mSongAdapter.setPlaying((boolean) args[2]);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshSongList();
    }

    @Override
    public void onStop() {
        cancelSongArtistLoadTask();
        super.onStop();
    }

    @Override
    public void onLoadCompleted(@NonNull List<SongDetail> songDetails) {
        mSongArtistLoadTask = null;
        progressBar.setVisibility(View.GONE);
        mSongAdapter.setSongDetails(songDetails);
        updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchMenuItem.setVisible(mSongAdapter != null && !mSongAdapter.isEmpty());

        final SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for_songs));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSongAdapter.filter(newText.trim());
                recyclerView.scrollToPosition(0);
                return true;
            }
        };

        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.setOnQueryTextListener(onQueryTextListener);
                mSongAdapter.filter(null);
                tvEmpty.setVisibility(View.GONE);
                tvArtistTitle.setText("");
                mArtistArt.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.setOnQueryTextListener(null);
                mSongAdapter.refresh();
                tvArtistTitle.setText(mArtist.getName());
                mArtistArt.setVisibility(View.VISIBLE);
                updateView();
                return true;
            }
        });

        mSearchMenuItem.setVisible(mSongAdapter != null && !mSongAdapter.isEmpty());

        return true;
    }

    @Override
    public void onItemClick(View view, SongDetail songDetail) {
        PlayerService.setPlaylist(mSongAdapter.getSongDetails());
        PlayerService.setPlayingSong(songDetail);
        PlayerService.playSong();
    }

    @Override
    public void onMoreOptionsClick(View view, final SongDetail songDetail) {
        SongPopupFragment songPopupFragment = new SongPopupFragment();
        songPopupFragment.setSongInAlbumAdapter(mSongAdapter);
        songPopupFragment.setSongDetail(songDetail);
        songPopupFragment.setCallback(new SongPopupFragment.Callback() {
            @Override
            public void disableRefreshList() {
                refreshList = false;
            }
        });
        songPopupFragment.remove(R.id.action_remove_from_queue);
        songPopupFragment.remove(R.id.action_remove_from_playlist);
        if (songDetail.equals(mSongAdapter.getPlayingSong())) {
            songPopupFragment.remove(R.id.action_add_to_queue);
            songPopupFragment.remove(R.id.action_delete);
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

    private void updateView() {
        recyclerView.setVisibility(mSongAdapter.isEmpty() ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(mSongAdapter.isEmpty() ? View.VISIBLE : View.GONE);
        if (mSearchMenuItem != null) {
            mSearchMenuItem.setVisible(!mSongAdapter.isEmpty());
        }
    }

    private void cancelSongArtistLoadTask() {
        if (mSongArtistLoadTask != null) {
            if (!mSongArtistLoadTask.isCancelled()) {
                mSongArtistLoadTask.cancel(true);
            }
            mSongArtistLoadTask = null;
        }
    }

    private void refreshSongList() {
        if (refreshList) {
            progressBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            cancelSongArtistLoadTask();
            mSongArtistLoadTask = new SongArtistLoadTask(mArtist, this).execute();
        } else {
            refreshList = true;
        }
    }

    public static void start(@NonNull Context context, Artist artist) {
        Intent starter = new Intent(context, ArtistActivity.class);
        starter.putExtra(EXTRA_ARTIST, artist);
        context.startActivity(starter);
    }
}
