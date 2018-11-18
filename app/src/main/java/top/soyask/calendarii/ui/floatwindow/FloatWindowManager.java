package top.soyask.calendarii.ui.floatwindow;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Toast;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.widget.WidgetManager;

import static android.content.Context.APPWIDGET_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class FloatWindowManager implements SeekBar.OnSeekBarChangeListener {

    private static FloatWindowManager mFloatWindowManager;

    private View mContentView;
    private WindowManager.LayoutParams mParams;
    private boolean isShown;

    public void show(Context context) {
        if (isShown) {
            return;
        }
        isShown = true;
        if (mContentView == null) {
            createContentAndFindView(context);
        }
        if (mParams == null) {
            createParams(context);
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (windowManager == null) {
            Toast.makeText(context, "似乎除了点儿问题,请稍后再试.", Toast.LENGTH_SHORT).show();
            return;
        }
        windowManager.addView(mContentView, mParams);
    }

    private void createContentAndFindView(Context context) {
        mContentView = LayoutInflater.from(context).inflate(R.layout.float_window, null);
        findView();
    }

    private void findView() {
        mContentView.findViewById(R.id.fab_close).setOnClickListener(this::close);
        mContentView.findViewById(R.id.btn_reset).setOnClickListener(this::reset);
        SeekBar mSbYear = mContentView.findViewById(R.id.sb_year);
        SeekBar mSbMonth = mContentView.findViewById(R.id.sb_month);
        SeekBar mSbLunarMonth = mContentView.findViewById(R.id.sb_lunar_month);
        SeekBar mSbWeek = mContentView.findViewById(R.id.sb_week);
        SeekBar mSbDay = mContentView.findViewById(R.id.sb_day);
        SeekBar mSbLunar = mContentView.findViewById(R.id.sb_lunar);


        mSbYear.setOnSeekBarChangeListener(this);
        mSbMonth.setOnSeekBarChangeListener(this);
        mSbLunarMonth.setOnSeekBarChangeListener(this);
        mSbWeek.setOnSeekBarChangeListener(this);
        mSbDay.setOnSeekBarChangeListener(this);
        mSbLunar.setOnSeekBarChangeListener(this);

        mSbYear.setProgress(Setting.TransparentWidget.trans_widget_year_text_size);
        mSbMonth.setProgress(Setting.TransparentWidget.trans_widget_month_text_size);
        mSbLunarMonth.setProgress(Setting.TransparentWidget.trans_widget_lunar_month_text_size);
        mSbWeek.setProgress(Setting.TransparentWidget.trans_widget_week_font_size);
        mSbDay.setProgress(Setting.TransparentWidget.trans_widget_number_font_size);
        mSbLunar.setProgress(Setting.TransparentWidget.trans_widget_lunar_font_size);
    }

    private void reset(View view) {
        Context context = view.getContext();
        Setting.TransparentWidget.reset(context);
        AppWidgetManager appWidgetManager =
                (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
        WidgetManager.updateMonthWidget(context, appWidgetManager);
        findView();
    }

    private void close(View view) {
        Context context = view.getContext();
        context.stopService(new Intent(context, FloatWindowService.class));
    }

    public void hide(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (windowManager == null) {
            Toast.makeText(context, "似乎除了点儿问题,请稍后再试.", Toast.LENGTH_SHORT).show();
            return;
        }
        windowManager.removeViewImmediate(mContentView);
    }

    public void free() {
        mParams = null;
        mContentView = null;
        mFloatWindowManager = null;
    }

    private void createParams(Context context) {
        mParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.BOTTOM | Gravity.START;
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;

        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point outSize = new Point();
        display.getRealSize(outSize);
        mParams.height = outSize.y / 2;
        mParams.x = 0;
        mParams.y = 0;
    }

    public static FloatWindowManager getInstance() {
        synchronized (FloatWindowManager.class) {
            if (mFloatWindowManager == null) {
                synchronized (FloatWindowManager.class) {
                    mFloatWindowManager = new FloatWindowManager();
                }
            }
        }
        return mFloatWindowManager;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Context context = mContentView.getContext();
        switch (seekBar.getId()) {
            case R.id.sb_month:
                if (Setting.TransparentWidget.trans_widget_month_text_size != progress) {
                    Setting.TransparentWidget.trans_widget_month_text_size = progress;
                    Setting.setting(context, Global.SETTING_TRANS_WIDGET_MONTH_TEXT_SIZE, progress);
                }
                break;
            case R.id.sb_year:
                if (Setting.TransparentWidget.trans_widget_year_text_size != progress) {
                    Setting.TransparentWidget.trans_widget_year_text_size = progress;
                    Setting.setting(context, Global.SETTING_TRANS_WIDGET_YEAR_TEXT_SIZE, progress);
                }
                break;
            case R.id.sb_lunar_month:
                if (Setting.TransparentWidget.trans_widget_lunar_month_text_size != progress) {
                    Setting.TransparentWidget.trans_widget_lunar_month_text_size = progress;
                    Setting.setting(context, Global.SETTING_TRANS_WIDGET_LUNAR_MONTH_TEXT_SIZE, progress);
                }
                break;
            case R.id.sb_day:
                if (Setting.TransparentWidget.trans_widget_number_font_size != progress) {
                    Setting.TransparentWidget.trans_widget_number_font_size = progress;
                    Setting.setting(context, Global.SETTING_TRANS_WIDGET_NUMBER_FONT_SIZE, progress);
                }
                break;
            case R.id.sb_lunar:
                if (Setting.TransparentWidget.trans_widget_lunar_font_size != progress) {
                    Setting.TransparentWidget.trans_widget_lunar_font_size = progress;
                    Setting.setting(context, Global.SETTING_TRANS_WIDGET_LUNAR_FONT_SIZE, progress);
                }
                break;
            case R.id.sb_week:
                if (Setting.TransparentWidget.trans_widget_week_font_size != progress) {
                    Setting.TransparentWidget.trans_widget_week_font_size = progress;
                    Setting.setting(context, Global.SETTING_TRANS_WIDGET_WEEK_FONT_SIZE, progress);
                }
                break;
//            case R.id.sb_line:
//                if (Setting.TransparentWidget.trans_widget_line_height != progress) {
//                    Setting.TransparentWidget.trans_widget_line_height = progress;
//                    Setting.setting(context, Global.SETTING_TRANS_WIDGET_LINE_HEIGHT, progress);
//                }
//                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Context context = mContentView.getContext();
        AppWidgetManager appWidgetManager =
                (AppWidgetManager) context.getSystemService(APPWIDGET_SERVICE);
        WidgetManager.updateMonthWidget(context, appWidgetManager);
    }
}
