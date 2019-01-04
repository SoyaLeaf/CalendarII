package top.soyask.calendarii.ui.widget.transparent.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.Calendar;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.widget.base.BaseRemoteViewFactory;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static top.soyask.calendarii.global.Global.VIEW_DAY;
import static top.soyask.calendarii.global.Global.VIEW_TODAY;
import static top.soyask.calendarii.global.Global.VIEW_WEEK;

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


        private RemoteViewFactory(Context context) {
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
            int layout;
            switch (getItemViewType(position)) {
                case VIEW_WEEK:
                    int index = (position + Setting.date_offset) % WEEK_ARRAY.length;
                    layout = Setting.TransparentWidget.trans_widget_theme_color == 0 ? R.layout.item_widget_week : R.layout.item_widget_week_light;
                    remoteViews = new RemoteViews(mContext.getPackageName(), layout);
                    remoteViews.setTextViewText(R.id.tv, WEEK_ARRAY[index]);
                    remoteViews.setTextViewTextSize(R.id.tv, COMPLEX_UNIT_SP, Setting.TransparentWidget.trans_widget_week_font_size);
                    break;
                case VIEW_TODAY:
                    layout = Setting.TransparentWidget.trans_widget_theme_color == 0 ? R.layout.item_widget_today : R.layout.item_widget_today_light;
                    remoteViews = new RemoteViews(mContext.getPackageName(), layout);
                    break;
                case VIEW_DAY:
                    layout = Setting.TransparentWidget.trans_widget_theme_color == 0 ? R.layout.item_widget_day : R.layout.item_widget_day_light;
                    remoteViews = new RemoteViews(mContext.getPackageName(), layout);
                    remoteViews.setTextViewText(R.id.tv_greg, "");
                    remoteViews.setTextViewText(R.id.tv_lunar, "");
                    break;
            }

            if (position >= mDateStartPos && position < mEndPosition && position - mDateStartPos < mDays.size()) {
                Day day = mDays.get(position - mDateStartPos);
                remoteViews.setTextViewText(R.id.tv_greg, String.format(Locale.CHINA, "%d", day.getDayOfMonth()));
                if (day.hasBirthday()) {
                    remoteViews.setTextViewText(R.id.tv_lunar, "生日");
                    remoteViews.setViewVisibility(R.id.iv_birth, View.VISIBLE);
                } else {
                    remoteViews.setTextViewText(R.id.tv_lunar, day.getLunar().getSimpleLunar());
                    remoteViews.setViewVisibility(R.id.iv_birth, View.INVISIBLE);
                }

                if (day.hasEvent()) {
                    remoteViews.setViewVisibility(R.id.fl_event, View.VISIBLE);
                } else {
                    remoteViews.setViewVisibility(R.id.fl_event, View.INVISIBLE);
                }
                remoteViews.setTextViewTextSize(R.id.tv_greg, COMPLEX_UNIT_SP, Setting.TransparentWidget.trans_widget_number_font_size);
                remoteViews.setTextViewTextSize(R.id.tv_lunar, COMPLEX_UNIT_SP, Setting.TransparentWidget.trans_widget_lunar_font_size);
                // FIXME 将道理，这个语句应该是合理的，但是加上它却导致小部件无法显示！！ wtf?
                // remoteViews.setFloat(R.id.rl,"setScaleX",0.6f);
            }
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(mContext.getPackageName(), R.layout.item_widget_day);
        }

    }

}
