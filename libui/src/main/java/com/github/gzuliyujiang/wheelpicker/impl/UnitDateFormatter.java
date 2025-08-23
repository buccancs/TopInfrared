

package com.github.gzuliyujiang.wheelpicker.impl;

import com.github.gzuliyujiang.wheelpicker.contract.DateFormatter;

public class UnitDateFormatter implements DateFormatter {

    @Override
    public String formatYear(int year) {
        return year + "年";
    }

    @Override
    public String formatMonth(int month) {
        return month + "月";
    }

    @Override
    public String formatDay(int day) {
        return day + "日";
    }

}
