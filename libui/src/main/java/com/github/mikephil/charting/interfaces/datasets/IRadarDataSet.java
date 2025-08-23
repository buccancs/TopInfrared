package com.github.mikephil.charting.interfaces.datasets;

import com.github.mikephil.charting.data.RadarEntry;

public interface IRadarDataSet extends ILineRadarDataSet<RadarEntry> {

    boolean isDrawHighlightCircleEnabled();

    void setDrawHighlightCircleEnabled(boolean enabled);

    int getHighlightCircleFillColor();

    int getHighlightCircleStrokeColor();

    int getHighlightCircleStrokeAlpha();

    float getHighlightCircleInnerRadius();

    float getHighlightCircleOuterRadius();

    float getHighlightCircleStrokeWidth();

}
