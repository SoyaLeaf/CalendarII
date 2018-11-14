package top.soyask.calendarii.ui.fragment.setting;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.GlobalData;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.activity.ZoomActivity;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.fragment.setting.birth.BirthFragment;
import top.soyask.calendarii.ui.fragment.setting.symbol.SymbolFragment;
import top.soyask.calendarii.ui.fragment.setting.theme.ThemeFragment;
import top.soyask.calendarii.ui.fragment.setting.widget.PicSetFragment;
import top.soyask.calendarii.ui.fragment.setting.widget.TransparentWidgetFragment;
import top.soyask.calendarii.ui.widget.WidgetManager;

import static android.content.Context.CLIPBOARD_SERVICE;


public class SettingFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, GlobalData.LoadCallBack {

    private static final int WAIT = 0;
    private static final int CANCEL = 1;
    private static final int UPDATE = 3;
    private static final int RESTART = 4;

    private Handler mHandler = new SettingHandler(this);

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
        setupSwitchReplenish();
        setupSwitchAnim();

        findViewById(R.id.rl_birth).setOnClickListener(this);
        findViewById(R.id.rl_theme).setOnClickListener(this);
        findViewById(R.id.rl_holiday).setOnClickListener(this);
        findViewById(R.id.rl_widget_pic).setOnClickListener(this);
        findViewById(R.id.rl_ui).setOnClickListener(this);
        findViewById(R.id.rl_symbol).setOnClickListener(this);
        findViewById(R.id.rl_trans_widget).setOnClickListener(this);
    }

    private void setupSwitchReplenish() {
        SwitchCompat switchCompat = findViewById(R.id.sc_replenish);
        switchCompat.setChecked(Setting.replenish);
        switchCompat.setOnCheckedChangeListener(this);
    }

    private void setupSwitchAnim() {
        SwitchCompat switchCompat = findViewById(R.id.sc_anim);
        switchCompat.setChecked(Setting.select_anim);
        switchCompat.setOnCheckedChangeListener(this);
    }

    private void setupSwitchStart() {
        SwitchCompat switchCompat = findViewById(R.id.sc_start);
        switchCompat.setChecked(Setting.date_offset == 1);
        switchCompat.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.rl_widget_pic:
                PicSetFragment picSetFragment = PicSetFragment.newInstance();
                addFragment(picSetFragment);
                break;
            case R.id.rl_ui:
                Intent intent = new Intent(mHostActivity, ZoomActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_symbol:
                SymbolFragment symbolFragment = SymbolFragment.newInstance();
                addFragment(symbolFragment);
                break;
            case R.id.rl_trans_widget:
                Window window = mHostActivity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                TransparentWidgetFragment widgetFragment = TransparentWidgetFragment.newInstance();
                addFragment(widgetFragment, R.anim.fade_in, R.anim.out_slide);
                break;
            default:
                removeFragment(this);
        }
    }

    protected void addFragment(Fragment fragment, @AnimatorRes @AnimRes int in, @AnimatorRes @AnimRes int out) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(in, out, in, out)
                .add(R.id.main, fragment)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    private void synHoliday() {
        mHandler.sendEmptyMessage(WAIT);
        GlobalData.synHoliday(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sc_start:
                if (isChecked && Setting.date_offset != 1) {
                    Setting.date_offset = 1;
                    Setting.setting(mHostActivity, Global.SETTING_DATE_OFFSET, Setting.date_offset);
                    mHandler.sendEmptyMessageDelayed(UPDATE, 500);
                    WidgetManager.updateAllWidget(mHostActivity);
                } else if (!isChecked && Setting.date_offset == 1) {
                    Setting.date_offset = 0;
                    Setting.setting(mHostActivity, Global.SETTING_DATE_OFFSET, Setting.date_offset);
                    mHandler.sendEmptyMessageDelayed(UPDATE, 500);
                    WidgetManager.updateAllWidget(mHostActivity);
                }
                break;
            case R.id.sc_replenish:
                if (isChecked != Setting.replenish) {
                    Setting.replenish = isChecked;
                    Setting.setting(mHostActivity, Global.SETTING_REPLENISH, isChecked);
                    mHostActivity.sendBroadcast(new Intent(MonthFragment.UPDATE_UI));
                }
                break;

            case R.id.sc_anim:
                if (isChecked != Setting.select_anim) {
                    Setting.select_anim = isChecked;
                    Setting.setting(mHostActivity, Global.SETTING_SELECT_ANIM, isChecked);
                    mHostActivity.sendBroadcast(new Intent(MonthFragment.UPDATE_UI));
                }
                break;
        }

    }

    @Override
    public void onSuccess() {
        showSnackbar("同步成功！");
        mHostActivity.sendBroadcast(new Intent(MonthFragment.UPDATE_EVENT));
        Setting.setting(mHostActivity, Global.SETTING_HOLIDAY, new HashSet<>(GlobalData.HOLIDAY));
        Setting.setting(mHostActivity, Global.SETTING_WORKDAY, new HashSet<>(GlobalData.WORKDAY));
        mHandler.sendEmptyMessage(CANCEL);
    }

    @Override
    public void onFail(String error) {
        showSnackbar("同步失败了，请联系开发者！", "复制错误信息", v -> {
            ClipboardManager manager = (ClipboardManager) mHostActivity.getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("exception", error);
            manager.setPrimaryClip(clipData);
            Toast.makeText(mHostActivity, "信息已复制", Toast.LENGTH_SHORT).show();
        });
        mHandler.sendEmptyMessage(CANCEL);
    }

    private static class SettingHandler extends Handler {
        private ProgressDialog mProgressDialog;

        private WeakReference<BaseFragment> mFragment;

        private SettingHandler(BaseFragment fragment) {
            this.mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseFragment fragment = mFragment.get();
            try {
                switch (msg.what) {
                    case UPDATE:
                        fragment.getActivity().sendBroadcast(new Intent(MonthFragment.WEEK_SETTING));
                        break;
                    case WAIT:
                        mProgressDialog = ProgressDialog.show(fragment.getContext(), null, "请稍等...");
                        break;
                    case CANCEL:
                        if (mProgressDialog != null) {
                            mProgressDialog.cancel();
                            mProgressDialog = null;
                        }
                        break;
                }
            } catch (NullPointerException ignored) {
            }
        }
    }
}
