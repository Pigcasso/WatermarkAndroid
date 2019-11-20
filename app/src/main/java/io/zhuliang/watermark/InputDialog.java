package io.zhuliang.watermark;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <pre>
 *     author : ZhuLiang
 *     time   : 2019/11/19
 *     desc   :
 *     version: 1.0
 * </pre>
 */

public class InputDialog extends DialogFragment {
    private OnInputListener mOnInputListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInputListener) {
            mOnInputListener = (OnInputListener) context;
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnInputListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getActivity();
        if (context == null) {
            throw new NullPointerException();
        }
        final View view = View.inflate(context, R.layout.dialog_input, null);
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    EditText inputEt = view.findViewById(R.id.et_wm_input);
                    mOnInputListener.onText(inputEt.getText());
                }
            }
        };
        return new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.watermark_positive, onClickListener)
                .setNegativeButton(R.string.watermark_negative, onClickListener)
                .create();
    }

    public interface OnInputListener {
        void onText(CharSequence text);
    }
}
