package top.soyask.calendarii.ui.fragment.month;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import top.soyask.calendarii.R;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.task.LoadDataTask;
import top.soyask.calendarii.task.PendingAction;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.view.CalendarView;

import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;

public class MonthFragment extends BaseFragment {


    private static final String POSITION = "position";
    private static final String TAG = "MonthFragment";
    public static final String ADD_EVENT = "add_event";
    public static final String DELETE_EVENT = "delete_event";
    public static final String UPDATE_EVENT = "update_event";
    public static final String UPDATE_UI = "update_ui";
    public static final String WEEK_SETTING = "week_setting";
    public static final String SKIP = "skip";

    private int mYear;
    private int mMonth;
    private OnDaySelectListener mOnDaySelectListener;
    private MonthReceiver mMonthReceiver;
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
    }

    private void initArguments() {
        int position = getArguments().getInt(POSITION);
        mYear = position / MONTH_COUNT + YEAR_START_REAL;
        mMonth = position % MONTH_COUNT + 1;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupReceiver();
    }

    private void setupReceiver() {
        mMonthReceiver = new MonthReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ADD_EVENT);
        filter.addAction(UPDATE_EVENT);
        filter.addAction(DELETE_EVENT);
        filter.addAction(UPDATE_UI);
        filter.addAction(SKIP);
        filter.addAction(WEEK_SETTING);
        mHostActivity.registerReceiver(mMonthReceiver, filter);
        Log.d(TAG, "onCreate and registerReceiver");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView and unregisterReceiver");
        mHostActivity.unregisterReceiver(mMonthReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMonthReceiver = null;
        if (mLoadDataTask != null) {
            mLoadDataTask.cancel(true);
            mLoadDataTask = null;
        }
        mOnDaySelectListener = null;
        System.gc();
    }

    public class MonthReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (mCalendarView != null) {
                switch (intent.getAction()) {
                    case SKIP:
                        int year = intent.getIntExtra("year", 0);
                        int month = intent.getIntExtra("month", 0);
                        int day = intent.getIntExtra("day", 0);
                        if (year == mYear && month == mMonth) {
                            if (mCalendarView.isInitialized()) {
                                mCalendarView.selectCurrentMonth(day);
                            } else {
                                mPendingAction.addAction(() -> mCalendarView.selectCurrentMonth(day));
                            }
                        }
                        break;
                    case WEEK_SETTING:
                        mCalendarView.updateStartDate();
                    case UPDATE_EVENT:
                    case ADD_EVENT:
                    case DELETE_EVENT:
                        LoadDataTask task = new LoadDataTask(mHostActivity, mCalendarView, mPendingAction);
                        task.execute(mYear, mMonth);
                        break;
                    case UPDATE_UI:
                        updateCalendarSetting();
                        break;
                }
            }
        }
    }


    protected void setupUI() {
        mCalendarView = findViewById(R.id.cv);
        updateCalendarSetting();
        mCalendarView.setOnDaySelectedListener((position, day) -> {
            if (mOnDaySelectListener != null) {
                if (day instanceof Day) {
                    mOnDaySelectListener.onSelected((Day) day);
                }
            }
        });
    }


    private void updateCalendarSetting() {
        mCalendarView.setDateCircleSize(Setting.day_size == -1 ? getDimension(R.dimen.item_day_size) : Setting.day_size)
                .setDateTextSize(Setting.day_number_text_size == -1 ? getDimension(R.dimen.date_text_size) : Setting.day_number_text_size)
                .setDateBottomTextSize(Setting.day_lunar_text_size == -1 ? getDimension(R.dimen.bottom_text_size) : Setting.day_lunar_text_size)
                .setHolidayTextSize(Setting.day_holiday_text_size == -1 ? getDimension(R.dimen.holiday_text_size) : Setting.day_holiday_text_size)
                .setWeekTextSize(Setting.day_week_text_size == -1 ? getDimension(R.dimen.week_text_size) : Setting.day_week_text_size)
                .postInvalidate();
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

    public interface OnDaySelectListener {
        void onSelected(Day day);
    }
}
