package top.soyask.calendarii.ui.fragment.setting.birth;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.lang.reflect.Method;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.BirthdayDao;
import top.soyask.calendarii.entity.Birthday;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.LunarUtils;


public class AddFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private String[] mName;
    private EditText mEtWho;
    private NumberPicker mNpMonth;
    private NumberPicker mNpDay;
    private boolean isLunar = false;
    private OnDoneClickListener mOnDoneClickListener;


    private static final NumberPicker.Formatter LUNAR_MONTH_FORMATTER = value -> LunarUtils.LUNAR_MONTH[value - 1];
    private static final NumberPicker.Formatter LUNAR_DAY_FORMATTER = LunarUtils::getLunarDay;
    private static final NumberPicker.Formatter NORMAL_MONTH_FORMATTER = value -> String.format(Locale.CHINA, "%d月", value);
    private static final NumberPicker.Formatter NORMAL_DAY_FORMATTER = String::valueOf;

    private RadioButton mRbNormal;
    private RadioButton mRbLunar;


    public AddFragment() {
        super(R.layout.fragment_add_birth);
    }

    public static AddFragment newInstance() {
        AddFragment fragment = new AddFragment();
        return fragment;
    }

    @Override
    protected void setupUI() {
        setupSpinner();
        setupNumberPicker();
        setupRadioButton();
        setupOther();
    }

    private void setupOther() {
        mEtWho = findViewById(R.id.et_who);
        findToolbar().setNavigationOnClickListener(this);
        findViewById(R.id.ib_done).setOnClickListener(this);
    }

    private void setupRadioButton() {
        mRbNormal = findViewById(R.id.rb_normal);
        mRbNormal.setChecked(true);
        mRbLunar = findViewById(R.id.rb_lunar);
        mRbNormal.setOnCheckedChangeListener(this);
        mRbLunar.setOnCheckedChangeListener(this);
    }

    private void setupNumberPicker() {
        mNpMonth = findViewById(R.id.np_month);
        mNpMonth.setMaxValue(12);
        mNpMonth.setMinValue(1);
        mNpDay = findViewById(R.id.np_day);
        mNpDay.setMaxValue(30);
        mNpDay.setMinValue(1);
        mNpMonth.setFormatter(NORMAL_MONTH_FORMATTER);
        mNpDay.setFormatter(NORMAL_DAY_FORMATTER);
        mNpMonth.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (isLunar) {
                mNpDay.setMaxValue(30);
            } else {
                int monthDayCount = DayUtils.getMonthDayCount(newVal, 2000);
                mNpDay.setMaxValue(monthDayCount);
            }
        });

        try {
            Method method = mNpMonth.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(mNpMonth, true);
            method.invoke(mNpDay, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSpinner() {
        Spinner spinner = findViewById(R.id.spinner_who);
        mName = getResources().getStringArray(R.array.who);
        spinner.setAdapter(new ArrayAdapter<>(mHostActivity, R.layout.item_who, R.id.tv, mName));
        spinner.setOnItemSelectedListener(this);
    }

    public void setOnDoneClickListener(OnDoneClickListener onDoneClickListener) {
        this.mOnDoneClickListener = onDoneClickListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        boolean enabled = position == mName.length - 1;
        mEtWho.setEnabled(enabled);
        mEtWho.setHint(mName[position]);
        mEtWho.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            isLunar = "农历".equals(buttonView.getText());
            setNumberPickerFormatter();
        }

    }

    private void setNumberPickerFormatter() {
        if (isLunar) {
            mNpMonth.setFormatter(LUNAR_MONTH_FORMATTER);
            mNpDay.setFormatter(LUNAR_DAY_FORMATTER);
            mNpDay.setMaxValue(30);
        } else {
            mNpMonth.setFormatter(NORMAL_MONTH_FORMATTER);
            mNpDay.setFormatter(NORMAL_DAY_FORMATTER);
        }
        mNpMonth.postInvalidate();
        mNpDay.postInvalidate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_done:
                done();
                removeFragment(this);
                break;
            default:
                removeFragment(this);
                break;
        }
    }

    private void done() {
        Birthday birthday = getBirthday();
        BirthdayDao birthdayDao = BirthdayDao.getInstance(mHostActivity);
        birthdayDao.add(birthday);
        mOnDoneClickListener.onDone(birthday);
    }

    @NonNull
    private Birthday getBirthday() {
        String who = getWho();
        String when = getWhen();
        Birthday birthday = new Birthday();
        birthday.setWho(who);
        birthday.setWhen(when);
        birthday.setLunar(isLunar);
        return birthday;
    }

    @NonNull
    private String getWhen() {
        String when;
        int month = mNpMonth.getValue();
        int day = mNpDay.getValue();

        if (isLunar) {
            when = LunarUtils.LUNAR_MONTH[month - 1] + LunarUtils.getLunarDay(day);
        } else {
            when = month + "月" + day + "日";
        }
        return when;
    }

    @NonNull
    private String getWho() {
        String who = mEtWho.getText().toString().trim();
        if ("".equals(who)) {
            who = mEtWho.getHint().toString();
        }
        return who;
    }

    public interface OnDoneClickListener {
        void onDone(Birthday birthday);
    }
}
