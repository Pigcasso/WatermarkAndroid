package io.zhuliang.watermark;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.zhuliang.watermark.util.DimenUtil;
import io.zhuliang.watermark.view.WatermarkView;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class WatermarkEditorActivity extends AppCompatActivity implements ColorPickerDialog.OnColorPickerListener,
        InputDialog.OnInputListener {
    private static final int REQUEST_CODE_READ_IMAGE = 233;
    private static final int REQUEST_CODE_SAVE_IMAGE_TO_FILE = 666;

    private TextView mTextSizeTv;
    private TextView mRotationTv;
    private TextView mAlphaTv;
    private TextView mSpacingTv;
    private WatermarkView mWatermarkView;
    private int mAlpha;
    private int mRed;
    private int mGreen;
    private int mBlue;

    /**
     * 是否是全局水印
     */
    private boolean isGlobalMode;

    /**
     * {@link SeekBar#setMin(int)} 是 {@link android.os.Build.VERSION_CODES#O} 新增的
     */
    private int mTextSizeProgress;

    public static Intent makeIntent(Context context, Uri data) {
        Intent starter = new Intent(context, WatermarkEditorActivity.class);
        starter.setData(data);
        return starter;
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, WatermarkEditorActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watermark_editor);
        isGlobalMode = getIntent().getData() == null;
        if (isGlobalMode) {
            WatermarkManager.newInstance(this, false).hide();
            WatermarkView watermarkView = findViewById(R.id.watermarkView);
            watermarkView.setVisibility(View.GONE);
            mWatermarkView = findViewById(R.id.fullscreenWatermarkView);
            mWatermarkView.setVisibility(View.VISIBLE);
        } else {
            WatermarkView watermarkView = findViewById(R.id.fullscreenWatermarkView);
            watermarkView.setVisibility(View.GONE);
            mWatermarkView = findViewById(R.id.watermarkView);
            mWatermarkView.setVisibility(View.VISIBLE);
        }
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    switch (seekBar.getId()) {
                        case R.id.sb_wm_text_size:
                            setWatermarkTextSize(progress);
                            break;
                        case R.id.sb_wm_rotation:
                            setWatermarkRotation(progress);
                            break;
                        case R.id.sb_wm_alpha:
                            setWatermarkAlpha(progress);
                            break;
                        case R.id.sb_wm_spacing:
                            setWatermarkSpacing(progress);
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        // 字体
        SeekBar textSizeSb = findViewById(R.id.sb_wm_text_size);
        mTextSizeTv = findViewById(R.id.tv_wm_text_size);
        textSizeSb.setOnSeekBarChangeListener(onSeekBarChangeListener);
        textSizeSb.setMax(100);

        // 旋转
        SeekBar rotationSb = findViewById(R.id.sb_wm_rotation);
        mRotationTv = findViewById(R.id.tv_wm_rotation);
        rotationSb.setOnSeekBarChangeListener(onSeekBarChangeListener);
        rotationSb.setMax(360);

        // 透明度
        SeekBar alphaSb = findViewById(R.id.sb_wm_alpha);
        mAlphaTv = findViewById(R.id.tv_wm_alpha);
        alphaSb.setOnSeekBarChangeListener(onSeekBarChangeListener);
        alphaSb.setMax(255);

        // 间距
        SeekBar spacingSb = findViewById(R.id.sb_wm_spacing);
        mSpacingTv = findViewById(R.id.tv_wm_spacing);
        spacingSb.setOnSeekBarChangeListener(onSeekBarChangeListener);
        spacingSb.setMax(Constants.MAX_TEXT_SIZE_PROGRESS);

        if (isGlobalMode) {
            App app = App.getInstance();
            // 字体
            int progress = app.getWmTextSizeProgress();
            textSizeSb.setProgress(progress);
            setWatermarkTextSize(progress);
            // 旋转
            int rotation = app.getWmRotation();
            rotationSb.setProgress(rotation);
            setWatermarkRotation(rotation);
            // 间距
            int spacing = app.getWmSpacing();
            spacingSb.setProgress(spacing);
            setWatermarkSpacing(spacing);
            // 透明度
            int color = app.getWmColor();
            mAlpha = Color.alpha(color);
            mRed = Color.red(color);
            mGreen = Color.green(color);
            mBlue = Color.blue(color);
            alphaSb.setProgress(mAlpha);
            setWatermarkAlpha(mAlpha);
            // 文字
            mWatermarkView.setWatermarkText(app.getWmText());
        } else {
            // 字体
            textSizeSb.setProgress(0);
            setWatermarkTextSize(0);
            // 旋转
            rotationSb.setProgress(35);
            setWatermarkRotation(35);
            // 间距
            spacingSb.setProgress(0);
            setWatermarkSpacing(0);
            // 透明度
            alphaSb.setProgress(127);
            setWatermarkAlpha(127);
        }


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_color:
                        new ColorPickerDialog().show(getSupportFragmentManager(), "color_picker");
                        break;
                    case R.id.btn_text_input:
                        new InputDialog().show(getSupportFragmentManager(), "input");
                        break;
                    case R.id.btn_save:
                        if (isGlobalMode) {
                            applyToGlobal();
                        } else {
                            saveBitmapToFile();
                        }
                        break;
                }
            }
        };
        findViewById(R.id.btn_color).setOnClickListener(onClickListener);
        findViewById(R.id.btn_text_input).setOnClickListener(onClickListener);
        findViewById(R.id.btn_save).setOnClickListener(onClickListener);

        if (!isGlobalMode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadBitmap();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_IMAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE_READ_IMAGE == requestCode) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                loadBitmap();
            } else {
                Toast.makeText(this, R.string.watermark_permission_failed_to_read_externail_storage, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (REQUEST_CODE_SAVE_IMAGE_TO_FILE == requestCode) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                saveBitmapToFile();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_watermark_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.wm_main_guideline).setChecked(mWatermarkView.isGuideline());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.wm_main_guideline) {
            boolean isGuideline = !mWatermarkView.isGuideline();
            mWatermarkView.setGuideline(isGuideline);
            item.setChecked(isGuideline);
        }
        return true;
    }

    private void loadBitmap() {
        Uri data = getIntent().getData();
        if (data != null) {
            Bitmap bitmap = null;
            if ("file".equals(data.getScheme())) {
                bitmap = BitmapFactory.decodeFile(data.getPath());
            } else if ("content".equals(data.getScheme())) {
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(data));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (bitmap != null) {
                mWatermarkView.setImageBitmap(bitmap);
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onColor(int red, int green, int blue) {
        mRed = red;
        mGreen = green;
        mBlue = blue;
        int color = Color.argb(mAlpha, mRed, mGreen, mBlue);
        mWatermarkView.setWatermarkColor(color);
    }

    @Override
    public void onText(CharSequence text) {
        if (text.length() > 0) {
            mWatermarkView.setWatermarkText(text.toString());
        }
    }

    private void setWatermarkTextSize(int progress) {
        mTextSizeProgress = progress;
        int sp = progress + Constants.MIN_TEXT_SIZE_SP;
        mTextSizeTv.setText(String.valueOf(sp));
        mWatermarkView.setWatermarkTextSize(DimenUtil.sp2px(this, sp));
    }

    private void setWatermarkRotation(int rotation) {
        mRotationTv.setText(String.valueOf(rotation));
        mWatermarkView.setWatermarkRotation(rotation);
    }

    private void setWatermarkAlpha(int alpha) {
        mAlpha = alpha;
        mAlphaTv.setText(String.valueOf(alpha));
        int color = Color.argb(alpha, mRed, mGreen, mBlue);
        mWatermarkView.setWatermarkColor(color);
    }

    private void setWatermarkSpacing(int spacing) {
        mSpacingTv.setText(String.valueOf(spacing));
        int spacingPx = DimenUtil.dip2px(this, spacing);
        mWatermarkView.setWatermarkSpacing(spacingPx);
    }

    private void applyToGlobal() {
        App app = App.getInstance();
        // 文字
        app.setWmText(mWatermarkView.getWatermarkText());
        // 字体
        app.setWmTextSizeProgress(mTextSizeProgress);
        // 旋转
        app.setWmRotation(mWatermarkView.getWatermarkRotation());
        // 间距
        app.setWmSpacing(mWatermarkView.getWatermarkSpacing());
        // 颜色
        app.setWmColor(Color.argb(mAlpha, mRed, mGreen, mBlue));
        if (isServiceOn()) {
            WatermarkManager.newInstance(this, false).show();
        } else {
            openAccessibilityPage();
        }
        finish();
    }

    private boolean isServiceOn() {
        int i;
        String sb2 = getPackageName() + "/" + WatermarkService.class.getCanonicalName();
        try {
            i = Settings.Secure.getInt(getContentResolver(), "accessibility_enabled");
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            i = 0;
        }
        TextUtils.SimpleStringSplitter simpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
        if (i == 1) {
            String string = Settings.Secure.getString(getContentResolver(), "enabled_accessibility_services");
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

    private void openAccessibilityPage() {
        try {
            startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBitmapToFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_SAVE_IMAGE_TO_FILE);
            return;
        }
        WatermarkThreadTool.execute(new Runnable() {
            @Override
            public void run() {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.getDefault());
                    final File file = new File(sdCard, String.format("wx-%s.png", sdf.format(new Date())));

                    try (OutputStream os = new FileOutputStream(file)) {
                        Bitmap bitmap = mWatermarkView.getWatermarkBitmap();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        MediaScannerConnection.scanFile(WatermarkEditorActivity.this, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(final String path, Uri uri) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(WatermarkEditorActivity.this, "保存文件到：" + path, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
