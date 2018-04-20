package top.soyask.calendarii.ui.fragment.setting.widget;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.InputStream;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;

public class AlphaSetFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, View.OnTouchListener {

    private static final int GET_IMAGE = 0;
    private static final int REQUEST_CODE_PERMISSION_GET = 12;

    private OnAlphaSetListener mOnAlphaSetListener;
    private View mViewAlpha;
    private View mViewBg;
    private SeekBar mSeekBar;

    public AlphaSetFragment() {
        super(R.layout.fragment_alpha);
    }

    public static AlphaSetFragment newInstance() {
        AlphaSetFragment fragment = new AlphaSetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnAlphaSetListener(OnAlphaSetListener onAlphaSetListener) {
        this.mOnAlphaSetListener = onAlphaSetListener;
    }

    @Override
    protected void setupUI() {
        mViewAlpha = findViewById(R.id.view_alpha);
        mViewAlpha.setOnTouchListener(this);

        findToolbar().setNavigationOnClickListener(this);
        setupSeekBar();
        mViewBg = findViewById(R.id.rl_bg);
        mViewBg.setOnClickListener(this);
    }

    private void setupSeekBar() {
        mSeekBar = findViewById(R.id.sb);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(255);
        mSeekBar.setProgress(Setting.widget_alpha);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mViewAlpha.setBackgroundColor(Color.argb(progress,0,0,0));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mOnAlphaSetListener.onAlphaSet(mSeekBar.getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_bg:
                select();
                break;
            default:
                removeFragment(this);
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        select();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = loadImage(mHostActivity,uri);
                findViewById(R.id.rl_bg).setBackground(new BitmapDrawable(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
                showSnackbar("图片加载出错了...");
            }
        }
    }

    private void select() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(mHostActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_GET);
                return;
            }
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GET_IMAGE);
    }


    public static Bitmap loadImage(Context context, Uri uri) throws IOException {
        InputStream in = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, opts);
        in.close();
        int height = opts.outHeight;
        int width = opts.outWidth;

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        if(width > height){
            int t = width;
            width = height;
            height = t;
        }

        int scaleX = (int) Math.ceil((1.0 * width) / point.x);
        int scaleY = (int) Math.ceil((1.0 * height) / point.y);

        int scale = 1;
        if (scaleX > 1 && scaleX > scaleY) {
            scale = scaleX;
        }
        if (scaleY > 1 && scaleY >= scaleX) {
            scale = scaleY;
        }
        opts.inSampleSize = scale+1;
        opts.inJustDecodeBounds = false;
        in = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, opts);
        in.close();
        return bitmap;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float y = event.getRawY() - v.getBottom() *2;
                v.setY(y);
                break;
        }
        return true;
    }


    public interface OnAlphaSetListener {
        void onAlphaSet(int alpha);
    }
}
