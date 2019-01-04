package top.soyask.calendarii.entity;

import java.io.Serializable;

/**
 * Created by mxf on 2017/11/6.
 */

public class LunarDay implements Serializable {

    private String dayOfMonth;
    private int year;
    private String month;

    private String holiday; // 优先显示农历节日
    private String solar; // 节气
    private String era; // 农历年号
    private String zodiac;

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getHoliday() {
        return holiday;
    }

    public void setHoliday(String holiday) {
        this.holiday = holiday;
    }

    public String getSolar() {
        return solar;
    }

    public void setSolar(String solar) {
        this.solar = solar;
    }

    public String getEra() {
        return era;
    }

    public void setEra(String era) {
        this.era = era;
    }

    public String getZodiac() {
        return zodiac;
    }

    public void setZodiac(String zodiac) {
        this.zodiac = zodiac;
    }

    /**
     * @return 返回{ x月初x }形式
     */
    public String getLunarDate() {
        return month + dayOfMonth;
    }

    public String getSimpleLunar() {

        if (holiday != null) {
            return holiday;
        } else if (solar != null) {
            return solar;
        } else if (dayOfMonth.equals("初一")) {
            return month;
        } else {
            return dayOfMonth;
        }
    }
}
