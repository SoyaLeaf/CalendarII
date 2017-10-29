package top.soyask.calendarii.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;

import top.soyask.calendarii.MainActivity;
import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.ui.widget.service.MonthService;
import top.soyask.calendarii.utils.LunarUtils;

/**
 * Implementation of App Widget functionality.
 */
public class MonthWidget extends AppWidgetProvider {

    private static final String TAG = "MonthWidget";

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {
        Log.i(TAG, "updateAppWidget");
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        RemoteViews views = setupRemoteViews(context, calendar);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.gv_month);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @NonNull
    private static RemoteViews setupRemoteViews(Context context, Calendar calendar) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.month_widget);
        String lunar = LunarUtils.getLunar(calendar);

        Intent intent = new Intent(context, MonthService.class);

        int alpha = context.getSharedPreferences("setting", Context.MODE_PRIVATE).getInt(Global.SETTING_WIDGET_ALPHA, 33);
        int month = calendar.get(Calendar.MONTH) + 1;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        views.setOnClickPendingIntent(R.id.iv_launch, pendingIntent);
        views.setInt(R.id.widget, "setBackgroundColor", Color.argb(alpha, 0, 0, 0));
        views.setRemoteAdapter(R.id.gv_month, intent);
        views.setTextViewText(R.id.tv_lunar, lunar);
        views.setTextViewText(R.id.tv_year, "" + calendar.get(Calendar.YEAR));
        views.setTextViewText(R.id.tv_date, (month < 10 ? "0" : "") + month + "月");

        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
    }
}

