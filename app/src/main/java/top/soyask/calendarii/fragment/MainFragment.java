package top.soyask.calendarii.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.adapter.MonthFragmentAdapter;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.fragment.base.BaseFragment;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;


public class MainFragment extends BaseFragment implements ViewPager.OnPageChangeListener, View.OnClickListener, MonthFragment.OnDaySelectListener, AddEventFragment.OnAddListener {

    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);
    private ViewPager mViewPager;
    private Day mSelectedDay;
    private ActionBar mActionBar;

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
        setupToolbar();
        setupViewPager();
        findViewById(R.id.add_event).setOnClickListener(this);
        mSelectedDay = new Day(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1, mCalendar.get(DAY_OF_MONTH));
    }

    private void setupViewPager() {
        int item = getCurrentMonth();
        mViewPager = findViewById(R.id.vp);
        mViewPager.setAdapter(new MonthFragmentAdapter(getChildFragmentManager(), mCalendar, this));
        mViewPager.setCurrentItem(item);
        mViewPager.addOnPageChangeListener(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mActionBar = setToolbar(toolbar);
        setToolbarDate(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1);
    }


    private int getCurrentMonth() {
        return (mCalendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + mCalendar.get(MONTH) + 1;
    }

    private void setToolbarDate(int year, int month) {
        StringBuffer date = new StringBuffer()
                .append(year).append("年")
                .append(month < 10 ? "0" + month : month).append("月");
        mActionBar.setTitle(date.toString());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_today) {
            mViewPager.setCurrentItem(getCurrentMonth());
        } else if (item.getItemId() == R.id.menu_all_event) {
            AllEventFragment allEventFragment = AllEventFragment.newInstance();
            addFragment(allEventFragment);
        } else if (item.getItemId() == R.id.menu_about) {
            AboutFragment aboutFragment = AboutFragment.newInstance();
            addFragment(aboutFragment);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        int year = position / 12 + MonthFragmentAdapter.YEAR_START;
        int month = (position - 1) % 12 + 1;
        setToolbarDate(year, month);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        AddEventFragment addEventFragment = AddEventFragment.newInstance(mSelectedDay, null);
        addEventFragment.setOnAddListener(this);
        addFragment(addEventFragment);
    }

    @Override
    public void onSelected(Day day) {
        this.mSelectedDay = day;
    }

    @Override
    public void onAdd() {
        showSnackbar("成功添加了新事件");
    }
}
