package top.soyask.calendarii.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import top.soyask.calendarii.R;

public class PacManView extends View {
    private Paint mTextPaint = new TextPaint();
    private Paint mPaint = new Paint();
    private RectF mFace = new RectF();
    private int mPacManColor;
    private int mPacManEyeColor;
    private int mPacManSize;
    private String mText;
    private float mTextWidth;
    private int mTextSize;
    private int mOffsetX;
    private int mAngle = 0;
    private volatile boolean mIsAnimationStart;
    private PacManAnimationCallback mCallback;
    private PacManThread mPacManThread;

    public PacManView(Context context) {
        this(context, null);
    }

    public PacManView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PacManView);
        mPacManColor = typedArray.getColor(R.styleable.PacManView_cv_pacManColor, 0);
        mPacManEyeColor = typedArray.getColor(R.styleable.PacManView_cv_pacManEyeColor, 0);
        mPacManSize = typedArray.getDimensionPixelSize(R.styleable.PacManView_cv_pacManSize, 0);
        int textColor = typedArray.getColor(R.styleable.PacManView_cv_textColor, Color.parseColor("#dd000000"));
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.PacManView_cv_textSize, 0);
        mText = typedArray.getString(R.styleable.PacManView_cv_text);
        typedArray.recycle();
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);
        mTextWidth = mTextPaint.measureText(mText);
        mPaint.setAntiAlias(true);
    }

    public void start() {
        if (mPacManThread != null) {
            mPacManThread.interrupt();
        }
        mPacManThread = new PacManThread();
        mPacManThread.start();
        mIsAnimationStart = true;
    }

    public void stop() {
        mIsAnimationStart = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = getWidth() / 2;
        int y = getHeight() / 2;

        float left = x - mTextWidth / 2 - mPacManSize + mOffsetX;
        int top = y - mPacManSize / 2 - mTextSize / 2;
        mFace.set(left, top, left + mPacManSize, top + mPacManSize);
        mPaint.setColor(mPacManColor);
        canvas.drawArc(mFace, 45f - mAngle, 270f + 2 * mAngle, true, mPaint);
        mPaint.setColor(mPacManEyeColor);
        canvas.drawCircle(left + mPacManSize / 2, top + mPacManSize / 4, 5, mPaint);
        canvas.clipRect(left + mPacManSize / 2, 0, x + mTextWidth, getHeight());
        canvas.drawText(mText, x, y, mTextPaint);
    }

    public void setCallback(PacManAnimationCallback callback) {
        this.mCallback = callback;
    }

    public class PacManThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                if (!mIsAnimationStart) {
                    return;
                }
                if (mOffsetX > mTextWidth + mPacManSize) {
                    break;
                }
                mOffsetX += 5;
                mAngle = (mAngle + 3) % 45;
                postInvalidate();
            }
            mAngle = 0;
            mOffsetX = 0;
            postInvalidate();
            mCallback.onEnd();
        }
    }

    public interface PacManAnimationCallback {
        void onEnd();
    }
}
