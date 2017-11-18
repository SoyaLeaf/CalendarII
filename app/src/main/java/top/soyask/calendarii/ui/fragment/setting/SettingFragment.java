package top.soyask.calendarii.ui.fragment.setting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.HashSet;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.GlobalData;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.setting.birth.BirthFragment;
import top.soyask.calendarii.ui.fragment.setting.theme.ThemeFragment;
import top.soyask.calendarii.ui.fragment.setting.widget.AlphaSetFragment;
import top.soyask.calendarii.ui.widget.WidgetManager;


public class SettingFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, AlphaSetFragment.OnAlphaSetListener, GlobalData.LoadCallBack {

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
                case WAIT:
                    mProgressDialog = ProgressDialog.show(getMainActivity(), null, "请稍等...");
                    break;
                case CANCEL:
                    if(mProgressDialog != null){
                        mProgressDialog.cancel();
                        mProgressDialog = null;
                    }
                    break;
            }
        }
    };
    private TextView mTvAlpha;
    private ProgressDialog mProgressDialog;

    public SettingFragment() {
        super(R.layout.fragment_setting);
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar().setNavigationOnClickListener(this);
        setupSwitchStart();
        setupWidgetAlpha();
        findViewById(R.id.rl_theme).setOnClickListener(this);
        findViewById(R.id.rl_holiday).setOnClickListener(this);
    }

    private void setupWidgetAlpha() {
        mTvAlpha = findViewById(R.id.tv_alpha);
        mTvAlpha.setText(String.valueOf(Setting.widget_alpha));
        findViewById(R.id.rl_alpha).setOnClickListener(this);
        findViewById(R.id.rl_birth).setOnClickListener(this);
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
            case R.id.rl_theme:
                ThemeFragment themeFragment = ThemeFragment.newInstance();
                addFragment(themeFragment);
                break;
            case R.id.rl_birth:
                BirthFragment birthFragment = BirthFragment.newInstance();
                addFragment(birthFragment);
                break;
            case R.id.rl_holiday:
                synHoliday();
                break;
            default:
                removeFragment(this);
        }
    }

    private void synHoliday() {
        mHandler.sendEmptyMessage(WAIT);
        GlobalData.synHoliday(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked && Setting.date_offset != 1) {
            Setting.date_offset = 1;
            Setting.setting(getContext(), Global.SETTING_DATE_OFFSET, Setting.date_offset);
            mHandler.sendEmptyMessageDelayed(UPDATE, 500);
            WidgetManager.updateMonthWidget(getMainActivity());
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
        WidgetManager.updateMonthWidget(getMainActivity());
    }


    @Override
    public void onSuccess() {
        showSnackbar("同步成功！");
        getMainActivity().sendBroadcast(new Intent(EventDao.UPDATE));
        Setting.setting(getMainActivity(),Global.SETTING_HOLIDAY, new HashSet<>(GlobalData.HOLIDAY));
        mHandler.sendEmptyMessage(CANCEL);
    }

    @Override
    public void onFail() {
        showSnackbar("同步失败了，请联系开发者！");
        mHandler.sendEmptyMessage(CANCEL);
    }
}
