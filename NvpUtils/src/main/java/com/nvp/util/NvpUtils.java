package com.nvp.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.appcompat.view.ContextThemeWrapper;
import android.util.TypedValue;
import android.view.WindowManager;

import com.nvp.R;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 11/4/2016.
 */
public class NvpUtils {
    @ColorInt
    public static int getColorAccentFromTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public static int getColorPrimaryFromTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public static int getColorPrimaryDarkFromTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public static int getTextColorPrimaryFromTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        return typedValue.data;
    }

    @ColorInt
    public static int getTextColorSecondaryFromTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
        return typedValue.data;
    }

    public static boolean isBrightColor(@ColorInt int color) {
        if (Color.TRANSPARENT == color) {
            return true;
        }
        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};
        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * 0.241 + rgb[1] * rgb[1] * 0.691 + rgb[2] * rgb[2] * 0.068);
        return brightness >= 200;
    }

    @ColorInt
    public static int getDarkerColor(@ColorInt int color) {
        float factor = 0.8f;
        return Color.argb(Color.alpha(color),
                Math.max((int) (Color.red(color) * factor), 0),
                Math.max((int) (Color.green(color) * factor), 0),
                Math.max((int) (Color.blue(color) * factor), 0));
    }

    @ColorInt
    public static int getLighterColor(@ColorInt int color) {
        float factor = 0.2f;
        return Color.argb(Color.alpha(color),
                (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255),
                (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255),
                (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255));
    }

    public static String colorIntToHex(@ColorInt int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static String formatTimeDuration(long timeMillis) {
        if (timeMillis < 0) {
            timeMillis = 0;
        }

        long hour = TimeUnit.MILLISECONDS.toHours(timeMillis);
        long minute = TimeUnit.MILLISECONDS.toMinutes(timeMillis) - TimeUnit.HOURS.toMinutes(hour);
        long second = TimeUnit.MILLISECONDS.toSeconds(timeMillis) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(minute);

        return hour > 0 ? String.format(Locale.getDefault(), "%1$02d:%2$02d:%3$02d", hour, minute, second) :
                String.format(Locale.getDefault(), "%1$02d:%2$02d", minute, second);
    }

    public static void setKeepOnScreen(Activity activity, boolean enable) {
        if (enable) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static int dpToPixels(int dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    public static int getMediaDuration(Context context, String path) {
        int duration = 0;

        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            duration = Integer.parseInt(durationStr);
            mediaMetadataRetriever.release();
        } catch (Exception e) {
            try {
                MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse("file://" + path));

                if (mediaPlayer != null) {
                    duration = mediaPlayer.getDuration();
                    mediaPlayer.release();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return duration;
    }

    public static String formatTime(String pattern, long timeInMillis) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(timeInMillis);
    }

    public static String formatTime(String pattern, Calendar calendar) {
        return formatTime(pattern, calendar.getTimeInMillis());
    }

    public static String getTimeStamp(String pattern) {
        return formatTime(pattern, Calendar.getInstance());
    }

    public static String normalize(String s) {
        if (s == null) {
            return null;
        }

        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d");
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return false;
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        String serviceClassName = serviceClass.getName();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static int selectableItemBackgroundThemeLight(Context context) {
        ContextThemeWrapper contextTheme = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_DarkActionBar);
        TypedValue outValue = new TypedValue();
        contextTheme.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        return outValue.resourceId;
    }

    public static int selectableItemBackgroundThemeDark(Context context) {
        ContextThemeWrapper contextTheme = new ContextThemeWrapper(context, R.style.Theme_AppCompat);
        TypedValue outValue = new TypedValue();
        contextTheme.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        return outValue.resourceId;
    }

    public static void swap(List<?> list, int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(list, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(list, i, i - 1);
            }
        }
    }

    public static <T> ArrayList<T> toArrayList(T... a) {
        ArrayList<T> arrayList = new ArrayList<>(a.length);
        Collections.addAll(arrayList, a);
        return arrayList;
    }
}
