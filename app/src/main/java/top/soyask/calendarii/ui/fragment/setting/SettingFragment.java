package top.soyask.calendarii.ui.fragment.setting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;


public class SettingFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int WAIT = 0;
    private static final int CANCEL = 1;
    private static final int UPDATE = 3;
    private static final int RESTART = 4;
    public static final String WEEK_SETTING = "week_setting";

    private Handler mHandler = new Handler(){
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
        SwitchCompat switchCompat = findViewById(R.id.sc_start);
        switchCompat.setChecked(Setting.date_offset == 1);
        switchCompat.setOnCheckedChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                removeFragment(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked && Setting.date_offset != 1){
            Setting.date_offset = 1;
            Setting.setting(getContext(), Global.SETTING_DATE_OFFSET,Setting.date_offset);
            mHandler.sendEmptyMessageDelayed(UPDATE,500);
        }else if(!isChecked && Setting.date_offset == 1){
            Setting.date_offset = 0;
            Setting.setting(getContext(), Global.SETTING_DATE_OFFSET,Setting.date_offset);
            mHandler.sendEmptyMessageDelayed(UPDATE,500);

        }
    }
}
