package top.soyask.calendarii.fragment.setting.theme;

import android.os.Bundle;
import android.view.View;

import top.soyask.calendarii.R;
import top.soyask.calendarii.fragment.base.BaseFragment;


public class CustomColorFragment extends BaseFragment implements View.OnClickListener {

    public CustomColorFragment() {
        super(R.layout.fragment_custom);
    }

    public static CustomColorFragment newInstance() {
        CustomColorFragment fragment = new CustomColorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {

        findToolbar(R.id.toolbar).setNavigationOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                removeFragment(this);
                break;
        }
    }
}
