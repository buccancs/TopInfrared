package com.topdon.commons.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.topdon.lms.sdk.utils.LanguageUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Desc
 * @ClassName 时间工具类
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2022/12/13 21:57
 */

public class TimeGMTUtils {


    /**
     * 判断是否在夏令时
     *
     * @param zone 当前时区
     * @param time 0时区
     * @return boolean
     */
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

    /**
     * 消息时间转换
     *
     * @param time 2022-11-01 20:50:13 GMT时间
     * @return String
     */
    public static String getGMTConvertTime(String time, String format) {
        try {
//            LLog.w("bcf", "GMT--time--" + time);
            if (TextUtils.isEmpty(time)) {
                return "";
            }
            long longTime = getStringToDate(time, "GMT+00:00", "yyyy-MM-dd HH:mm:ss");
            Locale curLocale = LanguageUtil.getSystemLocal();
            String gmt = TimeZone.getDefault().getDisplayName(isDaylight(TimeZone.getDefault(), time), TimeZone.SHORT, curLocale);
//            LLog.w("bcf", "GMT--" + gmt);
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

    /**
     * 将字符串转为时间戳
     * GMT转换中国Asia/Shanhai时间戳
     *
     * @param dateString 2022-07-13 09:58:09
     * @param pattern    yyyy-MM-dd HH:mm:ss
     * @return long
     */
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
