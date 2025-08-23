package com.github.mikephil.charting.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.blankj.utilcode.util.SizeUtils;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

public abstract class LineScatterCandleRadarRenderer extends BarLineScatterCandleBubbleRenderer {

    private Path mHighlightLinePath = new Path();

    public LineScatterCandleRadarRenderer(ChartAnimator animator, ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
    }

    protected void drawHighlightLines(Canvas c, float x, float y, ILineScatterCandleRadarDataSet set) {

        mHighlightPaint.setColor(set.getHighLightColor());
        mHighlightPaint.setStrokeWidth(set.getHighlightLineWidth());

        mHighlightPaint.setPathEffect(set.getDashPathEffectHighlight());

        if (set.isVerticalHighlightIndicatorEnabled()) {

            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(x, mViewPortHandler.contentTop());
            mHighlightLinePath.lineTo(x, mViewPortHandler.contentBottom());

            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }

        if (set.isHorizontalHighlightIndicatorEnabled()) {

            mHighlightLinePath.reset();
            mHighlightLinePath.moveTo(mViewPortHandler.contentLeft(), y);
            mHighlightLinePath.lineTo(mViewPortHandler.contentRight(), y);

            c.drawPath(mHighlightLinePath, mHighlightPaint);
        }

        mHighlightDotPaint.setColor(Color.rgb(243, 129, 47));
        mHighlightDotPaint.setStyle(Paint.Style.FILL);
        c.drawCircle(x, y, SizeUtils.dp2px(4f), mHighlightDotPaint);
        mHighlightDotPaint.setColor(Color.argb(80, 255, 255, 255));
        mHighlightDotPaint.setStyle(Paint.Style.STROKE);
        c.drawCircle(x, y, SizeUtils.dp2px(5f), mHighlightDotPaint);

    }
}
