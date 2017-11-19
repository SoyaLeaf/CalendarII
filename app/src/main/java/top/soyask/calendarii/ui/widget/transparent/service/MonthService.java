package top.soyask.calendarii.ui.widget.transparent.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.Calendar;

import top.soyask.calendarii.R;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.widget.base.BaseRemoteViewFactory;

public class MonthService extends RemoteViewsService {

    private static final String TAG = "MonthService";

    public MonthService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i(TAG, "onGetViewFactory");
        return new RemoteViewFactory(this);
    }

    private class RemoteViewFactory extends BaseRemoteViewFactory {


        public RemoteViewFactory(Context context) {
            super(context);
        }

        @Deprecated
        private boolean isToday(int i) {
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.DAY_OF_MONTH) == i + 1;
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
                    remoteViews.setInt(R.id.iv_birth, "setVisibility", View.VISIBLE);
                } else {
                    remoteViews.setTextViewText(R.id.tv_lunar, day.getLunar().getSimpleLunar());
                    remoteViews.setInt(R.id.iv_birth, "setVisibility", View.INVISIBLE);
                }

                if (day.hasEvent()) {
                    remoteViews.setInt(R.id.fl_event, "setVisibility", View.VISIBLE);
                } else {
                    remoteViews.setInt(R.id.fl_event, "setVisibility", View.INVISIBLE);
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

    }

}
