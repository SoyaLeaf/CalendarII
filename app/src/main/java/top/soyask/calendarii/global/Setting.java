package top.soyask.calendarii.global;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * Created by mxf on 2017/10/11.
 */
public final class Setting {
    private static final String SETTING = "setting";
    public static int date_offset = 1;
    public static int theme = 0;
    public static int widget_alpha = 33;

    public static void loadSetting(Context context){
        SharedPreferences setting = context.getSharedPreferences(SETTING,Context.MODE_PRIVATE);
        Setting.theme = setting.getInt(Global.SETTING_THEME, 0);
        Setting.date_offset = setting.getInt(Global.SETTING_DATE_OFFSET, 0);
        Setting.widget_alpha = setting.getInt(Global.SETTING_WIDGET_ALPHA, 0);
    }

    public static void setting(Context context,String name,int value){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putInt(name,value).commit();
    }

    public static void setting(Context context,String name,float value){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putFloat(name,value).commit();
    }

    public static void setting(Context context,String name,String value){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putString(name,value).commit();
    }

    public static void setting(Context context,String name,Set<String> value){
        SharedPreferences.Editor editor = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE).edit();
        editor.putStringSet(name,value).commit();
    }


}
