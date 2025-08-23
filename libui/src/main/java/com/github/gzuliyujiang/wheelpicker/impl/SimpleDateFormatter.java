

package com.github.gzuliyujiang.wheelpicker.impl;

import com.github.gzuliyujiang.wheelpicker.contract.DateFormatter;

public class SimpleDateFormatter implements DateFormatter {

    @Override
    public String formatYear(int year) {
        if (year < 1000) {
            year += 1000;
        }
        return "" + year;
    }

    @Override
    public String formatMonth(int month) {
        return month < 10 ? "0" + month : "" + month;
    }

    @Override
    public String formatDay(int day) {
        return day < 10 ? "0" + day : "" + day;
    }

}
