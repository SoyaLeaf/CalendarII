package top.soyask.calendarii.ui.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.entity.Birthday;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.entity.Event;
import top.soyask.calendarii.entity.LunarDay;
import top.soyask.calendarii.ui.adapter.month.MonthFragmentAdapter;
import top.soyask.calendarii.ui.fragment.about.AboutFragment;
import top.soyask.calendarii.ui.fragment.backup.BackupFragment;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;
import top.soyask.calendarii.ui.fragment.event.AllEventFragment;
import top.soyask.calendarii.ui.fragment.event.EditEventFragment;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.fragment.setting.SettingFragment;
import top.soyask.calendarii.utils.EraUtils;
import top.soyask.calendarii.utils.MonthUtils;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;


public class MainFragment extends BaseFragment implements ViewPager.OnPageChangeListener, View.OnClickListener, MonthFragment.OnDaySelectListener, EditEventFragment.OnAddListener, DateSelectDialog.DateSelectCallback {

    private static final int BIRTHDAY_INVISIBLE = 0x233;
    private static final int BIRTHDAY_VISIBLE = 0x234;

    private Calendar mCalendar = Calendar.getInstance(Locale.CHINA);
    private ViewPager mViewPager;
    private Day mSelectedDay;
    private ActionBar mActionBar;
    private TextView mTvTitle;
    private TextView mTvEvent;
    private TextView mTvDayCount;
    private TextView mTvDayCountM;
    private TextView mTvLunar;
    private TextView mTvLunarYear;
    private View mEventView;
    private View mPoint;
    private ImageView mIvYear;
    private View mIBtnMore;
    private MainReceiver mMainReceiver;
    private MenuItem mItemToday;
    private Animator mEventViewAnimator;
    private View mLeftBottom;
    private TextView mIvBirth;
    private View mFlBirth;

    private boolean isEventViewVisible = true;
    private boolean isBirthday = false;
    private int mEventViewWidth;
    private int mEventViewHeight;

