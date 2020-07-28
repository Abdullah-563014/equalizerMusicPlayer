package com.ahihi.moreapps;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.ahihi.moreapps.adapter.MoreAppAdapter;
import com.ahihi.moreapps.callback.OnSelectedApp;
import com.ahihi.moreapps.model.MoreApp;
import com.ahihi.moreapps.util.Utils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MoreAppDialog implements OnSelectedApp {
    private Activity mContext;
    private RequestQueue mRequestQueue;
    private String mUrl;
    private int mType;
    private DialogInterface.OnCancelListener onCancelListener;


    public MoreAppDialog(final Activity mContext, String mUrl, int mType, int timeCallAPI, String currentLang) {
        this.mContext = mContext;
        this.mType = mType;
        this.mUrl = mUrl;
        mRequestQueue = Volley.newRequestQueue(mContext);

        SharedPreferences settings = mContext.getSharedPreferences("MY_SHARED", Context.MODE_PRIVATE);
        if (settings.getBoolean("IS_FIRST_TIME", true)) {
            callAPI(currentLang);
            settings.edit().putBoolean("IS_FIRST_TIME", false).apply();
        } else {
            long currentTime = System.currentTimeMillis();
            long lastTime = Utils.getShared(mContext, "LAST_TIME", 0);
            String lastLang = Utils.getShared(mContext, "CHANGE_LANG", "");
            if ((currentTime - lastTime >= timeCallAPI * 60 * 1000) || !currentLang.equals(lastLang)) {
                callAPI(currentLang);
            }
        }
    }

    private void callAPI(final String currentLang) {
        JsonObjectRequest moreReq = new JsonObjectRequest(Request.Method.GET, mUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray moreArr = response.getJSONArray("more_apps");
                    List<MoreApp> moreApps = new ArrayList<>();
                    for (int i = 0; i < moreArr.length(); i++) {
                        JSONObject jsonObject = moreArr.getJSONObject(i);
                        MoreApp moreApp = new MoreApp();
                        moreApp.setUrlImage(jsonObject.getString("url_image"));
                        moreApp.setAppName(jsonObject.getString("app_name"));
                        moreApp.setAppId(jsonObject.getString("app_id"));
                        moreApps.add(moreApp);
                    }
                    saveSharedPreferencesLogList(mContext, moreApps);
                    long lastTime = System.currentTimeMillis();
                    Utils.setShared(mContext, "LAST_TIME", lastTime);
                    Utils.setShared(mContext, "CHANGE_LANG", currentLang);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        mRequestQueue.add(moreReq);
    }

    public boolean show() {
        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        switch (mType) {
            case 1:
                dialog.setContentView(R.layout.dialog_layout_template_1);
                break;
            case 2:
                dialog.setContentView(R.layout.dialog_layout_template_2);
                break;
            case 3:
                dialog.setContentView(R.layout.dialog_layout_template_3);
                break;
            case 4:
            default:
                dialog.setContentView(R.layout.dialog_layout_template_4);
                break;
        }
        if (bindViews(dialog, mContext).isEmpty()) {
            return false;
        }
        dialog.setOnCancelListener(onCancelListener);
        dialog.show();
        return true;
    }

    private List<MoreApp> bindViews(final Dialog dialog, final Activity context) {
        View llMain = dialog.findViewById(R.id.ll_main);
        View llTop = dialog.findViewById(R.id.llTop);
        View llBottom = dialog.findViewById(R.id.llBottom);
        ImageView ivStar = dialog.findViewById(R.id.ivStar);
        Button btnNo = dialog.findViewById(R.id.btnNo);
        Button btnYes = dialog.findViewById(R.id.btnYes);
        checkNetworkState(llTop, llBottom, llMain, ivStar);

        RecyclerView rvMoreApp = dialog.findViewById(R.id.rv_more_app);
        List<MoreApp> moreApps = loadSharedPreferencesLogList(context);
        if (moreApps.isEmpty()) {
            return moreApps;
        }

        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        if (moreApps.size() == 2 || moreApps.size() == 4) {
            manager.setSpanCount(2);
        }
        rvMoreApp.setLayoutManager(manager);
        MoreAppAdapter adapter = new MoreAppAdapter(context, moreApps, mType);
        rvMoreApp.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnSelectedApp(MoreAppDialog.this);

        if (btnNo != null) {
            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
        }
        if (btnYes != null) {
            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    context.finish();
                }
            });
        }

        return moreApps;
    }


    @Override
    public void onSelectedApp(String url) {
        try {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + url)));
        } catch (ActivityNotFoundException e) {
            try {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + url)));
            } catch (ActivityNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void checkNetworkState(View llTop, View llBottom, View llMain, ImageView ivStar) {
        if (llTop == null || llMain == null || llBottom == null || ivStar == null) {
            return;
        }
        if (isOnline()) {
            llTop.setVisibility(View.VISIBLE);
        } else {
            stateNoNetwork(llBottom, ivStar, llMain, llTop);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void saveSharedPreferencesLogList(Context context, List<MoreApp> callLog) {
        SharedPreferences mPrefs = context.getSharedPreferences("MY_SHARED", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(callLog);
        prefsEditor.putString("myJson", json);
        prefsEditor.apply();
    }

    public List<MoreApp> loadSharedPreferencesLogList(Context context) {
        String json = context.getSharedPreferences("MY_SHARED", Context.MODE_PRIVATE).getString("myJson", "");

        try {
            Type type = new TypeToken<List<MoreApp>>() {
            }.getType();

            List<MoreApp> callLog = new Gson().fromJson(json, type);

            if (callLog == null) {
                return new ArrayList<>();
            }

            Collections.shuffle(callLog);

            if (callLog.size() == 5) {
                return callLog.subList(0, 4);
            }

            return callLog.size() <= 6 ? callLog : callLog.subList(0, 6);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void stateNoNetwork(View llBottom, ImageView ivStar, View llMain, View llTop) {
        if (llTop == null || llMain == null || llBottom == null || ivStar == null) {
            return;
        }
        if (mType == 1) {
            llBottom.setBackgroundResource(R.drawable.custom_bg_no_moreapp);
            ivStar.setVisibility(View.GONE);
        } else {
            llMain.setBackgroundColor(Color.TRANSPARENT);
        }
        llTop.setVisibility(View.GONE);
    }

    public void setStyle(int style) {
        mType = style;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }
}
