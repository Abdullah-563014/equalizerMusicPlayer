package power.audio.pro.music.player.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import power.audio.pro.music.player.MainApplication;

public class SpUtils {
    public static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static void putString(String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit().putString(key, value).apply();
    }

    public static void putLong(String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit().putLong(key, value).apply();
    }

    public static void putInt(String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit().putInt(key, value).apply();
    }

    public static void putBoolean(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean def) {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).getBoolean(key, def);
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String def) {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).getString(key, def);
    }

    public static long getLong(String key) {
        return getLong(key, 0);
    }

    public static long getLong(String key, long def) {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).getLong(key, def);
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defInt) {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).getInt(key, defInt);
    }

    public static boolean contains(String key) {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).contains(key);
    }

    public static void remove(String key) {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit().remove(key).apply();
    }

    public static void clear() {
        PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext()).edit().clear().apply();
    }
}
