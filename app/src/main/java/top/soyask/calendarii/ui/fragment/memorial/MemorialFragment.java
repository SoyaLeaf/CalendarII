package top.soyask.calendarii.ui.fragment.memorial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.MemorialDayDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.entity.LunarDay;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.EditBottomDialogFragment;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.EraUtils;
import top.soyask.calendarii.utils.LunarUtils;

public class MemorialFragment extends BaseFragment {

    private static final String DAY = "Day";
    private static final String MEMORIAL_DAY = "memorial_day";
    private List<String> mSelecteds = new ArrayList<>();
    private TextView mTvDate;
    private ExpandableLayout mElWho;
    private ExpandableLayout mElDate;
    private ChipGroup mChipGroupWho;
    private ChipGroup mChipGroupDefaultPeople;
    private View mTvWhoHint;
    private EditText mEtDetail;
    private EditText mEtName;
    private MemorialDay mMemorialDay;
    private NumberPicker mNpYear;
    private NumberPicker mNpMonth;
    private NumberPicker mNpDay;
    private Calendar mCalendar;
    private CheckBox mCbLunar;
    private CheckBox mCbBirthday;
    private String mOriginName;
    private HorizontalScrollView mHsvGroupWho;

    public MemorialFragment() {
        super(R.layout.fragment_memorial);
    }

    public static MemorialFragment newInstance(Day day) {
        Bundle args = new Bundle();
        args.putSerializable(DAY, day);
        MemorialFragment fragment = new MemorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static MemorialFragment newInstance(MemorialDay day) {
        Bundle args = new Bundle();
        args.putSerializable(MEMORIAL_DAY, day);
        MemorialFragment fragment = new MemorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        initData();
        findToolbar().setNavigationOnClickListener(v -> removeFragment(this));
        setupTvWho();
        setupTvDate();
        boolean isBirthday = "生日".equals(mMemorialDay.getName());
        setupNameAndDetails(isBirthday);
        setupCheckbox(isBirthday);
        setupNumberPicker();
        findViewById(R.id.ib_done).setOnClickListener(this::done);
        mHsvGroupWho.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
                -> mHsvGroupWho.smoothScrollTo(mChipGroupWho.getWidth(), 0));
    }

