package com.github.mikephil.charting.interfaces.dataprovider;

import android.graphics.RectF;

import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.MPPointF;

public interface ChartInterface {

    float getXChartMin();

    float getXChartMax();

    float getXRange();

    float getYChartMin();

    float getYChartMax();

    float getMaxHighlightDistance();

    int getWidth();

    int getHeight();

    MPPointF getCenterOfView();

    MPPointF getCenterOffsets();

    RectF getContentRect();

    ValueFormatter getDefaultValueFormatter();

    ChartData getData();

    int getMaxVisibleCount();
}
