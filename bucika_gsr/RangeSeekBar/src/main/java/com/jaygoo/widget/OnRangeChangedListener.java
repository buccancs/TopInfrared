package com.jaygoo.widget;

/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2018/5/8
 * 描    述:
 * ================================================
 */
public interface OnRangeChangedListener {
    void onRangeChanged(DefRangeSeekBar view, float leftValue, float rightValue, boolean isFromUser);

    void onStartTrackingTouch(DefRangeSeekBar view, boolean isLeft);

    void onStopTrackingTouch(DefRangeSeekBar view, boolean isLeft);
}
