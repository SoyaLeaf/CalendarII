package top.soyask.calendarii.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.ui.adapter.MonthFragmentAdapter;
import top.soyask.calendarii.ui.widget.service.MonthService;
import top.soyask.calendarii.utils.LunarUtils;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static top.soyask.calendarii.global.Global.MONTH_COUNT;
import static top.soyask.calendarii.global.Global.YEAR_START_REAL;

/**
 * Implementation of App Widget functionality.
 */
public class MonthWidget extends AppWidgetProvider {


    private static final String NEXT = "next";
    private static final String PREV = "prev";
    private static final String RESET = "reset";
    private static final String WIDGET = "widget";
    private static final String TAG = "MonthWidget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.i(TAG, "updateAppWidget");
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int position = (calendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + calendar.get(MONTH);
        savePosition(context, position);
        RemoteViews views = initRemoteViews(context, calendar, position);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.gv_month);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @NonNull
    private static RemoteViews initRemoteViews(Context context, Calendar calendar, int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.month_widget);
        PendingIntent rightIntent = getRightIntent(context, calendar, position);
        PendingIntent leftIntent = getLeftIntent(context, calendar, position);
        PendingIntent resetIntent = getResetIntent(context, calendar);
        String lunar = LunarUtils.getLunar(calendar);

        Intent intent = new Intent(context, MonthService.class);
        intent.putExtra("position", position);
        intent.putExtra("calendar", calendar);
        views.setRemoteAdapter(R.id.gv_month, intent);

        views.setOnClickPendingIntent(R.id.ib_left, leftIntent);
        views.setOnClickPendingIntent(R.id.ib_right, rightIntent);
        views.setOnClickPendingIntent(R.id.ib_reset, resetIntent);

        views.setTextViewText(R.id.tv_lunar, lunar);
        views.setTextViewText(R.id.tv_year, "" + calendar.get(Calendar.YEAR));
        int month = calendar.get(Calendar.MONTH) + 1;
        views.setTextViewText(R.id.tv_date, (month < 10 ? "0" : "") + month + "月");
        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void modifyAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       Calendar calendar, int position) {
        ComponentName componentName = new ComponentName(context, MonthWidget.class);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.gv_month);

        int year = position / MONTH_COUNT + YEAR_START_REAL; //position==0时 1910/1
        int month = position % MONTH_COUNT + 1;
        int current = (calendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + calendar.get(MONTH);
        savePosition(context, position);
        RemoteViews views = updateRemoteViews(context, calendar, position, year, month, current);
        appWidgetManager.updateAppWidget(componentName, views);
    }

    private static void savePosition(Context context, int position) {
        SharedPreferences.Editor editor = context.getSharedPreferences(WIDGET, context.MODE_PRIVATE).edit();
        editor.putInt("current", position).commit();
    }

    @NonNull
    private static RemoteViews updateRemoteViews(Context context, Calendar calendar, int position, int year, int month, int current) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.month_widget);
        PendingIntent rightIntent = getRightIntent(context, calendar, position);
        PendingIntent leftIntent = getLeftIntent(context, calendar, position);
        PendingIntent resetIntent = getResetIntent(context, calendar);

        Intent intent = new Intent(context, MonthService.class);
        intent.putExtra("position", position);
        intent.putExtra("calendar", calendar);
        views.setRemoteAdapter(R.id.gv_month, intent);
        views.setTextViewText(R.id.tv_year, year + "");
        views.setTextViewText(R.id.tv_date, (month < 10 ? "0" : "") + month + "月");
        views.setOnClickPendingIntent(R.id.ib_right, rightIntent);
        views.setOnClickPendingIntent(R.id.ib_left, leftIntent);
        views.setOnClickPendingIntent(R.id.ib_reset, resetIntent);
        views.setInt(R.id.ib_reset, "setVisibility", position == current ? View.GONE : View.VISIBLE);

        if (current == position) {
            String lunar = LunarUtils.getLunar(calendar);
            views.setTextViewText(R.id.tv_lunar, lunar);
        } else {
            int diff = position - current;
            views.setTextViewText(R.id.tv_lunar, Math.abs(diff) + "个月之" + (diff < 0 ? "前" : "后"));
        }
        return views;
    }

    private static PendingIntent getLeftIntent(Context context, Calendar calendar, int position) {
        Intent prev = new Intent(context, MonthService.class);
        prev.putExtra("position", position - 1);
        prev.putExtra("calendar", calendar);
        prev.putExtra("action", PREV);
        prev.setAction(PREV);
        return PendingIntent.getService(context, 1, prev, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getRightIntent(Context context, Calendar calendar, int position) {
        Intent next = new Intent(context, MonthService.class);
        next.putExtra("position", position + 1);
        next.putExtra("calendar", calendar);
        next.putExtra("action", NEXT);
        next.setAction(NEXT);
        return PendingIntent.getService(context, 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getResetIntent(Context context, Calendar calendar) {

        Intent next = new Intent(context, MonthService.class);
        int position = (calendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + calendar.get(MONTH);
        next.putExtra("position", position);
        next.putExtra("calendar", calendar);
        next.putExtra("action", RESET);
        next.setAction(RESET);
        return PendingIntent.getService(context, 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void onLeftClick(Context context, AppWidgetManager appWidgetManager) {
        SharedPreferences widget = context.getSharedPreferences(WIDGET, context.MODE_PRIVATE);
        int current = widget.getInt("current", 2017);
        current--;
        Log.i(TAG, "left button click !");
        modifyAppWidget(context, appWidgetManager, Calendar.getInstance(Locale.CHINA), current);
    }

    public void onRightClick(Context context, AppWidgetManager appWidgetManager) {
        SharedPreferences widget = context.getSharedPreferences(WIDGET, context.MODE_PRIVATE);
        int current = widget.getInt("current", 2017);
        current++;
        Log.i(TAG, "right button click !");
        modifyAppWidget(context, appWidgetManager, Calendar.getInstance(Locale.CHINA), current);
    }

    public void onResetClick(Context context, AppWidgetManager appWidgetManager) {
        Log.i(TAG, "reset button click !");
        resetAppWidget(context, appWidgetManager, Calendar.getInstance(Locale.CHINA));
    }

    private void resetAppWidget(Context context, AppWidgetManager appWidgetManager, Calendar calendar) {
        ComponentName componentName = new ComponentName(context, MonthWidget.class);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.gv_month);
        int year = calendar.get(YEAR); //position==0时 1910/1
        int month = calendar.get(MONTH) + 1;
        int current = (calendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + calendar.get(MONTH);

        savePosition(context, current);
        RemoteViews views = updateRemoteViews(context, calendar, current, year, month, current);
        appWidgetManager.updateAppWidget(componentName, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String s = intent.getAction();
        if (PREV.equals(s)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onLeftClick(context, appWidgetManager);
        } else if (NEXT.equals(s)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onRightClick(context, appWidgetManager);
        } else if (RESET.equals(s)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onResetClick(context, appWidgetManager);
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

