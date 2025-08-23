package com.github.mikephil.charting.charts;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.BarHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;

public class BarChart extends BarLineChartBase<BarData> implements BarDataProvider {

    protected boolean mHighlightFullBarEnabled = false;

    private boolean mDrawValueAboveBar = true;

    private boolean mDrawBarShadow = false;

    private boolean mFitBars = false;

    public BarChart(Context context) {
        super(context);
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();

        mRenderer = new BarChartRenderer(this, mAnimator, mViewPortHandler);

        setHighlighter(new BarHighlighter(this));

        getXAxis().setSpaceMin(0.5f);
        getXAxis().setSpaceMax(0.5f);
    }

    @Override
    protected void calcMinMax() {

        if (mFitBars) {
            mXAxis.calculate(mData.getXMin() - mData.getBarWidth() / 2f, mData.getXMax() + mData.getBarWidth() / 2f);
        } else {
            mXAxis.calculate(mData.getXMin(), mData.getXMax());
        }

        mAxisLeft.calculate(mData.getYMin(YAxis.AxisDependency.LEFT), mData.getYMax(YAxis.AxisDependency.LEFT));
        mAxisRight.calculate(mData.getYMin(YAxis.AxisDependency.RIGHT), mData.getYMax(YAxis.AxisDependency
                .RIGHT));
    }

    @Override
    public Highlight getHighlightByTouchPoint(float x, float y) {

        if (mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.");
            return null;
        } else {
            Highlight h = getHighlighter().getHighlight(x, y);
            if (h == null || !isHighlightFullBarEnabled()) return h;

            return new Highlight(h.getX(), h.getY(),
                    h.getXPx(), h.getYPx(),
                    h.getDataSetIndex(), -1, h.getAxis());
        }
    }

    public RectF getBarBounds(BarEntry e) {

        RectF bounds = new RectF();
        getBarBounds(e, bounds);

        return bounds;
    }

    public void getBarBounds(BarEntry e, RectF outputRect) {

        RectF bounds = outputRect;

        IBarDataSet set = mData.getDataSetForEntry(e);

        if (set == null) {
            bounds.set(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            return;
        }

        float y = e.getY();
        float x = e.getX();

        float barWidth = mData.getBarWidth();

        float left = x - barWidth / 2f;
        float right = x + barWidth / 2f;
        float top = y >= 0 ? y : 0;
        float bottom = y <= 0 ? y : 0;

        bounds.set(left, top, right, bottom);

        getTransformer(set.getAxisDependency()).rectValueToPixel(outputRect);
    }

    public void setDrawValueAboveBar(boolean enabled) {
        mDrawValueAboveBar = enabled;
    }

    public boolean isDrawValueAboveBarEnabled() {
        return mDrawValueAboveBar;
    }

    public void setDrawBarShadow(boolean enabled) {
        mDrawBarShadow = enabled;
    }

    public boolean isDrawBarShadowEnabled() {
        return mDrawBarShadow;
    }

    public void setHighlightFullBarEnabled(boolean enabled) {
        mHighlightFullBarEnabled = enabled;
    }

    @Override
    public boolean isHighlightFullBarEnabled() {
        return mHighlightFullBarEnabled;
    }

    public void highlightValue(float x, int dataSetIndex, int stackIndex) {
        highlightValue(new Highlight(x, dataSetIndex, stackIndex), false);
    }

    @Override
    public BarData getBarData() {
        return mData;
    }

    public void setFitBars(boolean enabled) {
        mFitBars = enabled;
    }

    public void groupBars(float fromX, float groupSpace, float barSpace) {

        if (getBarData() == null) {
            throw new RuntimeException("You need to set data for the chart before grouping bars.");
        } else {
            getBarData().groupBars(fromX, groupSpace, barSpace);
            notifyDataSetChanged();
        }
    }
}
