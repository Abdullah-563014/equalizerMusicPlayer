package com.ahihi.moreapps.adapter;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.ahihi.moreapps.R;
import com.ahihi.moreapps.callback.OnSelectedApp;
import com.ahihi.moreapps.model.MoreApp;
import com.ahihi.moreapps.util.GlideApp;

import java.util.List;
import java.util.Random;

public class MoreAppAdapter extends RecyclerView.Adapter<MoreAppAdapter.MoreAppHolder> {
    private Random mRandom = new Random();
    private Context mContext;
    private List<MoreApp> mMoreApps;
    private OnSelectedApp mOnSelectedApp;
    private int mType;

    public MoreAppAdapter(Context mContext, List<MoreApp> mMoreApps, int mType) {
        this.mContext = mContext;
        this.mMoreApps = mMoreApps;
        this.mType = mType;
    }

    public void setOnSelectedApp(OnSelectedApp mOnSelectedApp) {
        this.mOnSelectedApp = mOnSelectedApp;
    }

    @NonNull
    @Override
    public MoreAppHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MoreAppHolder(LayoutInflater.from(mContext).inflate(R.layout.item_moreapp, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MoreAppHolder holder, int position) {
        final MoreApp moreApp = mMoreApps.get(position);
        if (moreApp != null) {
            int gift = mRandom.nextInt(2) == 0 ? R.drawable.ic_gift_4 : R.drawable.ic_gift;
            GlideApp.with(mContext)
                    .load(moreApp.getUrlImage())
                    .placeholder(gift)
                    .error(gift)
                    .into(holder.ivIconApp);
            holder.ivIconApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnSelectedApp != null) {
                        mOnSelectedApp.onSelectedApp(moreApp.getAppId());
                        switch (mType) {
                            case 1:
                                holder.ivIconApp.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_v1));
                                break;
                            case 2:
                                holder.ivIconApp.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_v2));
                                break;
                            case 3:
                                holder.ivIconApp.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_v3));
                                break;
                            case 4:
                            default:
                                holder.ivIconApp.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_v4));
                                break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMoreApps.size();
    }

    static class MoreAppHolder extends RecyclerView.ViewHolder {
        ImageView ivIconApp;

        MoreAppHolder(View itemView) {
            super(itemView);
            ivIconApp = itemView.findViewById(R.id.iv_app);
        }
    }

}
