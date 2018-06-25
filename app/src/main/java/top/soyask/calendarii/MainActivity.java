package top.soyask.calendarii;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import top.soyask.calendarii.global.GlobalData;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.main.MainFragment;

public class MainActivity extends AppCompatActivity {

    public static final int[] THEMES = {
            R.style.AppTheme,
            R.style.AppTheme_Green,
            R.style.AppTheme_Pink,
            R.style.AppTheme_Teal,
            R.style.AppTheme_Blue,
            R.style.AppTheme_Red,
            R.style.AppTheme_Purple,
            R.style.AppTheme_Black,
            R.style.AppTheme_Red
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkAndUpdateDpi();
        setupTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            new InitTask(this).execute();
        }
    }

    private void checkAndUpdateDpi() {
        if (Setting.density_dpi != -1) {
            Resources resources = getResources();
            Configuration configuration = new Configuration();
            configuration.setToDefaults();
            configuration.densityDpi = Setting.density_dpi;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
    }

    private void setupTheme() {
        int theme = THEMES[Setting.theme];
        setTheme(theme);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void init() {
        MainFragment mainFragment = MainFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, mainFragment)
                .commit();
    }

    private static  class InitTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<MainActivity> mActivity;

        InitTask(MainActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity activity = mActivity.get();
            if(activity != null){
                GlobalData.loadBirthday(activity);
                GlobalData.loadHoliday(activity);
                GlobalData.loadWorkday(activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MainActivity activity = mActivity.get();
            if(activity !=null){
                activity.init();
            }
        }
    }

}