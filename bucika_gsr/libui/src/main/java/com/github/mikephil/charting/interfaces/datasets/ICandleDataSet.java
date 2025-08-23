package com.github.mikephil.charting.interfaces.datasets;

import android.graphics.Paint;

import com.github.mikephil.charting.data.CandleEntry;

public interface ICandleDataSet extends ILineScatterCandleRadarDataSet<CandleEntry> {

    float getBarSpace();

    boolean getShowCandleBar();

    float getShadowWidth();

    int getShadowColor();

    int getNeutralColor();

    int getIncreasingColor();

    int getDecreasingColor();

    Paint.Style getIncreasingPaintStyle();

    Paint.Style getDecreasingPaintStyle();

    boolean getShadowColorSameAsCandle();
}
