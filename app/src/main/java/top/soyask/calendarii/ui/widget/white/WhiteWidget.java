package top.soyask.calendarii.ui.widget.white;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

import top.soyask.calendarii.MainActivity;
import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.widget.white.service.WhiteWidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class WhiteWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.white_widget);
        Intent intent = new Intent(context, WhiteWidgetService.class);
        Setting.loadSetting(context);
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        views.setTextViewText(R.id.tv_year, String.valueOf(calendar.get(Calendar.YEAR)));
        views.setTextViewText(R.id.tv_month, String.valueOf(calendar.get(Calendar.MONTH) + 1) + "月");
        views.setRemoteAdapter(R.id.gv_month, intent);
        if (Setting.white_widget_pic != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(Setting.white_widget_pic);
            views.setBitmap(R.id.iv, "setImageBitmap", bitmap);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.miku);
            views.setBitmap(R.id.iv, "setImageBitmap", bitmap);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        views.setOnClickPendingIntent(R.id.iv, pendingIntent);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.gv_month);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

