

package com.github.gzuliyujiang.wheelpicker.entity;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

@SuppressWarnings({"unused"})
public class DatimeEntity implements Serializable {
    private DateEntity date;
    private TimeEntity time;

    public static DatimeEntity now() {
        DatimeEntity entity = new DatimeEntity();
        entity.setDate(DateEntity.today());
        entity.setTime(TimeEntity.now());
        return entity;
    }

    public static DatimeEntity minuteOnFuture(int minute) {
        DatimeEntity entity = now();
        entity.setTime(TimeEntity.minuteOnFuture(minute));
        return entity;
    }

    public static DatimeEntity hourOnFuture(int hour) {
        DatimeEntity entity = now();
        entity.setTime(TimeEntity.hourOnFuture(hour));
        return entity;
    }

    public static DatimeEntity dayOnFuture(int day) {
        DatimeEntity entity = now();
        entity.setDate(DateEntity.dayOnFuture(day));
        return entity;
    }

    public static DatimeEntity monthOnFuture(int month) {
        DatimeEntity entity = now();
        entity.setDate(DateEntity.monthOnFuture(month));
        return entity;
    }

    public static DatimeEntity yearOnFuture(int year) {
        DatimeEntity entity = now();
        entity.setDate(DateEntity.yearOnFuture(year));
        return entity;
    }

    public DateEntity getDate() {
        return date;
    }

    public void setDate(DateEntity date) {
        this.date = date;
    }

    public TimeEntity getTime() {
        return time;
    }

    public void setTime(TimeEntity time) {
        this.time = time;
    }

    public long toTimeInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, date.getYear());
        calendar.set(Calendar.MONTH, date.getMonth() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, date.getDay());
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, time.getSecond());
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @NonNull
    @Override
    public String toString() {
        return date.toString() + " " + time.toString();
    }

}
