package coding.yu.codespace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.Nullable;

import coding.yu.codespace.highlight.ColorStyle;
import coding.yu.codespace.highlight.LightColor;
import coding.yu.codespace.lex.TokenType;
import coding.yu.codespace.touch.TouchGestureListener;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpace extends View {

    private static final int DEFAULT_TEXT_SIZE_SP = 16;

    private InputConnection mInputConnection;
    private GestureDetector mGestureDetector;

    private Document mDocument = new Document();

    private Paint mLineBackgroundPaint = new Paint();
    private Paint mSelectPaint = new Paint();
    private Paint mTextPaint = new Paint();
    private Paint mCursorPaint = new Paint();

    private int mCharWidth;
    private int mSpaceWidth;

    private ColorStyle mColorStyle = new LightColor();

    public CodeSpace(Context context) {
        super(context);
        init();
    }

    public CodeSpace(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CodeSpace(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mGestureDetector = TouchGestureListener.setup(this);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(sp2px(DEFAULT_TEXT_SIZE_SP));
        mTextPaint.setTypeface(Typeface.MONOSPACE);

        mLineBackgroundPaint.setColor(0x33ff0000);

        mCharWidth = (int) mTextPaint.measureText("x");
        mSpaceWidth = (int) mTextPaint.measureText(" ");

        setFocusable(true);
        setFocusableInTouchMode(true);
        setLongClickable(true);
        setHapticFeedbackEnabled(true);
        requestFocus();
    }

    public Document getDocument() {
        return mDocument;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                | EditorInfo.IME_ACTION_DONE
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        if (mInputConnection == null) {
            mInputConnection = new CodeSpaceInputConnection(this);
        }
        return mInputConnection;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFocused()) {
            mGestureDetector.onTouchEvent(event);
        } else {
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                requestFocus();
            }
        }
        return true;
    }

    private int getRowHeight() {
        Paint.FontMetricsInt metrics = mTextPaint.getFontMetricsInt();
        return (metrics.descent - metrics.ascent);
    }

    public int getPaintBaseline(int row) {
        Paint.FontMetricsInt metrics = mTextPaint.getFontMetricsInt();
        return (row + 1) * getRowHeight() - metrics.descent;
    }

    private int getCharWidth(char c) {
        if (c == '\n') {
            return mSpaceWidth;
        }

        if (c == '\t') {
            return mSpaceWidth * 4;
        }

        if (c == ' ') {
            return mSpaceWidth;
        }
        char[] ca = {c};
        return (int) mTextPaint.measureText(ca, 0, 1);
    }


    //////////////////////   Draw everything  //////////////////////

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        realDraw(canvas);
    }

    private void realDraw(Canvas canvas) {
        int needLineBgIndex = mDocument.findLineByOffset(mDocument.getCursorPosition());
        int needSelectionIndex = 0;

        Log.e("Yu", "needLineBgIndex:" + needLineBgIndex);

        mDocument.resetLastToken();
        for (int i = 0; i < mDocument.getLineCount(); i++) {
            if (i == needLineBgIndex) {
                drawLineBackground(canvas, needLineBgIndex);
            }
            drawSelection(canvas);
            drawLineText(canvas, i);
            drawCursor(canvas, i);
        }
    }

    private void drawLineText(Canvas canvas, int lineIndex) {
        int x = 0;
        int y = getPaintBaseline(lineIndex);
        String lineStr = mDocument.getLineText(lineIndex);
        for (int i = 0; i < lineStr.length(); i++) {
            char c = lineStr.charAt(i);
            TokenType tokenType = mDocument.getTokenTypeByLineOffset(lineIndex, i);
            Log.e("Yu", "tokenType:" + tokenType);
            drawChar(canvas, c, x, y, tokenType);
            x = x + getCharWidth(c);
        }
    }

    private void drawChar(Canvas canvas, char c, int x, int y, TokenType tokenType) {
        char[] chars = {c};
        if (tokenType == TokenType.KEYWORD2) {
            mTextPaint.setColor(mColorStyle.getKeyword2Color());
        } else if (tokenType == TokenType.KEYWORD) {
            mTextPaint.setColor(mColorStyle.getKeywordColor());
        } else if (tokenType == TokenType.TYPE) {
            mTextPaint.setColor(mColorStyle.getTypeColor());
        } else if (tokenType == TokenType.STRING) {
            mTextPaint.setColor(mColorStyle.getStringColor());
        } else if (tokenType == TokenType.COMMENT) {
            mTextPaint.setColor(mColorStyle.getCommentColor());
        } else {
            mTextPaint.setColor(mColorStyle.getCommonTextColor());
        }
        canvas.drawText(chars, 0, 1, x, y, mTextPaint);
    }

    private void drawLineBackground(Canvas canvas, int lineIndex) {
        canvas.drawRect(0,
                lineIndex * getRowHeight(),
                Integer.MAX_VALUE,
                (lineIndex + 1) * getRowHeight(),
                mLineBackgroundPaint);
    }

    private void drawSelection(Canvas canvas) {

    }

    private void drawCursor(Canvas canvas, int lineIndex) {

    }


    //////////////////////   sp2px  //////////////////////

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
