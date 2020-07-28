package com.nvp.widget;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * @author nguyenvietphu6794@gmail.com
 * Created on 11/17/2016.
 */
public class MarqueeTextView extends AppCompatTextView {
    public MarqueeTextView(Context context) {
        super(context);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSingleLine(true);
        setHorizontallyScrolling(true);
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setHorizontalFadingEdgeEnabled(true);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    public boolean isPressed() {
        return true;
    }
}
