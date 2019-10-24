package coding.yu.codespace.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by yu on 10/24/2019.
 */
public class LineNumIndicator extends BaseIndicator {

    private int mLineCount = 1;
    private int mCurrentIndex;

    private Paint mPaint = new Paint();

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
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(20);
        mPaint.setTypeface(Typeface.MONOSPACE);
    }

    @Override
    public void updateSizeInfo(float textSizePx) {
        mPaint.setTextSize(textSizePx);
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
        float width = mPaint.measureText(String.valueOf(mLineCount)) + getPaddingStart() + getPaddingEnd();
        float height = getRowHeight() * mLineCount + getPaddingTop() + getPaddingEnd();
        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mLineCount; i++) {
            String s = String.valueOf(i + 1);
            float textWidth = mPaint.measureText(s);
            int x = (int) (getWidth() - getPaddingStart() - getPaddingEnd() - textWidth) / 2;
            int y = getPaintBaseline(i) + getPaddingTop();
            canvas.drawText(s, x, y, mPaint);
        }
    }

    private int getRowHeight() {
        Paint.FontMetricsInt metrics = mPaint.getFontMetricsInt();
        return (metrics.bottom - metrics.top);
    }

    public int getPaintBaseline(int row) {
        Paint.FontMetricsInt metrics = mPaint.getFontMetricsInt();
        return (row + 1) * getRowHeight() - metrics.bottom;
    }
}
