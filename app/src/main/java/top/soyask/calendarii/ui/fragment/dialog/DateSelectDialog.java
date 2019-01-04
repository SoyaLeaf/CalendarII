package top.soyask.calendarii.ui.fragment.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import java.lang.reflect.Method;

import top.soyask.calendarii.R;
import top.soyask.calendarii.utils.DayUtils;


public class DateSelectDialog extends BottomSheetDialogFragment implements View.OnClickListener, NumberPicker.OnValueChangeListener {

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "dayOfMonth";

    private int mYear;
    private int mMonth;
    private int mDay;
    private View mContentView;
    private NumberPicker mNpYear;
    private NumberPicker mNpMonth;
    private NumberPicker mNpDay;
    private DateSelectCallback mDateSelectCallback;

    public DateSelectDialog() {

    }

    public static DateSelectDialog newInstance(int year, int month, int dayOfMonth) {
        DateSelectDialog fragment = new DateSelectDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, dayOfMonth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mYear = getArguments().getInt(ARG_YEAR);
            mMonth = getArguments().getInt(ARG_MONTH);
            mDay = getArguments().getInt(ARG_DAY);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupNumberPicker();
        setupButton();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mDateSelectCallback.onDismiss();
    }

    private void setupButton() {
        mContentView.findViewById(R.id.btn_cancel).setOnClickListener(this);
        mContentView.findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    public void setDateSelectCallback(DateSelectCallback dateSelectCallback) {
        this.mDateSelectCallback = dateSelectCallback;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                mDateSelectCallback.onSelectConfirm(mNpYear.getValue(), mNpMonth.getValue(), mNpDay.getValue());
                dismiss();
                break;
            case R.id.btn_cancel:
                mDateSelectCallback.onSelectCancel();
                mDateSelectCallback.onValueChange(mYear, mMonth, mDay);
                dismiss();
                break;
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        int year = mNpYear.getValue();
        int month = mNpMonth.getValue();
        mDateSelectCallback.onValueChange(year, month, mNpDay.getValue());
        updateDayCount(year, month);
    }

    private void updateDayCount(int year, int month) {
        int dayCount = DayUtils.getMonthDayCount(month, year);
        mNpDay.setMaxValue(dayCount);
    }

    public interface DateSelectCallback {
        void onSelectCancel();

        void onValueChange(int year, int month, int day);

        void onSelectConfirm(int year, int month, int day);

        void onDismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupNumberPicker() {
        mNpYear =  mContentView.findViewById(R.id.np_year);
        mNpMonth =  mContentView.findViewById(R.id.np_month);
        mNpDay =  mContentView.findViewById(R.id.np_day);

        mNpYear.setMinValue(1910);
        mNpYear.setMaxValue(2100);
        mNpYear.setValue(mYear);
        mNpYear.setOnValueChangedListener(this);
        mNpYear.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        mNpMonth.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mNpMonth.setMinValue(1);
        mNpMonth.setMaxValue(12);
        mNpMonth.setFormatter(value -> value + "月");
        mNpMonth.setValue(mMonth);
        mNpMonth.setOnValueChangedListener(this);
        try {
            Method method = mNpMonth.getClass().getDeclaredMethod("changeValueByOne", boolean.class);
            method.setAccessible(true);
            method.invoke(mNpMonth, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mNpDay.setMinValue(1);
        mNpDay.setMaxValue(30);
        mNpDay.setValue(mDay);
        mNpDay.setOnValueChangedListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_date_select, container, false);
        return mContentView;
    }
}
