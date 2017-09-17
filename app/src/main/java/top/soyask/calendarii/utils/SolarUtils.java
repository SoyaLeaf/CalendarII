package top.soyask.calendarii.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mxf on 2017/9/16.
 */
public class SolarUtils {

    public static final String[] SOLAR = {
            "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
            "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
            "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
            "立冬", "小雪", "大雪", "冬至", "小寒", "大寒",
    };

    private static final double[] C_IN_20 = {
            4.6295, 19.4599, 6.3826, 21.4155, 5.59, 20.888,
            6.318, 21.86, 6.5, 22.2, 7.928, 23.65,
            8.35, 23.95, 8.44, 23.822, 9.098, 24.218,
            8.218, 23.08, 7.9, 22.6, 6.11, 20.84
    };

    private static final double[] C_IN_21 = {
            3.87, 18.73, 5.63, 20.646, 4.81, 20.1,
            5.52, 21.04, 5.678, 21.37, 7.108, 22.83,
            7.5, 23.13, 7.646, 23.042, 8.318, 23.438,
            7.438, 22.36, 7.18, 21.94, 5.4055, 20.12
    };

    private static final double[][] C = {C_IN_20, C_IN_21};

    private static final Float D = 0.2422f;
    private static Map<Integer, Map<String, Integer>> SOLAR_MAP = new HashMap<>();

    public static String getSolar(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        String result = null;
        if (SOLAR_MAP.containsKey(year)) {
            Map<String, Integer> solarOfYear = SOLAR_MAP.get(year);
            Integer integer = solarOfYear.get(calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH));
            if (integer != null && integer >= 0 && integer < 24) {
                result = SOLAR[integer];
            }
        } else {
            initSolar(year);
            return getSolar(calendar);
        }
        return result;
    }

    private static void initSolar(int year) {
        Map<String, Integer> solarOfYear = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            int solarDay = getSolarDay(year, i);
            int m = (i / 2 + 1) % 12;
            solarOfYear.put(m + "/" + solarDay, i);
        }
        SOLAR_MAP.put(year, solarOfYear);
    }

    private static int getSolarDay(int year, int st) {
        int lastNum = year % 100; //取后两位
        int century = (year - 1) / 2000;
        return (int) (lastNum * D + getC(century, st) - getLeapCount(lastNum));
    }

    private static double getC(int century, int st) {
        return C[century][st];
    }

    private static int getLeapCount(int year) {
        return year / 4;
    }

}
