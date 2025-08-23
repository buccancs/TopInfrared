package com.jaygoo.widget;

public interface OnRangeChangedListener {
    void onRangeChanged(DefRangeSeekBar view, float leftValue, float rightValue, boolean isFromUser);

    void onStartTrackingTouch(DefRangeSeekBar view, boolean isLeft);

    void onStopTrackingTouch(DefRangeSeekBar view, boolean isLeft);
}
