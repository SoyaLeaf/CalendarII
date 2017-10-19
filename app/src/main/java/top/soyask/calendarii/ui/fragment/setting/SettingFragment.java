package top.soyask.calendarii.ui.fragment.setting;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.setting.widget.AlphaSetFragment;
import top.soyask.calendarii.ui.widget.MonthWidget;


public class SettingFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AlphaSetFragment.OnAlphaSetListener {

    private static final int WAIT = 0;
    private static final int CANCEL = 1;
    private static final int UPDATE = 3;
    private static final int RESTART = 4;
    public static final String WEEK_SETTING = "week_setting";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE:
                    getMainActivity().sendBroadcast(new Intent(WEEK_SETTING));
                    break;
            }
        }
    };
    private TextView mTvAlpha;

    public SettingFragment() {
        super(R.layout.fragment_setting);
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar().setNavigationOnClickListener(this);
        setupSwitchStart();
        setupWidgetAlpha();
    }

    private void setupWidgetAlpha() {
        mTvAlpha = findViewById(R.id.tv_alpha);
        mTvAlpha.setText(String.valueOf(Setting.widget_alpha));
        findViewById(R.id.rl_alpha).setOnClickListener(this);
    }

    private void setupSwitchStart() {
        SwitchCompat switchCompat = findViewById(R.id.sc_start);
        switchCompat.setChecked(Setting.date_offset == 1);
        switchCompat.setOnCheckedChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTvAlpha.setText(Setting.widget_alpha+"");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_alpha:
                AlphaSetFragment alphaSetFragment = AlphaSetFragment.newInstance();
                alphaSetFragment.setOnAlphaSetListener(this);
                addFragment(alphaSetFragment);
                break;
            default:
                removeFragment(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && Setting.date_offset != 1) {
            Setting.date_offset = 1;
            Setting.setting(getContext(), Global.SETTING_DATE_OFFSET, Setting.date_offset);
            mHandler.sendEmptyMessageDelayed(UPDATE, 500);
        } else if (!isChecked && Setting.date_offset == 1) {
            Setting.date_offset = 0;
            Setting.setting(getContext(), Global.SETTING_DATE_OFFSET, Setting.date_offset);
            mHandler.sendEmptyMessageDelayed(UPDATE, 500);
        }
    }

    @Override
    public void onAlphaSet(int alpha) {
        Setting.widget_alpha = alpha;
        Setting.setting(getMainActivity(), Global.SETTING_WIDGET_ALPHA, alpha);
        mTvAlpha.setText(String.valueOf(alpha));
        updateWidget();
    }

    private void updateWidget() {
        AppWidgetManager appWidgetManager =
                (AppWidgetManager) getMainActivity().getSystemService(Context.APPWIDGET_SERVICE);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(getMainActivity(), MonthWidget.class));
        if (appWidgetIds != null) {
            for (int appWidgetId : appWidgetIds) {
                MonthWidget.updateAppWidget(getMainActivity(), appWidgetManager, appWidgetId);
            }
        }
    }
}
