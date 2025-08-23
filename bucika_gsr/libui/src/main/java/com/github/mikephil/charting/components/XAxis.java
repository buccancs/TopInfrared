
package com.github.mikephil.charting.components;

import com.github.mikephil.charting.utils.Utils;

public class XAxis extends AxisBase {

    public int mLabelWidth = 1;

    public int mLabelHeight = 1;

    public int mLabelRotatedWidth = 1;

    public int mLabelRotatedHeight = 1;

    protected float mLabelRotationAngle = 0f;

    private boolean mAvoidFirstLastClipping = false;

    private XAxisPosition mPosition = XAxisPosition.TOP;

    public enum XAxisPosition {
        TOP, BOTTOM, BOTH_SIDED, TOP_INSIDE, BOTTOM_INSIDE
    }

    public XAxis() {
        super();

        mYOffset = Utils.convertDpToPixel(4.f);
    }

    public XAxisPosition getPosition() {
        return mPosition;
    }

    public void setPosition(XAxisPosition pos) {
        mPosition = pos;
    }

    public float getLabelRotationAngle() {
        return mLabelRotationAngle;
    }

    public void setLabelRotationAngle(float angle) {
        mLabelRotationAngle = angle;
    }

    public void setAvoidFirstLastClipping(boolean enabled) {
        mAvoidFirstLastClipping = enabled;
    }

    public boolean isAvoidFirstLastClippingEnabled() {
        return mAvoidFirstLastClipping;
    }

    private boolean isJumpFirstLabel = true;

    public boolean isJumpFirstLabel() {
        return isJumpFirstLabel;
    }

    public void setJumpFirstLabel(boolean jumpFirstLabel) {
        isJumpFirstLabel = jumpFirstLabel;
    }
}
