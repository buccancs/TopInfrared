

package com.github.gzuliyujiang.wheelpicker;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.github.gzuliyujiang.dialog.ModalDialog;
import com.github.gzuliyujiang.wheelpicker.contract.LinkageProvider;
import com.github.gzuliyujiang.wheelpicker.contract.OnLinkagePickedListener;
import com.github.gzuliyujiang.wheelpicker.widget.LinkageWheelLayout;
import com.github.gzuliyujiang.wheelview.widget.WheelView;

@SuppressWarnings({"WeakerAccess", "unused"})
public class LinkagePicker extends ModalDialog {
    protected LinkageWheelLayout wheelLayout;
    private OnLinkagePickedListener onLinkagePickedListener;

    public LinkagePicker(@NonNull Activity activity) {
        super(activity);
    }

    public LinkagePicker(@NonNull Activity activity, @StyleRes int themeResId) {
        super(activity, themeResId);
    }

    @NonNull
    @Override
    protected View createBodyView() {
        wheelLayout = new LinkageWheelLayout(activity);
        return wheelLayout;
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onOk() {
        if (onLinkagePickedListener != null) {
            Object first = wheelLayout.getFirstWheelView().getCurrentItem();
            Object second = wheelLayout.getSecondWheelView().getCurrentItem();
            Object third = wheelLayout.getThirdWheelView().getCurrentItem();
            onLinkagePickedListener.onLinkagePicked(first, second, third);
        }
    }

    public void setData(@NonNull LinkageProvider data) {
        wheelLayout.setData(data);
    }

    public void setDefaultValue(Object first, Object second, Object third) {
        wheelLayout.setDefaultValue(first, second, third);
    }

    public void setOnLinkagePickedListener(OnLinkagePickedListener onLinkagePickedListener) {
        this.onLinkagePickedListener = onLinkagePickedListener;
    }

    public final LinkageWheelLayout getWheelLayout() {
        return wheelLayout;
    }

    public final WheelView getFirstWheelView() {
        return wheelLayout.getFirstWheelView();
    }

    public final WheelView getSecondWheelView() {
        return wheelLayout.getSecondWheelView();
    }

    public final WheelView getThirdWheelView() {
        return wheelLayout.getThirdWheelView();
    }

    public final TextView getFirstLabelView() {
        return wheelLayout.getFirstLabelView();
    }

    public final TextView getSecondLabelView() {
        return wheelLayout.getSecondLabelView();
    }

    public final TextView getThirdLabelView() {
        return wheelLayout.getThirdLabelView();
    }

    public final ProgressBar getLoadingView() {
        return wheelLayout.getLoadingView();
    }

}
