
package com.github.mikephil.charting.data;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.interfaces.datasets.ILineRadarDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.List;

public abstract class LineRadarDataSet<T extends Entry> extends LineScatterCandleRadarDataSet<T> implements ILineRadarDataSet<T> {

    private int mFillColor = Color.rgb(140, 234, 255);

    protected Drawable mFillDrawable;

    private int mFillAlpha = 85;

    private float mLineWidth = 2.5f;

    private boolean mDrawFilled = false;

    public LineRadarDataSet(List<T> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public int getFillColor() {
        return mFillColor;
    }

    public void setFillColor(int color) {
        mFillColor = color;
        mFillDrawable = null;
    }

    @Override
    public Drawable getFillDrawable() {
        return mFillDrawable;
    }

    @TargetApi(18)
    public void setFillDrawable(Drawable drawable) {
        this.mFillDrawable = drawable;
    }

    @Override
    public int getFillAlpha() {
        return mFillAlpha;
    }

    public void setFillAlpha(int alpha) {
        mFillAlpha = alpha;
    }

    public void setLineWidth(float width) {

        if (width < 0.0f)
            width = 0.0f;
        if (width > 10.0f)
            width = 10.0f;
        mLineWidth = Utils.convertDpToPixel(width);
    }

    @Override
    public float getLineWidth() {
        return mLineWidth;
    }

    @Override
    public void setDrawFilled(boolean filled) {
        mDrawFilled = filled;
    }

    @Override
    public boolean isDrawFilledEnabled() {
        return mDrawFilled;
    }

    protected void copy(LineRadarDataSet lineRadarDataSet) {
        super.copy(lineRadarDataSet);
        lineRadarDataSet.mDrawFilled = mDrawFilled;
        lineRadarDataSet.mFillAlpha = mFillAlpha;
        lineRadarDataSet.mFillColor = mFillColor;
        lineRadarDataSet.mFillDrawable = mFillDrawable;
        lineRadarDataSet.mLineWidth = mLineWidth;
    }
}
