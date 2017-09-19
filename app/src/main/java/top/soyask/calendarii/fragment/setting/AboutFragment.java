package top.soyask.calendarii.fragment.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import c.b.BP;
import c.b.PListener;
import top.soyask.calendarii.R;
import top.soyask.calendarii.fragment.base.BaseFragment;
import top.soyask.calendarii.fragment.setting.theme.ThemeFragment;


public class AboutFragment extends BaseFragment implements View.OnClickListener {

    private ExpandableLayout mExpandableLayout;

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
        findToolbar(R.id.toolbar).setNavigationOnClickListener(this);
//        findViewById(R.id.rl_qr).setOnClickListener(this);
        findViewById(R.id.rl_email).setOnClickListener(this);
        findViewById(R.id.rl_color).setOnClickListener(this);
//        findViewById(R.id.btn_one).setOnClickListener(this);
//        findViewById(R.id.btn_two).setOnClickListener(this);
//        findViewById(R.id.btn_three).setOnClickListener(this);
        findViewById(R.id.rl_score).setOnClickListener(this);
        mExpandableLayout = findViewById(R.id.el);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.rl_qr:
//                mExpandableLayout.toggle();
//                break;
            case R.id.rl_color:
                setupTheme();
                break;
            case R.id.rl_email:
                copy();
                break;
            case R.id.rl_score:
                score();
                break;
//            case R.id.btn_one:
//                give(0.5f);
//                break;
//            case R.id.btn_two:
//                give(1.2f);
//                break;
//            case R.id.btn_three:
//                give(2.5f);
//                break;
            default:
                removeFragment(this);
                break;
        }
    }

    private void score() {

        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "您的系统中没有安装应用市场", Toast.LENGTH_SHORT).show();
        }
    }

    private static final String APP_ID = "25553c3637dd2ff500393d901326446a";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BP.init(APP_ID);
    }

    private void give(float money) {
        try {
            Toast.makeText(getMainActivity(), "正在打开支付界面...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.bmob.app.sport", "com.bmob.app.sport.wxapi.BmobActivity");
            intent.setComponent(cn);
            startActivity(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        BP.pay("您的支持是我最大的动力。谢谢orz", "日历打赏", money, true, new PListener() {
            // 因为网络等原因,支付结果未知(小概率事件),出于保险起见稍后手动查询
            @Override
            public void unknow() {
                Toast.makeText(getMainActivity(), "支付结果未知,请稍后手动查询", Toast.LENGTH_SHORT)
                        .show();
            }

            // 支付成功,如果金额较大请手动查询确认
            @Override
            public void succeed() {
                Toast.makeText(getMainActivity(), "谢谢您的支持!", Toast.LENGTH_SHORT).show();
            }

            // 无论成功与否,返回订单号
            @Override
            public void orderId(String orderId) {
            }

            // 支付失败,原因可能是用户中断支付操作,也可能是网络原因
            @Override
            public void fail(int code, String reason) {

                // 当code为-2,意味着用户中断了操作
                // code为-3意味着没有安装BmobPlugin插件
                if (code == -3) {
                    Toast.makeText(getMainActivity(),
                            "支付失败，但还是谢谢您。",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getMainActivity(), "支付失败，但还是谢谢您。", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });
    }

    private static final String[] THEME = {
            "原色", "酷安绿", "哔哩粉", "水鸭青",
            "知乎蓝", "高端黑", "基佬紫", "中国红"
    };

    private void setupTheme() {
        ThemeFragment themeFragment = ThemeFragment.newInstance();
        addFragment(themeFragment);
    }


    private void copy() {
        ClipboardManager clipboardManager = (ClipboardManager) getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("日历", "leaf0x520@gmail.com");
        clipboardManager.setPrimaryClip(data);
        showSnackbar("邮箱地址已经复制到剪切板。");
    }

}
