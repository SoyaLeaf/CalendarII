package top.soyask.calendarii.utils;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;

/**
 * Created by mxf on 2017/8/10.
 */
public class EraUtils {
    public static final String[] HEAVENLY_STEMS = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    public static final String[] EARTHLY_BRANCHES = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    public static final String[] TWELVE_ZODIAC = {"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    public static final int[] TWELVE_IMG = {
            R.drawable.shu,R.drawable.niu,R.drawable.hu,R.drawable.tu,
            R.drawable.lon,R.drawable.she,R.drawable.ma,R.drawable.yang,
            R.drawable.hou,R.drawable.ji,R.drawable.gou,R.drawable.zhu,
    };



    public static String getYearForHeavenlyStems(int year) {
        int position = (year - Global.YEAR_START) % 10;
        return HEAVENLY_STEMS[(position + 6) % 10];
    }


    public static String getYearForEarthlyBranches(int year) {
        int position = (year - Global.YEAR_START) % 12;
        return EARTHLY_BRANCHES[position];
    }


    public static String getYearForTwelveZodiac(int year) {
        int position = (year - Global.YEAR_START) % 12;
        return TWELVE_ZODIAC[position];
    }

    public static int getYearForTwelveZodiacImage(int year) {
        int position = (year - Global.YEAR_START) % 12;
        return TWELVE_IMG[position];
    }

}
