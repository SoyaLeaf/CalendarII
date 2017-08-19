package top.soyask.calendarii;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import top.soyask.calendarii.fragment.MainFragment;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        MainFragment mainFragment = MainFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, mainFragment)
                .commit();
        getApplication().onCreate();
    }


}
