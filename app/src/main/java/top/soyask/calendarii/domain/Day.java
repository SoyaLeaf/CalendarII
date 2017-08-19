package top.soyask.calendarii.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mxf on 2017/8/8.
 */
public class Day implements Serializable{
    private int dayOfMonth;
    private String lunar;
    private boolean isToday;
    private int dayOfWeek;
    private int year;
    private int month;
    private List<Event> events;

    public Day(int year,int month,String lunar, boolean isToday, int dayOfMonth, int dayOfWeek) {
        this.year = year;
        this.month = month;
        this.lunar = lunar;
        this.isToday = isToday;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
    }

    public Day(){}

    public Day(int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Event> getEvents() {
        return events;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean isToday() {
        return isToday;
    }


    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setLunar(String lunar) {
        this.lunar = lunar;
    }

    public String getLunar() {
        return lunar;
    }

    @Override
    public String toString() {
        return "Day{" +
                "year=" + year +
                ",month=" + month +
                ",dayOfMonth=" + dayOfMonth +
                ", lunar='" + lunar + '\'' +
                ", isToday=" + isToday +
                ", dayOfWeek=" + dayOfWeek +
                '}';
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }
}
