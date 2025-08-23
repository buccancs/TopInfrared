
package com.github.mikephil.charting.data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.util.Log;

import com.github.mikephil.charting.formatter.DefaultFillFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class LineDataSet extends LineRadarDataSet<Entry> implements ILineDataSet {

    private Mode mMode = Mode.LINEAR;

    private List<Integer> mCircleColors = null;

    private int mCircleHoleColor = Color.WHITE;

    private float mCircleRadius = 8f;

    private float mCircleHoleRadius = 4f;

    private float mCubicIntensity = 0.2f;

    private DashPathEffect mDashPathEffect = null;

    private IFillFormatter mFillFormatter = new DefaultFillFormatter();

    private boolean mDrawCircles = true;

    private boolean mDrawCircleHole = true;

    public LineDataSet(List<Entry> yVals, String label) {
        super(yVals, label);

        if (mCircleColors == null) {
            mCircleColors = new ArrayList<Integer>();
        }
        mCircleColors.clear();

        mCircleColors.add(Color.rgb(140, 234, 255));
    }

    @Override
    public DataSet<Entry> copy() {
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < mValues.size(); i++) {
            entries.add(mValues.get(i).copy());
        }
        LineDataSet copied = new LineDataSet(entries, getLabel());
        copy(copied);
        return copied;
    }

    protected void copy(LineDataSet lineDataSet) {
        super.copy(lineDataSet);
        lineDataSet.mCircleColors = mCircleColors;
        lineDataSet.mCircleHoleColor = mCircleHoleColor;
        lineDataSet.mCircleHoleRadius = mCircleHoleRadius;
        lineDataSet.mCircleRadius = mCircleRadius;
        lineDataSet.mCubicIntensity = mCubicIntensity;
        lineDataSet.mDashPathEffect = mDashPathEffect;
        lineDataSet.mDrawCircleHole = mDrawCircleHole;
        lineDataSet.mDrawCircles = mDrawCircleHole;
        lineDataSet.mFillFormatter = mFillFormatter;
        lineDataSet.mMode = mMode;
    }

    @Override
    public Mode getMode() {
        return mMode;
    }

    public void setMode(Mode mode) {
        mMode = mode;
    }

    public void setCubicIntensity(float intensity) {

        if (intensity > 1f)
            intensity = 1f;
        if (intensity < 0.05f)
            intensity = 0.05f;

        mCubicIntensity = intensity;
    }

    @Override
    public float getCubicIntensity() {
        return mCubicIntensity;
    }

    public void setCircleRadius(float radius) {

        if (radius >= 1f) {
            mCircleRadius = Utils.convertDpToPixel(radius);
        } else {
            Log.e("LineDataSet", "Circle radius cannot be < 1");
        }
    }

    @Override
    public float getCircleRadius() {
        return mCircleRadius;
    }

    public void setCircleHoleRadius(float holeRadius) {

        if (holeRadius >= 0.5f) {
            mCircleHoleRadius = Utils.convertDpToPixel(holeRadius);
        } else {
            Log.e("LineDataSet", "Circle radius cannot be < 0.5");
        }
    }

    @Override
    public float getCircleHoleRadius() {
        return mCircleHoleRadius;
    }

    @Deprecated
    public void setCircleSize(float size) {
        setCircleRadius(size);
    }

    @Deprecated
    public float getCircleSize() {
        return getCircleRadius();
    }

    public void enableDashedLine(float lineLength, float spaceLength, float phase) {
        mDashPathEffect = new DashPathEffect(new float[]{
                lineLength, spaceLength
        }, phase);
    }

    public void disableDashedLine() {
        mDashPathEffect = null;
    }

    @Override
    public boolean isDashedLineEnabled() {
        return mDashPathEffect == null ? false : true;
    }

    @Override
    public DashPathEffect getDashPathEffect() {
        return mDashPathEffect;
    }

    public void setDrawCircles(boolean enabled) {
        this.mDrawCircles = enabled;
    }

    @Override
    public boolean isDrawCirclesEnabled() {
        return mDrawCircles;
    }

    @Deprecated
    @Override
    public boolean isDrawCubicEnabled() {
        return mMode == Mode.CUBIC_BEZIER;
    }

    @Deprecated
    @Override
    public boolean isDrawSteppedEnabled() {
        return mMode == Mode.STEPPED;
    }

    public List<Integer> getCircleColors() {
        return mCircleColors;
    }

    @Override
    public int getCircleColor(int index) {
        return mCircleColors.get(index);
    }

    @Override
    public int getCircleColorCount() {
        return mCircleColors.size();
    }

    public void setCircleColors(List<Integer> colors) {
        mCircleColors = colors;
    }

    public void setCircleColors(int... colors) {
        this.mCircleColors = ColorTemplate.createColors(colors);
    }

    public void setCircleColors(int[] colors, Context c) {

        List<Integer> clrs = mCircleColors;
        if (clrs == null) {
            clrs = new ArrayList<>();
        }
        clrs.clear();

        for (int color : colors) {
            clrs.add(c.getResources().getColor(color));
        }

        mCircleColors = clrs;
    }

    public void setCircleColor(int color) {
        resetCircleColors();
        mCircleColors.add(color);
    }

    public void resetCircleColors() {
        if (mCircleColors == null) {
            mCircleColors = new ArrayList<Integer>();
        }
        mCircleColors.clear();
    }

    public void setCircleHoleColor(int color) {
        mCircleHoleColor = color;
    }

    @Override
    public int getCircleHoleColor() {
        return mCircleHoleColor;
    }

    public void setDrawCircleHole(boolean enabled) {
        mDrawCircleHole = enabled;
    }

    @Override
    public boolean isDrawCircleHoleEnabled() {
        return mDrawCircleHole;
    }

    public void setFillFormatter(IFillFormatter formatter) {

        if (formatter == null)
            mFillFormatter = new DefaultFillFormatter();
        else
            mFillFormatter = formatter;
    }

    @Override
    public IFillFormatter getFillFormatter() {
        return mFillFormatter;
    }

    public enum Mode {
        LINEAR,
        STEPPED,
        CUBIC_BEZIER,
        HORIZONTAL_BEZIER
    }
}