    private Handler mAnimatorHandler = new MainHandler(this);

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
        setupOtherView();
        initSelectDay();
    }

    private void initSelectDay() {
        mSelectedDay = MonthUtils.generateDay(mCalendar, EventDao.getInstance(mHostActivity));
        mAnimatorHandler.post(this::skipToday);
    }

    private void setupOtherView() {
        findViewById(R.id.add_event).setOnClickListener(this);
        mPoint = findViewById(R.id.point);
        mTvDayCount = findViewById(R.id.tv_day_count);
        mTvDayCountM = findViewById(R.id.tv_day_count_m);
        mLeftBottom = findViewById(R.id.rl_leftbottom);
        mTvLunar = findViewById(R.id.tv_lunar);
        mTvLunarYear = findViewById(R.id.tv_lunar_year);
        mIvYear = findViewById(R.id.iv_year);
        mIvBirth = findViewById(R.id.tv_birth);
        mFlBirth = findViewById(R.id.fl_birth);
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

    private void setEvent(final String title) {
        EventDao eventDao = EventDao.getInstance(mHostActivity);
        List<Event> events = eventDao.query(title);
        mTvTitle.setText(title);
        if (mEventViewWidth == 0) {
            mEventViewWidth = mEventView.getWidth();
            mEventViewHeight = mEventView.getHeight();
        }
        if (!events.isEmpty()) {
            setupEventView(title, events);
        } else {
            mTvEvent.setText(R.string.nothing);
            mAnimatorHandler.sendEmptyMessage(View.INVISIBLE);
        }
    }

    private void setupEventView(final String title, List<Event> events) {
        if (mSelectedDay.hasBirthday()) {
            String birthdayStr = getBirthdayStr();
            mTvEvent.setText(birthdayStr);
        } else {
            mTvEvent.setText(events.get(0).getDetail());
        }
        mIBtnMore.setOnClickListener(v -> {
            AllEventFragment allEventFragment = AllEventFragment.newInstance(title);
            addFragment(allEventFragment);
        });
        mAnimatorHandler.sendEmptyMessage(View.VISIBLE);
    }

    @NonNull
    private String getBirthdayStr() {
        List<Birthday> birthdays = mSelectedDay.getBirthdays();
        StringBuilder sb = new StringBuilder();
        for (Birthday birthday : birthdays) {
            sb.append(birthday.getWho()).append(',');
        }
        sb.deleteCharAt(sb.lastIndexOf(",")).append("的生日");
        return sb.toString();
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
                AllEventFragment allEventFragment = AllEventFragment.newInstance(null);
                addFragment(allEventFragment);
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
        EditEventFragment editEventFragment = EditEventFragment.newInstance(mSelectedDay, null);
        editEventFragment.setOnAddListener(this);
        addFragment(editEventFragment);
    }

    @Override
    public synchronized void onSelected(Day day) {
        this.mSelectedDay = day;
        setBirthday(day);
        setEvent(getString(R.string.xx_year_xx_month_xx, day.getYear(), day.getMonth(), day.getDayOfMonth()));
        setLunarInfo();
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

    private void setBirthday(Day day) {
        if (day.hasBirthday()) {
            String birthdayStr = getBirthdayStr();
            mIvBirth.setText(birthdayStr);
            mAnimatorHandler.sendEmptyMessage(BIRTHDAY_VISIBLE);
        } else {
            mAnimatorHandler.sendEmptyMessage(BIRTHDAY_INVISIBLE);
        }
    }

    private void setLunarInfo() {
        LunarDay lunar = mSelectedDay.getLunar();
        String era = lunar.getEra();
        int img = EraUtils.getYearForTwelveZodiacImage(lunar.getYear());
        mTvLunar.setText(lunar.getLunarDate());
        mTvLunarYear.setText(getString(R.string.xx_year, era));
        mIvYear.setImageDrawable(getResources().getDrawable(img));
    }

    // 计算所选的天数到今天的时间差
    private void calculateDelta_T() {
        Calendar selectDay = getSelectedCalendar();
        Calendar today = getTodayCalendar();

        long todayTime = today.getTime().getTime() / 86400000;
        long time = selectDay.getTime().getTime() / 86400000;
        Long l = time - todayTime;
        int dayCount = l.intValue();
        if (dayCount > 0) {
            mTvDayCount.setText(getString(R.string.till_xx_days_ago, dayCount));
            mTvDayCountM.setText(getString(R.string.xx_later, dayCount));
        } else if (dayCount < 0) {
            mTvDayCount.setText(getString(R.string.it_has_been_xx_days, -dayCount));
            mTvDayCountM.setText(getString(R.string.xx_before, -dayCount));
        } else {
            mTvDayCount.setText(R.string.today_things);
            mTvDayCountM.setText(null);
        }
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

    private static class MainHandler extends Handler {
        private final WeakReference<MainFragment> mFragment;

        private MainHandler(MainFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            if (fragment.mEventViewAnimator != null) {
                fragment.mEventViewAnimator.cancel();
                fragment.mEventViewAnimator = null;
            }
            switch (msg.what) {
                case View.INVISIBLE:
                    if (fragment.isEventViewVisible) {
                        fragment.isEventViewVisible = false;
                        hideEventView();
                        showLeftBottom();
                        movePoint();
                    }
                    break;
                case View.VISIBLE:
                    if (!fragment.isEventViewVisible) {
                        fragment.isEventViewVisible = true;
                        showEventView();
                        hideLeftBottom();
                        sendEmptyMessage(BIRTHDAY_INVISIBLE);
                    }
                    break;
                case BIRTHDAY_INVISIBLE:
                    if (fragment.isBirthday) {
                        hideFlBirth();
                    }
                    break;
                case BIRTHDAY_VISIBLE:
                    if (!fragment.isBirthday && !fragment.isEventViewVisible) {
                        if (!fragment.mSelectedDay.hasEvent()) {
                            showFlBirth();
                        }
                    }
                    break;
            }
        }

        private void hideLeftBottom() {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            View leftBottom = fragment.mLeftBottom;
            hideWithCircularReveal(leftBottom, 0, leftBottom.getHeight(), leftBottom.getHeight(), 0, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    leftBottom.setVisibility(!fragment.isEventViewVisible ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }

        private void hideEventView() {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            View eventView = fragment.mEventView;
            hideWithCircularReveal(eventView, fragment.mEventViewWidth / 2, fragment.mEventViewHeight / 2, fragment.mEventViewWidth, 0, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    eventView.setVisibility(fragment.isEventViewVisible ? View.VISIBLE : View.INVISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        eventView.setElevation(0);
                    }
                }
            });
        }

        private void hideFlBirth() {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            fragment.isBirthday = false;
            int width = fragment.mFlBirth.getWidth();
            int height = fragment.mFlBirth.getHeight();
            hideWithCircularReveal(fragment.mFlBirth, width / 2, height / 2, width, 0, new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator anim) {
                    fragment.mFlBirth.setVisibility(fragment.isBirthday ? View.VISIBLE : View.INVISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(fragment.getContext(), R.anim.point_2);
                    animation.setDuration(300);
                    fragment.mPoint.startAnimation(animation);
                }
            });
        }

        private void hideWithCircularReveal(View view, int centerX, int centerY, float startRadius, float endRadius, Animator.AnimatorListener listener) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Animator animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius);
                animator.setDuration(400);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addListener(listener);
                animator.start();
            } else {
                view.setVisibility(View.INVISIBLE);
            }
        }

        private void showEventView() {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            fragment.mEventView.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(fragment.getContext(), R.anim.in_from_top);
            animation.setDuration(800);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        float dimension = fragment.getResources().getDimension(R.dimen.card_normal_elevation);
                        fragment.mEventView.setElevation(dimension);
                    }
                }

            });
            fragment.mEventView.startAnimation(animation);
        }

        private void showLeftBottom() {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            showWithCircularReveal(fragment.mLeftBottom, 0, fragment.mLeftBottom.getHeight(), 0, fragment.mLeftBottom.getHeight(), new AnimatorListenerAdapter() {
            });
        }

        private void showFlBirth() {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            fragment.isBirthday = true;
            int width = fragment.mFlBirth.getWidth();
            int height = fragment.mFlBirth.getHeight();
            showWithCircularReveal(fragment.mFlBirth, width / 2, height / 2, 0, width, new AnimatorListenerAdapter() {
            });
        }

        private void showWithCircularReveal(View view, int centerX, int centerY, float startRadius, float endRadius, Animator.AnimatorListener listener) {
            view.setVisibility(View.VISIBLE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Animator animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius);
                animator.setDuration(400);
                animator.addListener(listener);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.start();
            }
        }


        private void movePoint() {
            MainFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            Animation animation = AnimationUtils.loadAnimation(fragment.getContext(), R.anim.point);
            animation.setDuration(800);
            fragment.mPoint.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (fragment.mSelectedDay.hasBirthday()) {
                        fragment.mAnimatorHandler.sendEmptyMessage(BIRTHDAY_VISIBLE);
                    } else {
                        animation = AnimationUtils.loadAnimation(fragment.getContext(), R.anim.point_2);
                        animation.setDuration(300);
                        fragment.mPoint.startAnimation(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

}
