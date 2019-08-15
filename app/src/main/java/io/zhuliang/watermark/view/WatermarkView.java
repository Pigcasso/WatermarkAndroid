package io.zhuliang.watermark.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatImageView;

import io.zhuliang.watermark.util.DimenUtil;

/**
 * @author ZhuLiang
 * @since 2019/08/10 09:29
 */
public class WatermarkView extends AppCompatImageView implements ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * 是否显示辅助线
     */
    private boolean guideline = false;

    private int mDegrees;
    private Paint mTextPaint;
    private Paint mTextBoundsPaint;
    private Paint mPointPaint;
    private Rect mTextBounds;
    private Paint mBitmapBoundsPaint;

    private String mWatermarkText = "Watermark";

    private int mSpacing;

    private boolean once = true;
    private float initScale;
    private Matrix scaleMatrix = new Matrix();

    private float mTextSize;

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
        mTextSize = DimenUtil.sp2px(getContext(), 25);
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.RED);

        mTextBoundsPaint = new Paint();
        mTextBoundsPaint.setStyle(Paint.Style.STROKE);
        mTextBoundsPaint.setStrokeWidth(4);
        mTextBoundsPaint.setColor(Color.GREEN);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.BLUE);

        mTextBounds = new Rect();

        mBitmapBoundsPaint = new Paint();
        mBitmapBoundsPaint.setStyle(Paint.Style.STROKE);
        mBitmapBoundsPaint.setStrokeWidth(4);
        mBitmapBoundsPaint.setColor(Color.MAGENTA);

        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        if (once) {

            Drawable d = getDrawable();
            if (d == null) {
                return;
            }
            int width = getWidth();
            int height = getHeight();
            // 拿到图片的宽和高
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();
            float scale = Math.min(width * 1f / dw, height * 1f / dh);
            initScale = scale;
            // 图片移动至屏幕中心
            scaleMatrix.postTranslate((width - dw) / 2f, (height - dh) / 2f);
            scaleMatrix.postScale(scale, scale, width / 2f, height / 2f);
            setImageMatrix(scaleMatrix);

            mTextPaint.setTextSize(mTextSize * initScale);
            once = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Drawable d = getDrawable();
        if (d == null) {
            return;
        }
        int width = getWidth();
        int height = getHeight();
        float dw = d.getIntrinsicWidth() * initScale;
        float dh = d.getIntrinsicHeight() * initScale;
        if (guideline) {
            canvas.drawRect((width - dw) / 2f,
                    (height - dh) / 2f,
                    (width - dw) / 2f + dw,
                    (height - dh) / 2f + dh,
                    mBitmapBoundsPaint);
        }

        mTextPaint.getTextBounds(mWatermarkText, 0, mWatermarkText.length(), mTextBounds);

        float verticalSpacing = Math.max(mTextBounds.width(), mTextBounds.height()) - mTextBounds.height() + mSpacing * initScale;
        float horizontalSpacing = mSpacing * initScale;

        // 计算列数
        int columns = (int) Math.ceil(dw / (mTextBounds.width() + horizontalSpacing));
        // 计算行数
        int rows = (int) Math.ceil(dh / verticalSpacing);

        float dl = (getWidth() - columns * (mTextBounds.width() + horizontalSpacing)) / 2f;
        float dt = (getHeight() - rows * (mTextBounds.height() + verticalSpacing)) / 2f;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                canvas.save();
                float left = dl + mTextBounds.width() * j + horizontalSpacing * j;
                float top = dt + mTextBounds.height() * i + verticalSpacing * i;
                canvas.rotate(mDegrees, left + mTextBounds.width() / 2f, top + mTextBounds.height() / 2f);
                canvas.drawText(mWatermarkText, 0, mWatermarkText.length(), left, top + mTextBounds.height(), mTextPaint);
                if (guideline) {
                    canvas.drawRect(left, top,
                            left + mTextBounds.width(), top + mTextBounds.height(),
                            mTextBoundsPaint);
                    canvas.drawCircle(left + mTextBounds.width() / 2f, top + mTextBounds.height() / 2f,
                            5, mPointPaint);
                }
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
        mTextSize = textSize;
        mTextPaint.setTextSize(mTextSize * initScale);
        invalidate();
    }

    public void setWatermarkSpacing(int spacing) {
        mSpacing = spacing;
        invalidate();
    }

    public void setGuideline(boolean guideline) {
        this.guideline = guideline;
        invalidate();
    }

    public boolean isGuideline() {
        return guideline;
    }

    public Bitmap getWatermarkBitmap() {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        Bitmap canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(canvasBitmap);
        int width = canvasBitmap.getWidth();
        int height = canvas.getHeight();
        if (guideline) {
            canvas.drawRect(0, 0, width, height, mBitmapBoundsPaint);
        }

        Paint textPaint = new Paint(mTextPaint);
        textPaint.setTextSize(mTextSize);
        Rect textBounds = new Rect();

        textPaint.getTextBounds(mWatermarkText, 0, mWatermarkText.length(), textBounds);

        float verticalSpacing = Math.max(textBounds.width(), textBounds.height()) - textBounds.height() + mSpacing;
        float horizontalSpacing = mSpacing;

        // 计算列数
        int columns = (int) Math.ceil(width / (textBounds.width() + horizontalSpacing));
        // 计算行数
        int rows = (int) Math.ceil(height / verticalSpacing);

        float dl = (width - columns * (textBounds.width() + horizontalSpacing)) / 2f;
        float dt = (height - rows * (textBounds.height() + verticalSpacing)) / 2f;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                canvas.save();
                float left = dl + textBounds.width() * j + horizontalSpacing * j;
                float top = dt + textBounds.height() * i + verticalSpacing * i;
                canvas.rotate(mDegrees, left + textBounds.width() / 2f, top + textBounds.height() / 2f);
                canvas.drawText(mWatermarkText, 0, mWatermarkText.length(), left, top + textBounds.height(), textPaint);
                if (guideline) {
                    canvas.drawRect(left, top,
                            left + textBounds.width(), top + textBounds.height(),
                            mTextBoundsPaint);
                    canvas.drawCircle(left + textBounds.width() / 2f, top + textBounds.height() / 2f,
                            5, mPointPaint);
                }
                canvas.restore();
            }
        }

        return canvasBitmap;
    }
}
