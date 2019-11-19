package io.zhuliang.watermark;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import io.zhuliang.watermark.util.DimenUtil;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class App extends Application {

    private static final String SP_NAME = "watermark";
    private static final String WM_TEXT_SIZE = "wm_text_size";
    private static final String WM_TEXT = "wm_text";
    private static final String WM_ROTATION = "wm_rotation";
    private static final String WM_COLOR = "wm_color";
    private static final String WM_SPACING = "wm_spacing";

    private static App sInstance;
    private SharedPreferences mSharedPreferences;

    public static App getInstance() {
        if (sInstance == null) {
            throw new NullPointerException();
        }
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mSharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public int getWmTextSizeProgress() {
        return mSharedPreferences.getInt(WM_TEXT_SIZE, 0);
    }

    public void setWmTextSizeProgress(int textSize) {
        if (textSize < 0 || textSize > Constants.MAX_TEXT_SIZE_PROGRESS) {
            throw new IllegalArgumentException(textSize + " is invalid.");
        }
        mSharedPreferences.edit().putInt(WM_TEXT_SIZE, textSize).apply();
    }

    public int getWmTextSizePx() {
        int textSize = getWmTextSizeProgress();
        return DimenUtil.sp2px(this, textSize + Constants.MIN_TEXT_SIZE_SP);
    }

    public String getWmText() {
        return mSharedPreferences.getString(WM_TEXT, getString(R.string.app_name));
    }

    public void setWmText(String text) {
        mSharedPreferences.edit().putString(WM_TEXT, text).apply();
    }

    public int getWmRotation() {
        return mSharedPreferences.getInt(WM_ROTATION, 35);
    }

    public void setWmRotation(int rotation) {
        mSharedPreferences.edit().putInt(WM_ROTATION, rotation).apply();
    }

    public int getWmColor() {
        return mSharedPreferences.getInt(WM_COLOR, Color.argb(127, 0, 0, 0));
    }

    public void setWmColor(int color) {
        mSharedPreferences.edit().putInt(WM_COLOR, color).apply();
    }

    public int getWmSpacingProgress() {
        return mSharedPreferences.getInt(WM_SPACING, 0);
    }

    public int getWmSpacingPx() {
        int progress = getWmSpacingProgress();
        return DimenUtil.dip2px(this, progress);
    }

    public void setWmSpacing(int spacing) {
        mSharedPreferences.edit().putInt(WM_SPACING, spacing).apply();
    }

}
