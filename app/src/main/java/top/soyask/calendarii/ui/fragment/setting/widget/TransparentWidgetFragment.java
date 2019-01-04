package top.soyask.calendarii.ui.fragment.setting.widget;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.entity.LunarDay;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.adapter.month.MonthDayAdapter;
import top.soyask.calendarii.ui.floatwindow.FloatWindowService;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.widget.WidgetManager;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.LunarUtils;
import top.soyask.calendarii.utils.MonthUtils;
import top.soyask.calendarii.utils.PermissionUtils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class TransparentWidgetFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {

    public static final String TAG = TransparentWidgetFragment.class.getSimpleName();
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 777;
    public static final int REQUEST_CODE_SYSTEM_ALERT_WINDOW = 666;
    private int mWallPagerOffset;
    private Bitmap mLastBackground;
    private boolean mIsWallPagerFit;
    private boolean mLoadWallPager;
    private SeekBar mSbWallPagerOffset;
    private View mWidgetView;
    private MonthDayAdapter mAdapter;

    public TransparentWidgetFragment() {
        super(R.layout.fragment_transparent_widget);
    }

    public static TransparentWidgetFragment newInstance() {
        TransparentWidgetFragment fragment = new TransparentWidgetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected void setupUI() {
        setWidgetView();
        SeekBar sbAlpha = findViewById(R.id.sb_alpha);
        sbAlpha.setProgress(Setting.TransparentWidget.trans_widget_alpha);
        sbAlpha.setOnSeekBarChangeListener(this);
        mSbWallPagerOffset = findViewById(R.id.sb_wallpager_offset);
        mSbWallPagerOffset.setOnSeekBarChangeListener(this);
        ((SwitchCompat) findViewById(R.id.sc_load_wallpager)).setOnCheckedChangeListener(this::toggleBackground);
        SwitchCompat scTheme = findViewById(R.id.sc_theme);
        scTheme.setChecked(Setting.TransparentWidget.trans_widget_theme_color == 0);
        scTheme.setOnCheckedChangeListener(this::toggleWidgetTheme);

        findViewById(R.id.btn_font).setOnClickListener(this::onFontButtonClick);
    }

    private void onFontButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        mHostActivity.startService(new Intent(mHostActivity, FloatWindowService.class));
    }

    private void toggleWidgetTheme(CompoundButton compoundButton, boolean b) {
        Setting.TransparentWidget.trans_widget_theme_color = b ? 0 : 255;
        int themeColor = Setting.TransparentWidget.trans_widget_theme_color;
        Setting.setting(mHostActivity, Global.SETTING_TRANS_WIDGET_THEME_COLOR, themeColor);
        mAdapter.notifyDataSetChanged();
        setWidgetView();
        AppWidgetManager appWidgetManager =
                (AppWidgetManager) mHostActivity.getSystemService(Context.APPWIDGET_SERVICE);
        new Handler().postDelayed(() -> {
            if (appWidgetManager != null) {
                WidgetManager.updateMonthWidget(mHostActivity, appWidgetManager);
            }
        }, 1500);
    }

    private void toggleBackground(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            setBackground();
        } else {
            mContentView.setBackgroundColor(Color.parseColor("#727272"));
            if (mLastBackground != null && !mIsWallPagerFit) {
                mLastBackground.recycle();
            }
        }
        mSbWallPagerOffset.setEnabled(isChecked);
        mLoadWallPager = isChecked;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SYSTEM_ALERT_WINDOW) {
            new AlertDialog.Builder(mHostActivity)
                    .setMessage("请授予悬浮窗权限")
                    .setPositiveButton("设置", (dialog, which) -> PermissionUtils.toSettings(mHostActivity))
                    .show();
        } else {
            if (PermissionUtils.handleResults(permissions, grantResults)) {
                setBackground();
            } else {
                PermissionUtils.manual(mHostActivity);
            }
        }
    }

    private void setWidgetView() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        LunarDay lunarDay = LunarUtils.getLunar(calendar);
        int themeColor = Setting.TransparentWidget.trans_widget_theme_color;
        int layout = themeColor == 0 ? R.layout.month_widget : R.layout.month_widget_light;
        mWidgetView = LayoutInflater.from(mHostActivity).inflate(layout, null);
        ViewGroup container = findViewById(R.id.widget_container);
        container.removeAllViews();
        container.addView(mWidgetView);
        mWidgetView.setBackgroundColor(Color.argb(Setting.TransparentWidget.trans_widget_alpha, themeColor, themeColor, themeColor));
        ((TextView) findViewById(R.id.tv_lunar)).setText(lunarDay.getLunarDate());
        ((TextView) findViewById(R.id.tv_year)).setText(String.valueOf(calendar.get(Calendar.YEAR)));
        int month = calendar.get(Calendar.MONTH) + 1;
        ((TextView) findViewById(R.id.tv_date)).setText(String.format(Locale.CHINA, "%02d月", month));
        int year = calendar.get(Calendar.YEAR);
        int dayCount = DayUtils.getMonthDayCount(month, year);
        EventDao dao = EventDao.getInstance(mHostActivity);
        List<Day> days = new ArrayList<>();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            Day day = MonthUtils.generateDay(calendar, dao);
            days.add(day);
        }
        mAdapter = new MonthDayAdapter(days);
        ((GridView) findViewById(R.id.gv_month)).setAdapter(mAdapter);
    }

    private void setBackground() {
        if (!PermissionUtils.checkSelfPermission(this, READ_EXTERNAL_STORAGE, REQUEST_CODE_READ_EXTERNAL_STORAGE)) {
            return;
        }
        Display display = mHostActivity.getWindowManager().getDefaultDisplay();
        Point out = new Point();
        display.getRealSize(out);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mHostActivity);
        Drawable drawable = wallpaperManager.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            int offset = (bitmap.getWidth() - out.x) * mWallPagerOffset / 100;
            if (bitmap.getWidth() < out.x) {
                out.x = bitmap.getWidth();
                Log.i(TAG, "Bitmap width:" + bitmap.getWidth());
            }
            if (bitmap.getHeight() < out.y) {
                out.y = bitmap.getHeight();
                Log.i(TAG, "Bitmap height:" + bitmap.getHeight());
            }
            Bitmap background = Bitmap.createBitmap(bitmap, offset, 0, out.x, out.y);
            mContentView.setBackground(new BitmapDrawable(getResources(), background));
            if (mLastBackground != null && !mIsWallPagerFit) {
                mLastBackground.recycle();
            }
            mLastBackground = background;
            mIsWallPagerFit = bitmap.getWidth() == out.x;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Window window = mHostActivity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_wallpager_offset:
                mWallPagerOffset = progress;
                if (!mIsWallPagerFit && mLoadWallPager) {
                    setBackground();
                }
                break;
            case R.id.sb_alpha:
                Setting.TransparentWidget.trans_widget_alpha = progress;
                Setting.setting(mHostActivity, Global.SETTING_WIDGET_ALPHA, progress);
                int themeColor = Setting.TransparentWidget.trans_widget_theme_color;
                mWidgetView.setBackgroundColor(Color.argb(Setting.TransparentWidget.trans_widget_alpha, themeColor, themeColor, themeColor));
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.sb_alpha:
                AppWidgetManager appWidgetManager =
                        (AppWidgetManager) mHostActivity.getSystemService(Context.APPWIDGET_SERVICE);
                WidgetManager.updateMonthWidget(mHostActivity, appWidgetManager);
                break;
        }
    }
}
