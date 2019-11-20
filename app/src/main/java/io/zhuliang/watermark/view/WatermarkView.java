package io.zhuliang.watermark.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import io.zhuliang.watermark.util.DimenUtil;

/**
 * @author ZhuLiang
 * @since 2019/08/10 09:29
 */
public class WatermarkView extends ImageView {

    /**
     * 是否显示辅助线
     */
    private boolean guideline = false;

    private int mDegrees = 35;
    private Paint mTextPaint;
    private Paint mTextBoundsPaint;
    private Paint mPointPaint;
    private Rect mTextBounds;
    private Paint mBitmapBoundsPaint;

    private String mWatermarkText = "Watermark";

    private int mSpacing;

    private float initScale;
    private Matrix scaleMatrix = new Matrix();

    private int mTextSize;

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
        mTextSize = DimenUtil.sp2px(getContext(), 12);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        Drawable d = getDrawable();
        int dw, dh;
        if (d == null) {
            dw = width;
            dh = height;
        } else {
            // 拿到图片的宽和高
            dw = d.getIntrinsicWidth();
            dh = d.getIntrinsicHeight();
        }

        initScale = Math.min(width * 1f / dw, height * 1f / dh);
        mTextPaint.setTextSize(mTextSize * initScale);
        setMeasuredDimension((int) (dw * initScale), (int) (dh * initScale));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Drawable d = getDrawable();
        int dw, dh;
        if (d == null) {
            dw = width;
            dh = height;
        } else {
            // 拿到图片的宽和高
            dw = d.getIntrinsicWidth();
            dh = d.getIntrinsicHeight();
        }
        // 图片移动至屏幕中心
        scaleMatrix.reset();
        scaleMatrix.postTranslate((width - dw) / 2f, (height - dh) / 2f);
        scaleMatrix.postScale(initScale, initScale, width / 2f, height / 2f);
        setImageMatrix(scaleMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int drawableWidth, drawableHeight;
        Drawable d = getDrawable();
        if (d == null) {
            drawableWidth = getWidth();
            drawableHeight = getHeight();
        } else {
            drawableWidth = d.getIntrinsicWidth();
            drawableHeight = d.getIntrinsicHeight();
        }
        float dw = drawableWidth * initScale;
        float dh = drawableHeight * initScale;
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

        float dl = (getWidth() - (columns * mTextBounds.width() + (columns - 1) * horizontalSpacing)) / 2;
        float dt = (getHeight() - (rows * mTextBounds.height() + (rows - 1) * verticalSpacing)) / 2;

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

    public int getWatermarkRotation() {
        return mDegrees;
    }

    public void setWatermarkColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public String getWatermarkText() {
        return mWatermarkText;
    }

    public void setWatermarkText(String text) {
        mWatermarkText = text;
        invalidate();
    }

    public void setWatermarkTextSize(int textSize) {
        mTextSize = textSize;
        mTextPaint.setTextSize(mTextSize * initScale);
        invalidate();
    }

    public int getWatermarkTextSize() {
        return mTextSize;
    }

    public void setWatermarkSpacing(int spacing) {
        mSpacing = spacing;
        invalidate();
    }

    public int getWatermarkSpacing() {
        return mSpacing;
    }

    public void setGuideline(boolean guideline) {
        this.guideline = guideline;
        invalidate();
    }

    public boolean isGuideline() {
        return guideline;
    }

    public Bitmap getWatermarkBitmap() {
        Drawable d = getDrawable();
        Bitmap canvasBitmap;
        if (d == null) {
            canvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        Canvas canvas = new Canvas(canvasBitmap);
        int width = canvasBitmap.getWidth();
        int height = canvas.getHeight();
        if (guideline) {
            canvas.drawRect(0, 0, width, height, mBitmapBoundsPaint);
        }

        Paint textPaint = new Paint(mTextPaint);
        textPaint.setTextSize(mTextSize);
        RectF textBounds = new RectF(mTextBounds.left / initScale, mTextBounds.top / initScale, mTextBounds.right / initScale, mTextBounds.bottom / initScale);

        // textPaint.getTextBounds(mWatermarkText, 0, mWatermarkText.length(), textBounds);

        float verticalSpacing = Math.max(textBounds.width(), textBounds.height()) - textBounds.height() + mSpacing;
        float horizontalSpacing = mSpacing;

        // 计算列数
        int columns = (int) Math.ceil(width / (textBounds.width() + horizontalSpacing));
        // 计算行数
        int rows = (int) Math.ceil(height / verticalSpacing);

        float dl = (width - (columns * textBounds.width() + (columns - 1) * horizontalSpacing)) / 2;
        float dt = (height - (rows * textBounds.height() + (rows - 1) * verticalSpacing)) / 2;

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
