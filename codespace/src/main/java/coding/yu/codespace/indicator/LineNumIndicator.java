package coding.yu.codespace.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;

import coding.yu.codespace.R;

/**
 * Created by yu on 10/24/2019.
 */
public class LineNumIndicator extends BaseIndicator {

    private static final int COLOR_LINE_BACKGROUND = 0x10333333;
    private static final int COLOR_TEXT = 0xff999999;

    private int mLineCount = 1;
    private int mCurrentIndex;
    private float mRowHeight;

    private Paint mPaint = new Paint();
    private Paint mBackgroundPaint = new Paint();

    private float mWidth = 0;
    private Rect mBackgroundRect = new Rect();

    public LineNumIndicator(Context context) {
        super(context);
        init();
    }

    public LineNumIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineNumIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(COLOR_TEXT);
        mPaint.setTextSize(20);
        mPaint.setTypeface(Typeface.MONOSPACE);

        mBackgroundPaint.setColor(COLOR_LINE_BACKGROUND);
    }

    @Override
    public void updateSizeInfo(float textSizePx, float rowHeight) {
        mPaint.setTextSize(textSizePx - 10);
        mRowHeight = rowHeight;
        requestLayout();
    }

    @Override
    public void updateLineCount(int count) {
        mLineCount = count;
        requestLayout();
    }

    @Override
    public void currentLineIndex(int index) {
        mCurrentIndex = index;
        invalidate();
    }

    @Override
    public void onCodeSpaceScroll(int x, int y) {
        scrollTo(0, y);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = mPaint.measureText(String.valueOf(mLineCount)) + getPaddingStart() + getPaddingEnd();
        float height = mRowHeight * mLineCount + getPaddingTop() + getPaddingEnd();
        setMeasuredDimension((int) mWidth, (int) height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mLineCount; i++) {
            String s = String.valueOf(i + 1);
            float textWidth = mPaint.measureText(s);
            int x = (int) (getPaddingStart() + (getWidth() - getPaddingStart() - getPaddingEnd() - textWidth) / 2);
            int y = getPaintBaseline(i) + getPaddingTop();

            if (i == mCurrentIndex) {
                int rX = 0;
                int rY = (int) (mRowHeight * i + getPaddingTop());

                mBackgroundRect.set(rX, rY, (int) (rX + mWidth), (int) (rY + mRowHeight));
                canvas.drawRect(mBackgroundRect, mBackgroundPaint);
            }

            canvas.drawText(s, x, y, mPaint);
        }
    }

    private int getTextRowHeight() {
        Paint.FontMetricsInt metrics = mPaint.getFontMetricsInt();
        return (metrics.bottom - metrics.top);
    }

    public int getPaintBaseline(int row) {
        Paint.FontMetricsInt metrics = mPaint.getFontMetricsInt();
        return (int) (row * mRowHeight + (mRowHeight - getTextRowHeight()) / 2 - metrics.top);
    }
}
