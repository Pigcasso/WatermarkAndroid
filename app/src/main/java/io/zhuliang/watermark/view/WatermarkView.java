package io.zhuliang.watermark.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author ZhuLiang
 * @since 2019/08/10 09:29
 */
public class WatermarkView extends AppCompatImageView {

    private int mDegrees;
    private Paint mTextPaint;
    private Paint mTextBoundsPaint;
    private Paint mPointPaint;
    private Rect mTextBounds;

    private String mWatermarkText = "Watermark";

    private int mSpacing;

    public WatermarkView(Context context) {
        this(context, null);
    }

    public WatermarkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatermarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(50);
        mTextPaint.setColor(Color.RED);

        mTextBoundsPaint = new Paint();
        mTextBoundsPaint.setStyle(Paint.Style.STROKE);
        mTextBoundsPaint.setStrokeWidth(4);
        mTextBoundsPaint.setColor(Color.GREEN);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.BLUE);

        mTextBounds = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mTextPaint.getTextBounds(mWatermarkText, 0, mWatermarkText.length(), mTextBounds);

        int verticalSpacing = Math.max(mTextBounds.width(), mTextBounds.height()) - mTextBounds.height() + mSpacing;
        int horizontalSpacing = mSpacing;

        // 计算列数
        int columns = (int) Math.ceil(getWidth() * 1.0f / mTextBounds.width());
        // 计算行数
        int rows = (int) Math.ceil(getHeight() * 1.0f / verticalSpacing);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                canvas.save();
                int left = mTextBounds.width() * j + horizontalSpacing * j;
                int top = mTextBounds.height() * i + verticalSpacing * i;
                canvas.rotate(mDegrees, left + mTextBounds.width() / 2f, top + mTextBounds.height() / 2f);
                canvas.drawText(mWatermarkText, 0, mWatermarkText.length(), left, top + mTextBounds.height(), mTextPaint);
                /*canvas.drawRect(left, top,
                        left + mTextBounds.width(), top + mTextBounds.height(),
                        mTextBoundsPaint);
                canvas.drawCircle(left + mTextBounds.width() / 2f, top + mTextBounds.height() / 2f,
                        5, mPointPaint);*/
                canvas.restore();
            }
        }

    }

    public void setWatermarkRotation(int degrees) {
        mDegrees = degrees;
        invalidate();
    }

    public void setWatermarkColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setWatermarkText(String text) {
        mWatermarkText = text;
        invalidate();
    }

    public void setWatermarkTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        invalidate();
    }

    public void setWatermarkSpacing(int spacing) {
        mSpacing = spacing;
        invalidate();
    }
}
