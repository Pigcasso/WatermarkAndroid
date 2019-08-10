package io.zhuliang.watermark.util;

import android.content.Context;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/08/11
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class DimenUtil {

    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据手机分辨率从DP转成PX
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
