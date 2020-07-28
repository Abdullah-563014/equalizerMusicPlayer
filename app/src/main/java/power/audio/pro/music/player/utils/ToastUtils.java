package power.audio.pro.music.player.utils;

import android.widget.Toast;

import power.audio.pro.music.player.MainApplication;

/**
 * @author nguyenvietphu6794@gmail.com
 *         Created on 07/24/17.
 */
public class ToastUtils {
    private static Toast mToast = null;

    private static void showToast(final CharSequence msg, final int duration) {
        MainApplication.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mToast == null) {
                        mToast = Toast.makeText(MainApplication.getAppContext(), msg, duration);
                    } else {
                        mToast.setText(msg);
                        mToast.setDuration(duration);
                    }
                    mToast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void shortMsg(CharSequence msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void shortMsg(int msgId) {
        shortMsg(MainApplication.getAppContext().getText(msgId));
    }

    public static void longMsg(CharSequence msg) {
        showToast(msg, Toast.LENGTH_LONG);
    }

    public static void longMsg(int msgId) {
        longMsg(MainApplication.getAppContext().getText(msgId));
    }
}
