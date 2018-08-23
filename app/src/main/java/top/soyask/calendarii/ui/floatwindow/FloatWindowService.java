package top.soyask.calendarii.ui.floatwindow;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import top.soyask.calendarii.R;

public class FloatWindowService extends Service {

    private FloatWindowManager mFloatWindow;

    public FloatWindowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFloatWindow = FloatWindowManager.getInstance();
        mFloatWindow.show(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFloatWindow.hide(this);
        mFloatWindow.free();
        mFloatWindow = null;
    }
}
