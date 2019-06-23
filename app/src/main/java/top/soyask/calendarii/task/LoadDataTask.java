package top.soyask.calendarii.task;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.database.dao.MemorialDayDao;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.ui.view.CalendarView;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

/**
 * Created by mxf on 2018/4/24.
 */

public class LoadDataTask extends AsyncTask<Integer, Void, List<Day>> {

    private ThingDao mThingDao;
    private MemorialDayDao mMemorialDayDao;

    private WeakReference<CalendarView> mCalendarView;
    private PendingAction mPendingAction;
    private Integer mYear;
    private Integer mMonth;

    public LoadDataTask(Context context, CalendarView calendarView, PendingAction action) {
        this.mThingDao = ThingDao.getInstance(context);
        this.mMemorialDayDao = MemorialDayDao.getInstance(context);
        this.mCalendarView = new WeakReference<>(calendarView);
        this.mPendingAction = action;
    }

    public LoadDataTask(Context context, CalendarView calendarView) {
        this(context, calendarView, null);
    }

    @Override
    protected List<Day> doInBackground(Integer... integers) {
        mYear = integers[0];
        mMonth = integers[1];
        return loadData(mYear, mMonth, mThingDao,mMemorialDayDao);
    }

    @Override
    protected void onPostExecute(List<Day> days) {
        CalendarView calendarView = mCalendarView.get();
        if(calendarView != null){
            calendarView.setData(mYear,mMonth,days);
            if (mPendingAction != null) {
                mPendingAction.execute();
            }
        }
    }


    private static List<Day> loadData(int year, int month, ThingDao thingDao,MemorialDayDao memorialDayDao) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        int dayCount = DayUtils.getMonthDayCount(month, year);
        List<Day> days = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Day day = MonthUtils.generateDay(calendar, thingDao,memorialDayDao);
            days.add(day);
        }
        return days;
    }

}
