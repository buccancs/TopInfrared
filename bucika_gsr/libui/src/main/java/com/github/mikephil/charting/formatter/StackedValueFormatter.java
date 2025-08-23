package com.github.mikephil.charting.formatter;

import com.github.mikephil.charting.data.BarEntry;

import java.text.DecimalFormat;

public class StackedValueFormatter extends ValueFormatter
{

    private boolean mDrawWholeStack;

    private String mSuffix;

    private DecimalFormat mFormat;

    public StackedValueFormatter(boolean drawWholeStack, String suffix, int decimals) {
        this.mDrawWholeStack = drawWholeStack;
        this.mSuffix = suffix;

        StringBuffer b = new StringBuffer();
        for (int i = 0; i < decimals; i++) {
            if (i == 0)
                b.append(".");
            b.append("0");
        }

        this.mFormat = new DecimalFormat("###,###,###,##0" + b.toString());
    }

    @Override
    public String getBarStackedLabel(float value, BarEntry entry) {
        if (!mDrawWholeStack) {

            float[] vals = entry.getYVals();

            if (vals != null) {

                if (vals[vals.length - 1] == value) {

                    return mFormat.format(entry.getY()) + mSuffix;
                } else {
                    return "";
                }
            }
        }

        return mFormat.format(value) + mSuffix;
    }
}
