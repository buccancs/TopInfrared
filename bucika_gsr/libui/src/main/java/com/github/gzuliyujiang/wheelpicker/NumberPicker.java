

package com.github.gzuliyujiang.wheelpicker;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.github.gzuliyujiang.dialog.ModalDialog;
import com.github.gzuliyujiang.wheelpicker.contract.OnNumberPickedListener;
import com.github.gzuliyujiang.wheelpicker.widget.NumberWheelLayout;
import com.github.gzuliyujiang.wheelview.contract.WheelFormatter;
import com.github.gzuliyujiang.wheelview.widget.WheelView;

@SuppressWarnings("unused")
public class NumberPicker extends ModalDialog {
    protected NumberWheelLayout wheelLayout;
    private OnNumberPickedListener onNumberPickedListener;

    public NumberPicker(@NonNull Activity activity) {
        super(activity);
    }

    public NumberPicker(@NonNull Activity activity, @StyleRes int themeResId) {
        super(activity, themeResId);
    }

    @NonNull
    @Override
    protected View createBodyView() {
        wheelLayout = new NumberWheelLayout(activity);
        return wheelLayout;
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onOk() {
        if (onNumberPickedListener != null) {
            int position = wheelLayout.getWheelView().getCurrentPosition();
            Number item = wheelLayout.getWheelView().getCurrentItem();
            onNumberPickedListener.onNumberPicked(position, item);
        }
    }

    public void setFormatter(WheelFormatter formatter) {
        wheelLayout.getWheelView().setFormatter(formatter);
    }

    public void setRange(int min, int max, int step) {
        wheelLayout.setRange(min, max, step);
    }

    public void setRange(float min, float max, float step) {
        wheelLayout.setRange(min, max, step);
    }

    public void setDefaultValue(Object item) {
        wheelLayout.setDefaultValue(item);
    }

    public void setDefaultPosition(int position) {
        wheelLayout.setDefaultPosition(position);
    }

    public final void setOnNumberPickedListener(OnNumberPickedListener onNumberPickedListener) {
        this.onNumberPickedListener = onNumberPickedListener;
    }

    public final NumberWheelLayout getWheelLayout() {
        return wheelLayout;
    }

    public final WheelView getWheelView() {
        return wheelLayout.getWheelView();
    }

    public final TextView getLabelView() {
        return wheelLayout.getLabelView();
    }

}

