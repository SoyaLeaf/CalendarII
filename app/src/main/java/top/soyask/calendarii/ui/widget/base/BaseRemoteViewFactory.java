package top.soyask.calendarii.ui.widget.base;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

/**
 * Created by mxf on 2017/11/19.
 */

public abstract class BaseRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    protected static final int VIEW_WEEK = 0; //显示星期
    protected static final int VIEW_DAY = 1; //显示日子
    protected static final int VIEW_TODAY = 4;
    protected static final int VIEW_EVENT = 5;
    protected static final String[] WEEK_ARRAY = {"日", "一", "二", "三", "四", "五", "六",};
    private static final String TAG = "RemoteViewFactory";

    protected List<Day> mDays;
    protected int mDateStartPos = 0;
    protected int mEndPosition;
    protected Context mContext;
    protected EventDao mEventDao;

    public BaseRemoteViewFactory(Context context) {
        Setting.loadSetting(context);
        this.mContext = context;
        this.mDays = new ArrayList<>();
        this.mEventDao = EventDao.getInstance(context);
        setupData();
        updateCount();
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        setupData();
        updateCount();
    }


    private void updateCount() {
        if (mDays.size() > 0) {
            this.mDateStartPos = (mDays.get(0).getDayOfWeek() + 6 - Setting.date_offset) % 7 + 7;
        } else {
            this.mDateStartPos = 6;
        }
        Log.d(TAG, "mDateStartPos:" + mDateStartPos);
        this.mEndPosition = mDateStartPos + mDays.size();
    }


    private synchronized void setupData() {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayCount = DayUtils.getMonthDayCount(month, year);
        mDays.clear();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Day day = MonthUtils.generateDay(calendar, mEventDao);
            mDays.add(day);
        }
    }


    public int getItemViewType(int position) {
        int type = VIEW_DAY;
        if (position >= mDateStartPos && position < mEndPosition && position - mDateStartPos < mDays.size()) {
            Day day = mDays.get(position - mDateStartPos);
            if (day.isToday()) {
                return VIEW_TODAY;
            }
        }
        type = position < 7 ? VIEW_WEEK : type;
        return type;
    }


    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return 49;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
