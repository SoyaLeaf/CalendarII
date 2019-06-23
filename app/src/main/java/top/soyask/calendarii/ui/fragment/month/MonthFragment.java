package top.soyask.calendarii.ui.fragment.month;

import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;
import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.task.LoadDataTask;
import top.soyask.calendarii.task.PendingAction;
import top.soyask.calendarii.ui.eventbus.Messages;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.view.CalendarView;
import top.soyask.calendarii.utils.EventBusDefault;

import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;

public class MonthFragment extends BaseFragment {

    private static final String TAG = "MonthFragment";

    private static final String POSITION = "position";

    private int mYear;
    private int mMonth;
    private OnDaySelectListener mOnDaySelectListener;
    private PendingAction mPendingAction;
    private CalendarView mCalendarView;
    private LoadDataTask mLoadDataTask;

    public MonthFragment() {
        super(R.layout.fragment_month);
    }

    public static MonthFragment newInstance(int position) {
        MonthFragment fragment = new MonthFragment();
        Bundle args = new Bundle();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPendingAction = new PendingAction();
        if (getArguments() != null) {
            initArguments();
        }
        EventBusDefault.register(this);
    }

    private void initArguments() {
        int position = getArguments().getInt(POSITION);
        mYear = position / MONTH_COUNT + YEAR_START_REAL;
        mMonth = position % MONTH_COUNT + 1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLoadDataTask != null) {
            mLoadDataTask.cancel(true);
            mLoadDataTask = null;
        }
        mOnDaySelectListener = null;
        System.gc();
        EventBusDefault.unregister(this);
    }

    protected void setupUI() {
        mCalendarView = findViewById(R.id.cv);
        mCalendarView
                .setFirstDayOffset(Setting.date_offset)
                .setDateCircleSize(Setting.day_size)
                .setDateTextSize(Setting.day_number_text_size)
                .setDateBottomTextSize(Setting.day_lunar_text_size)
                .setHolidayTextSize(Setting.day_holiday_text_size)
                .setWeekTextSize(Setting.day_week_text_size)
                .setReplenish(Setting.replenish)
                .setUseAnimation(Setting.select_anim)
                .postInvalidate();
        mCalendarView.setOnDaySelectedListener(new CalendarView.OnDayClickListener() {
            @Override
            public void onDaySelected(int position, CalendarView.IDay day) {
                if (mOnDaySelectListener != null) {
                    if (day instanceof Day) {
                        Day d = (Day) day;
                        mOnDaySelectListener.onSelected(d);
                        EventBusDefault.post(Messages
                                .createSelectedMessage(d.getYear(), d.getMonth(),d.getDayOfMonth()));
                    }
                }
            }

            @Override
            public void onNextMonthClick(int dayOfMonth) {
                mOnDaySelectListener.skipToNextMonth(dayOfMonth);
            }

            @Override
            public void onPrevMonthClick(int dayOfMonth) {
                mOnDaySelectListener.skipToPrevMonth(dayOfMonth);
            }
        });
    }

    private int getDimension(@DimenRes int id) {
        return getResources().getDimensionPixelOffset(id);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoadDataTask = new LoadDataTask(mHostActivity, mCalendarView, mPendingAction);
        mLoadDataTask.execute(mYear, mMonth);
    }

    public void setOnDaySelectListener(OnDaySelectListener onDaySelectListener) {
        this.mOnDaySelectListener = onDaySelectListener;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Messages.SkipMessage message) {
        boolean isCurrent = (message.year == mYear && message.month == mMonth);
        if (isCurrent) {
            mCalendarView.selectCurrentMonth(message.day);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Messages.SelectedMessage message) {
        boolean isCurrent = message.year == mYear && message.month == mMonth;
        if (!isCurrent) {
            mCalendarView.select(-1);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Messages.WeekSettingMessage message) {
        mCalendarView.setFirstDayOffset(Setting.date_offset)
                .postInvalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Messages.UpdateDataMessage message) {
        LoadDataTask task = new LoadDataTask(mHostActivity, mCalendarView, mPendingAction);
        task.execute(mYear, mMonth);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Messages.UpdateUIMessage message) {
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

    public interface OnDaySelectListener {
        void onSelected(Day day);

        void skipToPrevMonth(int dayOfMonth);

        void skipToNextMonth(int dayOfMonth);
    }

}
