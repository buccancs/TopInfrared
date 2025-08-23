
package com.github.mikephil.charting.data;

import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class PieDataSet extends DataSet<PieEntry> implements IPieDataSet {

    private float mSliceSpace = 0f;
    private boolean mAutomaticallyDisableSliceSpacing;

    private float mShift = 18f;

    private ValuePosition mXValuePosition = ValuePosition.INSIDE_SLICE;
    private ValuePosition mYValuePosition = ValuePosition.INSIDE_SLICE;
    private boolean mUsingSliceColorAsValueLineColor = false;
    private int mValueLineColor = 0xff000000;
    private float mValueLineWidth = 1.0f;
    private float mValueLinePart1OffsetPercentage = 75.f;
    private float mValueLinePart1Length = 0.3f;
    private float mValueLinePart2Length = 0.4f;
    private boolean mValueLineVariableLength = true;

    public PieDataSet(List<PieEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public DataSet<PieEntry> copy() {
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < mValues.size(); i++) {
            entries.add(mValues.get(i).copy());
        }
        PieDataSet copied = new PieDataSet(entries, getLabel());
        copy(copied);
        return copied;
    }

    protected void copy(PieDataSet pieDataSet) {
        super.copy(pieDataSet);
    }

    @Override
    protected void calcMinMax(PieEntry e) {

        if (e == null)
            return;

        calcMinMaxY(e);
    }

    public void setSliceSpace(float spaceDp) {

        if (spaceDp > 20)
            spaceDp = 20f;
        if (spaceDp < 0)
            spaceDp = 0f;

        mSliceSpace = Utils.convertDpToPixel(spaceDp);
    }

    @Override
    public float getSliceSpace() {
        return mSliceSpace;
    }

    public void setAutomaticallyDisableSliceSpacing(boolean autoDisable) {
        mAutomaticallyDisableSliceSpacing = autoDisable;
    }

    @Override
    public boolean isAutomaticallyDisableSliceSpacingEnabled() {
        return mAutomaticallyDisableSliceSpacing;
    }

    public void setSelectionShift(float shift) {
        mShift = Utils.convertDpToPixel(shift);
    }

    @Override
    public float getSelectionShift() {
        return mShift;
    }

    @Override
    public ValuePosition getXValuePosition() {
        return mXValuePosition;
    }

    public void setXValuePosition(ValuePosition xValuePosition) {
        this.mXValuePosition = xValuePosition;
    }

    @Override
    public ValuePosition getYValuePosition() {
        return mYValuePosition;
    }

    public void setYValuePosition(ValuePosition yValuePosition) {
        this.mYValuePosition = yValuePosition;
    }

    @Override
    public boolean isUsingSliceColorAsValueLineColor() {
        return mUsingSliceColorAsValueLineColor;
    }

    public void setUsingSliceColorAsValueLineColor(boolean usingSliceColorAsValueLineColor) {
        this.mUsingSliceColorAsValueLineColor = usingSliceColorAsValueLineColor;
    }

    @Override
    public int getValueLineColor() {
        return mValueLineColor;
    }

    public void setValueLineColor(int valueLineColor) {
        this.mValueLineColor = valueLineColor;
    }

    @Override
    public float getValueLineWidth() {
        return mValueLineWidth;
    }

    public void setValueLineWidth(float valueLineWidth) {
        this.mValueLineWidth = valueLineWidth;
    }

    @Override
    public float getValueLinePart1OffsetPercentage() {
        return mValueLinePart1OffsetPercentage;
    }

    public void setValueLinePart1OffsetPercentage(float valueLinePart1OffsetPercentage) {
        this.mValueLinePart1OffsetPercentage = valueLinePart1OffsetPercentage;
    }

    @Override
    public float getValueLinePart1Length() {
        return mValueLinePart1Length;
    }

    public void setValueLinePart1Length(float valueLinePart1Length) {
        this.mValueLinePart1Length = valueLinePart1Length;
    }

    @Override
    public float getValueLinePart2Length() {
        return mValueLinePart2Length;
    }

    public void setValueLinePart2Length(float valueLinePart2Length) {
        this.mValueLinePart2Length = valueLinePart2Length;
    }

    @Override
    public boolean isValueLineVariableLength() {
        return mValueLineVariableLength;
    }

    public void setValueLineVariableLength(boolean valueLineVariableLength) {
        this.mValueLineVariableLength = valueLineVariableLength;
    }

    public enum ValuePosition {
        INSIDE_SLICE,
        OUTSIDE_SLICE
    }
}
