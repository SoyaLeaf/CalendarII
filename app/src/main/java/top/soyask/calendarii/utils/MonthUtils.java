package top.soyask.calendarii.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import top.soyask.calendarii.database.dao.MemorialDayDao;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.Birthday;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.entity.LunarDay;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.global.GlobalData;

/**
 * Created by mxf on 2017/10/29.
 */

public class MonthUtils {
    private static final String DATE_SP = "-";

    /**
     * 判断是否有大年三十
     *
     * @param calendar
     * @return
     */
    public static final String checkNextDayIsChuxi(Calendar calendar) {
        Calendar next = Calendar.getInstance();
        next.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        next.add(Calendar.DATE, 1);
        LunarDay lunarDay = LunarUtils.getLunar(next);
        String lunar = lunarDay.getLunarDate();
        String lunarHoliday = HolidayUtils.getHolidayOfLunar(lunar);
        if ("除夕".equals(lunarHoliday)) {
            return null;
        }
        return "除夕";
    }

    public static final LunarDay getLunar(Calendar calendar) {
        LunarDay lunarDay = LunarUtils.getLunar(calendar);
        setHoliday(calendar, lunarDay);
        setSolar(calendar, lunarDay);
        setEra(lunarDay);
        setZodiac(lunarDay);
        setLunarHoliday(calendar, lunarDay);
        return lunarDay;
    }

    private static void setSolar(Calendar calendar, LunarDay lunarDay) {
        String solar = SolarUtils.getSolar(calendar);
        lunarDay.setSolar(solar);
    }

    private static void setHoliday(Calendar calendar, LunarDay lunarDay) {
        String holidayOfMonth = getHolidayOfMonth(calendar);
        lunarDay.setHoliday(holidayOfMonth);
    }

    private static void setLunarHoliday(Calendar calendar, LunarDay lunarDay) {
        String lunarHoliday = getLunarHoliday(calendar, lunarDay);
        if (lunarHoliday != null) {
            lunarDay.setHoliday(lunarHoliday);
        }
    }

    private static void setZodiac(LunarDay lunarDay) {
        String zodiac = EraUtils.getYearForTwelveZodiac(lunarDay.getYear());
        lunarDay.setZodiac(zodiac);
    }

    private static void setEra(LunarDay lunarDay) {
        String branches = EraUtils.getYearForEarthlyBranches(lunarDay.getYear());
        String stems = EraUtils.getYearForHeavenlyStems(lunarDay.getYear());
        lunarDay.setEra(stems + branches);
    }

    private static String getLunarHoliday(Calendar calendar, LunarDay lunarDay) {
        String lunar = lunarDay.getLunarDate();
        String lunarHoliday = HolidayUtils.getHolidayOfLunar(lunar);
        if ("除夕".equals(lunarHoliday)) {
            lunarHoliday = checkNextDayIsChuxi(calendar);
        }
        return lunarHoliday;
    }


    private static String getHolidayOfMonth(Calendar calendar) {
        String holidayOfMonth = HolidayUtils.getHolidayOfMonth(calendar);
        if (holidayOfMonth != null && holidayOfMonth.length() > 4) {
            holidayOfMonth = holidayOfMonth.substring(0, 4);
        }
        return holidayOfMonth;
    }

    @NonNull
    public static Day generateDay(Calendar calendar, ThingDao thingDao, MemorialDayDao memorialDayDao) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        boolean isToday = isToday(dayOfMonth, year, month);

        LunarDay lunarDay = MonthUtils.getLunar(calendar);
        Day day = new Day(year, month, lunarDay, isToday, dayOfMonth, dayOfWeek);
        setMemorialDay(memorialDayDao, month, dayOfMonth, lunarDay, day);
        setHoliday(year, month, dayOfMonth, day);
        setThing(thingDao, day);
        return day;
    }

    private static void setMemorialDay(MemorialDayDao memorialDayDao, int month, int dayOfMonth, LunarDay lunarDay, Day day) {
        List<MemorialDay> memorialDays = new ArrayList<MemorialDay>() {{
            addAll(memorialDayDao.findMemorialDays(lunarDay.getLunarDate()));
            addAll(memorialDayDao.findMemorialDays(month, dayOfMonth));
        }};
        day.setMemorialDays(memorialDays);
    }

    private static void setHoliday(int year, int month, int dayOfMonth, Day day) {
        String str = new StringBuilder()
                .append(year)
                .append(DATE_SP)
                .append(month)
                .append(DATE_SP)
                .append(dayOfMonth)
                .toString();
        day.setHoliday(GlobalData.HOLIDAY.contains(str));
        day.setWorkday(GlobalData.WORKDAY.contains(str));
    }

    private static void setThing(ThingDao thingDao, Day day) {
        Calendar.getInstance();
        long begin = DayUtils.getDateBegin(day.getYear(), day.getMonth(), day.getDayOfMonth());
        List<Thing> things = thingDao.listByDate(begin);
        day.setThings(things);
    }

    private static boolean isToday(int dayOfMonth, int year, int month) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth
                && calendar.get(Calendar.YEAR) == year
                && calendar.get(Calendar.MONTH) + 1 == month;
    }
}