    private void setupCheckbox(boolean isBirthday) {
        mCbLunar = findViewById(R.id.cb_lunar);
        mCbBirthday = findViewById(R.id.cb_birthday);
        mCbBirthday.setOnCheckedChangeListener(this::onBirthdayChange);
        mCbBirthday.setChecked(isBirthday);
        mCbLunar.setChecked(mMemorialDay.isLunar());
        mCbLunar.setOnCheckedChangeListener(this::onLunarChange);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupNameAndDetails(boolean isBirthday) {
        View.OnTouchListener listener = (v, event) -> {
            mElDate.collapse();
            mElWho.collapse();
            v.setFocusableInTouchMode(true);
            return false;
        };
        mEtName = findViewById(R.id.et_name);
        mEtName.setOnTouchListener(listener);
        if (!isBirthday) {
            mEtName.setText(mMemorialDay.getName());
        }
        mEtDetail = findViewById(R.id.et_detail);
        mEtDetail.setText(mMemorialDay.getDetails());
        mEtDetail.setOnTouchListener(listener);
    }

    private void setupTvDate() {
        mElDate = findViewById(R.id.el_date);
        mTvDate = findViewById(R.id.tv_date);
        mTvDate.setOnClickListener(v -> toggleElDate());
    }

    private void initData() {
        Bundle arguments = getArguments();
        mMemorialDay = (MemorialDay) arguments.getSerializable(MEMORIAL_DAY);
        if (mMemorialDay == null) {
            mMemorialDay = new MemorialDay();
            Day day = (Day) arguments.getSerializable(DAY);
            mCalendar = Calendar.getInstance();
            if (day != null) {
                mCalendar.set(Calendar.YEAR, day.getYear());
                mCalendar.set(Calendar.MONTH, day.getMonth() - 1);
                mCalendar.set(Calendar.DAY_OF_MONTH, day.getDayOfMonth());
            }
        } else {
            mCalendar = Calendar.getInstance();
            mCalendar.set(Calendar.YEAR, mMemorialDay.getYear());
            mCalendar.set(Calendar.MONTH, mMemorialDay.getMonth() - 1);
            mCalendar.set(Calendar.DAY_OF_MONTH, mMemorialDay.getDay());
        }
    }

    private void setupTvWho() {
        mElWho = findViewById(R.id.el_who);
        mChipGroupDefaultPeople = findViewById(R.id.chip_group_default_people);
        mChipGroupWho = findViewById(R.id.chip_group_who);
        mHsvGroupWho = findViewById(R.id.hsv_group_who);
        mTvWhoHint = findViewById(R.id.tv_who_hint);
        mTvWhoHint.setOnClickListener(v -> toggleElWho());

        String[] defaultNames = mHostActivity.getResources().getStringArray(R.array.who);
        Set<String> customPeople = Setting.memorial_custom_people;
        String who = mMemorialDay.getWho();
        if (who != null) {
            String[] whos = who.split(Global.FLAG);
            for (String name : whos) {
                Chip chip = new Chip(mHostActivity);
                chip.setCheckable(false);
                chip.setText(name);
                mChipGroupDefaultPeople.addView(chip);
                selectPeople(chip);
                if (!isDefaultName(name, defaultNames) && !Setting.memorial_custom_people.contains(name)) {
                    Setting.memorial_custom_people.add(name);
                    Setting.setting(mHostActivity, Global.MEMORIAL_CUSTOM_PEOPLE, Setting.memorial_custom_people);
                }
            }
        }
        for (String name : defaultNames) {
            if (!mSelecteds.contains(name)) {
                Chip chip = generateDefaultChip(name);
                mChipGroupDefaultPeople.addView(chip);
            }

        }
        for (String cp : customPeople) {
            if (!mSelecteds.contains(cp)) {
                Chip chip = generateCustomChip(cp);
                mChipGroupDefaultPeople.addView(chip);
            }
        }

        Chip add = new Chip(mHostActivity);
        add.setBackgroundColor(Color.BLACK);
        add.setTextColor(Color.WHITE);
        add.setText("新增");
        add.setChipIconVisible(true);
        add.setChipIconResource(R.drawable.ic_add_white_24dp);
        add.setOnClickListener(this::addNewPeople);
        mChipGroupDefaultPeople.addView(add);
    }

    private boolean isDefaultName(String name, String[] names) {
        for (String s : names) {
            if (s.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private Chip generateDefaultChip(String name) {
        Chip chip = new Chip(mHostActivity);
        chip.setCheckable(false);
        chip.setText(name);
        chip.setOnClickListener(v -> selectPeople(chip));
        return chip;
    }

    private Chip generateCustomChip(String name) {
        Chip chip = new Chip(mHostActivity);
        chip.setCheckable(false);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> deleteCustomPeople(chip));
        chip.setText(name);
        chip.setOnClickListener(v -> selectPeople(chip));
        return chip;
    }

    private void done(View view) {
        mMemorialDay.setDay(mCalendar.get(Calendar.DAY_OF_MONTH));
        mMemorialDay.setMonth(mCalendar.get(Calendar.MONTH) + 1);
        mMemorialDay.setYear(mCalendar.get(Calendar.YEAR));
        StringBuilder who = new StringBuilder();
        for (String name : mSelecteds) {
            who.append(name).append(Global.FLAG);
        }
        if (mCbLunar.isChecked()) {
            mMemorialDay.setLunar(true);
            LunarDay lunar = LunarUtils.getLunar(mCalendar);
            mMemorialDay.setLunar(lunar.getLunarDate());
        }
        mMemorialDay.setWho(who.toString());
        mMemorialDay.setName(mCbBirthday.isChecked() ? "生日" : mEtName.getText().toString());
        mMemorialDay.setDetails(mEtDetail.getText().toString());
        if (mMemorialDay.getId() > 0) {
            MemorialDayDao.getInstance(mHostActivity).update(mMemorialDay);
        } else {
            MemorialDayDao.getInstance(mHostActivity).insert(mMemorialDay);
        }
        showSnackbar("添加成功");
        removeFragment(this);
        //todo
    }

    private void onLunarChange(CompoundButton button, boolean isChecked) {
        mElWho.collapse();
        mElDate.collapse();
        hideSoftInput();
    }

    private void hideSoftInput() {
        mEtDetail.requestFocus();
        new Handler().post(this::hideSoftInputReal);
    }

    private void onBirthdayChange(CompoundButton button, boolean isChecked) {
        mElWho.collapse();
        mElDate.collapse();
        hideSoftInput();
        if (isChecked) {
            mOriginName = mEtName.getText().toString();
            SpannableString text = createDetailText();
            mEtName.setText(text);
            mEtName.setEnabled(false);
        } else {
            mEtName.setText(mOriginName);
            mEtName.setEnabled(true);
        }
    }

    private SpannableString createDetailText() {
        SpannableString text;
        if (mOriginName.isEmpty()) {
            text = new SpannableString("生日");
            text.setSpan(new ForegroundColorSpan(0xdd000000),
                    0, "生日".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mEtName.setText(text);
            mEtName.setText("生日");
        } else {
            String source = String.format("生日\t(%s)", mOriginName);
            int start = "生日\t".length();
            text = new SpannableString(source);
            StrikethroughSpan span = new StrikethroughSpan();
            text.setSpan(new ForegroundColorSpan(0xdd000000),
                    0, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(span, start, source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }

    private void deleteCustomPeople(Chip chip) {
        mChipGroupDefaultPeople.removeView(chip);
        Setting.memorial_custom_people.remove(chip.getText().toString());
        Setting.setting(mHostActivity, Global.MEMORIAL_CUSTOM_PEOPLE, Setting.memorial_custom_people);
    }

    private void addNewPeople(View view) {
        EditBottomDialogFragment dialogFragment =
                EditBottomDialogFragment.newInstance("", "要添加谁呢？");
        dialogFragment.setOnDoneListener(result -> {
            if (!result.isEmpty()) {
                Chip chip = generateCustomChip(result);
                Setting.memorial_custom_people.add(result);
                Setting.setting(mHostActivity, Global.MEMORIAL_CUSTOM_PEOPLE, Setting.memorial_custom_people);
                selectPeople(chip);
            }
        });

        dialogFragment.setOnDismissListener(this::hideSoftInput);
        dialogFragment.show(getFragmentManager(), null);
    }

    private void toggleElWho() {
        mElWho.toggle();
        mElDate.collapse();
        hideSoftInput();
    }

    private void toggleElDate() {
        mElDate.toggle();
        mElWho.collapse();
        hideSoftInput();
    }

    private void selectPeople(Chip chip) {
        mChipGroupDefaultPeople.removeView(chip);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> removeSelectPeople(chip));
        chip.setOnClickListener(v -> toggleElWho());
        mTvWhoHint.setVisibility(View.GONE);
        mSelecteds.add(chip.getText().toString());
        mChipGroupWho.addView(chip);
        mHsvGroupWho.smoothScrollTo(mChipGroupWho.getWidth(), 0);
    }

    private void removeSelectPeople(Chip chip) {
        mChipGroupWho.removeView(chip);
        if (Setting.memorial_custom_people.contains(chip.getText().toString())) {
            chip.setOnCloseIconClickListener(v -> deleteCustomPeople(chip));
        } else {
            chip.setCloseIconVisible(false);
        }
        chip.setOnClickListener(view -> selectPeople(chip));
        mChipGroupDefaultPeople.addView(chip, 0);
        mSelecteds.remove(chip.getText().toString());
        if (mSelecteds.isEmpty()) {
            mTvWhoHint.setVisibility(View.VISIBLE);
        }
    }

    private void hideSoftInputReal() {
        InputMethodManager imm =
                (InputMethodManager) mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(mEtDetail.getWindowToken(), 0);
            }
        }
        mEtDetail.setFocusableInTouchMode(false);
        mEtName.setFocusableInTouchMode(false);
        mEtDetail.clearFocus();
        mEtName.clearFocus();
    }

    private void setupNumberPicker() {
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH) + 1;
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        mNpYear = findViewById(R.id.np_year);
        mNpMonth = findViewById(R.id.np_month);
        mNpDay = findViewById(R.id.np_day);

        mNpYear.setMinValue(1910);
        mNpYear.setMaxValue(2100);
        mNpYear.setValue(year);
        mNpYear.setOnValueChangedListener(this::onDateChange);
        mNpYear.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        mNpMonth.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mNpMonth.setMinValue(1);
        mNpMonth.setMaxValue(12);
        mNpMonth.setFormatter(value -> value + "月");
        mNpMonth.setValue(month);
        mNpMonth.setOnValueChangedListener(this::onDateChange);
        try {
            Method method = mNpMonth.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(mNpMonth, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int count = DayUtils.getMonthDayCount(mNpMonth.getValue(), mNpYear.getValue());
        mNpDay.setMinValue(1);
        mNpDay.setMaxValue(count);
        mNpDay.setValue(day);
        mNpDay.setOnValueChangedListener(this::onDateChange);
        updateTvDateText();
    }

    private void onDateChange(NumberPicker numberPicker, int oldVal, int newVal) {
        if (numberPicker == mNpYear) {
            mCalendar.set(Calendar.YEAR, newVal);
        } else if (numberPicker == mNpMonth) {
            mCalendar.set(Calendar.MONTH, newVal - 1);
        } else if (numberPicker == mNpDay) {
            mCalendar.set(Calendar.DAY_OF_MONTH, newVal);
        }
        int count = DayUtils.getMonthDayCount(mNpMonth.getValue(), mNpYear.getValue());
        mNpDay.setMaxValue(count);
        updateTvDateText();
    }

    private void updateTvDateText() {
        LunarDay lunar = LunarUtils.getLunar(mCalendar);
        String branches = EraUtils.getYearForEarthlyBranches(lunar.getYear());
        String stems = EraUtils.getYearForHeavenlyStems(lunar.getYear());
        int year = mNpYear.getValue();
        int month = mNpMonth.getValue();
        int day = mNpDay.getValue();
        String date = String.format(Locale.CHINA, "%d年%02d月%02d日 (%s%s年%s)",
                year, month, day, stems, branches, lunar.getLunarDate());
        mTvDate.setText(date);
    }

}
