package top.soyask.calendarii.ui.fragment.month;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.ui.adapter.month.MonthAdapter;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.main.MainFragment;
import top.soyask.calendarii.ui.fragment.setting.SettingFragment;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;

public class MonthFragment extends BaseFragment implements MonthAdapter.OnItemClickListener {


    private static final String POSITION = "position";
    private static final String TAG = "MonthFragment";

    private List<Day> mDays = new ArrayList<>();
    private MonthAdapter mMonthAdapter;
    private int mYear;
    private int mMonth;
    private OnDaySelectListener mOnDaySelectListener;
    private EventDao mEventDao;
    private MonthReceiver mMonthReceiver;

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
        if (getArguments() != null) {
            initArguments();
        }
        mEventDao = EventDao.getInstance(getActivity());
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
        filter.addAction(EventDao.ADD);
        filter.addAction(EventDao.UPDATE);
        filter.addAction(EventDao.DELETE);
        filter.addAction(MainFragment.SKIP);
        filter.addAction(SettingFragment.WEEK_SETTING);
        mHostActivity.registerReceiver(mMonthReceiver, filter);
        Log.d(TAG, "onCreate and registerReceiver");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView and unregisterReceiver");
        mHostActivity.unregisterReceiver(mMonthReceiver);
    }

    public class MonthReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (mMonthAdapter != null) {
                switch (intent.getAction()) {
                    case MainFragment.SKIP:
                        int year = intent.getIntExtra("year", 0);
                        int month = intent.getIntExtra("month", 0);
                        int day = intent.getIntExtra("day", 0);
                        if (year == mYear && month == mMonth) {
                            mMonthAdapter.setSelectedDay(day);
                        }
                        break;
                    case SettingFragment.WEEK_SETTING:
                        mMonthAdapter.updateStartDate();
                    case EventDao.UPDATE:
                    case EventDao.ADD:
                    case EventDao.DELETE:
                        setupData();
                        mMonthAdapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mEventDao = EventDao.getInstance(getActivity());
        setupData();
        super.onActivityCreated(savedInstanceState);
    }

    private synchronized void setupData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth - 1);
        int dayCount = DayUtils.getMonthDayCount(mMonth - 1, mYear);
        mDays.clear();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH,i + 1);
            Day day = MonthUtils.generateDay(calendar, mEventDao);
            mDays.add(day);
        }
    }

    protected void setupUI() {
        mMonthAdapter = new MonthAdapter(mDays, this);
        RecyclerView recyclerView = findViewById(R.id.rv_date);
        recyclerView.setLayoutManager(new GridLayoutManager(mHostActivity, 7));
        recyclerView.setAdapter(mMonthAdapter);
        recyclerView.setItemAnimator(null);
    }

    @Override
    public void onDayClick(int position, Day day) {
        if (mOnDaySelectListener != null) {
            mOnDaySelectListener.onSelected(day);
        }
    }

    public void setOnDaySelectListener(OnDaySelectListener onDaySelectListener) {
        this.mOnDaySelectListener = onDaySelectListener;
    }

    public interface OnDaySelectListener {
        void onSelected(Day day);
    }
}
