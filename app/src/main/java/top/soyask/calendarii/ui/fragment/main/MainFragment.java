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

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Birthday;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.domain.Event;
import top.soyask.calendarii.domain.LunarDay;
import top.soyask.calendarii.global.GlobalData;
import top.soyask.calendarii.ui.adapter.month.MonthFragmentAdapter;
import top.soyask.calendarii.ui.fragment.about.AboutFragment;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;
import top.soyask.calendarii.ui.fragment.event.AddEventFragment;
import top.soyask.calendarii.ui.fragment.event.AllEventFragment;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.fragment.setting.SettingFragment;
import top.soyask.calendarii.utils.EraUtils;
import top.soyask.calendarii.utils.MonthUtils;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;


public class MainFragment extends BaseFragment implements ViewPager.OnPageChangeListener, View.OnClickListener, MonthFragment.OnDaySelectListener, AddEventFragment.OnAddListener, DateSelectDialog.DateSelectCallback {

    public static final String SKIP = "skip";
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

    private boolean isEventViewVisible = true;
    private boolean isBirthday = false;
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
                    if (isEventViewVisible) {
                        isEventViewVisible = false;
                        hideEventView();
                        showLeftBottom();
                        movePoint();
                    }
                    break;
                case View.VISIBLE:
                    if (!isEventViewVisible) {
                        isEventViewVisible = true;
                        showEventView();
                        hideLeftBottom();
                        sendEmptyMessage(BIRTHDAY_INVISIBLE);
                    }
                    break;
                case BIRTHDAY_INVISIBLE:
                    if (isBirthday) {
                        hideFlBirth();
                    }
                    break;
                case BIRTHDAY_VISIBLE:
                    if (!isBirthday && !isEventViewVisible) {
                        if(!mSelectedDay.hasEvent()){
                            showFlBirth();
                        }
                    }
                    break;
            }
        }

        private void hideLeftBottom() {
            hideWithCircularReveal(mLeftBottom, 0, mLeftBottom.getHeight(), mLeftBottom.getHeight(), 0, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLeftBottom.setVisibility(!isEventViewVisible ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }

        private void hideEventView() {
            hideWithCircularReveal(mEventView, mEventViewWidth / 2, mEventViewHeight / 2, mEventViewWidth, 0, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEventView.setVisibility(isEventViewVisible ? View.VISIBLE : View.INVISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mEventView.setElevation(0);
                    }
                }
            });
        }

        private void hideFlBirth() {
            isBirthday = false;
            int width = mFlBirth.getWidth();
            int height = mFlBirth.getHeight();
            hideWithCircularReveal(mFlBirth, width / 2, height / 2, width, 0, new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator anim) {
                    mFlBirth.setVisibility(isBirthday ? View.VISIBLE : View.INVISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.point_2);
                    animation.setDuration(300);
                    mPoint.startAnimation(animation);
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
            mEventView.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.in_from_top);
            animation.setDuration(800);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {  }
                @Override
                public void onAnimationRepeat(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        float dimension = getResources().getDimension(R.dimen.card_normal_elevation);
                        mEventView.setElevation(dimension);
                    }
                }

            });
            mEventView.startAnimation(animation);
        }

        private void showLeftBottom() {
            showWithCircularReveal(mLeftBottom, 0, mLeftBottom.getHeight(), 0, mLeftBottom.getHeight(), new AnimatorListenerAdapter() {
            });
        }

        private void showFlBirth() {
            isBirthday = true;
            int width = mFlBirth.getWidth();
            int height = mFlBirth.getHeight();
            showWithCircularReveal(mFlBirth, width / 2, height / 2, 0, width, new AnimatorListenerAdapter() {
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
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.point);
            animation.setDuration(800);
            mPoint.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mSelectedDay.hasBirthday()) {
                        mAnimatorHandler.sendEmptyMessage(BIRTHDAY_VISIBLE);
                    } else {
                        animation = AnimationUtils.loadAnimation(getContext(), R.anim.point_2);
                        animation.setDuration(300);
                        mPoint.startAnimation(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
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
    private TextView mIvBirth;
    private View mFlBirth;

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
        mSelectedDay = MonthUtils.generateDay(mCalendar, EventDao.getInstance(getMainActivity()));
        mAnimatorHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onSelected(mSelectedDay);
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
        mMonthFragmentAdapter = new MonthFragmentAdapter(getChildFragmentManager(), this);
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
        List<Event>  events = eventDao.query(title);
        mTvTitle.setText(title);
        if (mEventViewWidth == 0) {
            mEventViewWidth = mEventView.getWidth();
            mEventViewHeight = mEventView.getHeight();
        }
        if (!events.isEmpty()) {
            setupEventView(title, events);
        } else {
            mTvEvent.setText("这一天并没有添加任何的事件...");
            mAnimatorHandler.sendEmptyMessage(View.INVISIBLE);
        }
    }

    private void setupEventView(final String title, List<Event> events) {
        if(mSelectedDay.hasBirthday()){
            StringBuffer buffer = getBirthdayStr();
            mTvEvent.setText(buffer.toString());
        }else {
            mTvEvent.setText(events.get(0).getDetail());
        }
        mIBtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllEventFragment allEventFragment = AllEventFragment.newInstance(title);
                addFragment(allEventFragment);
            }
        });
        mAnimatorHandler.sendEmptyMessage(View.VISIBLE);
    }

    @NonNull
    private StringBuffer getBirthdayStr() {
        List<Birthday> birthdays = mSelectedDay.getBirthdays();
        StringBuffer sb = new StringBuffer();
        for (Birthday birthday : birthdays) {
            sb.append(birthday.getWho()).append(',');
        }
        sb.deleteCharAt(sb.lastIndexOf(",")).append("的生日");
        return sb;
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
                        "\n我也不是奢求很多这样，5毛一块的就当请开发者吃个棒棒糖这样。" +
                        "如果，您觉得这个app 5毛也不值的话...emmm..那还是感谢使用。" +
                        "\n饭饭会努力去做更好的app，请也一定要喜欢哦。")
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
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

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
        setBirthday(day);
        setEvent(day.getYear() + "年" + day.getMonth() + "月" + day.getDayOfMonth() + "日");
        setLunarInfo();
        calculateDelta_T();
    }

    private void setBirthday(Day day) {
        if (day.hasBirthday()) {
            StringBuffer buffer = getBirthdayStr();
            mIvBirth.setText(buffer.toString());
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
        mTvLunarYear.setText(era + "年");
        mIvYear.setImageDrawable(getResources().getDrawable(img));
    }

    // 计算所选的天数到今天的时间差
    private void calculateDelta_T() {
        Calendar selectDay = getSelectedCalendar();
        Calendar today = getTodayCalendar();

        long time = selectDay.getTime().getTime();
        long todayTime = today.getTime().getTime();
        int dayCount = (int) ((time - todayTime) / (1000 * 60 * 60 * 24));

        if (dayCount > 0) {
            mTvDayCount.setText("距今还有" + String.format("%4d", dayCount) + "天");
            mTvDayCountM.setText(dayCount + "天之后");
        } else if (dayCount < 0) {
            mTvDayCount.setText("距今已过" + String.format("%4d", -dayCount) + "天");
            mTvDayCountM.setText(-dayCount + "天之前");
        } else {
            mTvDayCount.setText("今日事,今日毕");
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
        GlobalData.loadBirthday(getMainActivity());
        GlobalData.loadHoliday(getMainActivity());
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
            onSelected(mSelectedDay);
        }
    }

    @Override
    public void onAdd() {
        showSnackbar("成功添加了新事件");
    }
}
