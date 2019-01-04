package top.soyask.calendarii.ui.widget.base;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

import static top.soyask.calendarii.global.Global.VIEW_DAY;
import static top.soyask.calendarii.global.Global.VIEW_TODAY;
import static top.soyask.calendarii.global.Global.VIEW_WEEK;

/**
 * Created by mxf on 2017/11/19.
 */

public abstract class BaseRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {


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
        Setting.loadSetting(mContext);
        setupData();
        updateCount();
    }


    private void updateCount() {
        if (mDays.size() > 0) {
            this.mDateStartPos = (mDays.get(0).getDayOfWeek() + 6 - Setting.date_offset) % 7 + 7;
        } else {
            this.mDateStartPos = 6;
        }
        this.mEndPosition = mDateStartPos + mDays.size();
    }


    private synchronized void setupData() {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayCount = DayUtils.getMonthDayCount(month + 1, year);
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
