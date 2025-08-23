package com.github.mikephil.charting.interfaces.datasets;

import android.graphics.DashPathEffect;
import android.graphics.Typeface;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.model.GradientColor;

import java.util.List;

public interface IDataSet<T extends Entry> {

    float getYMin();

    float getYMax();

    float getXMin();

    float getXMax();

    int getEntryCount();

    void calcMinMax();

    void calcMinMaxY(float fromX, float toX);

    T getEntryForXValue(float xValue, float closestToY, DataSet.Rounding rounding);

    T getEntryForXValue(float xValue, float closestToY);

    List<T> getEntriesForXValue(float xValue);

    T getEntryForIndex(int index);

    int getEntryIndex(float xValue, float closestToY, DataSet.Rounding rounding);

    int getEntryIndex(T e);

    int getIndexInEntries(int xIndex);

    boolean addEntry(T e);

    void addEntryOrdered(T e);

    boolean removeFirst();

    boolean removeLast();

    boolean removeEntry(T e);

    boolean removeEntryByXValue(float xValue);

    boolean removeEntry(int index);

    boolean contains(T entry);

    void clear();

    String getLabel();

    void setLabel(String label);

    YAxis.AxisDependency getAxisDependency();

    void setAxisDependency(YAxis.AxisDependency dependency);

    List<Integer> getColors();

    int getColor();

    GradientColor getGradientColor();

    List<GradientColor> getGradientColors();

    GradientColor getGradientColor(int index);

    int getColor(int index);

    boolean isHighlightEnabled();

    void setHighlightEnabled(boolean enabled);

    void setValueFormatter(ValueFormatter f);

    ValueFormatter getValueFormatter();

    boolean needsFormatter();

    void setValueTextColor(int color);

    void setValueTextColors(List<Integer> colors);

    void setValueTypeface(Typeface tf);

    void setValueTextSize(float size);

    int getValueTextColor();

    int getValueTextColor(int index);

    Typeface getValueTypeface();

    float getValueTextSize();

    Legend.LegendForm getForm();

    float getFormSize();

    float getFormLineWidth();

    DashPathEffect getFormLineDashEffect();

    void setDrawValues(boolean enabled);

    boolean isDrawValuesEnabled();

    void setDrawIcons(boolean enabled);

    boolean isDrawIconsEnabled();

    void setIconsOffset(MPPointF offset);

    MPPointF getIconsOffset();

    void setVisible(boolean visible);

    boolean isVisible();
}
