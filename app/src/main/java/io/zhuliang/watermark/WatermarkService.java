package io.zhuliang.watermark;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/14
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class WatermarkService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: ");
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: ");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        WatermarkManager.newInstance(this, true).show();
    }
}
