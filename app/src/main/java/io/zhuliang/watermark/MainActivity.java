package io.zhuliang.watermark;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_PICK_PHOTO = 123;
    private static final int REQUEST_CODE_GLOBAL = 124;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (REQUEST_CODE_PICK_PHOTO == requestCode) {
            if (Activity.RESULT_OK == resultCode && data != null && data.getData() != null) {
                startActivity(WatermarkEditorActivity.makeIntent(this, data.getData()));
            }
        } else if (REQUEST_CODE_GLOBAL == requestCode) {
            boolean isServiceOn = AccessibilityUtils.isServiceOn(this);
            if (Activity.RESULT_OK == resultCode) {
                // 如果开启了无障碍模式，则刷新水印属性，显示全局水印
                if (isServiceOn) {
                    WatermarkManager watermarkManager = WatermarkManager.newInstance(this, false);
                    watermarkManager.refresh();
                    watermarkManager.show();
                } else {
                    // 如果没开启无障碍模式，则打开设置页面
                    AccessibilityUtils.openAccessibilityPage(this);
                }
            } else {
                // 如果开启了无障碍模式，则回复全局水印
                if (isServiceOn) {
                    WatermarkManager.newInstance(this, false).show();
                }
            }
        }
    }

    public void onSelectPicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    public void onGlobalWatermark(View view) {
        if (AccessibilityUtils.isServiceOn(this)) {
            WatermarkManager.newInstance(this, false).hide();
        }
        startActivityForResult(WatermarkEditorActivity.makeIntent(this), REQUEST_CODE_GLOBAL);
    }

    public void onAccessibilityPage(View view) {
        AccessibilityUtils.openAccessibilityPage(this);
    }

    private void showAboutDialog() {
        View view = View.inflate(this, R.layout.dialog_about, null);
        TextView aboutTv = view.findViewById(R.id.tv_wm_about);
        aboutTv.setMovementMethod(LinkMovementMethod.getInstance());
        new AlertDialog.Builder(this)
                .setTitle(R.string.watermark_about)
                .setView(view)
                .show();
    }
}
