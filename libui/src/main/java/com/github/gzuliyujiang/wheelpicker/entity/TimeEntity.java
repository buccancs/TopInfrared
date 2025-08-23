

package com.github.gzuliyujiang.wheelpicker.entity;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

@SuppressWarnings({"unused"})
public class TimeEntity implements Serializable {
    private int hour;
    private int minute;
    private int second;

    public static TimeEntity target(int hour, int minute, int second) {
        TimeEntity entity = new TimeEntity();
        entity.setHour(hour);
        entity.setMinute(minute);
        entity.setSecond(second);
        return entity;
    }

    public static TimeEntity now() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return target(hour, minute, second);
    }

    public static TimeEntity minuteOnFuture(int minute) {
        TimeEntity entity = now();
        entity.setMinute(entity.getMinute() + minute);
        return entity;
    }

    public static TimeEntity hourOnFuture(int hour) {
        TimeEntity entity = now();
        entity.setHour(entity.getHour() + hour);
        return entity;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public long toTimeInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @NonNull
    @Override
    public String toString() {
        return hour + ":" + minute + ":" + second;
    }

}
