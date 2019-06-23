package top.soyask.calendarii.ui.fragment.about;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import java.net.URISyntaxException;

import top.soyask.calendarii.R;
import top.soyask.calendarii.ui.fragment.about.opensource.OpenSourceFragment;
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
        findViewById(R.id.btn_os).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_os:
                OpenSourceFragment openSourceFragment = OpenSourceFragment.newInstance();
                addFragment(openSourceFragment);
                break;
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
