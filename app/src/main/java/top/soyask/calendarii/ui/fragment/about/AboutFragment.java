package top.soyask.calendarii.ui.fragment.about;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import java.net.URISyntaxException;

import top.soyask.calendarii.R;
import top.soyask.calendarii.ui.fragment.about.opensource.OpenSourceFragment;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;


public class AboutFragment extends BaseFragment implements View.OnClickListener {

    private static final String INTENT_FULL_URL = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX01613AS644I1LR9US96%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" +
            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";

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
        findViewById(R.id.btn_donate).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_os:
                OpenSourceFragment openSourceFragment = OpenSourceFragment.newInstance();
                addFragment(openSourceFragment);
                break;
            case R.id.btn_donate:
                donate();
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


    private void donate() {
        new AlertDialog.Builder(mHostActivity)
                .setTitle(R.string.donate)
                .setMessage(R.string.thank_you)
                .setNegativeButton(R.string.not_interested, (dialog, which) -> Toast.makeText(mHostActivity, R.string.however_thanks, Toast.LENGTH_SHORT).show())
                .setPositiveButton(R.string.donate_little, (dialog, which) -> {
                    try {
                        toDonate();
                        Toast.makeText(mHostActivity, R.string.thanks_very_much, Toast.LENGTH_SHORT).show();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }).show();
    }

    private void toDonate() throws URISyntaxException {
        Intent intent = Intent.parseUri(INTENT_FULL_URL, Intent.URI_INTENT_SCHEME);
        startActivity(intent);
    }

}
