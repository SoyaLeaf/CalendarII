package top.soyask.calendarii.ui.widget.factory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.domain.Day;

/**
 * Created by mxf on 2017/9/21.
 */
public class RemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    static final int VIEW_WEEK = 0; //显示星期
    static final int VIEW_DAY = 1; //显示日子
    static final int VIEW_TODAY = 4;
    static final int VIEW_EVENT = 5;
    static final String[] WEEK_ARRAY = {"日", "一", "二", "三", "四", "五", "六",};

    private List<Day> mDays;
    private int mDateStartPos = 0;
    private int mEndPosition;
    private Context mContext;

    public RemoteViewFactory(Context context, @NonNull List<Day> days) {
        this.mContext = context;
        this.mDays = days;
        this.mDateStartPos = mDays.get(0).getDayOfWeek() + 6;
        this.mEndPosition = mDateStartPos + mDays.size();
    }


    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

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
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_day);
                break;
        }

        if (position >= mDateStartPos && position < mEndPosition) {
            Day day = mDays.get(position - mDateStartPos);
            remoteViews.setTextViewText(R.id.tv_greg, "" + day.getDayOfMonth());
            remoteViews.setTextViewText(R.id.tv_lunar, day.getLunar());
        }
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    public int getItemViewType(int position) {
        int type = VIEW_DAY;
        if (position > mDateStartPos && position < mEndPosition) {
            Day day = mDays.get(position - mDateStartPos);
            if (day.isToday()) {
                return VIEW_TODAY;
            }
            if (day.getEvents() != null && !day.getEvents().isEmpty()) {
                type = VIEW_EVENT;
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
