package top.soyask.calendarii.ui.fragment.setting.theme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.lang.ref.WeakReference;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.activity.LaunchActivity;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;


public class ThemeFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int[] RADIO_BUTTON = {
            R.id.rb_default, R.id.rb_green, R.id.rb_pink, R.id.rb_teal,
            R.id.rb_blue, R.id.rb_red, R.id.rb_purple, R.id.rb_black,
    };

    private static final int WAIT = 0;
    private static final int CANCEL = 1;
    private static final int UPDATE = 3;

    private int mCurrentTheme;
    private ProgressDialog mProgressDialog;

    private Handler mHandler = new ThemeHandler(this);

    public ThemeFragment() {
        super(R.layout.fragment_theme);
    }


    public static ThemeFragment newInstance() {
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar(R.id.toolbar).setNavigationOnClickListener(this);
        SharedPreferences setting = mHostActivity.getSharedPreferences("setting", Context.MODE_PRIVATE);
        mCurrentTheme = setting.getInt("theme", 0);
        RadioButton rb = findViewById(RADIO_BUTTON[mCurrentTheme]);
        rb.setChecked(true);
        for (int i = 0; i < RADIO_BUTTON.length; i++) {
            RadioButton radioButton = findViewById(RADIO_BUTTON[i]);
            radioButton.setOnCheckedChangeListener(this);
        }
    }

    private void setupTheme(int theme) {
        SharedPreferences.Editor setting = mHostActivity.getSharedPreferences("setting", Context.MODE_PRIVATE).edit();
        setting.putInt("theme", theme).apply();
        Setting.theme = theme;
        Intent intent = new Intent(mHostActivity, LaunchActivity.class);
        startActivity(intent);
        mHostActivity.finish();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                removeFragment(this);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (RADIO_BUTTON[mCurrentTheme] != buttonView.getId() && isChecked) {
            for (int i = 0; i < RADIO_BUTTON.length; i++) {
                if (buttonView.getId() == RADIO_BUTTON[i]) {
                    final int finalI = i;
                    new Thread() {
                        @Override
                        public void run() {
                            Message obtain = Message.obtain();
                            obtain.what = UPDATE;
                            obtain.arg1 = finalI;
                            mHandler.sendMessage(obtain);
                        }
                    }.start();
                    break;
                }
            }
        }
    }


    public static class ThemeHandler extends Handler{

        WeakReference<ThemeFragment> mFragment;

        ThemeHandler(ThemeFragment fragment){
            mFragment = new WeakReference<>(fragment);
        }
        @Override
        public void handleMessage(Message msg) {
            ThemeFragment fragment = mFragment.get();
            if(fragment == null){
                return;
            }
            switch (msg.what) {
                case WAIT:
                    fragment.mProgressDialog = ProgressDialog.show(fragment.mHostActivity, null, "请稍等...", true);
                    break;
                case CANCEL:
                    if (fragment.mProgressDialog != null) {
                        fragment.mProgressDialog.dismiss();
                        fragment.mProgressDialog = null;
                    }
                    break;
                case UPDATE:
                    fragment.setupTheme(msg.arg1);
                    break;
            }
        }
    }
}
