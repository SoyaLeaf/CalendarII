package top.soyask.calendarii.global;

/**
 * Created by mxf on 2017/8/10.
 */
public class Global {
    public static final int YEAR_START = 1900; //计算的起点
    public static final int YEAR_START_REAL = 1910; //日历上显示的最小年份
    public static final int YEAR_END = 2100;
    public static final int MONTH_COUNT = 12;

    public static final String SETTING_THEME = "theme";
    public static final String SETTING_DATE_OFFSET = "date_offset";
    public static final String SETTING_REPLENISH = "setting_replenish";
    public static final String SETTING_SELECT_ANIM = "select_anim";
    public static final String SETTING_WIDGET_ALPHA = "widget_alpha";
    public static final String SETTING_WHITE_WIDGET_PIC = "white_widget_pic";
    public static final String SETTING_DENSITY_DPI = "setting_density_dpi";

    public static final String SETTING_TRANS_WIDGET_ALPHA = SETTING_WIDGET_ALPHA; //为了同之前的版本保持兼容性
    public static final String SETTING_TRANS_WIDGET_THEME_COLOR = "setting_trans_widget_theme_color";
    public static final String SETTING_TRANS_WIDGET_WEEK_FONT_SIZE = "trans_widget_week_font_size";
    public static final String SETTING_TRANS_WIDGET_NUMBER_FONT_SIZE = "trans_widget_number_font_size";
    public static final String SETTING_TRANS_WIDGET_LUNAR_FONT_SIZE = "trans_widget_lunar_font_size";
    public static final String SETTING_TRANS_WIDGET_LINE_HEIGHT = "trans_widget_line_height";
    public static final String SETTING_TRANS_WIDGET_LUNAR_MONTH_TEXT_SIZE = "trans_widget_lunar_month_text_size";
    public static final String SETTING_TRANS_WIDGET_MONTH_TEXT_SIZE = "trans_widget_month_text_size ";
    public static final String SETTING_TRANS_WIDGET_YEAR_TEXT_SIZE = "trans_widget_year_text_size ";

    public static final String SETTING_DAY_SIZE = "day_size";
    public static final String SETTING_DAY_NUMBER_TEXT_SIZE = "day_number_text_size";
    public static final String SETTING_DAY_LUNAR_TEXT_SIZE = "day_lunar_text_size";
    public static final String SETTING_DAY_WEEK_TEXT_SIZE = "day_week_text_size";
    public static final String SETTING_DAY_HOLIDAY_TEXT_SIZE = "day_holiday_text_size";

    public static final String SETTING_HOLIDAY = "holiday";
    public static final String SETTING_WORKDAY = "workday";

    public static final int VIEW_WEEK = 0; //显示星期
    public static final int VIEW_DAY = 1; //显示日子
    public static final int VIEW_TODAY = 4;
    public static final int VIEW_EVENT = 5;
    public static final String DEFAULT_EVENT_TYPE = "default_event_type";
}
