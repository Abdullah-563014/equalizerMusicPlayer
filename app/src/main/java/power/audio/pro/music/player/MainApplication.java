package power.audio.pro.music.player;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.multidex.MultiDexApplication;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.gson.Gson;
import power.audio.pro.music.player.service.PlayerService;

public class MainApplication extends MultiDexApplication {
    private static Context appContext;
    private static Handler appHandler;
    private static Gson appGson;

    private static MainApplication instance;
    private static SharedPreferences pref;
    private static PlayerService service;

    //check if app is in foreground
    //this is for button actions on bluetooth headset
    public static boolean isAppVisible;

    static
    {
       AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        appContext = this;
        appHandler = new Handler(Looper.getMainLooper());
        appGson = new Gson();
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static ContentResolver getResolver() {
        return appContext.getContentResolver();
    }

    public static Handler getAppHandler() {
        return appHandler;
    }

    public static void runOnUiThread(Runnable action) {
        appHandler.post(action);
    }

    public static Gson getAppGson() {
        return appGson;
    }

    public static MainApplication getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
    }

    public static SharedPreferences getPref(){
        return pref;
    }

    public  static void setService(PlayerService s){
        service = s;
    }

    public static  PlayerService getService(){
        return  service;
    }

}
