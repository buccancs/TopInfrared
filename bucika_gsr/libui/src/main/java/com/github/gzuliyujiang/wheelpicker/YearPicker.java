

package com.github.gzuliyujiang.wheelpicker;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.gzuliyujiang.dialog.ModalDialog;
import com.github.gzuliyujiang.wheelpicker.contract.OnYearPickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity;
import com.github.gzuliyujiang.wheelpicker.widget.YearWheelLayout;
import com.topdon.lib.ui.R;

import java.util.Calendar;

@SuppressWarnings({"unused", "WeakerAccess"})
public class YearPicker extends ModalDialog {
    protected YearWheelLayout wheelLayout;
    private OnYearPickedListener onYearPickedListener;

    public YearPicker(@NonNull Activity activity, @Nullable Integer year) {
        super(activity);

        int nowYear = Calendar.getInstance().get(Calendar.YEAR);
        DatimeEntity startTimeEntity = new DatimeEntity();
        startTimeEntity.setDate(DateEntity.target(nowYear - 1000, 1, 1));
        DatimeEntity defaultEntity = new DatimeEntity();
        defaultEntity.setDate(DateEntity.target(year == null ? nowYear : year, 1, 1));
        wheelLayout.setRange(startTimeEntity, DatimeEntity.now(), defaultEntity);

        wheelLayout.setCurtainEnabled(true);
        wheelLayout.setCurtainColor(ContextCompat.getColor(getContext(), R.color.wheel_select_bg));
        wheelLayout.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.wheel_select_text));
        wheelLayout.setTextColor(ContextCompat.getColor(getContext(), R.color.wheel_unselect_text));

        wheelLayout.setResetWhenLinkage(false, false);

    }

    @NonNull
    @Override
    protected View createBodyView() {
        wheelLayout = new YearWheelLayout(activity);
        return wheelLayout;
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onOk() {
        if (onYearPickedListener != null) {
            onYearPickedListener.onYearPicked(wheelLayout.getSelectedYear());
        }
    }

    public YearPicker setOnYearPickedListener(OnYearPickedListener onYearPickedListener) {
        this.onYearPickedListener = onYearPickedListener;
        return this;
    }

    public final YearWheelLayout getWheelLayout() {
        return wheelLayout;
    }

}
