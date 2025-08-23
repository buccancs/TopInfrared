
package com.github.mikephil.charting.components;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.github.mikephil.charting.utils.Utils;

public class LimitLine extends ComponentBase {

    private float mLimit = 0f;

    private float mLineWidth = 2f;

    private int mLineColor = Color.rgb(237, 91, 91);

    private Paint.Style mTextStyle = Paint.Style.FILL_AND_STROKE;

    private String mLabel = "";

    private DashPathEffect mDashPathEffect = null;

    private LimitLabelPosition mLabelPosition = LimitLabelPosition.RIGHT_TOP;

    public enum LimitLabelPosition {
        LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
    }

    public LimitLine(float limit) {
        mLimit = limit;
    }

    public LimitLine(float limit, String label) {
        mLimit = limit;
        mLabel = label;
    }

    public float getLimit() {
        return mLimit;
    }

    public void setLineWidth(float width) {

        if (width < 0.2f)
            width = 0.2f;
        if (width > 12.0f)
            width = 12.0f;
        mLineWidth = Utils.convertDpToPixel(width);
    }

    public float getLineWidth() {
        return mLineWidth;
    }

    public void setLineColor(int color) {
        mLineColor = color;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public void enableDashedLine(float lineLength, float spaceLength, float phase) {
        mDashPathEffect = new DashPathEffect(new float[] {
                lineLength, spaceLength
        }, phase);
    }

    public void disableDashedLine() {
        mDashPathEffect = null;
    }

    public boolean isDashedLineEnabled() {
        return mDashPathEffect == null ? false : true;
    }

    public DashPathEffect getDashPathEffect() {
        return mDashPathEffect;
    }

    public void setTextStyle(Paint.Style style) {
        this.mTextStyle = style;
    }

    public Paint.Style getTextStyle() {
        return mTextStyle;
    }

    public void setLabelPosition(LimitLabelPosition pos) {
        mLabelPosition = pos;
    }

    public LimitLabelPosition getLabelPosition() {
        return mLabelPosition;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public String getLabel() {
        return mLabel;
    }
}
