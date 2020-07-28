package com.ahihi.ratedialog;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 10/31/17.
 */
public class RateDialog implements View.OnClickListener {
    @IntDef({BUTTON_GOOD, BUTTON_NOT_GOOD, BUTTON_REMIND_LATER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ButtonType {
    }

    public static final int BUTTON_GOOD = 0;
    public static final int BUTTON_NOT_GOOD = 1;
    public static final int BUTTON_REMIND_LATER = 2;

    public static final String PREF_SHOW_RATE = "show_rate";

    private Activity mActivity;
    private AlertDialog mDialog;

    private Drawable mBackground;
    private Drawable mIcon;
    private int mIconVisibility = View.VISIBLE;
    private Integer mIconColor;
    private String mTitle;
    private Integer mTitleColor;
    private String mMsg;
    private Integer mMsgColor;
    private Drawable mButtonBackground;
    private Integer mButtonTextColor;
    private int mBtnGoodVisibility = View.VISIBLE;
    private int mBtnNotGoodVisibility = View.VISIBLE;
    private int mBtnRemindLaterVisibility = View.VISIBLE;

    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private DialogInterface.OnKeyListener mOnKeyListener;
    private DialogInterface.OnShowListener mOnShowListener;
    private boolean canceledOnTouchOutside = true;
    private boolean cancelable = true;

    public RateDialog(Activity activity) {
        mActivity = activity;
        mTitle = activity.getString(R.string.rate_app);
        mMsg = activity.getString(R.string.rate_msg);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.rd_btn_not_good) {
            PreferenceManager.getDefaultSharedPreferences(mActivity).edit().putBoolean(PREF_SHOW_RATE, false).apply();
        } else if (id == R.id.rd_btn_good) {
            PreferenceManager.getDefaultSharedPreferences(mActivity).edit().putBoolean(PREF_SHOW_RATE, false).apply();
            try {
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + mActivity.getPackageName())));
            } catch (ActivityNotFoundException e) {
                try {
                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + mActivity.getPackageName())));
                } catch (ActivityNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        }

        mDialog.dismiss();
        mActivity.finish();
    }

    public boolean show() {
        if (!PreferenceManager.getDefaultSharedPreferences(mActivity).getBoolean(PREF_SHOW_RATE, true)) {
            return false;
        }

        boolean isLandscape = mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isLandscape) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (mDialog == null) {
            mDialog = new AlertDialog.Builder(mActivity).setCancelable(cancelable).setView(R.layout.dialog_rate).show();
            if (mDialog.getWindow() != null) {
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                if (mBackground != null) {
                    mDialog.getWindow().setBackgroundDrawable(mBackground);
                }
            }
            mDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
            if (mOnCancelListener != null) {
                mDialog.setOnCancelListener(mOnCancelListener);
            }
            if (mOnDismissListener != null) {
                mDialog.setOnDismissListener(mOnDismissListener);
            }
            if (mOnKeyListener != null) {
                mDialog.setOnKeyListener(mOnKeyListener);
            }
            if (mOnShowListener != null) {
                mDialog.setOnShowListener(mOnShowListener);
            }

            ImageView rdIcon = mDialog.findViewById(R.id.rd_icon);
            TextView rdTitle = mDialog.findViewById(R.id.rd_title);
            TextView rdMsg = mDialog.findViewById(R.id.rd_msg);
            Button rdBtnGood = mDialog.findViewById(R.id.rd_btn_good);
            Button rdBtnNotGood = mDialog.findViewById(R.id.rd_btn_not_good);
            Button rdBtnRemindLater = mDialog.findViewById(R.id.rd_btn_remind_later);

            if (rdIcon != null) {
                rdIcon.setVisibility(mIconVisibility);
                if (mIcon != null) {
                    rdIcon.setImageDrawable(mIcon);
                }
                if (mIconColor != null) {
                    rdIcon.setColorFilter(mIconColor);
                }
            }

            if (rdTitle != null) {
                rdTitle.setText(mTitle);
                if (mTitleColor != null) {
                    rdTitle.setTextColor(mTitleColor);
                }
            }

            if (rdMsg != null) {
                rdMsg.setText(mMsg);
                if (mMsgColor != null) {
                    rdMsg.setTextColor(mMsgColor);
                }
            }

            if (rdBtnGood != null) {
                rdBtnGood.setVisibility(mBtnGoodVisibility);
                rdBtnGood.setOnClickListener(this);
                if (mButtonBackground != null) {
                    rdBtnGood.setBackground(mButtonBackground);
                }
                if (mButtonTextColor != null) {
                    rdBtnGood.setTextColor(mButtonTextColor);
                }
            }

            if (rdBtnNotGood != null) {
                rdBtnNotGood.setVisibility(mBtnNotGoodVisibility);
                rdBtnNotGood.setOnClickListener(this);
                if (mButtonBackground != null) {
                    rdBtnNotGood.setBackground(mButtonBackground);
                }
                if (mButtonTextColor != null) {
                    rdBtnNotGood.setTextColor(mButtonTextColor);
                }
            }

            if (rdBtnRemindLater != null) {
                rdBtnRemindLater.setVisibility(mBtnRemindLaterVisibility);
                rdBtnRemindLater.setOnClickListener(this);
                if (mButtonBackground != null) {
                    rdBtnRemindLater.setBackground(mButtonBackground);
                }
                if (mButtonTextColor != null) {
                    rdBtnRemindLater.setTextColor(mButtonTextColor);
                }
            }
        } else {
            mDialog.show();
        }

        if (isLandscape) {
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    if (mOnCancelListener != null) {
                        mOnCancelListener.onCancel(dialog);
                    }
                }
            });
        }

        return true;
    }

    public RateDialog setBackground(Drawable drawable) {
        mBackground = drawable;
        return this;
    }

    public RateDialog setBackgroundRes(@DrawableRes int drawableRes) {
        mBackground = ContextCompat.getDrawable(mActivity, drawableRes);
        return this;
    }

    public RateDialog setIconVisibility(int visibility) {
        mIconVisibility = visibility;
        return this;
    }

    public RateDialog setIcon(Drawable drawable) {
        mIcon = drawable;
        return this;
    }

    public RateDialog setIconRes(@DrawableRes int drawableRes) {
        mIcon = ContextCompat.getDrawable(mActivity, drawableRes);
        return this;
    }

    public RateDialog setIconColor(@ColorInt int color) {
        mIconColor = color;
        return this;
    }

    public RateDialog setIconColorRes(@ColorRes int colorRes) {
        mIconColor = ContextCompat.getColor(mActivity, colorRes);
        return this;
    }

    public RateDialog setTitle(String title) {
        mTitle = title;
        return this;
    }

    public RateDialog setTitleRes(@StringRes int titleRes) {
        mTitle = mActivity.getString(titleRes);
        return this;
    }

    public RateDialog setTitleColor(@ColorInt int color) {
        mTitleColor = color;
        return this;
    }

    public RateDialog setTitleColorRes(@ColorRes int colorRes) {
        mTitleColor = ContextCompat.getColor(mActivity, colorRes);
        return this;
    }

    public RateDialog setMsgColor(@ColorInt int color) {
        mMsgColor = color;
        return this;
    }

    public RateDialog setMsgColorRes(@ColorRes int colorRes) {
        mMsgColor = ContextCompat.getColor(mActivity, colorRes);
        return this;
    }

    public RateDialog setMsg(String msg) {
        mMsg = msg;
        return this;
    }

    public RateDialog setMsgRes(@StringRes int msgRes) {
        mMsg = mActivity.getString(msgRes);
        return this;
    }

    public RateDialog setButtonBackground(Drawable drawable) {
        mButtonBackground = drawable;
        return this;
    }

    public RateDialog setButtonBackgroundRes(@DrawableRes int drawableRes) {
        mButtonBackground = ContextCompat.getDrawable(mActivity, drawableRes);
        return this;
    }

    public RateDialog setButtonTextColor(@ColorInt int color) {
        mButtonTextColor = color;
        return this;
    }

    public RateDialog setButtonTextColorRes(@ColorRes int colorRes) {
        mButtonTextColor = ContextCompat.getColor(mActivity, colorRes);
        return this;
    }

    public RateDialog setButtonVisibility(@ButtonType int buttonType, int visibility) {
        switch (buttonType) {
            case BUTTON_GOOD:
                mBtnGoodVisibility = visibility;
                break;
            case BUTTON_NOT_GOOD:
                mBtnNotGoodVisibility = visibility;
                break;
            case BUTTON_REMIND_LATER:
                mBtnRemindLaterVisibility = visibility;
                break;
        }
        return this;
    }

    public RateDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        mOnCancelListener = listener;
        return this;
    }

    public RateDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mOnDismissListener = listener;
        return this;
    }

    public RateDialog setOnKeyListener(DialogInterface.OnKeyListener listener) {
        mOnKeyListener = listener;
        return this;
    }

    public RateDialog setOnShowListener(DialogInterface.OnShowListener listener) {
        mOnShowListener = listener;
        return this;
    }

    public RateDialog setCanceledOnTouchOutside(boolean cancel) {
        canceledOnTouchOutside = cancel;
        return this;
    }

    public RateDialog setCancelable(boolean cancel) {
        cancelable = cancel;
        return this;
    }

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    public void cancel() {
        if (mDialog != null) {
            mDialog.cancel();
        }
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}
