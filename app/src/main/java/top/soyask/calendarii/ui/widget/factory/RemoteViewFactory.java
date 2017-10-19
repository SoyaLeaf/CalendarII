package top.soyask.calendarii.ui.widget.factory;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.domain.Event;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.HolidayUtils;
import top.soyask.calendarii.utils.LunarUtils;
import top.soyask.calendarii.utils.SolarUtils;

/**
 * Created by mxf on 2017/9/21.
 */
public class RemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    static final int VIEW_WEEK = 0; //显示星期
    static final int VIEW_DAY = 1; //显示日子
    static final int VIEW_TODAY = 4;
    static final int VIEW_EVENT = 5;
    static final String[] WEEK_ARRAY = {"日", "一", "二", "三", "四", "五", "六",};
    private static final String TAG = "RemoteViewFactory";

    private List<Day> mDays;
    private int mDateStartPos = 0;
    private int mEndPosition;
    private Context mContext;
    private EventDao mEventDao;

    public RemoteViewFactory(Context context) {
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
            this.mDateStartPos = mDays.get(0).getDayOfWeek() + 6;
        } else {
            this.mDateStartPos = 6;
        }
        Log.d(TAG, "mDateStartPos:" + mDateStartPos);
        this.mEndPosition = mDateStartPos + mDays.size();
    }

    private synchronized void setupData() {

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) - 1;
        int year = calendar.get(Calendar.YEAR);
        int dayCount = DayUtils.getMonthDayCount(month, year);
        mDays.clear();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            boolean isToday = isToday(i);
            String lunar = getLunar(calendar);
            Day day = new Day(year, month, lunar, isToday, i + 1, dayOfWeek);
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


    private boolean isToday(int i) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) == i + 1;
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


    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        return 49;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews remoteViews = null;
        switch (getItemViewType(position)) {
            case VIEW_WEEK:
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_week);
                remoteViews.setTextViewText(R.id.tv, WEEK_ARRAY[position]);
                break;
            case VIEW_TODAY:
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_today);
                break;
            case VIEW_DAY:
                Log.d(TAG, "position:" + position);
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_day);
                remoteViews.setTextViewText(R.id.tv_greg, "");
                remoteViews.setTextViewText(R.id.tv_lunar, "");
                break;
        }

        if (position >= mDateStartPos && position < mEndPosition && position - mDateStartPos < mDays.size()) {
            Day day = mDays.get(position - mDateStartPos);
            remoteViews.setTextViewText(R.id.tv_greg, "" + day.getDayOfMonth());
            remoteViews.setTextViewText(R.id.tv_lunar, day.getLunar());
            Log.d(TAG, "position:" + day.getLunar());
        }
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_day);
        return remoteViews;
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
