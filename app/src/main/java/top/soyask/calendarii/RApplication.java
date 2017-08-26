package top.soyask.calendarii;

import android.app.Application;

/**
 * Created by mxf on 2017/8/25.
 */
public class RApplication extends Application {


    @Override
    public void onCreate() {
        setTheme(R.style.AppTheme_Teal);
        super.onCreate();
    }

}
