package power.audio.pro.music.player.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.nvp.util.NvpUtils;

import power.audio.pro.music.player.R;

public class CustomProgressBar extends View {
    private long max = 100;
    private long progress;

    private Paint progressPaint;
    private Paint disabledProgressPaint;

    public CustomProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public CustomProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        int progressColor = NvpUtils.getColorAccentFromTheme(context);
        int disabledProgressColor = NvpUtils.getColorPrimaryFromTheme(context);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomProgressBar);
            progressColor = ta.getColor(R.styleable.CustomProgressBar_progressColor, progressColor);
            disabledProgressColor = ta.getColor(R.styleable.CustomProgressBar_disabledProgressColor, disabledProgressColor);
            ta.recycle();
        }

        progressPaint = new Paint();
        progressPaint.setColor(progressColor);

        disabledProgressPaint = new Paint();
        disabledProgressPaint.setColor(disabledProgressColor);
    }

    public void setMax(long max) {
        if (max > 0) {
            this.max = max;
        }
        invalidate();
    }

    public void setProgress(long value) {
        this.progress = value;
        invalidate();
    }

    public long getMax() {
        return max;
    }

    public long getProgress() {
        return progress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        progressPaint.setStrokeWidth(MeasureSpec.getSize(heightMeasureSpec));
        disabledProgressPaint.setStrokeWidth(MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, disabledProgressPaint);
        canvas.drawLine(0, getHeight() / 2, progress >= max ? getWidth() : progress * getWidth() / max, getHeight() / 2, progressPaint);
    }
}
