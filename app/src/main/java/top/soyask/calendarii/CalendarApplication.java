package top.soyask.calendarii;

import android.app.Application;

import top.soyask.calendarii.crash.CrashHandler;
import top.soyask.calendarii.global.Setting;

/**
 * Created by mxf on 2018/4/24.
 */

public class CalendarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        Setting.loadSetting(this);
    }
}
