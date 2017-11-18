package top.soyask.calendarii.ui.widget.factory;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

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
//            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//            boolean isToday = isToday(i);
//            LunarDay lunar = MonthUtils.getLunar(calendar);
//            Day day = new Day(year, month, lunar, isToday, i + 1, dayOfWeek);
//            List<Event> events = mEventDao.query(day.getYear() + "年" + day.getMonth() + "月" + day.getDayOfMonth() + "日");
//            day.setEvents(events);
            mDays.add(day);
        }
    }


    private boolean isToday(int i) {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) == i + 1;
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
                int index = (position + Setting.date_offset) % WEEK_ARRAY.length;
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_week);
                remoteViews.setTextViewText(R.id.tv, WEEK_ARRAY[index]);
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
            if (day.hasBirthday()) {
                remoteViews.setTextViewText(R.id.tv_lunar, "生日");
                remoteViews.setInt(R.id.iv_birth,"setVisibility", View.VISIBLE);
            }else {
                remoteViews.setTextViewText(R.id.tv_lunar, day.getLunar().getSimpleLunar());
                remoteViews.setInt(R.id.iv_birth,"setVisibility", View.INVISIBLE);
            }

            if(day.hasEvent()){
                remoteViews.setInt(R.id.fl_event,"setVisibility", View.VISIBLE);
            }else {
                remoteViews.setInt(R.id.fl_event,"setVisibility", View.INVISIBLE);
            }
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
