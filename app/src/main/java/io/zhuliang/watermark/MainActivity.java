package io.zhuliang.watermark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (REQUEST_CODE_PICK_PHOTO == requestCode) {
            if (Activity.RESULT_OK == resultCode && data != null && data.getData() != null) {
                startActivity(WatermarkEditorActivity.makeIntent(this, data.getData()));
            }
        }
    }

    public void onSelectPicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    public void onGlobalWatermark(View view) {
        startActivity(WatermarkEditorActivity.makeIntent(this));
    }
}
