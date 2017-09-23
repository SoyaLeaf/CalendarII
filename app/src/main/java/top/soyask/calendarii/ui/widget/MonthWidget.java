package top.soyask.calendarii.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.ui.adapter.MonthFragmentAdapter;
import top.soyask.calendarii.ui.widget.service.MonthService;
import top.soyask.calendarii.utils.LunarUtils;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

/**
 * Implementation of App Widget functionality.
 */
public class MonthWidget extends AppWidgetProvider {


    private static final String NEXT = "next";
    private static final String PREV = "prev";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Calendar calendar, int position) {
        Log.d("XX", position + "");
        SharedPreferences.Editor editor = context.getSharedPreferences("widget", context.MODE_PRIVATE).edit();
        editor.putInt("current", position).commit();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.month_widget);
        Intent intent = new Intent(context, MonthService.class);
        intent.putExtra("position", position);
        intent.putExtra("calendar", calendar);
        views.setRemoteAdapter(R.id.gv_month, intent);

        Intent prev = new Intent(PREV);
        prev.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent leftIntent = PendingIntent.getService(context, 0, prev, 0);

        Intent next = new Intent(NEXT);
        next.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent rightIntent = PendingIntent.getBroadcast(context, 0, next, 0);

        views.setOnClickPendingIntent(R.id.ib_left, leftIntent);
        views.setOnClickPendingIntent(R.id.ib_right, rightIntent);

        String lunar = LunarUtils.getLunar(calendar);
        views.setTextViewText(R.id.tv_lunar, lunar);
        views.setTextViewText(R.id.tv_year, "" + calendar.get(Calendar.YEAR));
        views.setTextViewText(R.id.tv_date, calendar.get(Calendar.MONTH) + 1 + "月");

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int currentPos = (calendar.get(YEAR) - MonthFragmentAdapter.YEAR_START) * 12 + calendar.get(MONTH);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, calendar, currentPos);
        }
    }

    public void onLeftClick(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences widget = context.getSharedPreferences("widget", context.MODE_PRIVATE);
        int current = widget.getInt("current", 2017);
        current--;
        updateAppWidget(context, appWidgetManager, appWidgetId, Calendar.getInstance(Locale.CHINA), current);
    }

    public void onRightClick(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences widget = context.getSharedPreferences("widget", context.MODE_PRIVATE);
        int current = widget.getInt("current", 2017);
        current++;
        updateAppWidget(context, appWidgetManager, appWidgetId, Calendar.getInstance(Locale.CHINA), current);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String s = intent.getAction();
        if (s.equals(PREV)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            onLeftClick(context,appWidgetManager,appWidgetId);
        } else if (s.equals(NEXT)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
            onRightClick(context,appWidgetManager,appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
    }
}

