
package com.github.mikephil.charting.data;

import android.graphics.Paint;

import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CandleDataSet extends LineScatterCandleRadarDataSet<CandleEntry> implements ICandleDataSet {

    private float mShadowWidth = 3f;

    private boolean mShowCandleBar = true;

    private float mBarSpace = 0.1f;

    private boolean mShadowColorSameAsCandle = false;

    protected Paint.Style mIncreasingPaintStyle = Paint.Style.STROKE;

    protected Paint.Style mDecreasingPaintStyle = Paint.Style.FILL;

    protected int mNeutralColor = ColorTemplate.COLOR_SKIP;

    protected int mIncreasingColor = ColorTemplate.COLOR_SKIP;

    protected int mDecreasingColor = ColorTemplate.COLOR_SKIP;

    protected int mShadowColor = ColorTemplate.COLOR_SKIP;

    public CandleDataSet(List<CandleEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public DataSet<CandleEntry> copy() {
        List<CandleEntry> entries = new ArrayList<CandleEntry>();
        for (int i = 0; i < mValues.size(); i++) {
            entries.add(mValues.get(i).copy());
        }
        CandleDataSet copied = new CandleDataSet(entries, getLabel());
        copy(copied);
        return copied;
    }

    protected void copy(CandleDataSet candleDataSet) {
        super.copy(candleDataSet);
        candleDataSet.mShadowWidth = mShadowWidth;
        candleDataSet.mShowCandleBar = mShowCandleBar;
        candleDataSet.mBarSpace = mBarSpace;
        candleDataSet.mShadowColorSameAsCandle = mShadowColorSameAsCandle;
        candleDataSet.mHighLightColor = mHighLightColor;
        candleDataSet.mIncreasingPaintStyle = mIncreasingPaintStyle;
        candleDataSet.mDecreasingPaintStyle = mDecreasingPaintStyle;
        candleDataSet.mNeutralColor = mNeutralColor;
        candleDataSet.mIncreasingColor = mIncreasingColor;
        candleDataSet.mDecreasingColor = mDecreasingColor;
        candleDataSet.mShadowColor = mShadowColor;
    }

    @Override
    protected void calcMinMax(CandleEntry e) {

        if (e.getLow() < mYMin)
            mYMin = e.getLow();

        if (e.getHigh() > mYMax)
            mYMax = e.getHigh();

        calcMinMaxX(e);
    }

    @Override
    protected void calcMinMaxY(CandleEntry e) {

        if (e.getHigh() < mYMin)
            mYMin = e.getHigh();

        if (e.getHigh() > mYMax)
            mYMax = e.getHigh();

        if (e.getLow() < mYMin)
            mYMin = e.getLow();

        if (e.getLow() > mYMax)
            mYMax = e.getLow();
    }

    public void setBarSpace(float space) {

        if (space < 0f)
            space = 0f;
        if (space > 0.45f)
            space = 0.45f;

        mBarSpace = space;
    }

    @Override
    public float getBarSpace() {
        return mBarSpace;
    }

    public void setShadowWidth(float width) {
        mShadowWidth = Utils.convertDpToPixel(width);
    }

    @Override
    public float getShadowWidth() {
        return mShadowWidth;
    }

    public void setShowCandleBar(boolean showCandleBar) {
        mShowCandleBar = showCandleBar;
    }

    @Override
    public boolean getShowCandleBar() {
        return mShowCandleBar;
    }

    public void setNeutralColor(int color) {
        mNeutralColor = color;
    }

    @Override
    public int getNeutralColor() {
        return mNeutralColor;
    }

    public void setIncreasingColor(int color) {
        mIncreasingColor = color;
    }

    @Override
    public int getIncreasingColor() {
        return mIncreasingColor;
    }

    public void setDecreasingColor(int color) {
        mDecreasingColor = color;
    }

    @Override
    public int getDecreasingColor() {
        return mDecreasingColor;
    }

    @Override
    public Paint.Style getIncreasingPaintStyle() {
        return mIncreasingPaintStyle;
    }

    public void setIncreasingPaintStyle(Paint.Style paintStyle) {
        this.mIncreasingPaintStyle = paintStyle;
    }

    @Override
    public Paint.Style getDecreasingPaintStyle() {
        return mDecreasingPaintStyle;
    }

    public void setDecreasingPaintStyle(Paint.Style decreasingPaintStyle) {
        this.mDecreasingPaintStyle = decreasingPaintStyle;
    }

    @Override
    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.mShadowColor = shadowColor;
    }

    @Override
    public boolean getShadowColorSameAsCandle() {
        return mShadowColorSameAsCandle;
    }

    public void setShadowColorSameAsCandle(boolean shadowColorSameAsCandle) {
        this.mShadowColorSameAsCandle = shadowColorSameAsCandle;
    }
}
