package top.soyask.calendarii.ui.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * Created by mxf on 2017/11/19.
 */

public class WidgetManager {
    public static final void updateMonthWidget(Context context) {
        AppWidgetManager appWidgetManager =
                (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MonthWidget.class));
        if (appWidgetIds != null) {
            for (int appWidgetId : appWidgetIds) {
                MonthWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }
}
