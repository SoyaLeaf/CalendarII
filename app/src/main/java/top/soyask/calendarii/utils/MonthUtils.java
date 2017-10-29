package top.soyask.calendarii.utils;

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
        String result = HolidayUtils.getHolidayOfMonth(calendar);
        if (result == null) {
            result = LunarUtils.getLunar(calendar);
            String lunarHoliday = HolidayUtils.getHolidayOfLunar(result);
            if ("除夕".equals(lunarHoliday)) {
                lunarHoliday = checkNextDayIsChuxi(calendar, lunarHoliday);
            }
            if (lunarHoliday != null) {
                return lunarHoliday;
            }
            int length = result.length();
            if (result.endsWith("初一")) {
                result = result.substring(0, length - 2);
            } else {
                result = result.substring(length - 2, length);
            }
        } else {
            if (result.length() > 4) {
                result = result.substring(0, 4);
            }
        }

        String solar = SolarUtils.getSolar(calendar);
        return solar == null ? result : solar;
    }
}
