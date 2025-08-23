package com.github.mikephil.charting.formatter;

import java.text.DecimalFormat;

public class DefaultValueFormatter extends ValueFormatter
{

    protected DecimalFormat mFormat;

    protected int mDecimalDigits;

    public DefaultValueFormatter(int digits) {
        setup(digits);
    }

    public void setup(int digits) {

        this.mDecimalDigits = digits;

        StringBuffer b = new StringBuffer();
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                b.append(".");
            b.append("0");
        }

        mFormat = new DecimalFormat("###,###,###,##0" + b.toString());
    }

    @Override
    public String getFormattedValue(float value) {

        return mFormat.format(value);
    }

    public int getDecimalDigits() {
        return mDecimalDigits;
    }
}
