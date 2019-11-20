package io.zhuliang.watermark;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import io.zhuliang.watermark.view.WatermarkView;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/14
 *     desc   :
 *     version: 1.0
 * </pre>
 */

class WatermarkManager {
    private static WatermarkManager sInstance;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private WatermarkView watermarkView;

    static WatermarkManager newInstance(Context context, boolean z) {
        if (sInstance == null || z) {
            sInstance = new WatermarkManager(context);
        }
        return sInstance;
    }

    private WatermarkManager(Context context) {
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        watermarkView = new WatermarkView(context);
        watermarkView.refresh();
        initWindowParams();
    }

    private void initWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.CENTER;
        // params.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
        params.flags = 67633432;
        params.x = 0;
        params.y = 0;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        mParams = params;
    }

    void show() {
        try {
            mWindowManager.addView(watermarkView, mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void hide() {
        try {
            mWindowManager.removeView(watermarkView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void refresh() {
        watermarkView.refresh();
    }
}
