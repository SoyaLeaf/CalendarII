package top.soyask.calendarii.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.domain.Event;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;


public class AddEventFragment extends BaseFragment implements View.OnClickListener, Animator.AnimatorListener, DateSelectDialog.DateSelectCallback {

    private static final String DATE = "DATE";
    private static final String EVENT = "EVENT";
    private EditText mEditText;
    private InputMethodManager mManager;
    private Day mDay;
    private Event mEvent;
    private Button mBtnDate;
    private OnUpdateListener mOnUpdateListener;
    private OnAddListener mOnAddListener;
    private boolean isExiting;

    public AddEventFragment() {
        super(R.layout.fragment_add_event);
    }

    public static AddEventFragment newInstance(Day day, Event event) {
        AddEventFragment fragment = new AddEventFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATE, day);
        args.putSerializable(EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar(R.id.toolbar).setNavigationOnClickListener(this);
        findViewById(R.id.ib_done).setOnClickListener(this);
        mEditText = findViewById(R.id.et);
        mBtnDate = findViewById(R.id.btn_date);
        mBtnDate.setOnClickListener(this);

        if (mEvent != null) {
            String title = mEvent.getTitle();
            mEditText.setText(mEvent.getDetail());
            mBtnDate.setText(title.substring(2));
        } else {
            String date = new StringBuffer()
                    .append(mDay.getYear()).append("年")
                    .append(mDay.getMonth()).append("月")
                    .append(mDay.getDayOfMonth()).append("日").toString();
            mBtnDate.setText(date.substring(2));
        }
        mEditText.requestFocus();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            enter(view);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mManager = (InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mManager.showSoftInput(mEditText, 0);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDay = (Day) getArguments().getSerializable(DATE);
            mEvent = (Event) getArguments().getSerializable(EVENT);
        }

        if (mDay == null && mEvent != null) {
            String title = mEvent.getTitle();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            Date date = null;
            try {
                date = dateFormat.parse(title);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mDay = new Day(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void enter(View view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        int width = display.getWidth();
        Animator anim = ViewAnimationUtils.createCircularReveal(view, width, height, 0, height);
        anim.setDuration(500);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addListener(this);
        anim.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_date:
                DateSelectDialog dateSelectDialog = DateSelectDialog.newInstance(mDay.getYear(), mDay.getMonth(), mDay.getDayOfMonth());
                dateSelectDialog.show(getChildFragmentManager(),"");
                dateSelectDialog.setDateSelectCallback(this);
                break;
            case R.id.ib_done:
                done();
                break;
            default:
                removeFragment(this);
                mManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
                break;
        }
    }

    private void done() {
        String detail = mEditText.getText().toString();
        String title = 20 + mBtnDate.getText().toString();
        EventDao eventDao = EventDao.getInstance(getMainActivity());

        if (mEvent == null) {
            Event event = new Event(title, detail);
            eventDao.add(event);
            if (mOnAddListener != null) {
                mOnAddListener.onAdd();
            }
        } else {
            mEvent.setTitle(title);
            mEvent.setDetail(detail);
            eventDao.update(mEvent);
            if (mOnUpdateListener != null) {
                mOnUpdateListener.onUpdate();
            }
        }
        mManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        if (!isExiting) {
            isExiting = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                exit();
            } else {
                removeFragment(this);
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exit() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        int width = display.getWidth();
        Animator anim = ViewAnimationUtils.createCircularReveal(getContentView(), width, 0, height, 0);
        anim.setDuration(500);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                getContentView().setVisibility(View.GONE);
                removeFragment(AddEventFragment.this);
            }
        });
        anim.start();
    }

    private void paste() {
        ClipboardManager clipboardManager =
                (ClipboardManager) getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        final String message = primaryClip.getItemAt(0).getText().toString();
        if (primaryClip.getItemCount() > 0) {
            new AlertDialog.Builder(getMainActivity()).setTitle("剪切板内容")
                    .setMessage(message)
                    .setPositiveButton("粘贴", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mEditText.append(message);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            new AlertDialog.Builder(getMainActivity()).setMessage("剪切板里什么也没有 >_<").show();
        }


    }

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.mOnUpdateListener = onUpdateListener;
    }

    public void setOnAddListener(OnAddListener onAddListener) {
        this.mOnAddListener = onAddListener;
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mManager = (InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mManager.showSoftInput(mEditText, 0);
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onSelectCancel() { }

    @Override
    public void onValueChange(int year, int month, int day) {
        String date = new StringBuffer()
                .append(year).append("年")
                .append(month).append("月")
                .append(day).append("日").toString();
        mBtnDate.setText(date.substring(2));
    }

    @Override
    public void onSelectConfirm(int year, int month, int day) {}

    @Override
    public void onDismiss() {
        mEditText.requestFocus();
        mManager.showSoftInput(mEditText, 0);
    }

    public interface OnUpdateListener {
        void onUpdate();
    }

    public interface OnAddListener {
        void onAdd();
    }
}
