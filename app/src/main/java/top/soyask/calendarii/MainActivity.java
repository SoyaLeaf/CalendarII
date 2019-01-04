package top.soyask.calendarii;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import top.soyask.calendarii.database.dao.BirthdayDao;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.entity.Backup;
import top.soyask.calendarii.global.GlobalData;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.main.MainFragment;
import top.soyask.calendarii.utils.BackupUtils;

public class MainActivity extends AppCompatActivity {

    public static final int[] THEMES = {
            R.style.AppTheme,
            R.style.AppTheme_Green,
            R.style.AppTheme_Pink,
            R.style.AppTheme_Teal,
            R.style.AppTheme_Blue,
            R.style.AppTheme_Red,
            R.style.AppTheme_Purple,
            R.style.AppTheme_Black,
            R.style.AppTheme_Red
    };
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkAndUpdateDpi();
        setupTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            new InitTask(this).execute();
        }
        importBackup();
        checkCrash();
    }

    private void checkCrash() {
        File file = new File(getCacheDir().getPath() + File.separator + "crash");
        if (file.exists()) {
            new AlertDialog.Builder(this)
                    .setMessage("检测到您的App奔溃了，是否上报错误信息？")
                    .setPositiveButton("复制并上报", (DialogInterface dialog, int which) -> {
                        copyAndSend(file);
                    })
                    .setNegativeButton("取消", null)
                    .setOnDismissListener(dialog -> file.delete())
                    .show();
        }
    }

    private void copyAndSend(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))
        ) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            String info = builder.toString();
            copy(info);
            launchMarket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copy(String info) {
        ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("crash", info);
        manager.setPrimaryClip(clipData);
    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_market, Toast.LENGTH_SHORT).show();
        }
    }

    private void importBackup() {
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                String path = uri.getPath();
                String[] split = path.split("/");
                String filename = split[split.length - 1];
                showBackupDialog(uri, filename);
            }
        }
    }

    private void showBackupDialog(Uri uri, String filename) {
        new AlertDialog.Builder(this)
                .setMessage("是否导入" + filename)
                .setPositiveButton("导入", (dialog, which) -> insertBackupData(uri))
                .setNegativeButton("覆盖原本数据", (dialog, which) -> {
                    EventDao.getInstance(MainActivity.this).deleteAll();
                    BirthdayDao.getInstance(MainActivity.this).deleteAll();
                    insertBackupData(uri);
                })
                .show();
    }

    private void insertBackupData(Uri uri) {
        Backup backup = BackupUtils.load(getContentResolver(), uri);
        if (backup == null) {
            Toast.makeText(MainActivity.this, "无法导入，错误的文件格式", Toast.LENGTH_SHORT).show();
            return;
        }
        BackupUtils.insertBackupData(backup, this);
        GlobalData.loadBirthday(MainActivity.this);
        Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkAndUpdateDpi() {
        if (Setting.density_dpi != -1) {
            Resources resources = getResources();
            Configuration configuration = new Configuration();
            configuration.setToDefaults();
            configuration.densityDpi = Setting.density_dpi;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
    }

    private void setupTheme() {
        int theme = THEMES[Setting.theme];
        setTheme(theme);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void init() {
        MainFragment mainFragment = MainFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, mainFragment)
                .commitAllowingStateLoss();
    }

    private static class InitTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> mActivity;

        InitTask(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                GlobalData.loadBirthday(activity);
                GlobalData.loadHoliday(activity);
                GlobalData.loadWorkday(activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.init();
            }
        }
    }

}