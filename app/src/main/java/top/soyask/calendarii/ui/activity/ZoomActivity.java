package top.soyask.calendarii.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.task.LoadDataTask;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.view.CalendarView;

import static android.content.res.Configuration.DENSITY_DPI_UNDEFINED;
import static top.soyask.calendarii.MainActivity.THEMES;

public class ZoomActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private List<Day> mDays = new ArrayList<>();
    private EventDao mEventDao;
    private Configuration mConfig;
    private TextView mTvDaySize;
    private TextView mTvLunarSize;
    private TextView mTvNumberSize;
    private TextView mTvWeekSize;
    private TextView mTvDpi;
    private TextView mTvHolidaySize;
    private CalendarView mCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        setupUI();
        updateCalendarSetting();
        LoadDataTask task = new LoadDataTask(this, mCalendarView);
        Calendar calendar = Calendar.getInstance();
        task.execute(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
    }

    private void setupTheme() {
        int theme = THEMES[Setting.theme];
        setTheme(theme);
    }

    protected void setupUI() {
        setupToolbar();
        findTextView();
        setupSeekBar();
        mCalendarView = findViewById(R.id.cv);
    }

    private void findTextView() {
        mTvDaySize = findViewById(R.id.tv_day_size);
        mTvLunarSize = findViewById(R.id.tv_lunar_size);
        mTvNumberSize = findViewById(R.id.tv_number_size);
        mTvWeekSize = findViewById(R.id.tv_week_size);
        mTvHolidaySize = findViewById(R.id.tv_holiday_size);
        mTvDpi = findViewById(R.id.tv_dpi);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(this);
    }

    private void setupSeekBar() {
        mConfig = getResources().getConfiguration();
        SeekBar seekBar = findViewById(R.id.sb_dpi);
        seekBar.setProgress(mConfig.densityDpi);
        seekBar.setOnSeekBarChangeListener(this);
        mTvDpi.setText(getString(R.string.current_dpi, mConfig.densityDpi));
        findViewById(R.id.btn_reset_dpi).setVisibility(Setting.density_dpi == -1 ? View.INVISIBLE : View.VISIBLE);

        SeekBar sbDaySize = findViewById(R.id.sb_day_size);
        int daySize = getDimension(R.dimen.item_day_size);
        sbDaySize.setMax(daySize * 4);
        sbDaySize.setProgress(Setting.day_size == -1 ? daySize : Setting.day_size);
        sbDaySize.setOnSeekBarChangeListener(this);
        mTvDaySize.setText(getString(R.string.current_day_size, sbDaySize.getProgress()));
        findViewById(R.id.btn_reset_day_size).setVisibility(Setting.day_size == -1 ? View.INVISIBLE : View.VISIBLE);

        SeekBar sbLunarSize = findViewById(R.id.sb_lunar_size);
        int bottomTextSize = getDimension(R.dimen.bottom_text_size);
        sbLunarSize.setMax(bottomTextSize * 4);
        sbLunarSize.setProgress(Setting.day_lunar_text_size == -1 ? bottomTextSize : (int) Setting.day_lunar_text_size);
        sbLunarSize.setOnSeekBarChangeListener(this);
        mTvLunarSize.setText(getString(R.string.current_lunar_size, sbLunarSize.getProgress()));
        findViewById(R.id.btn_reset_lunar).setVisibility(Setting.day_lunar_text_size == -1 ? View.INVISIBLE : View.VISIBLE);

        SeekBar sbNumberSize = findViewById(R.id.sb_number_size);
        int dateTextSize = getDimension(R.dimen.date_text_size);
        sbNumberSize.setMax(dateTextSize * 2);
        sbNumberSize.setProgress(Setting.day_number_text_size == -1 ? dateTextSize : (int) Setting.day_number_text_size);
        sbNumberSize.setOnSeekBarChangeListener(this);
        mTvNumberSize.setText(getString(R.string.current_number_size, sbNumberSize.getProgress()));
        findViewById(R.id.btn_reset_number).setVisibility(Setting.day_number_text_size == -1 ? View.INVISIBLE : View.VISIBLE);

        SeekBar sbWeekSize = findViewById(R.id.sb_week_size);
        int weekTextSize = getDimension(R.dimen.week_text_size);
        sbWeekSize.setMax(weekTextSize * 2);
        sbWeekSize.setProgress(Setting.day_week_text_size == -1 ? weekTextSize : (int) Setting.day_week_text_size);
        sbWeekSize.setOnSeekBarChangeListener(this);
        mTvWeekSize.setText(getString(R.string.current_week_size, sbWeekSize.getProgress()));
        findViewById(R.id.btn_reset_week).setVisibility(Setting.day_week_text_size == -1 ? View.INVISIBLE : View.VISIBLE);

        SeekBar sbHolidaySize = findViewById(R.id.sb_holiday_size);
        int holidayTextSize = getDimension(R.dimen.holiday_text_size);
        sbHolidaySize.setMax(holidayTextSize * 2);
        sbHolidaySize.setProgress(Setting.day_holiday_text_size == -1 ? holidayTextSize : (int) Setting.day_holiday_text_size);
        sbHolidaySize.setOnSeekBarChangeListener(this);
        mTvHolidaySize.setText(getString(R.string.current_holiday_size, sbHolidaySize.getProgress()));
        findViewById(R.id.btn_reset_holiday).setVisibility(Setting.day_holiday_text_size == -1 ? View.INVISIBLE : View.VISIBLE);

    }

    private int getDimension(int resId) {
        return getResources().getDimensionPixelOffset(resId);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()) {
                case R.id.sb_lunar_size:
                    Setting.day_lunar_text_size = progress;
                    mTvLunarSize.setText(getString(R.string.current_lunar_size, progress));
                    findViewById(R.id.btn_reset_lunar).setVisibility(View.VISIBLE);
                    break;
                case R.id.sb_number_size:
                    Setting.day_number_text_size = progress;
                    mTvNumberSize.setText(getString(R.string.current_number_size, progress));
                    findViewById(R.id.btn_reset_number).setVisibility(View.VISIBLE);
                    break;
                case R.id.sb_week_size:
                    Setting.day_week_text_size = progress;
                    mTvWeekSize.setText(getString(R.string.current_week_size, progress));
                    findViewById(R.id.btn_reset_week).setVisibility(View.VISIBLE);
                    break;
                case R.id.sb_day_size:
                    Setting.day_size = progress;
                    mTvDaySize.setText(getString(R.string.current_day_size, progress));
                    findViewById(R.id.btn_reset_day_size).setVisibility(View.VISIBLE);
                    break;
                case R.id.sb_holiday_size:
                    Setting.day_holiday_text_size = progress;
                    mTvHolidaySize.setText(getString(R.string.current_holiday_size, progress));
                    findViewById(R.id.btn_reset_holiday).setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getId() == R.id.sb_dpi) {
            int progress = seekBar.getProgress();
            if (progress < 200) {
                progress = 200;
            }
            setDpi(progress);
            recreate();
        } else {
            int progress = seekBar.getProgress();
            switch (seekBar.getId()) {
                case R.id.sb_lunar_size:
                    Setting.setting(this, Global.SETTING_DAY_LUNAR_TEXT_SIZE, progress);
                    break;
                case R.id.sb_number_size:
                    Setting.setting(this, Global.SETTING_DAY_NUMBER_TEXT_SIZE, progress);
                    break;
                case R.id.sb_week_size:
                    Setting.setting(this, Global.SETTING_DAY_WEEK_TEXT_SIZE, progress);
                    break;
                case R.id.sb_day_size:
                    Setting.setting(this, Global.SETTING_DAY_SIZE, progress);
                    break;
                case R.id.sb_holiday_size:
                    Setting.setting(this, Global.SETTING_DAY_HOLIDAY_TEXT_SIZE, progress);
                    break;
            }
            sendBroadcast(new Intent(MonthFragment.UPDATE_UI));
            updateCalendarSetting();
        }
    }

    private void setDpi(int progress) {
        Resources resources = getResources();
        Configuration newConfig = resources.getConfiguration();
        newConfig.setToDefaults();
        newConfig.densityDpi = progress;
        Setting.density_dpi = progress;
        resources.updateConfiguration(newConfig, resources.getDisplayMetrics());
        Setting.setting(this, Global.SETTING_DENSITY_DPI, progress);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        checkAndLaunch();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                checkAndLaunch();
        }
    }

    private void checkAndLaunch() {
        if (Setting.density_dpi != -1) {
            Intent intent = new Intent(this, LaunchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }

    public void reset(View view) {
        switch (view.getId()) {
            case R.id.btn_reset_day_size:
                Setting.day_size = -1;
                Setting.setting(this, Global.SETTING_DAY_SIZE, -1);
                break;
            case R.id.btn_reset_holiday:
                Setting.day_holiday_text_size = -1;
                Setting.setting(this, Global.SETTING_DAY_HOLIDAY_TEXT_SIZE, -1);
                break;
            case R.id.btn_reset_lunar:
                Setting.day_lunar_text_size = -1;
                Setting.setting(this, Global.SETTING_DAY_LUNAR_TEXT_SIZE, -1);
                break;
            case R.id.btn_reset_number:
                Setting.day_number_text_size = -1;
                Setting.setting(this, Global.SETTING_DAY_NUMBER_TEXT_SIZE, -1);
                break;
            case R.id.btn_reset_week:
                Setting.day_week_text_size = -1;
                Setting.setting(this, Global.SETTING_DAY_WEEK_TEXT_SIZE, -1);
                break;
            case R.id.btn_reset_dpi:
                resetDpi();
                recreate();
                break;
        }
        setupSeekBar();
        sendBroadcast(new Intent(MonthFragment.UPDATE_UI));
        updateCalendarSetting();
    }

    private void updateCalendarSetting() {
        mCalendarView.setFirstDayOffset(Setting.date_offset)
                .setDateCircleSize(Setting.day_size == -1 ? getDimension(R.dimen.item_day_size) : Setting.day_size)
                .setDateTextSize(Setting.day_number_text_size == -1 ? getDimension(R.dimen.date_text_size) : Setting.day_number_text_size)
                .setDateBottomTextSize(Setting.day_lunar_text_size == -1 ? getDimension(R.dimen.bottom_text_size) : Setting.day_lunar_text_size)
                .setHolidayTextSize(Setting.day_holiday_text_size == -1 ? getDimension(R.dimen.holiday_text_size) : Setting.day_holiday_text_size)
                .setWeekTextSize(Setting.day_week_text_size == -1 ? getDimension(R.dimen.week_text_size) : Setting.day_week_text_size)
                .setReplenish(Setting.replenish)
                .setUseAnimation(Setting.select_anim)
                .postInvalidate();
    }

    private void resetDpi() {
        Setting.remove(this, Global.SETTING_DENSITY_DPI);
        Setting.density_dpi = -1;
        Resources resources = getResources();
        Configuration newConfig = resources.getConfiguration();
        newConfig.densityDpi = DENSITY_DPI_UNDEFINED;
        resources.updateConfiguration(newConfig, resources.getDisplayMetrics());
    }

}
