

package com.github.gzuliyujiang.wheelpicker;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.github.gzuliyujiang.dialog.ModalDialog;
import com.github.gzuliyujiang.wheelpicker.contract.OnDatePickedListener;
import com.github.gzuliyujiang.wheelpicker.widget.DateWheelLayout;

@SuppressWarnings("unused")
public class DatePicker extends ModalDialog {
    protected DateWheelLayout wheelLayout;
    private OnDatePickedListener onDatePickedListener;

    public DatePicker(@NonNull Activity activity) {
        super(activity);
    }

    public DatePicker(@NonNull Activity activity, @StyleRes int themeResId) {
        super(activity, themeResId);
    }

    @NonNull
    @Override
    protected View createBodyView() {
        wheelLayout = new DateWheelLayout(activity);
        return wheelLayout;
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onOk() {
        if (onDatePickedListener != null) {
            int year = wheelLayout.getSelectedYear();
            int month = wheelLayout.getSelectedMonth();
            int day = wheelLayout.getSelectedDay();
            onDatePickedListener.onDatePicked(year, month, day);
        }
    }

    public void setOnDatePickedListener(OnDatePickedListener onDatePickedListener) {
        this.onDatePickedListener = onDatePickedListener;
    }

    public final DateWheelLayout getWheelLayout() {
        return wheelLayout;
    }

}
