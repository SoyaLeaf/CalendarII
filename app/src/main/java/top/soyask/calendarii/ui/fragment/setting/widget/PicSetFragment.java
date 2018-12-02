package top.soyask.calendarii.ui.fragment.setting.widget;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.widget.WidgetManager;

public class PicSetFragment extends BaseFragment implements View.OnClickListener {


    private static final int GET_IMAGE = 0x001;
    private static final int REQUEST_CODE_PERMISSION_GET = 12;
    private ImageView mImageView;

    public PicSetFragment() {
        super(R.layout.fragment_pic_set);
    }

    public static PicSetFragment newInstance() {
        PicSetFragment fragment = new PicSetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar().setNavigationOnClickListener(this);
        findViewById(R.id.btn_change).setOnClickListener(this);
        findViewById(R.id.btn_default).setOnClickListener(this);
        mImageView = findViewById(R.id.iv);
        mImageView.setOnClickListener(this);
        if (Setting.white_widget_pic != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(Setting.white_widget_pic);
            mImageView.setImageBitmap(bitmap);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change:
            case R.id.iv:
                select();
                break;
            case R.id.btn_default:
                changeToDefault();
                break;
            default:
                removeFragment(this);
                break;
        }

    }

    private void changeToDefault() {
        Setting.remove(mHostActivity, Global.SETTING_WHITE_WIDGET_PIC);
        Setting.white_widget_pic = null;
        mImageView.setImageDrawable(getResources().getDrawable(R.mipmap.miku));
        AppWidgetManager appWidgetManager =
                (AppWidgetManager) mHostActivity.getSystemService(Context.APPWIDGET_SERVICE);
        WidgetManager.updateWhiteWidget(mHostActivity, appWidgetManager);
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
                Bitmap bitmap = loadImage(mHostActivity, uri);
                mImageView.setImageBitmap(bitmap);
                setupBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                showSnackbar("图片加载出错了...");
            }
        }
    }

    private void setupBitmap(Bitmap bitmap) throws FileNotFoundException {
        File filesDir = mHostActivity.getFilesDir();
        Setting.white_widget_pic = filesDir + File.separator + Global.SETTING_WHITE_WIDGET_PIC + ".png";
        FileOutputStream fos = new FileOutputStream(Setting.white_widget_pic);
        boolean compress = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        if (compress) {
            Setting.setting(mHostActivity, Global.SETTING_WHITE_WIDGET_PIC, Setting.white_widget_pic);
            AppWidgetManager appWidgetManager =
                    (AppWidgetManager) mHostActivity.getSystemService(Context.APPWIDGET_SERVICE);
            WidgetManager.updateWhiteWidget(mHostActivity, appWidgetManager);
        }
    }

    private void select() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mHostActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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

        if (width > height) {
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
        opts.inSampleSize = scale + 1;
        opts.inJustDecodeBounds = false;
        in = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, opts);
        in.close();
        return bitmap;
    }

}
