package top.soyask.calendarii.ui.fragment.setting.theme;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;


public class ThemeFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final int[] COLORS = {
            R.color.colorPrimary, R.color.colorPrimary_Green,
            R.color.colorPrimary_Pink, R.color.colorPrimary_Teal,
            R.color.colorPrimary_Blue, R.color.colorPrimary_Red,
            R.color.colorPrimary_Purple, R.color.colorPrimary_Black,
    };
    private static final int[] COLOR_NAMES = {
            R.string.color_default, R.string.color_green, R.string.color_pink, R.string.color_teal,
            R.string.color_blue, R.string.color_red, R.string.color_purple, R.string.color_black,
    };

    private static final int WAIT = 0;
    private static final int CANCEL = 1;
    private static final int UPDATE = 3;

    private int mCurrentTheme;
    private ProgressDialog mProgressDialog;

    private Handler mHandler = new ThemeHandler(this);

    public ThemeFragment() {
        super(R.layout.fragment_theme);
    }


    public static ThemeFragment newInstance() {
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar(R.id.toolbar).setNavigationOnClickListener(this);
        SharedPreferences setting = mHostActivity.getSharedPreferences("setting", Context.MODE_PRIVATE);
        mCurrentTheme = setting.getInt("theme", 0);

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setAdapter(getAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(mHostActivity, RecyclerView.VERTICAL, false));
//        RadioButton rb = findViewById(RADIO_BUTTON_IDS[mCurrentTheme]);
//        rb.setChecked(true);
//        for (int id : RADIO_BUTTON_IDS) {
//            RadioButton radioButton = findViewById(id);
//            radioButton.setOnCheckedChangeListener(this);
//        }
    }

    private RecyclerView.Adapter getAdapter() {
        return new RecyclerView.Adapter<ColorViewHolder>() {
            @NonNull
            @Override
            public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_theme, parent, false);
                return new ColorViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
                holder.rb_color.setChecked(position == Setting.theme);
                holder.rb_color.setText(COLOR_NAMES[position]);
                holder.rb_color.setTextColor(holder.itemView.getResources().getColor(COLORS[position]));
                holder.view_color.setBackgroundResource(COLORS[position]);
                holder.rb_color.setOnClickListener(v -> setupTheme(position));
            }

            @Override
            public int getItemCount() {
                return COLORS.length;
            }
        };
    }


    private void setupTheme(int theme) {
        SharedPreferences.Editor setting = mHostActivity.getSharedPreferences("setting", Context.MODE_PRIVATE).edit();
        setting.putInt("theme", theme).apply();
        Setting.theme = theme;
        Intent intent = mHostActivity.getIntent();
        mHostActivity.finish();
        startActivity(intent);
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (COLORS[mCurrentTheme] != buttonView.getId() && isChecked) {
            for (int i = 0; i < COLORS.length; i++) {
                if (buttonView.getId() == COLORS[i]) {
                    final int finalI = i;
                    new Thread() {
                        @Override
                        public void run() {
                            Message obtain = Message.obtain();
                            obtain.what = UPDATE;
                            obtain.arg1 = finalI;
                            mHandler.sendMessage(obtain);
                        }
                    }.start();
                    break;
                }
            }
        }
    }

    private static class ColorViewHolder extends RecyclerView.ViewHolder {
        RadioButton rb_color;
        View view_color;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            rb_color = itemView.findViewById(R.id.rb_color);
            view_color = itemView.findViewById(R.id.view_color);
        }
    }

    public static class ThemeHandler extends Handler {

        WeakReference<ThemeFragment> mFragment;

        ThemeHandler(ThemeFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            ThemeFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case WAIT:
                    fragment.mProgressDialog = ProgressDialog.show(fragment.mHostActivity, null, "请稍等...", true);
                    break;
                case CANCEL:
                    if (fragment.mProgressDialog != null) {
                        fragment.mProgressDialog.dismiss();
                        fragment.mProgressDialog = null;
                    }
                    break;
                case UPDATE:
                    fragment.setupTheme(msg.arg1);
                    break;
            }
        }
    }
}
