package top.soyask.calendarii.ui.fragment.event;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.entity.Symbol;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.DateSelectDialog;


public class EditThingFragment extends BaseFragment
        implements View.OnClickListener, Animator.AnimatorListener, DateSelectDialog.DateSelectCallback {
    private static final SimpleDateFormat sDateFormat =
            new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
    private static final String DATE = "DATE";
    private static final String THING = "THING";
    private EditText mEditText;
    private InputMethodManager mManager;
    private Day mDay;
    private Thing mThing;
    private Button mBtnDate;
    private OnUpdateListener mOnUpdateListener;
    private OnAddListener mOnAddListener;
    private OnDeleteListener mOnDeleteListener;
    private boolean isExiting;
    private Spinner mSpinnerEventType;

    public EditThingFragment() {
        super(R.layout.fragment_add_event);
    }

    public static EditThingFragment newInstance(Day day, Thing thing) {
        EditThingFragment fragment = new EditThingFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATE, day);
        args.putSerializable(THING, thing);
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
        if (mThing != null) {
            long targetTime = mThing.getTargetTime();
            mEditText.setText(mThing.getDetail());
            mBtnDate.setText(sDateFormat.format(new Date(targetTime)));
        } else {
            String date = String.format(Locale.CHINA, "%s年%d月%d日",
                    String.valueOf(mDay.getYear()), mDay.getMonth(), mDay.getDayOfMonth());
            mBtnDate.setText(date);
        }
        mEditText.requestFocus();

        mSpinnerEventType = findViewById(R.id.spinner_event_type);
        mSpinnerEventType.setAdapter(mAdapter);
        int position = mThing != null ? mThing.getType() : Setting.default_event_type;
        mSpinnerEventType.setSelection(position);
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
        mManager = (InputMethodManager) mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mManager != null) {
            mManager.showSoftInput(mEditText, 0);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDay = (Day) getArguments().getSerializable(DATE);
            mThing = (Thing) getArguments().getSerializable(THING);
        }

        if (mDay == null && mThing != null) {
            long createTime = mThing.getTargetTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(createTime);
            mDay = new Day(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
    }

    private void enter(View view) {
        WindowManager windowManager = mHostActivity.getWindowManager();
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point outSize = new Point();
            display.getRealSize(outSize);
            int height = outSize.y;
            int width = outSize.x;
            Animator anim = ViewAnimationUtils.createCircularReveal(view, width, height, 0, height);
            anim.setDuration(500);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addListener(this);
            anim.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_date:
                DateSelectDialog dateSelectDialog = DateSelectDialog.newInstance(mDay.getYear(), mDay.getMonth(), mDay.getDayOfMonth());
                dateSelectDialog.show(getChildFragmentManager(), "");
                dateSelectDialog.setDateSelectCallback(this);
                break;
            case R.id.ib_done:
                done();
                break;
            default:
                String detail = mEditText.getText().toString();
                if (isAddNewThing()) {
                    if (!hasContent(detail)) {
                        removeSelf();
                    } else {
                        confirmExit();
                    }
                } else {
                    if (mThing.getDetail().equals(detail)) {
                        removeSelf();
                    } else {
                        if (hasContent(detail)) {
                            confirmExit();
                        } else {
                            confirmDeleteIfContentIsEmpty();
                        }
                    }
                }
                break;
        }
    }

    private void confirmDeleteIfContentIsEmpty() {
        new AlertDialog.Builder(mHostActivity)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    ThingDao.getInstance(mHostActivity).delete(mThing);
                    mOnDeleteListener.onDelete();
                    removeSelf();
                })
                .setNegativeButton(R.string.do_not_delete, (dialog, which) -> removeSelf())
                .show();
    }

    private void confirmExit() {
        new AlertDialog.Builder(mHostActivity)
                .setMessage(R.string.whether_to_save)
                .setPositiveButton(R.string.confirm, (dialog, which) -> done())
                .setNegativeButton(R.string.cancel, (dialog, which) -> removeSelf())
                .show();
    }


    private void done() {
        String detail = mEditText.getText().toString();
        ThingDao thingDao = ThingDao.getInstance(mHostActivity);
        if (hasContent(detail)) {
            long targetTime = getTargetTime();
            if (isAddNewThing()) {
                addNewThing(detail, targetTime, thingDao);
            } else {
                updateEvent(detail, targetTime, thingDao);
            }
            removeWithAnimationAndHideSoftInput();
        } else {
            if (!isAddNewThing()) {
                confirmDeleteIfContentIsEmpty();
            }
        }
    }

    private long getTargetTime() {
        try {
            String time = mBtnDate.getText().toString();
            return sDateFormat.parse(time).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private boolean isAddNewThing() {
        return mThing == null;
    }

    private boolean hasContent(String detail) {
        return detail != null && !detail.trim().isEmpty();
    }

    private void addNewThing(String detail, long targetTime, ThingDao thingDao) {
        int position = mSpinnerEventType.getSelectedItemPosition();
        Thing thing = new Thing();
        thing.setDetail(detail);
        thing.setType(position);
        thing.setTargetTime(targetTime);
        thing.setUpdateTime(System.currentTimeMillis());
        thingDao.insert(thing);
        if (mOnAddListener != null) {
            mOnAddListener.onAdd();
        }
    }

    private void updateEvent(String detail, long targetTime, ThingDao thingDao) {
        int position = mSpinnerEventType.getSelectedItemPosition();
        mThing.setDetail(detail);
        mThing.setType(position);
        mThing.setTargetTime(targetTime);
        mThing.setUpdateTime(System.currentTimeMillis());
        thingDao.update(mThing);
        if (mOnUpdateListener != null) {
            mOnUpdateListener.onUpdate();
        }
    }

    private void removeWithAnimationAndHideSoftInput() {
        mManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        if (!isExiting) {
            isExiting = true;
            removeWithAnimation();
        }
    }

    private void removeWithAnimation() {
        Display display = mHostActivity.getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        display.getRealSize(outSize);
        int height = outSize.y;
        int width = outSize.x;
        Animator anim = ViewAnimationUtils.createCircularReveal(mContentView, width, 0, height, 0);
        anim.setDuration(500);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContentView.setVisibility(View.GONE);
                removeFragment(EditThingFragment.this);
            }
        });
        anim.start();
    }

    private void removeSelf() {
        isExiting = true;
        mManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        removeFragment(this);
    }

    private void paste() {
        ClipboardManager clipboardManager =
                (ClipboardManager) mHostActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip = clipboardManager.getPrimaryClip();
        final String message = primaryClip.getItemAt(0).getText().toString();
        if (primaryClip.getItemCount() > 0) {
            new AlertDialog.Builder(mHostActivity).setTitle("剪切板内容")
                    .setMessage(message)
                    .setPositiveButton("粘贴", (dialog, which) -> {
                        mEditText.append(message);
                        dialog.dismiss();
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss()).show();
        } else {
            new AlertDialog.Builder(mHostActivity).setMessage("剪切板里什么也没有 >_<").show();
        }
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        Symbol[] symbols = Symbol.values();
        int[] resIds = {
                R.drawable.ic_rect_black_24dp,
                R.drawable.ic_circle_black_24dp,
                R.drawable.ic_triangle_black_24dp,
                R.drawable.ic_star_black_24dp,
                R.drawable.ic_favorite_black_24dp,
        };

        @Override
        public int getCount() {
            return symbols.length;
        }

        @Override
        public Object getItem(int position) {
            return symbols[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Symbol symbol = symbols[position];
            if (convertView == null) {
                convertView = LayoutInflater.from(mHostActivity).inflate(R.layout.spinner_item_symbol, null);
            }
            TextView textView = convertView.findViewById(R.id.tv);
            ImageView imageView = convertView.findViewById(R.id.iv);
            String comment = Setting.symbol_comment.get(symbol.key);
            textView.setText(comment);
            imageView.setImageResource(resIds[position]);
            return convertView;
        }
    };


    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.mOnUpdateListener = onUpdateListener;
    }

    public void setOnAddListener(OnAddListener onAddListener) {
        this.mOnAddListener = onAddListener;
    }

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.mOnDeleteListener = onDeleteListener;
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        mManager = (InputMethodManager) mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mManager != null) {
            mManager.showSoftInput(mEditText, 0);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onSelectCancel() {
    }

    @Override
    public void onValueChange(int year, int month, int day) {
        String date = String.format(Locale.CHINA, "%s年%d月%d日", String.valueOf(year), month, day);
        mBtnDate.setText(date);
    }

    @Override
    public void onSelectConfirm(int year, int month, int day) {
    }

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

    public interface OnDeleteListener {
        void onDelete();
    }
}
