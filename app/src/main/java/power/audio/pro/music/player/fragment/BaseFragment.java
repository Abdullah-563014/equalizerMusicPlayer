package power.audio.pro.music.player.fragment;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import power.audio.pro.music.player.utils.PlayerNotificationManager;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {
    private PlayerNotificationManager.PlayerNotificationCenterDelegate playerNotificationCenterDelegate = new PlayerNotificationManager.PlayerNotificationCenterDelegate() {
        @Override
        public void onReceivedPlayerNotification(int id, Object... args) {
            BaseFragment.this.onReceivedPlayerNotification(id, args);
        }
    };

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(getLayoutId(), container, false);
            mUnbinder = ButterKnife.bind(this, view);
            return view;
        } finally {
            if (getObserverIds() != null) {
                for (int id : getObserverIds()) {
                    PlayerNotificationManager.addObserver(playerNotificationCenterDelegate, id);
                }
            }
        }
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @Override
    public void onDestroyView() {
        if (getObserverIds() != null) {
            for (int id : getObserverIds()) {
                PlayerNotificationManager.removeObserver(playerNotificationCenterDelegate, id);
            }
        }
        mUnbinder.unbind();
        super.onDestroyView();
    }

    protected int[] getObserverIds() {
        return null;
    }

    protected void onReceivedPlayerNotification(int id, Object... args) {
    }
}
