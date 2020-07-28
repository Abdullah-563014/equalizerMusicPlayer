package com.ogaclejapan.smarttablayout;

import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

final class Utils {
    static int getMeasuredWidth(View v) {
        return (v == null) ? 0 : v.getMeasuredWidth();
    }

    static int getWidth(View v) {
        return (v == null) ? 0 : v.getWidth();
    }

    static int getWidthWithMargin(View v) {
        return getWidth(v) + getMarginHorizontally(v);
    }

    static int getStart(View v) {
        return getStart(v, false);
    }

    static int getStart(View v, boolean withoutPadding) {
        return v == null ? 0 : (isLayoutRtl(v) ? (withoutPadding ? v.getRight() - getPaddingStart(v) : v.getRight()) : (withoutPadding ? v.getLeft() + getPaddingStart(v) : v.getLeft()));
    }

    static int getEnd(View v) {
        return getEnd(v, false);
    }

    static int getEnd(View v, boolean withoutPadding) {
        return v == null ? 0 : (isLayoutRtl(v) ? (withoutPadding ? v.getLeft() + getPaddingEnd(v) : v.getLeft()) : (withoutPadding ? v.getRight() - getPaddingEnd(v) : v.getRight()));
    }

    static int getPaddingStart(View v) {
        return v == null ? 0 : ViewCompat.getPaddingStart(v);
    }

    static int getPaddingEnd(View v) {
        return v == null ? 0 : ViewCompat.getPaddingEnd(v);
    }

    static int getPaddingHorizontally(View v) {
        return v == null ? 0 : v.getPaddingLeft() + v.getPaddingRight();
    }

    static int getMarginStart(View v) {
        return v == null ? 0 : MarginLayoutParamsCompat.getMarginStart((ViewGroup.MarginLayoutParams) v.getLayoutParams());
    }

    static int getMarginEnd(View v) {
        return v == null ? 0 : MarginLayoutParamsCompat.getMarginEnd((ViewGroup.MarginLayoutParams) v.getLayoutParams());
    }

    static int getMarginHorizontally(View v) {
        if (v == null) {
            return 0;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginStart(lp) + MarginLayoutParamsCompat.getMarginEnd(lp);
    }

    static boolean isLayoutRtl(View v) {
        return ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }
}
