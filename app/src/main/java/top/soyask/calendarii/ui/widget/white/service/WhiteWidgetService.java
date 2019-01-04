package top.soyask.calendarii.ui.widget.white.service;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.widget.base.BaseRemoteViewFactory;

import static top.soyask.calendarii.global.Global.VIEW_DAY;
import static top.soyask.calendarii.global.Global.VIEW_TODAY;
import static top.soyask.calendarii.global.Global.VIEW_WEEK;

public class WhiteWidgetService extends RemoteViewsService {
    public WhiteWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewFactory(this);
    }

    private class RemoteViewFactory extends BaseRemoteViewFactory {
        private RemoteViewFactory(Context context) {
            super(context);
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = null;
            switch (getItemViewType(position)) {
                case VIEW_WEEK:
                    int index = (position + Setting.date_offset) % WEEK_ARRAY.length;
                    remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_white_widget_week);
                    remoteViews.setTextViewText(R.id.tv, WEEK_ARRAY[index]);
                    break;
                case VIEW_TODAY:
                    remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_white_widget_today);
                    break;
                case VIEW_DAY:
                    remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_white_widget_day);
                    remoteViews.setTextViewText(R.id.tv_greg, "");
                    remoteViews.setTextViewText(R.id.tv_lunar, "");
                    break;
            }

            if (position >= mDateStartPos && position < mEndPosition && position - mDateStartPos < mDays.size()) {
                Day day = mDays.get(position - mDateStartPos);
                remoteViews.setTextViewText(R.id.tv_greg, String.valueOf(day.getDayOfMonth()));
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
            }
            return remoteViews;

        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_white_widget_day);
            return remoteViews;
        }
    }
}
