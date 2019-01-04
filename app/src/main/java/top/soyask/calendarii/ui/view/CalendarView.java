package top.soyask.calendarii.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Symbol;
import top.soyask.calendarii.utils.DayUtils;

/**
 * Created by mxf on 2018/5/7.
 */

public class CalendarView extends View implements ValueAnimator.AnimatorUpdateListener {

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
    private Path mSelectPath = new Path();
    private Paint mSelectPaint;
    private Set<Runnable> mPendingList = new HashSet<>();

    private WeekView[] mWeekViews = new WeekView[7];
    private AbstractDateView[][] mDateViews = new AbstractDateView[6][7];
    private PathHelper mPathHelper;
    private ValueAnimator mSelectAnimator;
    private int mTouchSlop;


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

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
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

    private void initWeekViews() {
        mDateWidth = mDisplayWidth / 7;
        for (int i = 0; i < mWeekViews.length; i++) {
            mWeekViews[i] = new WeekView(i, mDisplayWidth / 7);
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
        if (mUseAnimation) {
            drawSelectCircle(canvas);
        }
    }

    private void drawSelectCircle(Canvas canvas) {
        if (mSelectPaint == null) {
            mSelectPaint = new Paint();
            mSelectPaint.setColor(mDateCircleColor);
            mSelectPaint.setStrokeWidth(3);
            mSelectPaint.setAntiAlias(true);
            mSelectPaint.setStyle(Paint.Style.STROKE);
        }
        canvas.drawPath(mSelectPath, mSelectPaint);
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
                    moved = Math.abs(event.getRawX() - mStartX) > mTouchSlop || Math.abs(event.getRawY() - mStartY) > mTouchSlop;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!moved) {
                    float x = event.getX();
                    float y = event.getY();
                    int weekHeight = mDisplayWidth / 14;
                    float v = y - weekHeight - mDateMargin - getPaddingTop();
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
        if (mUseAnimation) {
            analyzeSelectDate(position);
            startSelectAnimation();
        }
        mSelectPos = position;
        if (mListener != null) {
            IDay day = mDays.get(position - mFirstDayOfWeek);
            mListener.onDaySelected(position, day);
        }

    }

    private void analyzeSelectDate(int position) {
        int i = position / 7;
        int j = position % 7;
        AbstractDateView target = mDateViews[i][j];
        float ocx;
        float ocy;
        float r = mDateCircleSize / 2;
        float tcx = target.rect.centerX();
        float tcy = target.rect.centerY();

        if (mSelectPos == -1) {
            mSelectPos = position;
        }
        i = mSelectPos / 7;
        j = mSelectPos % 7;
        AbstractDateView old = mDateViews[i][j];
        ocx = old.rect.centerX();
        ocy = old.rect.centerY();
        int hor = (int) (tcx - ocx);//hor>0 新的在旧的右边
        int ver = (int) (tcy - ocy);//ver>0 新的在旧的下面
        int h;
        int v;
        if (Math.abs(hor) > Math.abs(ver)) { //左右
            h = 0;
            v = -1;
        } else {
            h = 1;
            v = 0;
        }
        mPathHelper = new PathHelper(ocx, ocy
                , tcx, tcy, ocx + r * h, ocy + r * v, tcx + r * h, tcy + r * v);
        mPathHelper.angle = v * 90;
        mPathHelper.flag = hor > 0 || ver > 0 ? 1 : -1;
        mPathHelper.flag = hor < 0 && ver > 0 && h == 0 ? -1 : mPathHelper.flag;
        mPathHelper.flag = hor > 0 && ver < 0 && h != 0 ? -1 : mPathHelper.flag;
    }

    @Override
    public void startAnimation(Animation animation) {
        super.startAnimation(animation);
    }

    private void startSelectAnimation() {
        if (mSelectAnimator != null && mSelectAnimator.isRunning()) {
            mSelectAnimator.cancel();
        }
        mSelectAnimator = ValueAnimator.ofInt(0, 150);
        mSelectAnimator.addUpdateListener(this);
        mSelectAnimator.setDuration(300);
        mSelectAnimator.start();
    }


    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        synchronized (CalendarView.this) {
            int i = (int) animation.getAnimatedValue();
            float r = mDateCircleSize / 2;
            mSelectPath.reset();

            if (i < 100) {
                mSelectPath.addArc(mPathHelper.center0X - r, mPathHelper.center0Y - r,
                        mPathHelper.center0X + r, mPathHelper.center0Y + r, mPathHelper.angle, mPathHelper.flag * 3.6f * (i - 100));
            }
            if (i > 100 && i < 150) {
                mSelectPath.moveTo(mPathHelper.t0X, mPathHelper.t0Y);
                float p = (150 - i) / 50f;
                float x = mPathHelper.t1X - (mPathHelper.t1X - mPathHelper.t0X) * p;
                float y = mPathHelper.t1Y - (mPathHelper.t1Y - mPathHelper.t0Y) * p;
                mSelectPath.moveTo(x, y);
                mSelectPath.lineTo(mPathHelper.t1X, mPathHelper.t1Y);
            } else if (i < 50) {
                mSelectPath.moveTo(mPathHelper.t0X, mPathHelper.t0Y);
                float p = i / 50f;
                float x = mPathHelper.t0X + (mPathHelper.t1X - mPathHelper.t0X) * p;
                float y = mPathHelper.t0Y + (mPathHelper.t1Y - mPathHelper.t0Y) * p;
                mSelectPath.lineTo(x, y);
            } else if (i < 150) {
                mSelectPath.moveTo(mPathHelper.t0X, mPathHelper.t0Y);
                mSelectPath.lineTo(mPathHelper.t1X, mPathHelper.t1Y);
            }
            if (i > 50) {
                mSelectPath.addArc(mPathHelper.center1X - r, mPathHelper.center1Y - r,
                        mPathHelper.center1X + r, mPathHelper.center1Y + r, mPathHelper.angle, mPathHelper.flag * 3.6f * (i - 50));
            }
            postInvalidate();
        }
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


    private class WeekView {
        private float paddingTop;
        private float size;
        private Paint paint = new Paint();
        private int index;

        private WeekView(int index, int size) {
            this.index = index;
            this.size = size;
            this.paddingTop = getPaddingTop();
            paint.setTextSize(mWeekTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
        }


        void onDraw(Canvas canvas) {
            float x = (index + 0.5f) * size;
            float y = size / 3 + paddingTop;
            canvas.drawText(WEEK_ARRAY[(index + mFirstDayOffset) % WEEK_ARRAY.length], x, y, paint);
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
            int weekHeight = mDisplayWidth / 14;
            float top = i * mDateHeight + weekHeight + (2 * i + 1) * mDateMargin + getPaddingTop();
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
                if (!mUseAnimation && isSelected()) {
                    drawSelectCircle(canvas);
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

        @Deprecated
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
                drawEventSymbol(canvas);
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

        private void drawEventSymbol(Canvas canvas) {
            float symbolCenterX = rect.centerX() + rect.width() / 6;
            float symbolCenterY = rect.centerY() + mEventRectSize / 2;
            Symbol symbol = day.getSymbol();
            switch (symbol) {
                case TRIANGLE:
                    drawEventTriangle(canvas, symbolCenterX, symbolCenterY);
                    break;
                case CIRCLE:
                    drawEventCircle(canvas, symbolCenterX, symbolCenterY);
                    break;
                case STAR:
                    drawEventStar(canvas, symbolCenterX, symbolCenterY);
                    break;
                case HEART:
                    drawEventHeart(canvas, symbolCenterX, symbolCenterY);
                    break;
                default:
                    drawEventRect(canvas, symbolCenterX, symbolCenterY);
            }
        }


        private void drawEventRect(Canvas canvas, float centerX, float centerY) {
            float left = centerX - mEventRectSize / 2;
            float right = centerX + mEventRectSize / 2;
            float top = centerY - mEventRectSize / 2;
            float bottom = centerY + mEventRectSize / 2;
            canvas.drawRect(left, top, right, bottom, paint);
        }

        private void drawEventCircle(Canvas canvas, float centerX, float centerY) {
            canvas.drawCircle(centerX, centerY, mEventRectSize / 3 * 2, paint);
        }

        private void drawEventStar(Canvas canvas, float centerX, float centerY) {
            float starR = mEventRectSize;
            Path path = new Path();
            path.reset();
            float offsetX = centerX - mEventRectSize;
            float offsetY = centerY - mEventRectSize;
            path.moveTo(offsetX, offsetY + starR * 0.73f);
            path.lineTo(offsetX + starR * 2, offsetY + starR * 0.73f);
            path.lineTo(offsetX + starR * 0.38f, offsetY + starR * 1.9f);
            path.lineTo(offsetX + starR, offsetY + 0);
            path.lineTo(offsetX + starR * 1.62f, offsetY + starR * 1.9f);
            path.lineTo(offsetX + 0, offsetY + starR * 0.73f);
            path.close();
            canvas.drawPath(path, paint);
        }

        private void drawEventHeart(Canvas canvas, float centerX, float centerY) {
            float heartSize = mEventRectSize * 2;
            centerX += mEventRectSize * 0.5f; //向右移动一点儿
            float left = centerX - mEventRectSize * 1f;
            float right = centerX + mEventRectSize * 1f;

            Path path = new Path();
            path.moveTo(centerX, centerY);
            float leftControlX0 = left + heartSize * 0.15f;
            float leftControlY0 = centerY - heartSize * 0.25f;
            float leftControlX1 = left;
            float leftControlY1 = centerY + heartSize * 0.25f;
            path.cubicTo(leftControlX0, leftControlY0, leftControlX1, leftControlY1, centerX, centerY + heartSize * 0.5f);

            path.moveTo(centerX, centerY + heartSize * 0.5f);
            float rightControlX0 = right;
            float rightControlY0 = centerY + heartSize * 0.25f;
            float rightControlX1 = right - heartSize * 0.15f;
            float rightControlY1 = centerY - heartSize * 0.25f;
            path.cubicTo(rightControlX0, rightControlY0, rightControlX1, rightControlY1, centerX, centerY);
            path.close();
            canvas.drawPath(path, paint);
        }


        private void drawEventTriangle(Canvas canvas, float centerX, float centerY) {
            Path path = new Path();
            float sqrt = (float) Math.sqrt(3) * 0.5f;
            float top = centerY - mEventRectSize * sqrt;
            float bottom = centerY + mEventRectSize * 0.5f;
            float left = centerX - mEventRectSize * sqrt;
            float right = centerX + mEventRectSize * sqrt;

            path.moveTo(left, bottom);
            path.lineTo(right, bottom);
            path.lineTo(centerX, top);
            path.close();
            canvas.drawPath(path, paint);
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

        Symbol getSymbol();

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

        @Override
        public Symbol getSymbol() {
            return null;
        }
    }

    public interface OnDayClickListener {
        void onDaySelected(int position, IDay day);

        void onNextMonthClick(int dayOfMonth);

        void onPrevMonthClick(int dayOfMonth);
    }

    static class PathHelper {
        final float center0X;
        final float center0Y;
        final float center1X;
        final float center1Y;
        final float t0X;
        final float t0Y;
        final float t1X;
        final float t1Y;
        int flag;
        float angle;


        PathHelper(float center0X, float center0Y, float center1X, float center1Y, float t0X, float t0Y, float t1X, float t1Y) {
            this.center0X = center0X;
            this.center0Y = center0Y;
            this.center1X = center1X;
            this.center1Y = center1Y;
            this.t0X = t0X;
            this.t0Y = t0Y;
            this.t1X = t1X;
            this.t1Y = t1Y;
        }
    }
}
