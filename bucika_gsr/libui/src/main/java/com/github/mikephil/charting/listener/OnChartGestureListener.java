package com.github.mikephil.charting.listener;

import android.view.MotionEvent;

public interface OnChartGestureListener {

    void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture);

    void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture);

    void onChartLongPressed(MotionEvent me);

    void onChartDoubleTapped(MotionEvent me);

    void onChartSingleTapped(MotionEvent me);

    void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY);

    void onChartScale(MotionEvent me, float scaleX, float scaleY);

    void onChartTranslate(MotionEvent me, float dX, float dY);
}
