package com.topdon.lib.ui.widget.seekbar;

public interface OnRangeChangedListener {
    void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser,int tempMode);

    void onStartTrackingTouch(RangeSeekBar view, boolean isLeft);

    void onStopTrackingTouch(RangeSeekBar view, boolean isLeft);
}
