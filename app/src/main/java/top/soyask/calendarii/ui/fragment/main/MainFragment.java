package top.soyask.calendarii.ui.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.domain.Event;
import top.soyask.calendarii.ui.adapter.month.MonthFragmentAdapter;
import top.soyask.calendarii.ui.fragment.about.AboutFragment;
import top.soyask.calendarii.ui.fragment.event.AddEventFragment;
import top.soyask.calendarii.ui.fragment.event.AllEventFragment;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;
import top.soyask.calendarii.ui.fragment.setting.SettingFragment;
import top.soyask.calendarii.ui.fragment.setting.theme.ThemeFragment;
import top.soyask.calendarii.utils.EraUtils;
import top.soyask.calendarii.utils.LunarUtils;

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
    private TextView mTvDayCount;
    private TextView mTvDayCountM;
    private TextView mTvLunar;
    private TextView mTvLunarYear;
    private View mEventView;
    private View mPoint;
    private ImageView mIvYear;

    private boolean isVisible = true;
    private int mEventViewWidth;
    private int mEventViewHeight;
    private MonthFragmentAdapter mMonthFragmentAdapter;

    private Handler mAnimatorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mEventViewAnimator != null) {
                mEventViewAnimator.cancel();
                mEventViewAnimator = null;
            }

            switch (msg.what) {
                case View.INVISIBLE:
                    if (isVisible) {
                        isVisible = false;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                            mEventViewAnimator = ViewAnimationUtils.createCircularReveal(mEventView, mEventViewWidth / 2, mEventViewHeight / 2, mEventViewWidth, 0);
                            mEventViewAnimator.setDuration(500);
                            mEventViewAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                            mEventViewAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mEventView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
                                }
                            });
                            mEventViewAnimator.start();
                        } else {
                            mEventView.setVisibility(View.INVISIBLE);
                        }

                        mLeftBottom.setVisibility(View.VISIBLE);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Animator animator = ViewAnimationUtils.createCircularReveal(mLeftBottom, 0, mLeftBottom.getHeight(), 0, mLeftBottom.getHeight());
                            animator.setDuration(500);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.start();
                        }

                        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.point);
                        animation.setDuration(800);
                        mPoint.startAnimation(animation);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                animation = AnimationUtils.loadAnimation(getContext(), R.anim.point_2);
                                animation.setDuration(300);
                                mPoint.startAnimation(animation);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

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

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Animator animator = ViewAnimationUtils.createCircularReveal(mLeftBottom, 0, mLeftBottom.getHeight(), mLeftBottom.getHeight(), 0);
                            animator.setDuration(500);
                            animator.setInterpolator(new AccelerateDecelerateInterpolator());
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mLeftBottom.setVisibility(!isVisible ? View.VISIBLE : View.INVISIBLE);
                                }
                            });
                            animator.start();
                        } else {
                            mLeftBottom.setVisibility(View.INVISIBLE);
                        }
                    }
                    break;
            }
        }
    };
    private View mIBtnMore;
    private MainReceiver mMainReceiver;
    private MenuItem mItemToday;
    private Animator mEventViewAnimator;
    private View mLeftBottom;
    private static final String INTENT_FULL_URL = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX01613AS644I1LR9US96%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" +
            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";

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

        mSelectedDay = new Day(mCalendar.get(YEAR), mCalendar.get(MONTH) + 1, mCalendar.get(DAY_OF_MONTH));
        mAnimatorHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setEvent(mSelectedDay.getYear() + "年" + mSelectedDay.getMonth() + "月" + mSelectedDay.getDayOfMonth() + "日");
            }
        }, 1000);
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
            mAnimatorHandler.sendEmptyMessage(View.VISIBLE);
        } else {
            mTvEvent.setText("这一天并没有添加任何的事件...");
            mAnimatorHandler.sendEmptyMessage(View.INVISIBLE);
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
            case R.id.menu_setting:
                SettingFragment settingFragment = SettingFragment.newInstance();
                addFragment(settingFragment);
                break;
            case R.id.menu_donate:
                donate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void donate() {
        new AlertDialog.Builder(getMainActivity())
                .setTitle("捐赠")
                .setMessage("首先感谢你点击了这个按钮。" +
                        "\n我也不是奢求很多这样，5毛一块的棒棒糖，" +
                        "就当请开发者吃个棒棒糖这样。" +
                        "如果，您觉得这个app 5毛也不值的话，那还是感谢使用。饭饭会努力去做更好的app，请也一定要喜欢哦。")
                .setNegativeButton("没兴趣", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getMainActivity(), "还是谢谢支持！我会更加努力的。", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("捐赠一点", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            toDonate();
                            Toast.makeText(getMainActivity(), "万分感谢！我会更加努力的。", Toast.LENGTH_SHORT).show();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }).show();
    }

    private void toDonate() throws URISyntaxException {
        Intent intent = Intent.parseUri(INTENT_FULL_URL, Intent.URI_INTENT_SCHEME);
        startActivity(intent);
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
        setLunarInfo();
        calculateDelta_T();
    }

    private void setLunarInfo() {
        Calendar selectDay = Calendar.getInstance();
        selectDay.set(YEAR, mSelectedDay.getYear());
        selectDay.set(MONTH, mSelectedDay.getMonth() - 1);
        selectDay.set(DAY_OF_MONTH, mSelectedDay.getDayOfMonth());
        String lunar = LunarUtils.getLunar(selectDay);
        String branches = EraUtils.getYearForEarthlyBranches(mSelectedDay.getYear());
        String stems = EraUtils.getYearForHeavenlyStems(mSelectedDay.getYear());
        int img = EraUtils.getYearForTwelveZodiacImage(mSelectedDay.getYear());

        mTvLunar.setText(lunar);
        mTvLunarYear.setText(stems + branches + "年");
        mIvYear.setImageDrawable(getResources().getDrawable(img));
    }

    // 计算所选的天数到今天的时间差
    private void calculateDelta_T() {
        Calendar selectDay = Calendar.getInstance();
        selectDay.set(YEAR, mSelectedDay.getYear());
        selectDay.set(MONTH, mSelectedDay.getMonth() - 1);
        selectDay.set(DAY_OF_MONTH, mSelectedDay.getDayOfMonth());

        Calendar today = Calendar.getInstance();
        today.set(YEAR, mCalendar.get(YEAR));
        today.set(MONTH, mCalendar.get(MONTH));
        today.set(DAY_OF_MONTH, mCalendar.get(DAY_OF_MONTH));

        long time = selectDay.getTime().getTime();
        long todayTime = today.getTime().getTime();
        int dayCount = (int) ((time - todayTime) / (1000 * 60 * 60 * 24));

        if (dayCount > 0) {
            mTvDayCount.setText("距今还有" + String.format("%04d", dayCount) + "天");
            mTvDayCountM.setText(dayCount + "天之后");
        } else if (dayCount < 0) {
            mTvDayCount.setText("距今已过" + String.format("%04d", -dayCount) + "天");
            mTvDayCountM.setText(-dayCount + "天之前");
        } else {
            mTvDayCount.setText("今日事,今日毕");
        }
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
