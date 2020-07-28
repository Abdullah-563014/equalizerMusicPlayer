package com.ahihi.moreapps.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {

    public static void setShared(Context context, String key, long value) {
        SharedPreferences pref = context.getSharedPreferences("MY_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getShared(Context context, String key, long value) {
        SharedPreferences pref = context.getSharedPreferences("MY_PREF", Context.MODE_PRIVATE);
        return pref.getLong(key, value);
    }

    public static void setShared(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences("MY_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getShared(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences("MY_PREF", Context.MODE_PRIVATE);
        return pref.getString(key, value);
    }
}
