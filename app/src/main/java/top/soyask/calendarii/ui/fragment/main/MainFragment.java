package top.soyask.calendarii.ui.fragment.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.ui.adapter.month.MonthFragmentAdapter;
import top.soyask.calendarii.ui.fragment.about.AboutFragment;
import top.soyask.calendarii.ui.fragment.backup.BackupFragment;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;
import top.soyask.calendarii.ui.fragment.event.AllThingsFragment;
import top.soyask.calendarii.ui.fragment.event.EditThingFragment;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.fragment.setting.SettingFragment;
import top.soyask.calendarii.utils.MonthUtils;

import java.util.Calendar;

import static java.util.Calendar.*;
import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;


public class MainFragment extends BaseFragment implements ViewPager.OnPageChangeListener, View.OnClickListener, MonthFragment.OnDaySelectListener, EditThingFragment.OnAddListener, DateSelectDialog.DateSelectCallback {


    private Calendar mCalendar = Calendar.getInstance();
    private ViewPager mViewPager;
    private Day mSelectedDay;
    private ActionBar mActionBar;
    private MainReceiver mMainReceiver;
    private MenuItem mItemToday;

    public MainFragment() {
        super(R.layout.fragment_main);
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        initSelectDay();
        setupViewPager();
        setupToolbar();
    }

    private void initSelectDay() {
        mSelectedDay = MonthUtils.generateDay(mCalendar, ThingDao.getInstance(mHostActivity));
    }

