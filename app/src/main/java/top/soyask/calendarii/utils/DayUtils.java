package top.soyask.calendarii.utils;

import java.util.Calendar;

/**
 * Created by mxf on 2017/8/10.
 */
public class DayUtils {
    /**
     * @param month 传入实际的月份
     * @param year
     * @return
     */
    public static int getMonthDayCount(int month, int year) {
        month = month - 1;
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0) ? 29 : 28;
            default:
                throw new IllegalArgumentException("Didn't find this month(未找到该月份):" + month);
        }

    }

    public static int getPrevMonthDayCount(int month, int year) {
        if (month > 1) {
            month--;
        } else {
            year--;
        }
        return getMonthDayCount(month, year);
    }


    public static int getDayForWeek(Calendar calendar) {
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        if (date < 1 || date > 31) {
            return 1;
        }
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static int getWeekForMonth(Calendar calendar) {
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        if (date < 1 || date > 31) {
            return 1;
        }
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }
}
