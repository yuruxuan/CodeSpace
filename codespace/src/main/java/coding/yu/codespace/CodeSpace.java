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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

import coding.yu.codespace.highlight.ColorStyle;
import coding.yu.codespace.highlight.LightColor;
import coding.yu.codespace.lex.TokenType;
import coding.yu.codespace.touch.TouchGestureListener;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpace extends View implements Document.CursorMoveCallback {

    private static final int DEFAULT_TEXT_SIZE_SP = 16;
    private static final int DEFAULT_CURSOR_WIDTH_DP = 2;

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

    private InputMethodManager mInputMethodManager;

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
        mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mGestureDetector = TouchGestureListener.setup(this);
        mDocument.setCursorMoveCallback(this);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(sp2px(DEFAULT_TEXT_SIZE_SP));
        mTextPaint.setTypeface(Typeface.MONOSPACE);

        mLineBackgroundPaint.setColor(0x33ff0000);

        mCursorPaint.setColor(0x9900ff00);

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

    /**
     * Android Develop Doc:
     * Editor authors, you need to call this method whenever the cursor moves in your editor.
     * Remember that in addition to doing this, your editor needs to always supply current
     * cursor values in EditorInfo#initialSelStart and EditorInfo#initialSelEnd every time
     * View.onCreateInputConnection(EditorInfo) is called, which happens whenever the keyboard
     * shows up or the focus changes to a text field, among other cases.
     */
    @Override
    public void onCursorMoved(int start, int end) {
        mInputMethodManager.updateSelection(this, start, end, -1, -1);
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
        int cursorLineIndex = mDocument.findLineForDraw(mDocument.getCursorPosition());

        Log.e("Yu", "needLineBgIndex:" + cursorLineIndex);

        mDocument.resetLastToken();
        for (int i = 0; i < mDocument.getLineCountForDraw(); i++) {
            if (i == cursorLineIndex) {
                drawLineBackground(canvas, cursorLineIndex);
            }
            drawSelection(canvas);
            drawLineText(canvas, i);

            if (i == cursorLineIndex) {
                drawCursor(canvas, cursorLineIndex);
            }
        }
    }

    private void drawLineText(Canvas canvas, int lineIndex) {
        int x = 0;
        int y = getPaintBaseline(lineIndex);
        String lineStr = mDocument.getLineText(lineIndex);
        for (int i = 0; i < lineStr.length(); i++) {
            char c = lineStr.charAt(i);
            TokenType tokenType = mDocument.getTokenTypeByLineOffset(lineIndex, i);
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
        int count = 0;
        for (int i = 0; i < lineIndex; i++) {
            count += mDocument.getLineText(i).length();
        }
        int lineOffset = mDocument.getCursorPosition() - count;

        int x = 0;
        String lineStr = mDocument.getLineText(lineIndex);
        for (int i = 0; i < lineOffset; i++) {
            char c = lineStr.charAt(i);
            x = x + getCharWidth(c);
        }

        int cursorWidth = dp2px(DEFAULT_CURSOR_WIDTH_DP);
        canvas.drawRect(x - cursorWidth / 2,
                lineIndex * getRowHeight(),
                x + cursorWidth / 2,
                (lineIndex + 1) * getRowHeight(),
                mCursorPaint);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mDocument.handleKeyEvent(event);
        invalidate();
        return true;
    }

    //////////////////////   Utils  //////////////////////

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
