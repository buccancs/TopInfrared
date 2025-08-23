package com.github.mikephil.charting.data;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.highlight.Range;

@SuppressLint("ParcelCreator")
public class BarEntry extends Entry {

    private float[] mYVals;

    private Range[] mRanges;

    private float mNegativeSum;

    private float mPositiveSum;

    public BarEntry(float x, float y) {
        super(x, y);
    }

    public BarEntry(float x, float y, Object data) {
        super(x, y, data);
    }

    public BarEntry(float x, float y, Drawable icon) {
        super(x, y, icon);
    }

    public BarEntry(float x, float y, Drawable icon, Object data) {
        super(x, y, icon, data);
    }

    public BarEntry(float x, float[] vals) {
        super(x, calcSum(vals));

        this.mYVals = vals;
        calcPosNegSum();
        calcRanges();
    }

    public BarEntry(float x, float[] vals, Object data) {
        super(x, calcSum(vals), data);

        this.mYVals = vals;
        calcPosNegSum();
        calcRanges();
    }

    public BarEntry(float x, float[] vals, Drawable icon) {
        super(x, calcSum(vals), icon);

        this.mYVals = vals;
        calcPosNegSum();
        calcRanges();
    }

    public BarEntry(float x, float[] vals, Drawable icon, Object data) {
        super(x, calcSum(vals), icon, data);

        this.mYVals = vals;
        calcPosNegSum();
        calcRanges();
    }

    public BarEntry copy() {

        BarEntry copied = new BarEntry(getX(), getY(), getData());
        copied.setVals(mYVals);
        return copied;
    }

    public float[] getYVals() {
        return mYVals;
    }

    public void setVals(float[] vals) {
        setY(calcSum(vals));
        mYVals = vals;
        calcPosNegSum();
        calcRanges();
    }

    @Override
    public float getY() {
        return super.getY();
    }

    public Range[] getRanges() {
        return mRanges;
    }

    public boolean isStacked() {
        return mYVals != null;
    }

    @Deprecated
    public float getBelowSum(int stackIndex) {
        return getSumBelow(stackIndex);
    }

    public float getSumBelow(int stackIndex) {

        if (mYVals == null)
            return 0;

        float remainder = 0f;
        int index = mYVals.length - 1;

        while (index > stackIndex && index >= 0) {
            remainder += mYVals[index];
            index--;
        }

        return remainder;
    }

    public float getPositiveSum() {
        return mPositiveSum;
    }

    public float getNegativeSum() {
        return mNegativeSum;
    }

    private void calcPosNegSum() {

        if (mYVals == null) {
            mNegativeSum = 0;
            mPositiveSum = 0;
            return;
        }

        float sumNeg = 0f;
        float sumPos = 0f;

        for (float f : mYVals) {
            if (f <= 0f)
                sumNeg += Math.abs(f);
            else
                sumPos += f;
        }

        mNegativeSum = sumNeg;
        mPositiveSum = sumPos;
    }

    private static float calcSum(float[] vals) {

        if (vals == null)
            return 0f;

        float sum = 0f;

        for (float f : vals)
            sum += f;

        return sum;
    }

    protected void calcRanges() {

        float[] values = getYVals();

        if (values == null || values.length == 0)
            return;

        mRanges = new Range[values.length];

        float negRemain = -getNegativeSum();
        float posRemain = 0f;

        for (int i = 0; i < mRanges.length; i++) {

            float value = values[i];

            if (value < 0) {
                mRanges[i] = new Range(negRemain, negRemain - value);
                negRemain -= value;
            } else {
                mRanges[i] = new Range(posRemain, posRemain + value);
                posRemain += value;
            }
        }
    }
}

