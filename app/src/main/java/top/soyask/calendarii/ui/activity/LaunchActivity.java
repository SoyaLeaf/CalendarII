package top.soyask.calendarii.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import top.soyask.calendarii.MainActivity;
import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Setting;

import static top.soyask.calendarii.MainActivity.THEMES;

public class LaunchActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setupTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        },200);

    }

    private void setupTheme() {
        int theme = THEMES[Setting.theme];
        setTheme(theme);
    }

}
