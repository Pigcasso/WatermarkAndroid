package io.zhuliang.watermark;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class ColorPickerDialog extends DialogFragment {
    private static final String TAG = "ColorPickerDialog";

    private int mRed = 10;
    private int mGreen = 150;
    private int mBlue = 200;
    private View mDisplayView;
    private TextView mRedView;
    private TextView mGreenView;
    private TextView mBlueView;
    private OnColorPickerListener mOnColorPickerListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnColorPickerListener) {
            mOnColorPickerListener = (OnColorPickerListener) context;
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnColorPickerListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        if (context == null) {
            throw new NullPointerException();
        }
        View view = View.inflate(context, R.layout.dialog_color_picker, null);
        mDisplayView = view.findViewById(R.id.displayView);
        mRedView = view.findViewById(R.id.redView);
        mGreenView = view.findViewById(R.id.greenView);
        mBlueView = view.findViewById(R.id.blueView);
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: " + fromUser);
                if (fromUser) {
                    switch (seekBar.getId()) {
                        case R.id.seek_colorR:
                            mRed = progress;
                            break;
                        case R.id.seek_colorG:
                            mGreen = progress;
                            break;
                        case R.id.seek_colorB:
                            mBlue = progress;
                            break;
                    }
                    invalidColorPalette();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        SeekBar redSeekBar = view.findViewById(R.id.seek_colorR);
        SeekBar greenSeekBar = view.findViewById(R.id.seek_colorG);
        SeekBar blueSeekBar = view.findViewById(R.id.seek_colorB);
        redSeekBar.setProgress(mRed);
        greenSeekBar.setProgress(mGreen);
        blueSeekBar.setProgress(mBlue);
        redSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        greenSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        blueSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        invalidColorPalette();
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    mOnColorPickerListener.onColor(mRed, mGreen, mBlue);
                }
            }
        };
        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("确认", onClickListener)
                .setNegativeButton("取消", onClickListener)
                .create();
    }

    private void invalidColorPalette() {
        int color = Color.rgb(mRed, mGreen, mBlue);
        mRedView.setText(String.valueOf(mRed));
        mGreenView.setText(String.valueOf(mGreen));
        mBlueView.setText(String.valueOf(mBlue));
        mDisplayView.setBackgroundColor(color);
    }

    public interface OnColorPickerListener {
        void onColor(int red, int green, int blue);
    }
}
