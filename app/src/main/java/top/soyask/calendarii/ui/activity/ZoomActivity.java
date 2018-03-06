package top.soyask.calendarii.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.adapter.month.MonthAdapter;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

import static android.content.res.Configuration.DENSITY_DPI_UNDEFINED;
import static top.soyask.calendarii.MainActivity.THEMES;

public class ZoomActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, MonthAdapter.OnItemClickListener, View.OnClickListener {
    private List<Day> mDays = new ArrayList<>();
    private EventDao mEventDao;
    private MonthAdapter mMonthAdapter;
    private Configuration mConfig;
    private boolean isChange;
    private MenuItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        isChange = Setting.density_dpi != -1;
        setupData();
        setupUI();
    }

    private void setupTheme() {
        int theme = THEMES[Setting.theme];
        setTheme(theme);
    }

    private synchronized void setupData() {
        mEventDao = EventDao.getInstance(this);
        Calendar calendar = Calendar.getInstance();
        int dayCount = DayUtils.getMonthDayCount(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        mDays.clear();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Day day = MonthUtils.generateDay(calendar, mEventDao);
            mDays.add(day);
        }
    }

    protected void setupUI() {
        setupToolbar();
        setupSeekBar();
        setupRecyclerView();
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(this);
    }

    private void setupSeekBar() {
        mConfig = getResources().getConfiguration();
        SeekBar seekBar = (SeekBar) findViewById(R.id.sb);
        seekBar.setProgress(mConfig.densityDpi);
        seekBar.setOnSeekBarChangeListener(this);
    }

    private void setupRecyclerView() {
        mMonthAdapter = new MonthAdapter(mDays, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7));
        recyclerView.setAdapter(mMonthAdapter);
        recyclerView.setItemAnimator(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                reset();
                recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reset() {
        Setting.remove(this, Global.SETTING_DENSITY_DPI);
        Setting.density_dpi = -1;
        Resources resources = getResources();
        Configuration newConfig = resources.getConfiguration();
        newConfig.densityDpi = DENSITY_DPI_UNDEFINED;
        resources.updateConfiguration(newConfig, resources.getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zoom, menu);
        mItem = menu.findItem(R.id.menu_reset);
        mItem.setVisible(isChange);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDayClick(int position, Day day) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress < 200) {
            progress = 200;
        }
        setDpi(progress);
        recreate();
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
        if(isChange){
            Intent intent = new Intent(this, LaunchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
    }
}
