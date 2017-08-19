package top.soyask.calendarii.fragment;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import net.cachapa.expandablelayout.ExpandableLayout;

import top.soyask.calendarii.R;
import top.soyask.calendarii.fragment.base.BaseFragment;


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
        findViewById(R.id.rl_qr).setOnClickListener(this);
        findViewById(R.id.rl_email).setOnClickListener(this);
        findViewById(R.id.rl_info).setOnClickListener(this);
        findViewById(R.id.btn_qr).setOnClickListener(this);
        findViewById(R.id.iv_qr).setOnClickListener(this);
        mExpandableLayout = findViewById(R.id.el);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_qr:
                mExpandableLayout.toggle();
                break;
            case R.id.rl_info:
                new AlertDialog.Builder(getMainActivity())
                        .setMessage("亲，小秘密自己去发掘哦。有什么意见或者建议可以发送邮件告诉我哦！")
                        .show();
                break;
            case R.id.rl_email:
                copy();
                break;
            case R.id.btn_qr:
            case R.id.iv_qr:
                saveImage();
                break;
            default:
                removeFragment(this);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage();
        }
    }

    private void saveImage() {
        int checkSelfPermission = ContextCompat.checkSelfPermission(getMainActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            new Thread(){
                @Override
                public void run() {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qrcode);
                    String uri = MediaStore.Images.Media.insertImage(getMainActivity().getContentResolver(), bitmap, "qrcode", null);
                    getMainActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(uri)));
                }
            }.start();
            showSnackbar("二维码已经保存到您的相册中,打开支付宝扫一扫吧。谢谢支持 orz");
        } else {
            ActivityCompat.requestPermissions(getMainActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
        }

    }

    private void copy() {
        ClipboardManager clipboardManager = (ClipboardManager) getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("日历", "leaf0x520@gmail.com");
        clipboardManager.setPrimaryClip(data);
        showSnackbar("邮箱地址已经复制到剪切板。");
    }


}
