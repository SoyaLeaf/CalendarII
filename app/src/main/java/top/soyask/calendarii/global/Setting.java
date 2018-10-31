package top.soyask.calendarii.global;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import top.soyask.calendarii.entity.Symbol;

/**
 * Created by mxf on 2017/10/11.
 */
public final class Setting {
    public static final String SETTING = "setting";
    public static int date_offset = 1;
    public static int theme = 0;
    public static int density_dpi = -1;
    public static boolean replenish; //是否在日历空白填充文字
    public static boolean select_anim;
    public static String white_widget_pic = "";

    public static int day_size;
    public static float day_number_text_size;
    public static float day_lunar_text_size;
    public static float day_week_text_size;
    public static float day_holiday_text_size;
    public static Map<String, String> symbol_comment = new HashMap<>();
    public static int default_event_type;

    private static boolean isLoaded = false;

    public static void loadSetting(Context context) {
        if (isLoaded) {
            return;
        }
        SharedPreferences setting = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        Setting.theme = setting.getInt(Global.SETTING_THEME, 0);
        Setting.date_offset = setting.getInt(Global.SETTING_DATE_OFFSET, 0);

        Setting.white_widget_pic = setting.getString(Global.SETTING_WHITE_WIDGET_PIC, null);
        Setting.density_dpi = setting.getInt(Global.SETTING_DENSITY_DPI, -1);
        Setting.day_size = setting.getInt(Global.SETTING_DAY_SIZE, -1);
        Setting.day_number_text_size = setting.getInt(Global.SETTING_DAY_NUMBER_TEXT_SIZE, -1);
        Setting.day_lunar_text_size = setting.getInt(Global.SETTING_DAY_LUNAR_TEXT_SIZE, -1);
        Setting.day_week_text_size = setting.getInt(Global.SETTING_DAY_WEEK_TEXT_SIZE, -1);
        Setting.day_holiday_text_size = setting.getInt(Global.SETTING_DAY_HOLIDAY_TEXT_SIZE, -1);
        Setting.replenish = setting.getBoolean(Global.SETTING_REPLENISH, true);
        Setting.select_anim = setting.getBoolean(Global.SETTING_SELECT_ANIM, true);

        Setting.default_event_type = setting.getInt(Global.DEFAULT_EVENT_TYPE, 0);
        putEventType(setting, Symbol.STAR.KEY, "默认");
        putEventType(setting, Symbol.RECT.KEY, "默认");
        putEventType(setting, Symbol.CIRCLE.KEY, "默认");
        putEventType(setting, Symbol.TRIANGLE.KEY, "默认");
        putEventType(setting, Symbol.HEART.KEY, "默认");

        TransparentWidget.loadSetting(setting);

        isLoaded = true;
    }

    private static void putEventType(SharedPreferences setting, String key, String defaultVal) {
        symbol_comment.put(key, setting.getString(key, defaultVal));
    }

    public static class TransparentWidget {
        public static int trans_widget_theme_color = 0;
        public static int trans_widget_alpha = 33;
        public static int trans_widget_week_font_size = 14;
        public static int trans_widget_number_font_size = 14;
        public static int trans_widget_lunar_font_size = 8;
        //        public static int trans_widget_line_height = 100;
        public static int trans_widget_lunar_month_text_size = 10;
        public static int trans_widget_month_text_size = 28;
        public static int trans_widget_year_text_size = 12;

        public static void reset(Context context) {
            trans_widget_week_font_size = 14;
            trans_widget_number_font_size = 14;
            trans_widget_lunar_font_size = 8;
//            trans_widget_line_height = 100;
            trans_widget_lunar_month_text_size = 10;
            trans_widget_month_text_size = 28;
            trans_widget_year_text_size = 12;

            remove(context, Global.SETTING_TRANS_WIDGET_WEEK_FONT_SIZE);
            remove(context, Global.SETTING_TRANS_WIDGET_NUMBER_FONT_SIZE);
            remove(context, Global.SETTING_TRANS_WIDGET_LUNAR_FONT_SIZE);
//            remove(context, Global.SETTING_TRANS_WIDGET_LINE_HEIGHT);
            remove(context, Global.SETTING_TRANS_WIDGET_LUNAR_MONTH_TEXT_SIZE);
            remove(context, Global.SETTING_TRANS_WIDGET_MONTH_TEXT_SIZE);
            remove(context, Global.SETTING_TRANS_WIDGET_YEAR_TEXT_SIZE);
        }

        private static void loadSetting(SharedPreferences setting) {
            TransparentWidget.trans_widget_alpha = setting.getInt(Global.SETTING_TRANS_WIDGET_ALPHA, 0);
            TransparentWidget.trans_widget_theme_color = setting.getInt(Global.SETTING_TRANS_WIDGET_THEME_COLOR, 0);
            TransparentWidget.trans_widget_week_font_size = setting.getInt(Global.SETTING_TRANS_WIDGET_WEEK_FONT_SIZE, TransparentWidget.trans_widget_week_font_size);
            TransparentWidget.trans_widget_number_font_size = setting.getInt(Global.SETTING_TRANS_WIDGET_NUMBER_FONT_SIZE, TransparentWidget.trans_widget_number_font_size);
            TransparentWidget.trans_widget_lunar_font_size = setting.getInt(Global.SETTING_TRANS_WIDGET_LUNAR_FONT_SIZE, TransparentWidget.trans_widget_lunar_font_size);
//            TransparentWidget.trans_widget_line_height = setting.getInt(Global.SETTING_TRANS_WIDGET_LINE_HEIGHT, TransparentWidget.trans_widget_line_height);
            TransparentWidget.trans_widget_lunar_month_text_size = setting.getInt(Global.SETTING_TRANS_WIDGET_LUNAR_MONTH_TEXT_SIZE, TransparentWidget.trans_widget_lunar_month_text_size);
            TransparentWidget.trans_widget_month_text_size = setting.getInt(Global.SETTING_TRANS_WIDGET_MONTH_TEXT_SIZE, TransparentWidget.trans_widget_month_text_size);
            TransparentWidget.trans_widget_year_text_size = setting.getInt(Global.SETTING_TRANS_WIDGET_YEAR_TEXT_SIZE, TransparentWidget.trans_widget_year_text_size);
        }
    }

    public static void setting(Context context, String name, int value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putInt(name, value).apply();
    }

    public static void setting(Context context, String name, float value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putFloat(name, value).apply();
    }

    public static void setting(Context context, String name, boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putBoolean(name, value).apply();
    }

    public static void setting(Context context, String name, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putString(name, value).apply();
    }

    public static void setting(Context context, String name, Set<String> value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putStringSet(name, value).apply();
    }

    public static void remove(Context context, String name) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Setting.SETTING, Context.MODE_PRIVATE).edit();
        editor.remove(name).apply();
    }
}
