package top.soyask.calendarii.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import top.soyask.calendarii.R;
import top.soyask.calendarii.utils.DayUtils;

/**
 * Created by mxf on 2018/5/7.
 */

public class CalendarView extends View {

    static final String[] WEEK_ARRAY = {"日", "一", "二", "三", "四", "五", "六",};

    private static final int DATE_VIEW_TYPE_CURRENT = 0;
    private static final int DATE_VIEW_TYPE_PREV = 1;
    private static final int DATE_VIEW_TYPE_NEXT = 2;

    private List<? extends IDay> mDays;
    private int mFirstDayOfWeek;
    private int mFirstDayOffset = 0;
    private int mDisplayWidth;
    private int mSelectPos = -1;
    private float mWeekTextSize;
    private float mDateWidth;
    private float mDateHeight;
    private float mDateTextSize;
    private int mDateTextColor;
    private float mDateMargin;
    private float mDateBottomTextSize;
    private float mDateCircleSize;
    private int mDateCircleColor;
    private float mEventRectSize;
    private float mHolidayTextSize;
    private int mHolidayTextColor;
    private int mWeekdayTextColor;
    private int mWeekendTextColor;
    private int mTodayTextColor;
    private boolean mReplenish = true;
    private boolean mUseAnimation = true;
    private OnDayClickListener mListener;
    private boolean initialized;
    private int mSelectProgress;
    private Set<Runnable> mPendingList = new HashSet<>();

    private WeekView[] mWeekViews = new WeekView[7];
    private AbstractDateView[][] mDateViews = new AbstractDateView[6][7];
    private Thread mSelectAnimProgress;


