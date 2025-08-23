package com.github.mikephil.charting.interfaces.datasets;

import com.github.mikephil.charting.data.BarEntry;

public interface IBarDataSet extends IBarLineScatterCandleBubbleDataSet<BarEntry> {

    boolean isStacked();

    int getStackSize();

    int getBarShadowColor();

    float getBarBorderWidth();

    int getBarBorderColor();

    int getHighLightAlpha();

    String[] getStackLabels();
}