    private void setupViewPager() {
        int item = getCurrentMonth();
        mViewPager = findViewById(R.id.vp);
        MonthFragmentAdapter monthFragmentAdapter = new MonthFragmentAdapter(getChildFragmentManager(), this);
        mViewPager.setAdapter(monthFragmentAdapter);
        mViewPager.setCurrentItem(item);
        mViewPager.addOnPageChangeListener(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mActionBar = setToolbar(toolbar);
        setToolbarDate(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1);
    }

    private int getCurrentMonth() {
        return (mCalendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + mCalendar.get(MONTH);
    }

    private void setToolbarDate(int year, int month) {
        String title = getString(R.string.xx_year_xx_month, year, month);
        mActionBar.setTitle(title);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        mItemToday = menu.getItem(0);
        toggleItemToday(mViewPager.getCurrentItem());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_today:
                skipToday();
                break;
            case R.id.menu_all_event:
                AllThingsFragment allThingsFragment = AllThingsFragment.newInstance(null);
                addFragment(allThingsFragment);
                break;
            case R.id.menu_select:
                showSelectDialog();
                break;
            case R.id.menu_score:
                score();
                break;
            case R.id.menu_about:
                AboutFragment aboutFragment = AboutFragment.newInstance();
                addFragment(aboutFragment);
                break;
            case R.id.menu_setting:
                SettingFragment settingFragment = SettingFragment.newInstance();
                addFragment(settingFragment);
                break;
            case R.id.menu_backup:
                BackupFragment backupFragment = BackupFragment.newInstance();
                addFragment(backupFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void skipToday() {
        skipToOneDay(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1, mCalendar.get(DAY_OF_MONTH));
    }


    private void showSelectDialog() {
        DateSelectDialog dateSelectDialog = DateSelectDialog.newInstance(mSelectedDay.getYear(), mSelectedDay.getMonth(), mSelectedDay.getDayOfMonth());
        dateSelectDialog.show(getChildFragmentManager(), "");
        dateSelectDialog.setDateSelectCallback(this);
    }

    private void score() {
        Uri uri = Uri.parse("market://details?id=" + mHostActivity.getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(mHostActivity.getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else {
            Toast.makeText(mHostActivity, R.string.no_market, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        int year = position / MONTH_COUNT + YEAR_START_REAL;
        int month = position % MONTH_COUNT + 1;
        setToolbarDate(year, month);
        toggleItemToday(position);
    }

    private void toggleItemToday(int position) {
        if (getCurrentMonth() == position) {
            if (mItemToday != null && mItemToday.isVisible()) {
                mItemToday.setVisible(false);
            }
        } else {
            if (mItemToday != null && !mItemToday.isVisible()) {
                mItemToday.setVisible(true);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        EditThingFragment editThingFragment = EditThingFragment.newInstance(mSelectedDay, null);
        editThingFragment.setOnAddListener(this);
        addFragment(editThingFragment);
    }

    @Override
    public synchronized void onSelected(Day day) {
        this.mSelectedDay = day;
        calculateDelta_T();
    }

    @Override
    public void skipToNextMonth(int dayOfMonth) {
        skipToOneDay(1, dayOfMonth);
    }

    @Override
    public void skipToPrevMonth(int dayOfMonth) {
        skipToOneDay(-1, dayOfMonth);
    }

    // 计算所选的天数到今天的时间差
    private void calculateDelta_T() {
        Calendar selectDay = getSelectedCalendar();
        Calendar today = getTodayCalendar();

        long todayTime = today.getTime().getTime() / 86400000;
        long time = selectDay.getTime().getTime() / 86400000;
        Long l = time - todayTime;
        int dayCount = l.intValue();
//        if (dayCount > 0) {
//            mTvDayCount.setText(getString(R.string.till_xx_days_ago, dayCount));
//            mTvDayCountM.setText(getString(R.string.xx_later, dayCount));
//        } else if (dayCount < 0) {
//            mTvDayCount.setText(getString(R.string.it_has_been_xx_days, -dayCount));
//            mTvDayCountM.setText(getString(R.string.xx_before, -dayCount));
//        } else {
//            mTvDayCount.setText(R.string.today_things);
//            mTvDayCountM.setText(null);
//        }
    }

    @NonNull
    private Calendar getSelectedCalendar() {
        return getCalendarBy(mSelectedDay.getYear(), mSelectedDay.getMonth() - 1, mSelectedDay.getDayOfMonth());
    }

    @NonNull
    private Calendar getTodayCalendar() {
        return getCalendarBy(mCalendar.get(YEAR), mCalendar.get(MONTH), mCalendar.get(DAY_OF_MONTH));
    }

    @NonNull
    private Calendar getCalendarBy(int year, int month, int dayOfMonth) {
        Calendar today = Calendar.getInstance();
        today.set(YEAR, year);
        today.set(MONTH, month);
        today.set(DAY_OF_MONTH, dayOfMonth);
        return today;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupReceiver();
    }


    private void setupReceiver() {
        mMainReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MonthFragment.ADD_EVENT);
        filter.addAction(MonthFragment.UPDATE_EVENT);
        filter.addAction(MonthFragment.DELETE_EVENT);
        mHostActivity.registerReceiver(mMainReceiver, filter);
    }

    @Override
    public void onDestroy() {
        mHostActivity.unregisterReceiver(mMainReceiver);
        super.onDestroy();
    }

    @Override
    public void onSelectCancel() {
    }

    @Override
    public void onDismiss() {
    }

    @Override
    public void onValueChange(int year, int month, int day) {
    }

    @Override
    public void onSelectConfirm(int year, int month, int day) {
        skipToOneDay(year, month, day);
    }

    private void skipToOneDay(int offset, int dayOfMonth) {
        int position = mViewPager.getCurrentItem() + offset;
        int year = position / MONTH_COUNT + YEAR_START_REAL;
        int month = position % MONTH_COUNT + 1;
        mViewPager.setCurrentItem(position);
        Intent intent = new Intent(MonthFragment.SKIP);
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", dayOfMonth);
        mHostActivity.sendBroadcast(intent);
    }

    private void skipToOneDay(int year, int month, int day) {
        int position = (year - MonthFragmentAdapter.YEAR_START) * 12 + month - 1;
        skipToOneDay(position - mViewPager.getCurrentItem(), day);
    }

    @Override
    public void onAdd() {
        showSnackbar("成功添加了新事件");
    }

    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onSelected(mSelectedDay);
        }
    }

}