    public CalendarView(Context context) {
        super(context);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CalendarView);
        mWeekTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_weekTextSize, 0);
        mDateHeight = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_dateHeight, 0);
        mDateTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_dateTextSize, 0);
        mDateTextColor = typedArray.getColor(R.styleable.CalendarView_cv_dateTextColor, Color.parseColor("#dd000000"));
        mDateMargin = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_dateMargin, 0);
        mDateBottomTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_dateBottomTextSize, 0);
        mDateCircleSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_dateCircleSize, 0);
        mDateCircleColor = typedArray.getColor(R.styleable.CalendarView_cv_dateCircleColor, Color.BLACK);
        mEventRectSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_eventRectSize, 0);
        mHolidayTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_cv_holidayTextSize, 0);
        mHolidayTextColor = typedArray.getColor(R.styleable.CalendarView_cv_holidayTextColor, Color.RED);
        mWeekdayTextColor = typedArray.getColor(R.styleable.CalendarView_cv_weekdayTextColor, Color.RED);
        mWeekendTextColor = typedArray.getColor(R.styleable.CalendarView_cv_weekendTextColor, Color.RED);
        mTodayTextColor = typedArray.getColor(R.styleable.CalendarView_cv_todayTextColor, Color.WHITE);
        mReplenish = typedArray.getBoolean(R.styleable.CalendarView_cv_replenish, true);
        typedArray.recycle();

    }

    public void setData(int currentYear, int currentMonth, List<? extends IDay> days) {
        mDays = days;
        mFirstDayOfWeek = (mDays.get(0).getDayOfWeek() + 6 - mFirstDayOffset) % 7;
        initWeekViews();
        initPrevMonth(currentYear, currentMonth);
        initCurrentMonth(days);
        initNextMonth(days);
        initialized = true;
        if (!mPendingList.isEmpty()) {
            for (Runnable runnable : mPendingList) {
                runnable.run();
            }
        }
        mPendingList.clear();
        postInvalidate();
    }


    public boolean isInitialized() {
        return initialized;
    }

    private void initWeekViews() {
        WeekView.paddingTop = getPaddingTop();
        WeekView.paint.setTextSize(mWeekTextSize);
        WeekView.size = mDisplayWidth / 7;
        mDateWidth = mDisplayWidth / 7;
        WeekView.offset = mFirstDayOffset;
        for (int i = 0; i < mWeekViews.length; i++) {
            mWeekViews[i] = new WeekView(i);
        }
    }

    private void initPrevMonth(int currentYear, int currentMonth) {
        int prevMonthDayCount = DayUtils.getPrevMonthDayCount(currentMonth, currentYear);
        for (int i = 0; i < mFirstDayOfWeek; i++) {
            IDay day = new OtherMonthDay(prevMonthDayCount + 1 - mFirstDayOfWeek + i);
            AbstractDateView dateView = new AbstractDateView(i, day, DATE_VIEW_TYPE_PREV);
            dateView.init();
            int m = dateView.index / 7;
            int j = dateView.index % 7;
            mDateViews[m][j] = dateView;
        }
    }

    private void initCurrentMonth(List<? extends IDay> days) {
        for (int i = 0; i < days.size(); i++) {
            AbstractDateView dateView = new AbstractDateView(i + mFirstDayOfWeek, days.get(i), DATE_VIEW_TYPE_CURRENT);
            dateView.init();
            int m = dateView.index / 7;
            int j = dateView.index % 7;
            mDateViews[m][j] = dateView;
        }
    }

    private void initNextMonth(List<? extends IDay> days) {
        for (int i = mFirstDayOfWeek + days.size(); i < 42; i++) {
            OtherMonthDay day = new OtherMonthDay(i - (mFirstDayOfWeek + days.size()) + 1);
            AbstractDateView dateView = new AbstractDateView(i, day, DATE_VIEW_TYPE_NEXT);
            dateView.init();
            int m = dateView.index / 7;
            int j = dateView.index % 7;
            mDateViews[m][j] = dateView;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDays == null) {
            return;
        }
        for (WeekView weekView : mWeekViews) {
            weekView.onDraw(canvas);
        }
        for (AbstractDateView[] views : mDateViews) {
            for (AbstractDateView dateView : views) {
                if (dateView != null) {
                    dateView.onDraw(canvas);
                }
            }
        }
    }

    private Point outSize = new Point();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        getDisplay().getRealSize(outSize);
        mDisplayWidth = outSize.x;
        int weekHeight = mDisplayWidth / 7 / 2;
        int h = (int) (6 * mDateHeight + weekHeight + 2 * 6 * mDateMargin) + getPaddingTop() + getPaddingBottom();
        setMinimumHeight(h);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDisplayWidth, h);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDisplayWidth, heightSize);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, h);
        }
    }

    private float mStartX;
    private float mStartY;
    private boolean moved;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                moved = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!moved) {
                    moved = Math.abs(event.getRawX() - mStartX) > 5 || Math.abs(event.getRawY() - mStartY) > 5;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!moved) {
                    float x = event.getX();
                    float y = event.getY();

                    float v = y - WeekView.size / 2 - mDateMargin - getPaddingTop();
                    if (v > 0) {
                        int i = (int) (v / (mDateHeight + 2 * mDateMargin));
                        int j = (int) (x / mDateWidth);
                        int position = i * 7 + j;
                        select(position);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void select(int position) {
        if (initialized) {
            performSelect(position);
        } else {
            mPendingList.add(() -> performSelect(position));
        }
    }

    private void performSelect(int position) {
        if (position < 0) {
            mSelectPos = -1;
        } else if (isPositionInCurrentMonth(position)) {
            onDaySelected(position);
        } else if (isPositionInPrevMonth(position)) {
            onPrevMonthClick(position);
        } else if (isPositionInNextMonth(position)) {
            onNextMonthClick(position);
        }
        postInvalidate();
    }

    private boolean isPositionInCurrentMonth(int position) {
        return position >= mFirstDayOfWeek && position < mFirstDayOfWeek + mDays.size();
    }

    private boolean isPositionInPrevMonth(int position) {
        return position < mFirstDayOfWeek;
    }

    private boolean isPositionInNextMonth(int position) {
        return position >= mFirstDayOfWeek + mDays.size();
    }

    private void onDaySelected(int position) {
        mSelectPos = position;
        if (mListener != null) {
            IDay day = mDays.get(position - mFirstDayOfWeek);
            mListener.onDaySelected(position, day);
        }
        if (mUseAnimation) {
            startSelectAnimation();
        }
    }

    private void startSelectAnimation() {
        if (mSelectAnimProgress != null) {
            mSelectAnimProgress.interrupt();
            mSelectAnimProgress = null;
        }
        mSelectAnimProgress = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i <= 100; i++) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    mSelectProgress = i;
                    postInvalidate();
                    SystemClock.sleep(5);
                }
            }
        };
        mSelectAnimProgress.start();
    }

    private void onPrevMonthClick(int position) {
        mSelectPos = -1;
        if (mListener != null) {
            int i = position / 7;
            int j = position % 7;
            int dayOfMonth = mDateViews[i][j].day.getDayOfMonth();
            mListener.onPrevMonthClick(dayOfMonth);
        }
    }

    private void onNextMonthClick(int position) {
        mSelectPos = -1;
        if (mListener != null) {
            mListener.onNextMonthClick(position + 1 - mFirstDayOfWeek - mDays.size());
        }
    }

    public void selectCurrentMonth(int position) {
        if (initialized) {
            select(position + mFirstDayOfWeek - 1);
        } else {
            mPendingList.add(() -> selectCurrentMonth(position));
        }
    }

    public void setOnDaySelectedListener(OnDayClickListener listener) {
        this.mListener = listener;
    }

    public CalendarView setFirstDayOffset(int firstDayOffset) {
        mFirstDayOffset = firstDayOffset;
        return this;
    }

    public CalendarView setDateCircleSize(int dateCircleSize) {
        if (dateCircleSize != -1) {
            mDateCircleSize = dateCircleSize;
        }
        return this;
    }

    public CalendarView setDateTextSize(float dateTextSize) {
        if (dateTextSize != -1) {
            mDateTextSize = dateTextSize;
        }
        return this;
    }

    public CalendarView setDateBottomTextSize(float bottomTextSize) {
        if (bottomTextSize != -1) {
            mDateBottomTextSize = bottomTextSize;
        }
        return this;
    }

    public CalendarView setWeekTextSize(float weekTextSize) {
        if (weekTextSize != -1) {
            mWeekTextSize = weekTextSize;
        }
        return this;
    }

    public CalendarView setHolidayTextSize(float holidayTextSize) {
        if (holidayTextSize != -1) {
            mHolidayTextSize = holidayTextSize;
        }
        return this;
    }

    public CalendarView setReplenish(boolean replenish) {
        this.mReplenish = replenish;
        return this;
    }

    public CalendarView setUseAnimation(boolean useAnimation) {
        this.mUseAnimation = useAnimation;
        return this;
    }


    static class WeekView {
        static float paddingTop;
        static float size;
        static Paint paint = new Paint();
        static int offset;
        int index;

        private WeekView(int index) {
            this.index = index;
        }

        static {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
        }

        void onDraw(Canvas canvas) {
            float x = (index + 0.5f) * size;
            float y = size / 3 + paddingTop;
            canvas.drawText(WEEK_ARRAY[(index + offset) % WEEK_ARRAY.length], x, y, paint);
        }
    }

    private class AbstractDateView {

        private Paint paint = new Paint();
        private int index;
        private int viewType;
        private RectF rect;
        private IDay day;

        private AbstractDateView(int index, IDay day, int viewType) {
            this.index = index;
            this.day = day;
            this.viewType = viewType;
        }

        private void init() {
            int i = index / 7;
            int j = index % 7;
            float left = mDateWidth * j;
            float top = i * mDateHeight + WeekView.size / 2 + (2 * i + 1) * mDateMargin + getPaddingTop();
            float right = left + mDateWidth;
            float bottom = top + mDateHeight;
            rect = new RectF(left, top, right, bottom);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
        }

        private void onDraw(Canvas canvas) {
            if (viewType != DATE_VIEW_TYPE_CURRENT) {
                if (mReplenish) {
                    drawOtherMonthDay(canvas);
                }
            } else {
                drawCurrentDay(canvas);
                if (isSelected()) {
                    if (mUseAnimation) {
                        drawSelectCircle(canvas, mSelectProgress);
                    } else {
                        drawSelectCircle(canvas);
                    }
                }
            }
        }

        private boolean isSelected() {
            return index == mSelectPos;
        }

        private void drawSelectCircle(Canvas canvas) {
            paint.setColor(mDateCircleColor);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(rect.centerX(), rect.centerY(), mDateCircleSize / 2, paint);
        }

        private void drawSelectCircle(Canvas canvas, int progress) {
            paint.setColor(mDateCircleColor);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);

            float radius = mDateCircleSize / 2;
            float cx = rect.centerX();
            float cy = rect.centerY();
            RectF rectF = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawArc(rectF, 0, (float) (progress * 360 / 100), false, paint);
        }

        private void drawOtherMonthDay(Canvas canvas) {
            paint.setColor(Color.argb(110, 0, 0, 0));
            paint.setTextSize(mDateTextSize);
            float textY = rect.centerY() + mDateTextSize / 2;
            canvas.drawText(String.valueOf(day.getDayOfMonth()), rect.centerX(), textY, paint);
        }

        private void drawCurrentDay(Canvas canvas) {
            paint.setTextSize(mDateTextSize);
            if (day.isToday()) {
                drawToday(canvas);
            } else if (isWeekend(day)) {
                drawWeekend(canvas);
            } else {
                drawNormalDay(canvas);
            }
        }

        private boolean isWeekend(IDay day) {
            return day.getDayOfWeek() == 1 || day.getDayOfWeek() == 7;
        }

        private void drawWeekend(Canvas canvas) {
            drawCurrentDayReal(canvas, mWeekendTextColor, mHolidayTextColor, Color.WHITE);
        }


        private void drawNormalDay(Canvas canvas) {
            drawCurrentDayReal(canvas, mDateTextColor, mHolidayTextColor, Color.WHITE);
        }

        private void drawToday(Canvas canvas) {
            drawTodayCircle(canvas);
            drawCurrentDayReal(canvas, mTodayTextColor, mTodayTextColor, mDateCircleColor);
        }

        private void drawTodayCircle(Canvas canvas) {
            paint.setColor(mDateCircleColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(rect.centerX(), rect.centerY(), mDateCircleSize / 2, paint);
        }

        private void drawCurrentDayReal(Canvas canvas, int dateTextColor, int holidayTextColor, int holidayBgColor) {
            drawNumber(canvas, dateTextColor);
            drawBottomText(canvas);
            if (day.hasEvent()) {
                drawEventRect(canvas);
            }
            if (day.isHoliday()) {
                drawHoliday(canvas, holidayTextColor, holidayBgColor);
            }
            if (day.isWorkday()) {
                drawWorkday(canvas);
            }
            if (day.hasBirthday()) {
                drawBirthday(canvas);
            }
        }

        private void drawNumber(Canvas canvas, int color) {
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(String.valueOf(day.getDayOfMonth()), rect.centerX(), rect.centerY(), paint);
        }

        private void drawBottomText(Canvas canvas) {
            paint.setTextSize(mDateBottomTextSize);
            float bottomTextY = rect.centerY() + rect.height() / 4 + mDateBottomTextSize / 2; //将底部文居中在在3/4处
            canvas.drawText(day.getBottomText(), rect.centerX(), bottomTextY, paint);
        }

        private void drawEventRect(Canvas canvas) {
            float eventRectLeft = rect.centerX() + rect.width() / 6;
            canvas.drawRect(eventRectLeft, rect.centerY(),
                    eventRectLeft + mEventRectSize, rect.centerY() + mEventRectSize, paint);
        }

        private void drawHoliday(Canvas canvas, int holidayTextColor, int holidayBgColor) {
            float textX = rect.centerX() + rect.width() / 4;
            float textY = rect.centerY() - mDateTextSize / 2;
            paint.setColor(holidayBgColor);
            canvas.drawCircle(textX, textY - mHolidayTextSize / 2, mHolidayTextSize * 3 / 4, paint);
            paint.setTextSize(mHolidayTextSize);
            paint.setColor(holidayTextColor);
            canvas.drawText("假", textX, textY, paint);
        }

        private void drawWorkday(Canvas canvas) {
            float textX = rect.centerX() + rect.width() / 4;
            float textY = rect.centerY() - mDateTextSize / 2;
            paint.setColor(mDateCircleColor);
            canvas.drawCircle(textX, textY - mHolidayTextSize / 2, mHolidayTextSize * 3 / 4, paint);
            paint.setTextSize(mHolidayTextSize);
            paint.setColor(mWeekdayTextColor);
            canvas.drawText("班", textX, textY, paint);
        }

        private void drawBirthday(Canvas canvas) {
            Drawable drawable = getResources().getDrawable(R.drawable.circle);
            int centerX = (int) rect.centerX();
            int centerY = (int) rect.centerY();
            int r = (int) (mDateCircleSize / 2);
            drawable.setBounds(centerX - r, centerY - r, centerX + r, centerY + r);
            drawable.draw(canvas);
        }

    }

    public interface IDay {

        int getDayOfMonth();

        String getBottomText();

        boolean isToday();

        int getDayOfWeek();

        boolean isHoliday();

        boolean isWorkday();

        boolean hasEvent();

        boolean hasBirthday();

    }

    private class OtherMonthDay implements IDay {

        private int dayOfMonth;

        private OtherMonthDay(int dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
        }

        @Override
        public int getDayOfMonth() {
            return dayOfMonth;
        }

        @Override
        public String getBottomText() {
            return null;
        }

        @Override
        public boolean isToday() {
            return false;
        }

        @Override
        public int getDayOfWeek() {
            return 0;
        }

        @Override
        public boolean isHoliday() {
            return false;
        }

        @Override
        public boolean isWorkday() {
            return false;
        }

        @Override
        public boolean hasEvent() {
            return false;
        }

        @Override
        public boolean hasBirthday() {
            return false;
        }
    }

    public interface OnDayClickListener {
        void onDaySelected(int position, IDay day);

        void onNextMonthClick(int dayOfMonth);

        void onPrevMonthClick(int dayOfMonth);
    }
}
