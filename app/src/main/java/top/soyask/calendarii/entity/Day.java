package top.soyask.calendarii.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import top.soyask.calendarii.ui.view.CalendarView;

/**
 * Created by mxf on 2017/8/8.
 */
public class Day implements CalendarView.IDay, Serializable {

    private static final String BIRTHDAY = "生日";
    private int dayOfMonth;
    private LunarDay lunar;
    private boolean isToday;
    private int dayOfWeek;
    private int year;
    private int month;
    private boolean isHoliday;
    private boolean isWorkday; //是否被调休
    private List<Birthday> birthdays;
    private List<Event> events;

    public Day(int year, int month, LunarDay lunar, boolean isToday, int dayOfMonth, int dayOfWeek) {
        this.year = year;
        this.month = month;
        this.lunar = lunar;
        this.isToday = isToday;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
    }


    public Day() {
    }

    public void addBirthday(Birthday birthday) {
        if (birthday != null) {
            if (birthdays == null) {
                birthdays = new ArrayList<>();
            }
            birthdays.add(birthday);
        }
    }

    public void addBirthday(List<Birthday> birthday) {
        if (birthday != null) {
            if (birthdays == null) {
                birthdays = new ArrayList<>();
            }
            birthdays.addAll(birthday);
        }
    }

    public List<Birthday> getBirthdays() {
        return birthdays;
    }

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

    @Override
    public String getBottomText() {
        return hasBirthday() ? BIRTHDAY : getLunar().getSimpleLunar();
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

    public void setLunar(LunarDay lunar) {
        this.lunar = lunar;
    }

    public LunarDay getLunar() {
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

    public boolean isHoliday() {
        return isHoliday;
    }

    public void setHoliday(boolean holiday) {
        isHoliday = holiday;
    }

    public boolean hasBirthday() {
        return birthdays != null && !birthdays.isEmpty();
    }

    @Override
    public Symbol getSymbol() {
        if(hasEvent()){
            Event event = events.get(0);
            int type = event.getType();
            Symbol[] values = Symbol.values();
            return values[type];
        }
        return null;
    }

    public boolean hasEvent() {
        return events != null && !events.isEmpty();
    }

    public boolean isWorkday() {
        return isWorkday;
    }

    public void setWorkday(boolean workday) {
        isWorkday = workday;
    }
}
