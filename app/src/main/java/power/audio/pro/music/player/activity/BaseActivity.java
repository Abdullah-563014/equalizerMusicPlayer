package power.audio.pro.music.player.activity;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.RelativeLayout;

import power.audio.pro.music.player.R;
import power.audio.pro.music.player.utils.PlayerNotificationManager;
import power.audio.pro.music.player.utils.ThemeUtils;

import butterknife.ButterKnife;


public abstract class BaseActivity extends AppCompatActivity {


    private PlayerNotificationManager.PlayerNotificationCenterDelegate playerNotificationCenterDelegate = new PlayerNotificationManager.PlayerNotificationCenterDelegate() {
        @Override
        public void onReceivedPlayerNotification(int id, Object... args) {
            BaseActivity.this.onReceivedPlayerNotification(id, args);
        }
    };
    RelativeLayout relativeLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getObserverIds() != null) {
            for (int id : getObserverIds()) {
                PlayerNotificationManager.addObserver(playerNotificationCenterDelegate, id);
            }
        }

        relativeLayout = (RelativeLayout)findViewById(R.id.RelativeLayout1);
        ThemeUtils.showTheme(relativeLayout);

    }





    @Override
    protected void onDestroy() {
        if (getObserverIds() != null) {
            for (int id : getObserverIds()) {
                PlayerNotificationManager.removeObserver(playerNotificationCenterDelegate, id);
            }
        }
        super.onDestroy();
    }

    protected int[] getObserverIds() {
        return null;
    }

    protected void onReceivedPlayerNotification(int id, Object... args) {
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    public boolean isShowAds() {
       return true;
    }
}
