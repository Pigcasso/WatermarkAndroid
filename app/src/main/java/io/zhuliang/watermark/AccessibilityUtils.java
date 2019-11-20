package io.zhuliang.watermark;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */

class AccessibilityUtils {
    static boolean isServiceOn(Context context) {
        int i;
        String sb2 = context.getPackageName() + "/" + WatermarkService.class.getCanonicalName();
        try {
            i = Settings.Secure.getInt(context.getContentResolver(), "accessibility_enabled");
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            i = 0;
        }
        TextUtils.SimpleStringSplitter simpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
        if (i == 1) {
            String string = Settings.Secure.getString(context.getContentResolver(), "enabled_accessibility_services");
            if (string != null) {
                simpleStringSplitter.setString(string);
                while (simpleStringSplitter.hasNext()) {
                    if (simpleStringSplitter.next().equalsIgnoreCase(sb2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static void openAccessibilityPage(Context context) {
        try {
            context.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
