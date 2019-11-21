package io.zhuliang.watermark;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.zhuliang.watermark.util.DimenUtil;
import io.zhuliang.watermark.util.ImageUtil;
import io.zhuliang.watermark.view.WatermarkView;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class WatermarkEditorActivity extends Activity implements ColorPickerDialog.OnColorPickerListener,
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
    private int mSpacingProgress;

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
        textSizeSb.setMax(Constants.MAX_TEXT_SIZE_PROGRESS);

        // 旋转
        SeekBar rotationSb = findViewById(R.id.sb_wm_rotation);
        mRotationTv = findViewById(R.id.tv_wm_rotation);
        rotationSb.setOnSeekBarChangeListener(onSeekBarChangeListener);
        rotationSb.setMax(Constants.MAX_ROTATION);

        // 透明度
        SeekBar alphaSb = findViewById(R.id.sb_wm_alpha);
        mAlphaTv = findViewById(R.id.tv_wm_alpha);
        alphaSb.setOnSeekBarChangeListener(onSeekBarChangeListener);
        alphaSb.setMax(255);

        // 间距
        SeekBar spacingSb = findViewById(R.id.sb_wm_spacing);
        mSpacingTv = findViewById(R.id.tv_wm_spacing);
        spacingSb.setOnSeekBarChangeListener(onSeekBarChangeListener);
        spacingSb.setMax(100);

        App app = App.getInstance();
        // 字体
        int textSizeProgress = app.getWmTextSizeProgress();
        textSizeSb.setProgress(textSizeProgress);
        setWatermarkTextSize(textSizeProgress);
        // 旋转
        int rotation = app.getWmRotation();
        rotationSb.setProgress(rotation);
        setWatermarkRotation(rotation);
        // 间距
        int spacingProgress = app.getWmSpacingProgress();
        spacingSb.setProgress(spacingProgress);
        setWatermarkSpacing(spacingProgress);
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

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_color:
                        new ColorPickerDialog().show(getFragmentManager(), "color_picker");
                        break;
                    case R.id.btn_text_input:
                        new InputDialog().show(getFragmentManager(), "input");
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
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadBitmap();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_IMAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_CODE_READ_IMAGE == requestCode) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                loadBitmap();
            } else {
                showToastAndFinish(R.string.watermark_permission_failed_to_read_external_storage);
            }
        } else if (REQUEST_CODE_SAVE_IMAGE_TO_FILE == requestCode) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                saveBitmapToFile();
            } else {
                showToastAndFinish(R.string.watermark_permission_failed_to_write_external_storage);
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
            String result;
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(data, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cursor == null) {
                result = data.getPath();
            } else {
                cursor.moveToFirst();
                result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                cursor.close();
            }
            if (result == null || result.isEmpty()) {
                showToastAndFinish(R.string.watermark_failed_to_load_bitmap_file_not_found);
            } else {
                setFilePath(result);
            }
        } else {
            finish();
        }
    }

    /**
     * 参考：百度OCR SDK 的 CropView#setFilePath(String)
     */
    private void setFilePath(@NonNull String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap original = BitmapFactory.decodeFile(path, options);
        Bitmap bitmap;
        try {
            // 图片太大会导致内存泄露，所以在显示前对图片进行裁剪。
            int maxPreviewImageSize = 2560;
            int min = Math.min(options.outWidth, options.outHeight);
            min = Math.min(min, maxPreviewImageSize);

            WindowManager windowManager = getWindowManager();
            Point screenSize = new Point();
            windowManager.getDefaultDisplay().getSize(screenSize);
            min = Math.min(min, screenSize.x * 2 / 3);

            options.inSampleSize = ImageUtil.calculateInSampleSize(options, min, min);
            options.inScaled = true;
            options.inDensity = options.outWidth;
            options.inTargetDensity = min * options.inSampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(path, options);
        } catch (IOException e) {
            e.printStackTrace();
            bitmap = original;
        }
        mWatermarkView.setImageBitmap(bitmap);
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
        int sp = progress + Constants.MIN_TEXT_SIZE_PROGRESS;
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

    private void setWatermarkSpacing(int progress) {
        mSpacingProgress = progress;
        mSpacingTv.setText(String.valueOf(progress));
        int spacingPx = DimenUtil.dip2px(this, progress);
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
        app.setWmSpacing(mSpacingProgress);
        // 颜色
        app.setWmColor(Color.argb(mAlpha, mRed, mGreen, mBlue));
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void saveBitmapToFile() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_SAVE_IMAGE_TO_FILE);
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
                                showToastAndFinish(getString(R.string.watermark_save_file_to, path));
                            }
                        });
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError e) {
                        showToastAndFinish(R.string.watermark_failed_to_save_oom);
                    }
                } else {
                    showToastAndFinish(R.string.watermark_external_storage_not_mounted);
                }
            }
        });
    }

    private boolean isUiThread() {
        return Thread.currentThread() == getMainLooper().getThread();
    }

    private void showToastAndFinish(final int resId) {
        if (isUiThread()) {
            Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
            finish();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WatermarkEditorActivity.this, resId, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }

    private void showToastAndFinish(final CharSequence text) {
        if (isUiThread()) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
            finish();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(WatermarkEditorActivity.this, text, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }
}
