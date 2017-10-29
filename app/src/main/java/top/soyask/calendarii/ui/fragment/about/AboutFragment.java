package top.soyask.calendarii.ui.fragment.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import top.soyask.calendarii.R;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;


public class AboutFragment extends BaseFragment implements View.OnClickListener {

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar().setNavigationOnClickListener(this);
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
