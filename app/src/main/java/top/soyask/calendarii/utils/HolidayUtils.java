package top.soyask.calendarii.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mxf on 2017/8/10.
 */
public class HolidayUtils {

    private static final Map<String, String> LUNAR_HOLIDAY = new HashMap<>();

    static {
        if (LUNAR_HOLIDAY.isEmpty()) {
            LUNAR_HOLIDAY.put("正月初一", "春节");
            LUNAR_HOLIDAY.put("正月十五", "元宵节");
            LUNAR_HOLIDAY.put("五月初五", "端午节");
            LUNAR_HOLIDAY.put("七月初七", "七夕节");
            LUNAR_HOLIDAY.put("七月十五", "中元节");
            LUNAR_HOLIDAY.put("八月十五", "中秋节");
            LUNAR_HOLIDAY.put("九月初九", "重阳节");
            LUNAR_HOLIDAY.put("腊月初八", "腊八节");
            LUNAR_HOLIDAY.put("腊月廿九", "除夕");
            LUNAR_HOLIDAY.put("腊月三十", "除夕");
        }
    }

    public static final String getHolidayOfMonth(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        switch (dayOfMonth) {
            case 1:
                if (month == Calendar.JANUARY) {
                    return "元旦";
                }
                if (month == Calendar.APRIL) {
                    return "愚人节";
                }
                if (month == Calendar.MAY) {
                    return "劳动节";
                }
                if (month == Calendar.JUNE) {
                    return "儿童节";
                }
                if (month == Calendar.JULY) {
                    return "建党节";
                }
                if (month == Calendar.AUGUST) {
                    return "建军节";
                }
                if (month == Calendar.OCTOBER) {
                    return "国庆节";
                }
            default:
                switch (month) {
                    case Calendar.FEBRUARY:
                        if (dayOfMonth == 2) {
                            return "湿地日";
                        }
                        if (dayOfMonth == 14) {
                            return "情人节";
                        }
                        break;
                    case Calendar.MARCH:
                        if (dayOfMonth == 8) {
                            return "妇女节";
                        }
                        if (dayOfMonth == 12) {
                            return "植树节";
                        }
                        if (dayOfMonth == 15) {
                            return "消权日";
                        }
                        break;
                    case Calendar.APRIL:
                        if (dayOfMonth == 22) {
                            return "地球日";
                        }
                        break;

                    case Calendar.MAY:
                        switch (dayOfMonth) {
                            case 4:
                                return "青年节";
                            case 12:
                                return "护士节";
                            case 15:
                                return "博物馆日";
                            default:
                                return calculateHolidayForWeek(calendar);
                        }
                    case Calendar.JUNE:
                        return calculateHolidayForWeek(calendar);
                    case Calendar.JULY:
                        break;
                    case Calendar.AUGUST:
                        break;
                    case Calendar.SEPTEMBER:
                        if (dayOfMonth == 3) {
                            return "抗战胜利日";
                        }
                        break;
                    case Calendar.OCTOBER:
                        break;
                    case Calendar.NOVEMBER:
                        break;
                    case Calendar.DECEMBER:
                        if (dayOfMonth == 1) {
                            return "艾滋病日";
                        }
                        if (dayOfMonth == 25) {
                            return "圣诞节";
                        }
                        break;
                }
                return null;
        }
    }

    public static String getHolidayOfLunar(String lunar) {
        return LUNAR_HOLIDAY.get(lunar);
    }

    private static String calculateHolidayForWeek(Calendar calendar) {
        int dayForWeek = DayUtils.getDayForWeek(calendar);
        String holiday = null;
        if (dayForWeek == 0) {
            switch (calendar.get(Calendar.MONTH)) {
                case Calendar.MAY:
                    if (DayUtils.getWeekForMonth(calendar) == 3) {
                        holiday = "母亲节";
                    }
                    break;
                case Calendar.JUNE:
                    if (DayUtils.getWeekForMonth(calendar) == 4) {
                        holiday = "父亲节";
                    }
                    break;
            }
        }
        return holiday;
    }

}
