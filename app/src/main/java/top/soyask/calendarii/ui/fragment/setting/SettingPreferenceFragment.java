package top.soyask.calendarii.ui.fragment.setting;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.GlobalData;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.activity.ZoomActivity;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;
import top.soyask.calendarii.ui.fragment.setting.symbol.SymbolFragment;
import top.soyask.calendarii.ui.fragment.setting.theme.ThemeFragment;
import top.soyask.calendarii.ui.fragment.setting.widget.PicSetFragment;
import top.soyask.calendarii.ui.fragment.setting.widget.TransparentWidgetFragment;
import top.soyask.calendarii.ui.widget.WidgetManager;

import static android.content.Context.CLIPBOARD_SERVICE;


public class SettingPreferenceFragment extends PreferenceFragmentCompat
        implements GlobalData.LoadCallBack, Preference.OnPreferenceClickListener {

    private static final int WAIT = 0;
    private static final int CANCEL = 1;
    private static final int UPDATE = 3;
    private static final int RESTART = 4;

    private Handler mHandler = new SettingHandler(this);

    public SettingPreferenceFragment() {
    }

    public static SettingPreferenceFragment newInstance() {
        SettingPreferenceFragment fragment = new SettingPreferenceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View parent = inflater.inflate(R.layout.fragment_setting, container, false);
        ((Toolbar) parent.findViewById(R.id.toolbar)).setNavigationOnClickListener(
                v -> getFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss());
        FrameLayout layout = parent.findViewById(R.id.prefs);
        layout.addView(view);
        getListView().setScrollBarSize(0);
        return parent;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_setting, rootKey);
        setupUI();
    }

    private void setupUI() {
        setupSwitchStart();
        setupSwitchReplenish();
        setupSwitchAnim();
        findPreference("pref_category_normal").setIcon(null);
        findPreference("pref_category_normal").setIconSpaceReserved(false);
        findPreference("pref_category_widget").setIconSpaceReserved(false);
        initPreference("pref_theme");
        initPreference("pref_symbol");
        initPreference("pref_custom_ui");
        initPreference("pref_holiday");
        initPreference("pref_trans_widget");
        initPreference("pref_widget_pic");
    }

    private void initPreference(String key) {
        Preference preference = findPreference(key);
//        preference.setIconSpaceReserved(false);
        preference.setOnPreferenceClickListener(this);
    }

    private void setupSwitchStart() {
        SwitchPreference preference = (SwitchPreference) findPreference("pref_monday_start");
//        preference.setIconSpaceReserved(false);
        preference.setChecked(Setting.date_offset == 1);
        preference.setOnPreferenceChangeListener((pref, newValue) -> {
            boolean isChecked = (boolean) newValue;
            if (isChecked && Setting.date_offset != 1) {
                Setting.date_offset = 1;
                Setting.setting(getActivity(), Global.SETTING_DATE_OFFSET, Setting.date_offset);
                mHandler.sendEmptyMessageDelayed(UPDATE, 500);
                WidgetManager.updateAllWidget(getActivity());
            } else if (!isChecked && Setting.date_offset == 1) {
                Setting.date_offset = 0;
                Setting.setting(getActivity(), Global.SETTING_DATE_OFFSET, Setting.date_offset);
                mHandler.sendEmptyMessageDelayed(UPDATE, 500);
                WidgetManager.updateAllWidget(getActivity());
            }
            preference.setChecked(isChecked);
            return false;
        });
    }

    private void setupSwitchReplenish() {
        SwitchPreference preference = (SwitchPreference) findPreference("pref_fill_date");
//        preference.setIconSpaceReserved(false);
        preference.setChecked(Setting.replenish);
        preference.setOnPreferenceChangeListener((pref, newValue) -> {
            boolean isChecked = (boolean) newValue;
            if (isChecked != Setting.replenish) {
                Setting.replenish = isChecked;
                Setting.setting(getActivity(), Global.SETTING_REPLENISH, isChecked);
                getActivity().sendBroadcast(new Intent(MonthFragment.UPDATE_UI));
            }
            preference.setChecked(isChecked);
            return false;
        });
    }

    private void setupSwitchAnim() {
        SwitchPreference preference = (SwitchPreference) findPreference("pref_select_anim");
//        preference.setIconSpaceReserved(false);
        preference.setChecked(Setting.select_anim);
        preference.setOnPreferenceChangeListener((pref, newValue) -> {
            boolean isChecked = (boolean) newValue;
            if (isChecked != Setting.select_anim) {
                Setting.select_anim = isChecked;
                Setting.setting(getActivity(), Global.SETTING_SELECT_ANIM, isChecked);
                getActivity().sendBroadcast(new Intent(MonthFragment.UPDATE_UI));
            }
            preference.setChecked(isChecked);
            return false;
        });
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "pref_theme":
                ThemeFragment themeFragment = ThemeFragment.newInstance();
                addFragment(themeFragment, R.anim.fade_in, R.anim.out_slide);
                break;
            case "pref_symbol":
                SymbolFragment symbolFragment = SymbolFragment.newInstance();
                addFragment(symbolFragment, R.anim.fade_in, R.anim.out_slide);
                break;
            case "pref_custom_ui":
                Intent intent = new Intent(getActivity(), ZoomActivity.class);
                startActivity(intent);
                break;
            case "pref_holiday":
                synHoliday();
                break;
            case "pref_trans_widget":
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                TransparentWidgetFragment widgetFragment = TransparentWidgetFragment.newInstance();
                addFragment(widgetFragment, R.anim.fade_in, R.anim.out_slide);
                break;
            case "pref_widget_pic":
                PicSetFragment picSetFragment = PicSetFragment.newInstance();
                addFragment(picSetFragment, R.anim.fade_in, R.anim.out_slide);
                break;
            default:
        }
        return false;
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
    public void onSuccess() {
        Snackbar.make(getView(), R.string.sync_success, Snackbar.LENGTH_SHORT).show();
        getActivity().sendBroadcast(new Intent(MonthFragment.UPDATE_EVENT));
        Setting.setting(getActivity(), Global.SETTING_HOLIDAY, new HashSet<>(GlobalData.HOLIDAY));
        Setting.setting(getActivity(), Global.SETTING_WORKDAY, new HashSet<>(GlobalData.WORKDAY));
        mHandler.sendEmptyMessage(CANCEL);
    }

    @Override
    public void onFail(String error) {
        Snackbar.make(getView(), getString(R.string.sync_fail_and_contact_developer), Snackbar.LENGTH_SHORT)
                .setAction(R.string.copy_error_message, v -> {
                    ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("exception", error);
                    manager.setPrimaryClip(clipData);
                    Toast.makeText(getActivity(), getString(R.string.message_is_copied), Toast.LENGTH_SHORT).show();
                })
                .show();
        mHandler.sendEmptyMessage(CANCEL);
    }


    private static class SettingHandler extends Handler {
        private ProgressDialog mProgressDialog;

        private WeakReference<SettingPreferenceFragment> mFragment;

        private SettingHandler(SettingPreferenceFragment fragment) {
            this.mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SettingPreferenceFragment fragment = mFragment.get();
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
