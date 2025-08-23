
package com.github.mikephil.charting.highlight;

import com.github.mikephil.charting.components.YAxis;

public class Highlight {

    private float mX = Float.NaN;

    private float mY = Float.NaN;

    private float mXPx;

    private float mYPx;

    private int mDataIndex = -1;

    private int mDataSetIndex;

    private int mStackIndex = -1;

    private YAxis.AxisDependency axis;

    private float mDrawX;

    private float mDrawY;

    public Highlight(float x, float y, int dataSetIndex) {
        this.mX = x;
        this.mY = y;
        this.mDataSetIndex = dataSetIndex;
    }

    public Highlight(float x, int dataSetIndex, int stackIndex) {
        this(x, Float.NaN, dataSetIndex);
        this.mStackIndex = stackIndex;
    }

    public Highlight(float x, float y, float xPx, float yPx, int dataSetIndex, YAxis.AxisDependency axis) {
        this.mX = x;
        this.mY = y;
        this.mXPx = xPx;
        this.mYPx = yPx;
        this.mDataSetIndex = dataSetIndex;
        this.axis = axis;
    }

    public Highlight(float x, float y, float xPx, float yPx, int dataSetIndex, int stackIndex, YAxis.AxisDependency axis) {
        this(x, y, xPx, yPx, dataSetIndex, axis);
        this.mStackIndex = stackIndex;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public float getXPx() {
        return mXPx;
    }

    public float getYPx() {
        return mYPx;
    }

    public int getDataIndex() {
        return mDataIndex;
    }

    public void setDataIndex(int mDataIndex) {
        this.mDataIndex = mDataIndex;
    }

    public int getDataSetIndex() {
        return mDataSetIndex;
    }

    public int getStackIndex() {
        return mStackIndex;
    }

    public boolean isStacked() {
        return mStackIndex >= 0;
    }

    public YAxis.AxisDependency getAxis() {
        return axis;
    }

    public void setDraw(float x, float y) {
        this.mDrawX = x;
        this.mDrawY = y;
    }

    public float getDrawX() {
        return mDrawX;
    }

    public float getDrawY() {
        return mDrawY;
    }

    public boolean equalTo(Highlight h) {

        if (h == null)
            return false;
        else {
            if (this.mDataSetIndex == h.mDataSetIndex && this.mX == h.mX
                    && this.mStackIndex == h.mStackIndex && this.mDataIndex == h.mDataIndex)
                return true;
            else
                return false;
        }
    }

    @Override
    public String toString() {
        return "Highlight, x: " + mX + ", y: " + mY + ", dataSetIndex: " + mDataSetIndex
                + ", stackIndex (only stacked barentry): " + mStackIndex;
    }
}
