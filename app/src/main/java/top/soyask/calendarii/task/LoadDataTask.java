package top.soyask.calendarii.task;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.ui.adapter.month.MonthAdapter;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

/**
 * Created by mxf on 2018/4/24.
 */

public class LoadDataTask extends AsyncTask<Integer, Void, List<Day>> {

    private EventDao mEventDao;
    private MonthAdapter mAdapter;
    private PendingAction mPendingAction;

    public LoadDataTask(Context context, MonthAdapter adapter, PendingAction action) {
        this.mEventDao = EventDao.getInstance(context);
        this.mAdapter = adapter;
        this.mPendingAction = action;
    }

    public LoadDataTask(Context context, MonthAdapter adapter) {
        this(context, adapter, null);
    }

    @Override
    protected List<Day> doInBackground(Integer... integers) {
        return loadData(integers[0], integers[1], mEventDao);
    }

    @Override
    protected void onPostExecute(List<Day> days) {
        mAdapter.setDays(days);
        if (mPendingAction != null) {
            mPendingAction.execute();
        }
    }


    private static List<Day> loadData(int year, int month, EventDao eventDao) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        int dayCount = DayUtils.getMonthDayCount(month - 1, year);
        List<Day> days = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Day day = MonthUtils.generateDay(calendar, eventDao);
            days.add(day);
        }
        return days;
    }

}
