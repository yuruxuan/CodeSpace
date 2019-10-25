package coding.yu.codespace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import coding.yu.codespace.color.ColorStyle;
import coding.yu.codespace.color.LightColor;
import coding.yu.codespace.handle.InsertionHandleView;
import coding.yu.codespace.handle.SelectionLeftHandleView;
import coding.yu.codespace.handle.SelectionRightHandleView;
import coding.yu.codespace.ime.IMEHelper;
import coding.yu.codespace.indicator.BaseIndicator;
import coding.yu.codespace.lex.TokenType;
import coding.yu.codespace.touch.TouchGestureListener;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpace extends View implements Document.OffsetMeasure, Document.TextChangeListener {

    private static final int DEFAULT_TEXT_SIZE_SP = 16;
    private static final int DEFAULT_CURSOR_WIDTH_DP = 2;
    private static final int DEFAULT_COMPOSING_UNDERLINE_WIDTH_DP = 1;
    private static final int COMPOSING_UNDERLINE_TEXT_SPACE_PX = 1;
    private static final int SEARCH_FORWARD_BEHIND_COUNT = 6;

    private CodeSpaceInputConnection mInputConnection;
    private GestureDetector mGestureDetector;

    private Document mDocument = new Document();
    private ColorStyle mColorStyle = new LightColor();

    private ActionCallbackCompat mActionCallback = new ActionCallbackCompat();
    private ActionMode mActionMode;

    // Contain max selection region and two handle.
    private Rect mSelectionRegion = new Rect();

    private Rect mCursorRect = new Rect();
    private Rect mSelectionTop = new Rect();
    private Rect mSelectionMiddle = new Rect();
    private Rect mSelectionBottom = new Rect();

    private InsertionHandleView mInsertionHandle;
    private SelectionLeftHandleView mSelectionLeftHandle;
    private SelectionRightHandleView mSelectionRightHandle;
    private Point mSelectionLeftHandlePoint = new Point();
    private Point mSelectionRightHandlePoint = new Point();


    private Paint mLineBackgroundPaint = new Paint();
    private Paint mSelectionPaint = new Paint();
    private Paint mTextPaint = new Paint();
    private Paint mCursorPaint = new Paint();

    private int mSpaceWidth;

    private int mLongestLineWidth = 0;
    private int mContentHeight = 0;

    private InputMethodManager mInputMethodManager;

    private BaseIndicator mBaseIndicator;

    public CodeSpace(Context context) {
        super(context);
        init();
    }

    public CodeSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CodeSpace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mGestureDetector = TouchGestureListener.setup(this);
        mDocument.setOffsetMeasure(this);
        mDocument.setTextChangeListener(this);

        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(sp2px(DEFAULT_TEXT_SIZE_SP));
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setStrokeWidth(dp2px(DEFAULT_COMPOSING_UNDERLINE_WIDTH_DP));

        mLineBackgroundPaint.setColor(getColorStyle().getLineBackgroundColor());
        mSelectionPaint.setColor(getColorStyle().getSelectionColor());
        mCursorPaint.setColor(getColorStyle().getCursorColor());

        mSpaceWidth = (int) mTextPaint.measureText(" ");

        mInsertionHandle = new InsertionHandleView(this, getColorStyle().getCursorHandleColor());
        mInsertionHandle.setOnTouchListener(new InsertionHandleListener());

        mSelectionLeftHandle = new SelectionLeftHandleView(this, getColorStyle().getSelectionHandleColor());
        mSelectionLeftHandle.setOnTouchListener(new SelectionHandleListener(true));

        mSelectionRightHandle = new SelectionRightHandleView(this, getColorStyle().getSelectionHandleColor());
        mSelectionRightHandle.setOnTouchListener(new SelectionHandleListener(false));

        setFocusable(true);
        setFocusableInTouchMode(true);
        setLongClickable(true);
        setHapticFeedbackEnabled(true);
        requestFocus();

        measureRect();
    }

    public void bindLineIndicator(BaseIndicator indicator) {
        mBaseIndicator = indicator;
        if (mBaseIndicator != null) {
            mBaseIndicator.updateSizeInfo(mTextPaint.getTextSize(), getRowHeight());
        }
    }

    public Document getDocument() {
        return mDocument;
    }

    public ColorStyle getColorStyle() {
        return mColorStyle;
    }

    public Rect getCursorRect() {
        return mCursorRect;
    }

    public Rect getCursorRectOnScreen() {
        Rect rect = new Rect();
        Rect cursorRect = new Rect(mCursorRect);
        getGlobalVisibleRect(rect);
        cursorRect.offset(rect.left, rect.top);
        return cursorRect;
    }

    public Point getPointOnScreen(Point p) {
        Point newPoint = new Point(p);
        Rect rect = new Rect();
        getGlobalVisibleRect(rect);
        newPoint.offset(rect.left, rect.top);
        return newPoint;
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
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        if (mInputConnection == null) {
            mInputConnection = new CodeSpaceInputConnection(this);
        }
        mInputConnection.setCurrentIME(IMEHelper.getDefaultInputMethodPkgName(getContext()));
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

    public void notifySelectionChangeInvalidate() {
        // Android Develop Doc:
        // Editor authors, you need to call this method whenever the cursor moves in your editor.
        mInputMethodManager.updateSelection(this, mDocument.getSelectionStart(), mDocument.getSelectionEnd(),
                mDocument.getComposingIndexStart(), mDocument.getComposingIndexEnd());

        measureRect();
        invalidate();
    }

    @Override
    public int getLastLineRelativeOffset() {
        int x = (mCursorRect.left + mCursorRect.right) / 2;
        int y = Math.max(0, (mCursorRect.top + mCursorRect.bottom) / 2 - getRowHeight());
        return getOffsetNearXY(x + getScrollX() - getPaddingStart(), y + getScrollY() - getPaddingTop());
    }

    @Override
    public int getNextLineRelativeOffset() {
        int x = (mCursorRect.left + mCursorRect.right) / 2;
        int y = Math.min(mDocument.getLineCountForDraw() * getRowHeight(),
                (mCursorRect.top + mCursorRect.bottom) / 2 + getRowHeight());
        return getOffsetNearXY(x + getScrollX() - getPaddingStart(), y + getScrollY() - getPaddingTop());
    }

    @Override
    public void onTextChanged(String before, String after) {
        if (mBaseIndicator != null) {
            mBaseIndicator.updateLineCount(mDocument.getLineCountForDraw());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        measureRect();
        scrollFollowCursor();
        updateInsertionHandleIfShown();
    }

    // (x, y) is base on first char rather than screen left-top corner.
    private int getOffsetNearXY(int x, int y) {
        int rowHeight = getRowHeight();

        int targetLineIndex = mDocument.getLineCountForDraw() - 1;
        for (int i = 0; i <= targetLineIndex; i++) {
            if (y < rowHeight * (i + 1)) {
                targetLineIndex = i;
                break;
            }
        }

        return getOffsetNearXLineIndex(x, targetLineIndex);
    }

    private int getOffsetNearXLineIndex(int x, int lineIndex) {
        String lineStr = mDocument.getLineText(lineIndex);

        int tempX = 0;
        int targetLineOffset = 0;
        for (int i = 0; i < lineStr.length(); i++) {
            targetLineOffset = i;

            char c = lineStr.charAt(i);
            tempX = tempX + getCharWidth(c);
            if (tempX > x) {
                break;
            }
        }

        int offset = targetLineOffset;

        for (int i = 0; i < lineIndex; i++) {
            offset += mDocument.getLineText(i).length();
        }

        if (lineIndex == mDocument.getLineCountForDraw() - 1 && tempX < x) {
            offset += 1;
        }

        return offset;
    }

    public int[] getWordNearXY(int x, int y) {
        String str = mDocument.toString();
        int position = getOffsetNearXY(x, y);

        int from = position - SEARCH_FORWARD_BEHIND_COUNT;
        int to = position + SEARCH_FORWARD_BEHIND_COUNT;

        for (int i = position - 1; i >= position - SEARCH_FORWARD_BEHIND_COUNT; i--) {
            if (i < 0 || i >= mDocument.length()) {
                break;
            }
            char c = str.charAt(i);
            if (c == ' ' || c == '\n') {
                from = i + 1;
                break;
            }
        }

        for (int i = position; i < position + SEARCH_FORWARD_BEHIND_COUNT; i++) {
            if (i < 0 || i >= mDocument.length()) {
                break;
            }
            char c = str.charAt(i);
            if (c == ' ' || c == '\n') {
                to = i;
                if (from == to) {
                    from--;
                }
                break;
            }
        }

        if (from < 0) {
            from = 0;
        }

        if (from > mDocument.length()) {
            from = mDocument.length();
        }

        if (to > mDocument.length()) {
            to = mDocument.length();
        }

        return new int[]{from, to};
    }

    private int getRowHeight() {
        Paint.FontMetricsInt metrics = mTextPaint.getFontMetricsInt();
        return (metrics.bottom - metrics.top);
    }

    public int getPaintBaseline(int row) {
        Paint.FontMetricsInt metrics = mTextPaint.getFontMetricsInt();
        return (row + 1) * getRowHeight() - metrics.bottom;
    }

    private int getCharWidth(char c) {
        if (c == '\n') {
            return 0;
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

    // They are contain padding.
    public int getLongestLineWidth() {
        return mLongestLineWidth;
    }

    public int getContentHeight() {
        return mContentHeight;
    }

    public void finishActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public void hideActionMode() {
        if (mActionMode != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActionMode.hide(500);
            }
        }
    }

    public void invalidateActionMode() {
        if (mActionMode != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActionMode.invalidateContentRect();
            }
        }
    }


    //////////////////////   Measure Rect   //////////////////////

    /**
     * We measure the rect which be used before onDraw(),
     * for example: Insertion handle & Selection Handle
     */
    private void measureRect() {
        measureCursorRect();
        measureSelectionRect();
    }

    private void measureCursorRect() {
        int lineIndex = mDocument.findLineForDraw(mDocument.getCursorPosition());
        int count = 0;
        for (int i = 0; i < lineIndex; i++) {
            count += mDocument.getLineText(i).length();
        }
        int lineOffset = mDocument.getCursorPosition() - count;

        int x = getPaddingStart();
        String lineStr = mDocument.getLineText(lineIndex);
        for (int i = 0; i < lineOffset; i++) {
            char c = lineStr.charAt(i);
            x = x + getCharWidth(c);
        }

        int cursorWidth = dp2px(DEFAULT_CURSOR_WIDTH_DP);
        mCursorRect = new Rect(x - cursorWidth / 2,
                lineIndex * getRowHeight() + getPaddingTop(),
                x + cursorWidth / 2,
                (lineIndex + 1) * getRowHeight() + getPaddingTop());

        if (mBaseIndicator != null) {
            if (mDocument.hasSelection()) {
                mBaseIndicator.currentLineIndex(-1);
            } else {
                mBaseIndicator.currentLineIndex(lineIndex);
            }
        }
    }

    private void measureSelectionRect() {
        mSelectionTop.setEmpty();
        mSelectionMiddle.setEmpty();
        mSelectionBottom.setEmpty();

        int startSelect = mDocument.getSelectionStart();
        int endSelect = mDocument.getSelectionEnd();

        if (startSelect == endSelect) {
            return;
        }

        int startLine = mDocument.findLineForDraw(startSelect);
        int endLine = mDocument.findLineForDraw(endSelect);

        if (startLine == endLine) {
            int count = 0;
            for (int i = 0; i < startLine; i++) {
                count += mDocument.getLineText(i).length();
            }
            int startX = 0;
            for (int i = count; i < mDocument.getSelectionStart(); i++) {
                char c = mDocument.toString().charAt(i);
                startX = startX + getCharWidth(c);
            }
            int endX = startX;
            for (int i = mDocument.getSelectionStart(); i < mDocument.getSelectionEnd(); i++) {
                char c = mDocument.toString().charAt(i);
                endX = endX + getCharWidth(c);
            }

            mSelectionMiddle.set(startX + getPaddingStart(),
                    startLine * getRowHeight() + getPaddingTop(),
                    endX + getPaddingStart(),
                    (startLine + 1) * getRowHeight() + getPaddingTop());

            mSelectionLeftHandlePoint.set(mSelectionMiddle.left, mSelectionMiddle.bottom);
            mSelectionRightHandlePoint.set(mSelectionMiddle.right, mSelectionMiddle.bottom);

        } else {
            // 1.Pre draw first select line background.
            int count = 0;
            for (int i = 0; i < startLine; i++) {
                count += mDocument.getLineText(i).length();
            }
            int startX = 0;
            for (int i = count; i < mDocument.getSelectionStart(); i++) {
                char c = mDocument.toString().charAt(i);
                startX = startX + getCharWidth(c);
            }

            mSelectionTop.set(startX + getPaddingStart(),
                    startLine * getRowHeight() + getPaddingTop(),
                    Integer.MAX_VALUE,
                    (startLine + 1) * getRowHeight() + getPaddingTop());

            // 2.Pre draw last select line background.
            count = 0;
            for (int i = 0; i < endLine; i++) {
                count += mDocument.getLineText(i).length();
            }
            int endX = 0;
            for (int i = count; i < mDocument.getSelectionEnd(); i++) {
                char c = mDocument.toString().charAt(i);
                endX = endX + getCharWidth(c);
            }

            mSelectionBottom.set(0,
                    endLine * getRowHeight() + getPaddingTop(),
                    endX + getPaddingStart(),
                    (endLine + 1) * getRowHeight() + getPaddingTop());

            // 3.Pre draw middle select line background.
            if (endLine - startLine > 1) {
                mSelectionMiddle.set(0,
                        (startLine + 1) * getRowHeight() + getPaddingTop(),
                        Integer.MAX_VALUE,
                        endLine * getRowHeight() + getPaddingTop());

            }

            mSelectionLeftHandlePoint.set(mSelectionTop.left, mSelectionTop.bottom);
            mSelectionRightHandlePoint.set(mSelectionBottom.right, mSelectionBottom.bottom);
        }

        // Measure the max selection region
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;

        if (!mSelectionTop.isEmpty()) {
            left = Math.min(left, mSelectionTop.left);
            top = Math.min(top, mSelectionTop.top);
            right = Math.max(right, mSelectionTop.right);
            bottom = Math.max(bottom, mSelectionTop.bottom);
        }
        if (!mSelectionMiddle.isEmpty()) {
            left = Math.min(left, mSelectionMiddle.left);
            top = Math.min(top, mSelectionMiddle.top);
            right = Math.max(right, mSelectionMiddle.right);
            bottom = Math.max(bottom, mSelectionMiddle.bottom);
        }
        if (!mSelectionBottom.isEmpty()) {
            left = Math.min(left, mSelectionBottom.left);
            top = Math.min(top, mSelectionBottom.top);
            right = Math.max(right, mSelectionBottom.right);
            bottom = Math.max(bottom, mSelectionBottom.bottom);
        }
        mSelectionRegion.set(left, top, right, bottom);
    }

    public Rect getSelectionRegion() {
        return mSelectionRegion;
    }


    //////////////////////   Draw everything  //////////////////////

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        realDraw(canvas);
    }

    //TODO:Analyze each line first, and draw every char.
    private void realDraw(Canvas canvas) {
        int cursorLineIndex = mDocument.findLineForDraw(mDocument.getCursorPosition());
        boolean hasSelection = mDocument.hasSelection();

        mDocument.resetLastToken();
        mLongestLineWidth = 0;

        if (hasSelection) {
            drawSelection(canvas);
        }

        int lineCount = mDocument.getLineCountForDraw();
        for (int i = 0; i < lineCount; i++) {
            if (i == cursorLineIndex && !hasSelection) {
                drawLineBackground(canvas, cursorLineIndex);
            }

            drawLineText(canvas, i);

            if (i == cursorLineIndex) {
                if (mDocument.isComposingTextExist()) {
                    drawComposingTextUnderline(canvas, cursorLineIndex);
                }
                if (!hasSelection) {
                    drawCursor(canvas);
                }
            }
        }

        mContentHeight = lineCount * getRowHeight() + getPaddingTop() + getPaddingEnd();
    }

    private void drawLineText(Canvas canvas, int lineIndex) {
        int x = getPaddingStart();
        int y = getPaintBaseline(lineIndex) + getPaddingTop();
        String lineStr = mDocument.getLineText(lineIndex);
        for (int i = 0; i < lineStr.length(); i++) {
            char c = lineStr.charAt(i);
            TokenType tokenType = mDocument.getTokenTypeByLineOffset(lineIndex, i);
            drawChar(canvas, c, x, y, tokenType);
            int charWidth = getCharWidth(c);
            x = x + charWidth;
        }

        if (mLongestLineWidth < x + getPaddingEnd()) {
            mLongestLineWidth = x + getPaddingEnd();
        }
    }

    private void drawComposingTextUnderline(Canvas canvas, int lineIndex) {
        int sum = 0;
        for (int i = 0; i < lineIndex; i++) {
            sum += mDocument.getLineText(i).length();
        }

        int start = mDocument.getComposingIndexStart();
        int end = mDocument.getComposingIndexEnd();
        int cursorPos = mDocument.getCursorPosition();

        if (cursorPos < start || cursorPos > end) {
            return;
        }

        int currLineComposingStart = start - sum;
        int currLineEnd = sum + mDocument.getLineText(lineIndex).length();

        int width = 0;
        int temp = start;
        while (temp >= currLineComposingStart && temp < currLineEnd && temp < end) {
            char c = mDocument.toString().charAt(temp);
            width += getCharWidth(c);
            temp++;
        }

        if (width <= 0) {
            return;
        }

        int startX = getPaddingStart();
        for (int i = 0; i < currLineComposingStart; i++) {
            char c = mDocument.getLineText(lineIndex).charAt(i);
            startX += getCharWidth(c);
        }

        int baseline = getRowHeight() * (lineIndex + 1) + getPaddingTop();
        canvas.drawLine(
                startX,
                baseline + COMPOSING_UNDERLINE_TEXT_SPACE_PX,
                startX + width,
                baseline + COMPOSING_UNDERLINE_TEXT_SPACE_PX,
                mTextPaint);
    }

    private void drawChar(Canvas canvas, char c, int x, int y, TokenType tokenType) {
        char[] chars = {c};
        if (tokenType == TokenType.KEYWORD2) {
            mTextPaint.setColor(getColorStyle().getKeyword2Color());
        } else if (tokenType == TokenType.KEYWORD) {
            mTextPaint.setColor(getColorStyle().getKeywordColor());
        } else if (tokenType == TokenType.TYPE) {
            mTextPaint.setColor(getColorStyle().getTypeColor());
        } else if (tokenType == TokenType.STRING) {
            mTextPaint.setColor(getColorStyle().getStringColor());
        } else if (tokenType == TokenType.COMMENT) {
            mTextPaint.setColor(getColorStyle().getCommentColor());
        } else {
            mTextPaint.setColor(getColorStyle().getCommonTextColor());
        }
        canvas.drawText(chars, 0, 1, x, y, mTextPaint);
    }

    private void drawLineBackground(Canvas canvas, int lineIndex) {
        canvas.drawRect(0,
                lineIndex * getRowHeight() + getPaddingTop(),
                Integer.MAX_VALUE,
                (lineIndex + 1) * getRowHeight() + getPaddingTop(),
                mLineBackgroundPaint);
    }

    private void drawSelection(Canvas canvas) {
        int startSelect = mDocument.getSelectionStart();
        int endSelect = mDocument.getSelectionEnd();

        int startLine = mDocument.findLineForDraw(startSelect);
        int endLine = mDocument.findLineForDraw(endSelect);

        if (startLine == endLine) {
            canvas.drawRect(mSelectionMiddle, mSelectionPaint);

        } else {
            // 1.Draw first select line background.
            canvas.drawRect(mSelectionTop, mSelectionPaint);

            // 2.Draw last select line background.
            canvas.drawRect(mSelectionBottom, mSelectionPaint);

            // 3.Draw middle select line background.
            if (endLine - startLine > 1) {
                canvas.drawRect(mSelectionMiddle, mSelectionPaint);
            }
        }
    }

    private void drawCursor(Canvas canvas) {
        canvas.drawRect(mCursorRect, mCursorPaint);
    }


    //////////////////////   Scroll  //////////////////////

    // We just follow cursor when user input something,
    // don't care selection and others.
    public void postScrollFollowCursor() {
        post(new Runnable() {
            @Override
            public void run() {
                scrollFollowCursor();
            }
        });
    }

    private void scrollFollowCursor() {
        int fromX = getScrollX();
        int fromY = getScrollY();
        int toX = getScrollX() + getWidth();
        int toY = getScrollY() + getHeight();

        boolean needScroll = false;
        int targetX = fromX;
        int targetY = fromY;

        int cursorCenterX = (mCursorRect.left + mCursorRect.right) / 2;
        if (fromX > cursorCenterX) {
            needScroll = true;
            targetX = cursorCenterX - getWidth() / 2;
        } else if (toX < cursorCenterX) {
            needScroll = true;
            targetX = cursorCenterX + getWidth() / 2;
        }

        if (fromY > mCursorRect.top) {
            needScroll = true;
            targetY = mCursorRect.top - getHeight() / 2;
        } else if (toY < mCursorRect.bottom) {
            needScroll = true;
            targetY = mCursorRect.bottom + getHeight() / 2;
        }

        if (needScroll) {
            safeScrollTo(targetX, targetY);
        }
    }

    public void safeScrollTo(int targetX, int targetY) {
        int maxScrollX = Math.max(0, getLongestLineWidth() - getWidth());
        int maxScrollY = Math.max(0, getContentHeight() - getHeight());

        targetX = Math.max(0, targetX);
        targetX = Math.min(maxScrollX, targetX);

        targetY = Math.max(0, targetY);
        targetY = Math.min(maxScrollY, targetY);

        scrollTo(targetX, targetY);

        if (mBaseIndicator != null) {
            mBaseIndicator.onCodeSpaceScroll(targetX, targetY);
        }
    }


    //////////////////////   Handle  //////////////////////

    public void showInsertionHandle(Rect rect) {
        if (mInsertionHandle.isShowing()) {
            mInsertionHandle.update((rect.left + rect.right) / 2, rect.bottom);
        } else {
            mInsertionHandle.show((rect.left + rect.right) / 2, rect.bottom);
        }
    }

    public void updateInsertionHandleIfShown() {
        if (mInsertionHandle.isShowing()) {
            Rect rect = getCursorRectOnScreen();
            rect.offset(-getScrollX(), -getScrollY());
            showInsertionHandle(rect);
        }
    }

    public void dismissInsertionHandle() {
        mInsertionHandle.dismiss();
    }

    private void showSelectionHandle(Point start, Point end) {
        if (mSelectionLeftHandle.isShowing()) {
            mSelectionLeftHandle.update(start.x, start.y);
        } else {
            mSelectionLeftHandle.show(start.x, start.y);
        }

        if (mSelectionRightHandle.isShowing()) {
            mSelectionRightHandle.update(end.x, end.y);
        } else {
            mSelectionRightHandle.show(end.x, end.y);
        }
    }

    public void dismissSelectionHandle() {
        mSelectionLeftHandle.dismiss();
        mSelectionRightHandle.dismiss();
    }

    public void showSelectionHandle() {
        Point leftPoint = getPointOnScreen(mSelectionLeftHandlePoint);
        leftPoint.offset(-getScrollX(), -getScrollY());
        Point rightPoint = getPointOnScreen(mSelectionRightHandlePoint);
        rightPoint.offset(-getScrollX(), -getScrollY());
        showSelectionHandle(leftPoint, rightPoint);
    }

    public void updateSelectionHandleIfShown() {
        if (mSelectionLeftHandle.isShowing() && mSelectionRightHandle.isShowing()) {
            showSelectionHandle();
        }
    }


    //////////////////////   KeyEvent  //////////////////////

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result = mDocument.handleKeyEvent(event);
        if (result) {
            notifySelectionChangeInvalidate();
            dismissInsertionHandle();
            dismissSelectionHandle();
            finishActionMode();
            postScrollFollowCursor();
        }
        return result;
    }


    //////////////////////   Touch  //////////////////////

    public void onSingleTapUp(int x, int y) {
        int offset = getOffsetNearXY(x + getScrollX() - getPaddingStart(), y + getScrollY() - getPaddingTop());
        mDocument.moveCursor(offset, false);
        notifySelectionChangeInvalidate();

        Rect rect = getCursorRectOnScreen();
        rect.offset(-getScrollX(), -getScrollY());
        showInsertionHandle(rect);
        dismissSelectionHandle();

        finishActionMode();
    }

    public void onLongPress(int x, int y) {
        if (!TextUtils.isEmpty(mDocument.toString())) {
            int[] range = getWordNearXY(x + getScrollX() - getPaddingStart(), y + getScrollY() - getPaddingTop());
            mDocument.setSelection(range[0], range[1]);
            notifySelectionChangeInvalidate();

            dismissInsertionHandle();
            showSelectionHandle();

            mActionMode = mActionCallback.startActionMode(this, false);
        } else {
            mActionMode = mActionCallback.startActionMode(this, true);
        }
    }

    private class InsertionHandleListener implements OnTouchListener {

        private Rect mGlobalVisibleRect = new Rect();
        private int mOffsetY;
        private boolean isMoved;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int rX = (int) e.getRawX();
            int rY = (int) e.getRawY();
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mOffsetY = (int) (e.getY() + mCursorRect.height() / 2);
                    isMoved = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    getGlobalVisibleRect(mGlobalVisibleRect);
                    int targetX = rX + getScrollX() - getPaddingStart() - mGlobalVisibleRect.left;
                    int targetY = rY + getScrollY() - getPaddingTop() - mGlobalVisibleRect.top;
                    int offset = getOffsetNearXY(targetX, targetY - mOffsetY);

                    if (mDocument.getCursorPosition() != offset) {
                        int oldPos = mDocument.getCursorPosition();
                        mDocument.moveCursor(offset, false);
                        if (oldPos != mDocument.getCursorPosition()) {
                            isMoved = true;
                        }
                        measureRect();
                        invalidate();

                        Rect rect = getCursorRectOnScreen();
                        rect.offset(-getScrollX(), -getScrollY());
                        showInsertionHandle(rect);
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    notifySelectionChangeInvalidate();
                    if (!isMoved) {
                        mActionMode = mActionCallback.startActionMode(CodeSpace.this, true);
                    }
                    break;
            }
            return true;
        }
    }

    private class SelectionHandleListener implements OnTouchListener {

        boolean isLeft;
        private Rect mGlobalVisibleRect = new Rect();
        private int mOffsetX;
        private int mOffsetY;

        public SelectionHandleListener(boolean isLeft) {
            this.isLeft = isLeft;
        }

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            int rX = (int) e.getRawX();
            int rY = (int) e.getRawY();
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isLeft) {
                        mOffsetX = mSelectionLeftHandle.getPaddingSpace();
                    } else {
                        mOffsetX = -mSelectionRightHandle.getPaddingSpace();
                    }
                    mOffsetY = (int) (e.getY() + getRowHeight() / 2);
                    break;
                case MotionEvent.ACTION_MOVE:
                    getGlobalVisibleRect(mGlobalVisibleRect);
                    int targetX = rX + getScrollX() - getPaddingStart() - mGlobalVisibleRect.left;
                    int targetY = rY + getScrollY() - getPaddingTop() - mGlobalVisibleRect.top;
                    int offset = getOffsetNearXY(targetX + mOffsetX, targetY - mOffsetY);

                    int origin = isLeft ? mDocument.getSelectionStart() : mDocument.getSelectionEnd();
                    int other = isLeft ? mDocument.getSelectionEnd() : mDocument.getSelectionStart();
                    boolean needUpdate = false;
                    if (offset != origin) {
                        if (isLeft) {
                            if (offset < other) {
                                needUpdate = true;
                            }
                        } else {
                            if (offset > other) {
                                needUpdate = true;
                            }
                        }
                    }

                    if (needUpdate) {
                        if (isLeft) {
                            mDocument.setSelection(offset, mDocument.getSelectionEnd());
                        } else {
                            mDocument.setSelection(mDocument.getSelectionStart(), offset);
                        }
                        measureRect();
                        invalidate();
                        showSelectionHandle();
                        hideActionMode();
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    notifySelectionChangeInvalidate();
                    invalidateActionMode();
                    break;
            }
            return true;
        }
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
