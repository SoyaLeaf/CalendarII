package top.soyask.calendarii.utils;

import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * Created by mxf on 2017/10/29.
 */

public class MonthUtils {

    /**
     * 判断是否有大年三十
     * @param calendar
     * @param lunarHoliday
     * @return
     */
    public static final String checkNextDayIsChuxi(Calendar calendar, String lunarHoliday) {
        Calendar next = Calendar.getInstance();
        next.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        next.add(Calendar.DATE, 1);
        String lunar = HolidayUtils.getHolidayOfLunar(LunarUtils.getLunar(next));
        if ("除夕".equals(lunar)) {
            lunarHoliday = null;
        }
        return lunarHoliday;
    }

    public static final String getLunar(Calendar calendar) {
        String holidayOfMonth = getHolidayOfMonth(calendar);
        String solar = SolarUtils.getSolar(calendar);
        String lunar = getLunarSimple(calendar);
        String lunarHoliday = getLunarHoliday(calendar, lunar);

        if(lunarHoliday != null){
            return lunarHoliday;
        }else if(holidayOfMonth != null){
            return holidayOfMonth;
        }else if(solar != null){
            return solar;
        }else {
            return lunar;
        }
    }

    private static String getLunarHoliday(Calendar calendar, String lunar) {
        String lunarHoliday = HolidayUtils.getHolidayOfLunar(lunar);
        if ("除夕".equals(lunarHoliday)) {
            lunarHoliday = checkNextDayIsChuxi(calendar, lunarHoliday);
        }
        return lunarHoliday;
    }

    @NonNull
    private static String getLunarSimple(Calendar calendar) {
        String lunar = LunarUtils.getLunar(calendar);
        int length = lunar.length();
        if (lunar.endsWith("初一")) {
            lunar = lunar.substring(0, length - 2);
        } else {
            lunar = lunar.substring(length - 2, length);
        }
        return lunar;
    }

    private static String getHolidayOfMonth(Calendar calendar) {
        String holidayOfMonth = HolidayUtils.getHolidayOfMonth(calendar);
        if (holidayOfMonth != null && holidayOfMonth.length() > 4) {
            holidayOfMonth = holidayOfMonth.substring(0, 4);
        }
        return holidayOfMonth;
    }
}
