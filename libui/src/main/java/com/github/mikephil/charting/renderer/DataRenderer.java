package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public abstract class DataRenderer extends Renderer {

    protected ChartAnimator mAnimator;

    protected Paint mRenderPaint;

    protected Paint mHighlightPaint;

    protected Paint mHighlightDotPaint;

    protected Paint mDrawPaint;

    protected Paint mValuePaint;

    public DataRenderer(ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(viewPortHandler);
        this.mAnimator = animator;

        mRenderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRenderPaint.setStyle(Style.FILL);

        mDrawPaint = new Paint(Paint.DITHER_FLAG);

        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setColor(Color.rgb(63, 63, 63));
        mValuePaint.setTextAlign(Align.CENTER);
        mValuePaint.setTextSize(Utils.convertDpToPixel(9f));

        mHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPaint.setStyle(Style.STROKE);
        mHighlightPaint.setStrokeWidth(2f);
        mHighlightPaint.setColor(Color.rgb(255, 187, 115));

        mHighlightDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightDotPaint.setStyle(Style.FILL_AND_STROKE);
        mHighlightDotPaint.setStrokeWidth(2f);
        mHighlightDotPaint.setColor(Color.rgb(243, 129, 47));
    }

    protected boolean isDrawingValuesAllowed(ChartInterface chart) {
        return chart.getData().getEntryCount() < chart.getMaxVisibleCount()
                * mViewPortHandler.getScaleX();
    }

    public Paint getPaintValues() {
        return mValuePaint;
    }

    public Paint getPaintHighlight() {
        return mHighlightPaint;
    }

    public Paint getPaintRender() {
        return mRenderPaint;
    }

    protected void applyValueTextStyle(IDataSet set) {

        mValuePaint.setTypeface(set.getValueTypeface());
        mValuePaint.setTextSize(set.getValueTextSize());
    }

    public abstract void initBuffers();

    public abstract void drawData(Canvas c);

    public abstract void drawValues(Canvas c);

    public abstract void drawValue(Canvas c, String valueText, float x, float y, int color);

    public abstract void drawExtras(Canvas c);

    public abstract void drawHighlighted(Canvas c, Highlight[] indices);
}
