package top.soyask.calendarii.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.domain.Event;
import top.soyask.calendarii.ui.adapter.MonthFragmentAdapter;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;
import top.soyask.calendarii.ui.fragment.setting.AboutFragment;
import top.soyask.calendarii.ui.fragment.setting.theme.ThemeFragment;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;


public class MainFragment extends BaseFragment implements ViewPager.OnPageChangeListener, View.OnClickListener, MonthFragment.OnDaySelectListener, AddEventFragment.OnAddListener, DateSelectDialog.DateSelectCallback {

    public static final String SKIP = "skip";

    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);
    private ViewPager mViewPager;
    private Day mSelectedDay;
    private ActionBar mActionBar;
    private TextView mTvTitle;
    private TextView mTvEvent;
    private View mEventView;
    private boolean isVisible = true;
    private int mEventViewWidth;
    private int mEventViewHeight;
    private MonthFragmentAdapter mMonthFragmentAdapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case View.INVISIBLE:
                    if (isVisible) {
                        isVisible = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Animator anim = ViewAnimationUtils.createCircularReveal(mEventView, mEventViewWidth / 2, mEventViewHeight / 2, mEventViewWidth, 0);
                            anim.setDuration(500);
                            anim.setInterpolator(new AccelerateDecelerateInterpolator());
                            anim.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mEventView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
                                }
                            });
                            anim.start();
                        } else {
                            mEventView.setVisibility(View.INVISIBLE);
                        }
                    }
                    break;
                case View.VISIBLE:
                    if (!isVisible) {
                        isVisible = true;
                        mEventView.setVisibility(View.VISIBLE);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Animator anim = ViewAnimationUtils.createCircularReveal(mEventView, mEventViewWidth / 2, mEventViewHeight / 2, 0, mEventViewWidth);
                            anim.setDuration(500);
                            anim.setInterpolator(new AccelerateDecelerateInterpolator());
                            anim.start();
                        }

                    }
                    break;
            }
        }
    };
    private View mIBtnMore;
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
        setupCard();
        setupToolbar();
        setupViewPager();
        findViewById(R.id.add_event).setOnClickListener(this);
        mSelectedDay = new Day(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1, mCalendar.get(DAY_OF_MONTH));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setEvent(mSelectedDay.getYear() + "年" + mSelectedDay.getMonth() + "月" + mSelectedDay.getDayOfMonth() + "日");
            }
        }, 1000);
    }

    private void setupCard() {
        mTvTitle = findViewById(R.id.tv_title);
        mTvEvent = findViewById(R.id.tv_event);
        mEventView = findViewById(R.id.cv_event);
        mIBtnMore = findViewById(R.id.ib_more);
    }

    private void setupViewPager() {
        int item = getCurrentMonth();
        mViewPager = findViewById(R.id.vp);
        mMonthFragmentAdapter = new MonthFragmentAdapter(getChildFragmentManager(), mCalendar, this);
        mViewPager.setAdapter(mMonthFragmentAdapter);
        mViewPager.setCurrentItem(item);
        mViewPager.addOnPageChangeListener(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        mActionBar = setToolbar(toolbar);
        setToolbarDate(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1);
    }

    private void setEvent(final String title) {
        EventDao eventDao = EventDao.getInstance(getMainActivity());
        List<Event> events = new ArrayList<>();
        try {
            events = eventDao.query(title);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mTvTitle.setText(title);
        if (mEventViewWidth == 0) {
            mEventViewWidth = mEventView.getWidth();
            mEventViewHeight = mEventView.getHeight();
        }
        if (!events.isEmpty()) {
            mTvEvent.setText(events.get(0).getDetail());
            if (events.size() > 1) {
                mIBtnMore.setVisibility(View.VISIBLE);
                mIBtnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AllEventFragment allEventFragment = AllEventFragment.newInstance(title);
                        addFragment(allEventFragment);
                    }
                });
            } else {
                findViewById(R.id.ib_more).setVisibility(View.GONE);
            }
            mHandler.sendEmptyMessage(View.VISIBLE);
        } else {
            mTvEvent.setText("这一天并没有添加任何的事件...");
            mHandler.sendEmptyMessage(View.INVISIBLE);
        }
    }

    private int getCurrentMonth() {
        return (mCalendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + mCalendar.get(MONTH);
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
        mItemToday = menu.getItem(0);
        toggleItemToday(mViewPager.getCurrentItem());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_today:
//                mViewPager.setCurrentItem(getCurrentMonth());
                skipToOneDay(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1, mCalendar.get(DAY_OF_MONTH));
                break;
            case R.id.menu_all_event:
                AllEventFragment allEventFragment = AllEventFragment.newInstance(null);
                addFragment(allEventFragment);
                break;
            case R.id.menu_select:
                showSelectDialog();
                break;
            case R.id.menu_theme:
                ThemeFragment themeFragment = ThemeFragment.newInstance();
                addFragment(themeFragment);
                break;
            case R.id.menu_score:
                score();
                break;
            case R.id.menu_about:
                AboutFragment aboutFragment = AboutFragment.newInstance();
                addFragment(aboutFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSelectDialog() {
        DateSelectDialog dateSelectDialog = DateSelectDialog.newInstance(mSelectedDay.getYear(), mSelectedDay.getMonth(), mSelectedDay.getDayOfMonth());
        dateSelectDialog.show(getChildFragmentManager(), "");
        dateSelectDialog.setDateSelectCallback(this);
    }

    private void score() {

        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "您的系统中没有安装应用市场", Toast.LENGTH_SHORT).show();
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
        AddEventFragment addEventFragment = AddEventFragment.newInstance(mSelectedDay, null);
        addEventFragment.setOnAddListener(this);
        addFragment(addEventFragment);
    }

    @Override
    public void onSelected(Day day) {
        this.mSelectedDay = day;
        setEvent(day.getYear() + "年" + day.getMonth() + "月" + day.getDayOfMonth() + "日");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupReceiver();
    }

    private void setupReceiver() {
        mMainReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(EventDao.ADD);
        filter.addAction(EventDao.UPDATE);
        filter.addAction(EventDao.DELETE);
        getMainActivity().registerReceiver(mMainReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getMainActivity().unregisterReceiver(mMainReceiver);
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

    private void skipToOneDay(int year, int month, int day) {
        int position = (year - MonthFragmentAdapter.YEAR_START) * 12 + month - 1;
        mViewPager.setCurrentItem(position);
        Intent intent = new Intent(SKIP);
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        getMainActivity().sendBroadcast(intent);
    }

    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setEvent(mSelectedDay.getYear() + "年" + mSelectedDay.getMonth() + "月" + mSelectedDay.getDayOfMonth() + "日");
        }
    }

    @Override
    public void onAdd() {
        showSnackbar("成功添加了新事件");
    }
}
