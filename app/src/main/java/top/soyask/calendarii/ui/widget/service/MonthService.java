package top.soyask.calendarii.ui.widget.service;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

import top.soyask.calendarii.ui.widget.factory.RemoteViewFactory;

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

}
