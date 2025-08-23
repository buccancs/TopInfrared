

package com.github.gzuliyujiang.wheelpicker;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.impl.BirthdayFormatter;

import java.util.Calendar;

@SuppressWarnings("unused")
public class BirthdayPicker extends DatePicker {
    private static final int MAX_AGE = 100;
    private DateEntity defaultValue;
    private boolean initialized = false;

    public BirthdayPicker(@NonNull Activity activity) {
        super(activity);
    }

    public BirthdayPicker(@NonNull Activity activity, @StyleRes int themeResId) {
        super(activity, themeResId);
    }

    @Override
    protected void initData() {
        super.initData();
        initialized = true;
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        DateEntity startValue = DateEntity.target(currentYear - MAX_AGE, 1, 1);
        DateEntity endValue = DateEntity.target(currentYear, currentMonth, currentDay);
        wheelLayout.setRange(startValue, endValue, defaultValue);
        wheelLayout.setDateMode(DateMode.YEAR_MONTH_DAY);
        wheelLayout.setDateFormatter(new BirthdayFormatter());
    }

    public void setDefaultValue(int year, int month, int day) {
        defaultValue = DateEntity.target(year, month, day);
        if (initialized) {
            wheelLayout.setDefaultValue(defaultValue);
        }
    }

}
