package com.github.mikephil.charting.components;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.util.Log;

import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class AxisBase extends ComponentBase {

    protected ValueFormatter mAxisValueFormatter;

    private int mGridColor = Color.GRAY;

    private float mGridLineWidth = 1f;

    private int mAxisLineColor = Color.GRAY;

    private float mAxisLineWidth = 1f;

    public float[] mEntries = new float[]{};

    public float[] mCenteredEntries = new float[]{};

    public int mEntryCount;

    public int mDecimals;

    private int mLabelCount = 6;

    protected float mGranularity = 1.0f;

    protected boolean mGranularityEnabled = false;

    protected boolean mForceLabels = false;

    protected boolean mDrawGridLines = true;

    protected boolean mDrawAxisLine = true;

    protected boolean mDrawLabels = true;

    protected boolean mCenterAxisLabels = false;

    private DashPathEffect mAxisLineDashPathEffect = null;

    private DashPathEffect mGridDashPathEffect = null;

    protected List<LimitLine> mLimitLines;

    protected boolean mDrawLimitLineBehindData = false;

    protected boolean mDrawGridLinesBehindData = true;

    protected float mSpaceMin = 0.f;

    protected float mSpaceMax = 0.f;

    protected boolean mCustomAxisMin = false;

    protected boolean mCustomAxisMax = false;

    public float mAxisMaximum = 0f;

    public float mAxisMinimum = 0f;

    public float mAxisRange = 0f;

    public AxisBase() {
        this.mTextSize = Utils.convertDpToPixel(10f);
        this.mXOffset = Utils.convertDpToPixel(5f);
        this.mYOffset = Utils.convertDpToPixel(5f);
        this.mLimitLines = new ArrayList<LimitLine>();
    }

    public void setDrawGridLines(boolean enabled) {
        mDrawGridLines = enabled;
    }

    public boolean isDrawGridLinesEnabled() {
        return mDrawGridLines;
    }

    public void setDrawAxisLine(boolean enabled) {
        mDrawAxisLine = enabled;
    }

    public boolean isDrawAxisLineEnabled() {
        return mDrawAxisLine;
    }

    public void setCenterAxisLabels(boolean enabled) {
        mCenterAxisLabels = enabled;
    }

    public boolean isCenterAxisLabelsEnabled() {
        return mCenterAxisLabels && mEntryCount > 0;
    }

    public void setGridColor(int color) {
        mGridColor = color;
    }

    public int getGridColor() {
        return mGridColor;
    }

    public void setAxisLineWidth(float width) {
        mAxisLineWidth = Utils.convertDpToPixel(width);
    }

    public float getAxisLineWidth() {
        return mAxisLineWidth;
    }

    public void setGridLineWidth(float width) {
        mGridLineWidth = Utils.convertDpToPixel(width);
    }

    public float getGridLineWidth() {
        return mGridLineWidth;
    }

    public void setAxisLineColor(int color) {
        mAxisLineColor = color;
    }

    public int getAxisLineColor() {
        return mAxisLineColor;
    }

    public void setDrawLabels(boolean enabled) {
        mDrawLabels = enabled;
    }

    public boolean isDrawLabelsEnabled() {
        return mDrawLabels;
    }

    public void setLabelCount(int count) {

        if (count > 25)
            count = 25;
        if (count < 2)
            count = 2;

        mLabelCount = count;
        mForceLabels = false;
    }

    public void setLabelCount(int count, boolean force) {

        setLabelCount(count);
        mForceLabels = force;
    }

    public boolean isForceLabelsEnabled() {
        return mForceLabels;
    }

    public int getLabelCount() {
        return mLabelCount;
    }

    public boolean isGranularityEnabled() {
        return mGranularityEnabled;
    }

    public void setGranularityEnabled(boolean enabled) {
        mGranularityEnabled = enabled;
    }

    public float getGranularity() {
        return mGranularity;
    }

    public void setGranularity(float granularity) {
        mGranularity = granularity;
        mGranularityEnabled = true;
    }

    public void addLimitLine(LimitLine l) {
        mLimitLines.add(l);

        if (mLimitLines.size() > 6) {
            Log.e("MPAndroiChart",
                    "Warning! You have more than 6 LimitLines on your axis, do you really want " +
                            "that?");
        }
    }

    public void removeLimitLine(LimitLine l) {
        mLimitLines.remove(l);
    }

    public void removeAllLimitLines() {
        mLimitLines.clear();
    }

    public List<LimitLine> getLimitLines() {
        return mLimitLines;
    }

    public void setDrawLimitLinesBehindData(boolean enabled) {
        mDrawLimitLineBehindData = enabled;
    }

    public boolean isDrawLimitLinesBehindDataEnabled() {
        return mDrawLimitLineBehindData;
    }

    public void setDrawGridLinesBehindData(boolean enabled) { mDrawGridLinesBehindData = enabled; }

    public boolean isDrawGridLinesBehindDataEnabled() {
        return mDrawGridLinesBehindData;
    }

    public String getLongestLabel() {

        String longest = "";

        for (int i = 0; i < mEntries.length; i++) {
            String text = getFormattedLabel(i);

            if (text != null && longest.length() < text.length())
                longest = text;
        }

        return longest;
    }

    public String getFormattedLabel(int index) {

        if (index < 0 || index >= mEntries.length)
            return "";
        else
            return getValueFormatter().getAxisLabel(mEntries[index], this);
    }

    public void setValueFormatter(ValueFormatter f) {

        if (f == null)
            mAxisValueFormatter = new DefaultAxisValueFormatter(mDecimals);
        else
            mAxisValueFormatter = f;
    }

    public ValueFormatter getValueFormatter() {

        if (mAxisValueFormatter == null ||
                (mAxisValueFormatter instanceof DefaultAxisValueFormatter &&
                        ((DefaultAxisValueFormatter)mAxisValueFormatter).getDecimalDigits() != mDecimals))
            mAxisValueFormatter = new DefaultAxisValueFormatter(mDecimals);

        return mAxisValueFormatter;
    }

    public void enableGridDashedLine(float lineLength, float spaceLength, float phase) {
        mGridDashPathEffect = new DashPathEffect(new float[]{
                lineLength, spaceLength
        }, phase);
    }

    public void setGridDashedLine(DashPathEffect effect) {
        mGridDashPathEffect = effect;
    }

    public void disableGridDashedLine() {
        mGridDashPathEffect = null;
    }

    public boolean isGridDashedLineEnabled() {
        return mGridDashPathEffect == null ? false : true;
    }

    public DashPathEffect getGridDashPathEffect() {
        return mGridDashPathEffect;
    }

    public void enableAxisLineDashedLine(float lineLength, float spaceLength, float phase) {
        mAxisLineDashPathEffect = new DashPathEffect(new float[]{
                lineLength, spaceLength
        }, phase);
    }

    public void setAxisLineDashedLine(DashPathEffect effect) {
        mAxisLineDashPathEffect = effect;
    }

    public void disableAxisLineDashedLine() {
        mAxisLineDashPathEffect = null;
    }

    public boolean isAxisLineDashedLineEnabled() {
        return mAxisLineDashPathEffect == null ? false : true;
    }

    public DashPathEffect getAxisLineDashPathEffect() {
        return mAxisLineDashPathEffect;
    }

    public float getAxisMaximum() {
        return mAxisMaximum;
    }

    public float getAxisMinimum() {
        return mAxisMinimum;
    }

    public void resetAxisMaximum() {
        mCustomAxisMax = false;
    }

    public boolean isAxisMaxCustom() {
        return mCustomAxisMax;
    }

    public void resetAxisMinimum() {
        mCustomAxisMin = false;
    }

    public boolean isAxisMinCustom() {
        return mCustomAxisMin;
    }

    public void setAxisMinimum(float min) {
        mCustomAxisMin = true;
        mAxisMinimum = min;
        this.mAxisRange = Math.abs(mAxisMaximum - min);
    }

    @Deprecated
    public void setAxisMinValue(float min) {
        setAxisMinimum(min);
    }

    public void setAxisMaximum(float max) {
        mCustomAxisMax = true;
        mAxisMaximum = max;
        this.mAxisRange = Math.abs(max - mAxisMinimum);
    }

    @Deprecated
    public void setAxisMaxValue(float max) {
        setAxisMaximum(max);
    }

    public void calculate(float dataMin, float dataMax) {

        float min = mCustomAxisMin ? mAxisMinimum : (dataMin - mSpaceMin);
        float max = mCustomAxisMax ? mAxisMaximum : (dataMax + mSpaceMax);

        float range = Math.abs(max - min);

        if (range == 0f) {
            max = max + 1f;
            min = min - 1f;
        }

        this.mAxisMinimum = min;
        this.mAxisMaximum = max;

        this.mAxisRange = Math.abs(max - min);
    }

    public float getSpaceMin()
    {
        return mSpaceMin;
    }

    public void setSpaceMin(float mSpaceMin)
    {
        this.mSpaceMin = mSpaceMin;
    }

    public float getSpaceMax()
    {
        return mSpaceMax;
    }

    public void setSpaceMax(float mSpaceMax)
    {
        this.mSpaceMax = mSpaceMax;
    }
}
