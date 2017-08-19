package top.soyask.calendarii.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.adapter.MonthAdapter;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.utils.DayUitls;
import top.soyask.calendarii.utils.HolidayUtils;
import top.soyask.calendarii.utils.LunarUtils;

import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START;

public class MonthFragment extends Fragment implements MonthAdapter.OnItemClickListener {


    private static final String CALENDAR = "calendar";
    private static final String POSITION = "position";
    private static final String TAG = "MonthFragment";

    private View mContentView;
    private List<Day> mDays = new ArrayList<>();
    private MonthAdapter mMonthAdapter;
    private Calendar mCalendar;
    private int mYear;
    private int mMonth;
    private OnDaySelectListener mOnDaySelectListener;
    private EventDao mEventDao;

    public MonthFragment() {

    }


    public static MonthFragment newInstance(Calendar calendar, int position) {
        MonthFragment fragment = new MonthFragment();
        Bundle args = new Bundle();
        args.putSerializable(CALENDAR, calendar);
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    private void setupData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth - 1);
        int dayCount = DayUitls.getMonthDayCount(mMonth - 1, mYear);
        mDays.clear();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            boolean isToday = isToday(i);
            String lunar = getLunar(calendar);
            Day day = new Day(mYear, mMonth, lunar, isToday, i + 1, dayOfWeek);

//            String title = new StringBuffer()
//                    .append(mYear).append("年")
//                    .append(mMonth).append("月")
//                    .append(i + 1).append("日")
//                    .toString();
//            Log.d(TAG,title);
//            List<Event> events = mEventDao.query(title.substring(2));
//            day.setEvents(events);

            mDays.add(day);
        }
    }

    private String getLunar(Calendar calendar) {
        String result = HolidayUtils.getHolidayOfMonth(calendar);
        if (result == null) {
            result = LunarUtils.getLunar(calendar);
            int length = result.length();
            if (result.endsWith("初一")) {
                result = result.substring(0, length - 2);
            } else {
                result = result.substring(length - 2, length);
            }
        } else {
            if (result.length() > 4) {
                result = result.substring(0, 4);
            }
        }
        return result;
    }

    private boolean isToday(int i) {
        return mCalendar.get(Calendar.DAY_OF_MONTH) == i + 1
                && mCalendar.get(Calendar.YEAR) == mYear
                && mCalendar.get(Calendar.MONTH) + 1 == mMonth;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int position = getArguments().getInt(POSITION);
            mYear = position / MONTH_COUNT + YEAR_START;
            mMonth = (position - 1) % MONTH_COUNT + 1;
            mCalendar = (Calendar) getArguments().getSerializable(CALENDAR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_month, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEventDao = EventDao.getInstance(getActivity());
        setupData();
        setupUI();
    }


    private void setupUI() {
        initDateView();
//        initEventView();
    }


    private void initDateView() {
        RecyclerView recyclerView;
        recyclerView = find(R.id.rv_date);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 7));
        mMonthAdapter = new MonthAdapter(mDays, this);
        recyclerView.setAdapter(mMonthAdapter);
        recyclerView.setItemAnimator(null);
    }

    <T extends View> T find(@IdRes int id) {
        return (T) mContentView.findViewById(id);
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
