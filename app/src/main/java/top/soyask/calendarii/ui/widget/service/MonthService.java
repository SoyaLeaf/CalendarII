package top.soyask.calendarii.ui.widget.service;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.domain.Event;
import top.soyask.calendarii.ui.adapter.MonthFragmentAdapter;
import top.soyask.calendarii.ui.widget.factory.RemoteViewFactory;
import top.soyask.calendarii.utils.DayUitls;
import top.soyask.calendarii.utils.HolidayUtils;
import top.soyask.calendarii.utils.LunarUtils;
import top.soyask.calendarii.utils.SolarUtils;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;

public class MonthService extends RemoteViewsService {

    private static final String TAG = "MonthService";
    private Calendar mCalendar;
    private int mYear;
    private int mMonth;
    private EventDao mEventDao;
    private List<Day> mDays;
    private RemoteViewFactory mRemoteViewFactory;

    public MonthService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDays = new ArrayList<>();
        mEventDao = EventDao.getInstance(this);
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");
    }

    public void update(int position) {
        mYear = position / MONTH_COUNT + YEAR_START_REAL;
        mMonth = position % MONTH_COUNT + 1;
        setupData();
        mRemoteViewFactory.setDays(mDays);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i(TAG, "onGetViewFactory");
        init(intent);
        setupData();
        mRemoteViewFactory = new RemoteViewFactory(this, mDays);
        return mRemoteViewFactory;
    }

    private void init(Intent intent) {
        mCalendar = (Calendar) intent.getSerializableExtra("calendar");
        int position = intent.getIntExtra("position", getCurrentMonth());
        mYear = position / MONTH_COUNT + YEAR_START_REAL;
        mMonth = position % MONTH_COUNT + 1;
        Log.i(TAG, "init:" + mYear + " --" + mMonth);
    }

    private int getCurrentMonth() {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance(Locale.CHINA);
        }
        return (mCalendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + mCalendar.get(MONTH);
    }


    @Override
    public void onStart(Intent intent, int startId) {
        init(intent);
        setupData();
        sendBroadcast(new Intent(intent.getStringExtra("action")));
        if(mRemoteViewFactory != null){
            mRemoteViewFactory.setDays(mDays);
        }
        Log.d(TAG, "onStart");
    }

    private synchronized void setupData() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, mYear);
        calendar.set(Calendar.MONTH, mMonth - 1);
        int dayCount = DayUitls.getMonthDayCount(mMonth - 1, mYear);
        mDays.clear();
        Log.d(TAG, " mDays.clear()" + mDays);
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            boolean isToday = isToday(i);
            String lunar = getLunar(calendar);
            Day day = new Day(mYear, mMonth, lunar, isToday, i + 1, dayOfWeek);
            try {
                List<Event> events = mEventDao.query(day.getYear() + "年" + day.getMonth() + "月" + day.getDayOfMonth() + "日");
                day.setEvents(events);
            } catch (Exception e) {
                e.printStackTrace();
                day.setEvents(new ArrayList<Event>());
            }
            mDays.add(day);
        }
    }

    private String getLunar(Calendar calendar) {
        String result = HolidayUtils.getHolidayOfMonth(calendar);
        if (result == null) {
            result = LunarUtils.getLunar(calendar);
            String lunarHoliday = HolidayUtils.getHolidayOfLunar(result);
            if (lunarHoliday != null) {
                return lunarHoliday;
            }
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

        String solar = SolarUtils.getSolar(calendar);

        return solar == null ? result : solar;
    }

    private boolean isToday(int i) {
        return mCalendar.get(Calendar.DAY_OF_MONTH) == i + 1
                && mCalendar.get(Calendar.YEAR) == mYear
                && mCalendar.get(Calendar.MONTH) + 1 == mMonth;
    }

}
