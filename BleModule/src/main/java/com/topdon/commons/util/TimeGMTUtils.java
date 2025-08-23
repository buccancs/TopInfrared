package com.topdon.commons.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.topdon.lms.sdk.utils.LanguageUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeGMTUtils {

    private static boolean isDaylight(TimeZone zone, String time) {
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = sf.parse(time);
            return zone.useDaylightTime() && zone.inDaylightTime(d1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getGMTConvertTime(String time, String format) {
        try {
            if (TextUtils.isEmpty(time)) {
                return "";
            }
            long longTime = getStringToDate(time, "GMT+00:00", "yyyy-MM-dd HH:mm:ss");
            Locale curLocale = LanguageUtil.getSystemLocal();
            String gmt = TimeZone.getDefault().getDisplayName(isDaylight(TimeZone.getDefault(), time), TimeZone.SHORT, curLocale);
            return getDateToString(longTime, gmt, format);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDateToString(long milSecond, String gmt, String pattern) {
        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        TimeZone timeZone = TimeZone.getTimeZone(gmt);
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    public static long getStringToDate(String dateString, String gmt, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Date date = new Date();
        try {
            TimeZone timeZone = TimeZone.getTimeZone(gmt);
            dateFormat.setTimeZone(timeZone);
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}
