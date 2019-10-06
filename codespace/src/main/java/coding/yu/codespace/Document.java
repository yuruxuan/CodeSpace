package coding.yu.codespace;

import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

import coding.yu.codespace.lex.CLexer;
import coding.yu.codespace.lex.Token;
import coding.yu.codespace.lex.TokenType;

/**
 * Created by yu on 9/20/2019.
 */
public class Document {

    private static final String TAG = "Document";

    private StringBuilder mText = new StringBuilder();

    private List<String> mLines = new ArrayList<>();

    private CLexer mLaxer = new CLexer();
    private List<Token> mTokenList = new ArrayList<>();
    private Token mLastToken;

    private int mCursorPosition;

    private int mSelectionStart;
    private int mSelectionEnd;

    private int mComposingIndexStart;
    private int mComposingIndexEnd;

    private CursorMoveCallback mCursorMoveCallback;

    public void setCursorMoveCallback(CursorMoveCallback callback) {
        this.mCursorMoveCallback = callback;
    }

    private OffsetMeasure mOffsetMeasure;

    public void setOffsetMeasure(OffsetMeasure offsetMeasure) {
        this.mOffsetMeasure = offsetMeasure;
    }

    //////////////////////   Edit  //////////////////////

    public void clear() {
        mText.delete(0, mText.length());
        analyze(null);
    }

    public void setText(String s) {
        mText.delete(0, mText.length());
        mText.append(s);
        analyze(null);
    }

    public int length() {
        return mText.length();
    }

    public void insert(int offset, char c) {
        mText.insert(offset, c);
        analyze(null);
    }

    public void insert(int offset, String s) {
        mText.insert(offset, s);
        analyze(null);
    }

    public void append(String s) {
        mText.append(s);
        analyze(null);
    }

    public void append(char c) {
        mText.append(c);
        analyze(null);
    }

    public void replace(int start, int end, String s) {
        mText.replace(start, end, s);
        analyze(null);
    }

    public void delete(int start, int end) {
        mText.delete(start, end);
        analyze(null);
    }

    @Override
    public String toString() {
        return mText.toString();
    }


    //////////////////////   Composing Text  //////////////////////

    public void setComposingRegion(int start, int end) {
        this.mComposingIndexStart = start;
        this.mComposingIndexEnd = end;
    }

    public int getComposingIndexStart() {
        return this.mComposingIndexStart;
    }

    public int getComposingIndexEnd() {
        return this.mComposingIndexEnd;
    }

    public int getComposingLength() {
        return this.mComposingIndexEnd - this.mComposingIndexStart;
    }

    public boolean isComposingTextExist() {
        if (this.mComposingIndexStart >= 0 && this.mComposingIndexEnd >= 0
                && this.mComposingIndexStart < mComposingIndexEnd
                && this.mComposingIndexStart <= this.mCursorPosition
                && this.mCursorPosition <= this.mComposingIndexEnd) {
            return true;
        }
        return false;
    }


    //////////////////////   Line & Keyword  //////////////////////

    public int findLineForDraw(int offset) {
        if (offset < 0 || mText.length() < offset) {
            return -1;
        }

        if (mText.length() == 0) {
            return 0;
        }

        int sum = 0;
        for (int i = 0; i < mLines.size(); i++) {
            sum += mLines.get(i).length();
            if (sum > offset) {
                return i;
            } else if (sum == offset) { // offset is end of line.
                if (mText.charAt(sum - 1) == '\n') { // '\n' is end of line.
                    return i + 1;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getLineCountForDraw() {
        if (mText.length() == 0 || mText.charAt(mText.length() - 1) == '\n') {
            return mLines.size() + 1;
        }
        return mLines.size();
    }

    private void analyze(String s) {
        analyzeLines();
        analyzeKeyword();
    }

    /**
     * Split text by '\n'. '\n' is end line of text
     */
    private void analyzeLines() {
        mLines.clear();

        int from = 0;
        int to = 0;
        for (int i = 0; i < mText.length(); i++) {
            to++;
            if (mText.charAt(i) == '\n') {
                mLines.add(mText.substring(from, to));
                from = to;
            }
        }

        if (from != to) {
            mLines.add(mText.substring(from, to));
        }

    }

    private void analyzeKeyword() {
        mTokenList.clear();
        mLaxer.parse(mText.toString(), mTokenList);
    }

    /**
     * Get last line cursor offset.
     * <p>
     * eg:
     * current offset:5
     * 1  2  3  \n
     * 3 |4
     * <p>
     * return:2
     * 1 |2  3  \n
     * 3  4
     */
    private int getLastLineOffset(int offset) {
        int sum = 0;
        int currentLineIndex = -1;
        for (int i = 0; i < mLines.size(); i++) {
            sum += mLines.get(i).length();
            if (offset <= sum) {
                currentLineIndex = i;
                break;
            }
        }

        return -1;
    }

    private int getNextLineOffset(int offset) {
        return -1;
    }

    /**
     * This time draw transaction can not use last time draw transaction token.
     * So we need reset mLastToken when onDraw() invoked.
     */
    public void resetLastToken() {
        mLastToken = null;
    }

    public TokenType getTokenTypeByLineOffset(int line, int lineOffset) {
        int offset = 0;
        for (int i = 0; i < line; i++) {
            offset += mLines.get(i).length();
        }
        offset += lineOffset;
        return getTokenTypeByOffset(offset);
    }

    /**
     * Avoid traversing every element, we need check last token first.
     * <p>
     * 1. mLastToken
     * 2. mLastToken + 1
     * 3. every element
     */
    public TokenType getTokenTypeByOffset(int offset) {
        int[] skipIndex = {-1, -1};
        if (mLastToken != null) {
            if (offset >= mLastToken.start && offset < mLastToken.end()) {
                return mLastToken.type;
            }

            int lastTokenIndex = mTokenList.indexOf(mLastToken);
            int nextTokenIndex = lastTokenIndex + 1;
            if (nextTokenIndex > 0 && nextTokenIndex < mTokenList.size()) {
                Token nextToken = mTokenList.get(nextTokenIndex);
                if (offset >= nextToken.start && offset < nextToken.end()) {
                    mLastToken = nextToken;
                    return nextToken.type;
                }
            }
            skipIndex[0] = lastTokenIndex;
            skipIndex[1] = nextTokenIndex;
        }

        for (int i = 0; i < mTokenList.size(); i++) {
            if (i == skipIndex[0] || i == skipIndex[1]) {
                continue;
            }
            Token token = mTokenList.get(i);
            if (offset >= token.start && offset < token.end()) {
                mLastToken = token;
                return token.type;
            }
        }
        return null;
    }

    /**
     * We think every line has string.
     */
    public String getLineText(int line) {
        if (line < mLines.size()) {
            return mLines.get(line);
        } else {
            return "";
        }
    }

    //////////////////////   Cursor   //////////////////////

    /**
     * The cursor position is [0, mText.length()]
     * <p>
     * |a|b|c|d|\n
     * 0 1 2 3 4
     * |e|f|\n
     * 5 6 7
     * |
     * 8
     */
    public int getCursorPosition() {
        return mCursorPosition;
    }

    private void setCursorPosition(int position) {
        this.mCursorPosition = position;
    }

    /**
     * There is a safe method to move cursor. The offset will be [0, mText.length()]
     * If cursor position is not changed, we will do nothing.
     */
    public void moveCursor(int offset, boolean isRelative) {
        int absOffset;
        if (isRelative) {
            absOffset = getCursorPosition() + offset;
            absOffset = Math.min(absOffset, mText.length());
            absOffset = Math.max(absOffset, 0);
        } else {
            absOffset = Math.min(offset, mText.length());
            absOffset = Math.max(absOffset, 0);
        }

        boolean needUpdate = absOffset != mCursorPosition;

        if (needUpdate) {
            setCursorPosition(absOffset);

            if (mCursorMoveCallback != null) {
                mCursorMoveCallback.onCursorMoved(absOffset, absOffset);
            }
        }
    }

    public void moveCursorLeft() {
        moveCursor(-1, true);
    }

    public void moveCursorRight() {
        moveCursor(1, true);
    }

    /**
     * Move cursor up is very similar to touch the relative position at last line.
     */
    public void moveCursorUp() {
        if (mOffsetMeasure != null) {
            int offset = mOffsetMeasure.getLastLineRelativeOffset();
            moveCursor(offset, false);
        }
    }

    public void moveCursorDown() {
        if (mOffsetMeasure != null) {
            int offset = mOffsetMeasure.getNextLineRelativeOffset();
            moveCursor(offset, false);
        }
    }

    public boolean hasSelection() {
        if (mSelectionStart >= 0 && mComposingIndexEnd >= 0
                && mSelectionStart < mSelectionEnd) {
            return true;
        }
        return false;
    }

    public void setSelection(int start, int end) {
        setSelection(start, end, true);
    }

    public void setSelection(int start, int end, boolean needNotify) {
        mSelectionStart = Math.min(start, mText.length());
        mSelectionStart = Math.max(mSelectionStart, 0);
        mSelectionEnd = Math.min(end, mText.length());
        mSelectionEnd = Math.max(mSelectionEnd, 0);

        if (mCursorMoveCallback != null && needNotify) {
            mCursorMoveCallback.onCursorMoved(mSelectionStart, mSelectionEnd);
        }
    }

    public int getSelectionStart() {
        return mSelectionStart;
    }

    public int getSelectionEnd() {
        return mSelectionEnd;
    }


    //////////////////////   KeyEvent   //////////////////////

    /**
     * KeyEvent is received from:
     * 1.Input Method
     * 2.Framework > Activity > View
     */
    public boolean handleKeyEvent(KeyEvent event) {
        Log.e("Yu", "handleKeyEvent " + KeyEvent.keyCodeToString(event.getKeyCode()));

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return false;
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            char ch = 0;
            if (handleControlKey(event)) {
                // Do nothing...
            } else if ((ch = KeyCodeConverter.convertOther(event.getKeyCode())) != 0) {
                insert(getCursorPosition(), ch);
                moveCursorRight();
            } else if ((ch = KeyCodeConverter.convertLetter(event.getKeyCode(),
                    event.isCapsLockOn() || event.isShiftPressed())) != 0) {
                insert(getCursorPosition(), ch);
                moveCursorRight();
            } else if ((ch = KeyCodeConverter.convertNumSign(event.getKeyCode(),
                    event.isShiftPressed())) != 0) {
                insert(getCursorPosition(), ch);
                moveCursorRight();
            }
        }
        return true;
    }

    private boolean handleControlKey(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            moveCursorDown();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            moveCursorUp();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            moveCursorLeft();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            moveCursorRight();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            if (mCursorPosition - 1 >= 0) {
                delete(mCursorPosition - 1, mCursorPosition);
                moveCursorLeft();
            }
            return true;
        }

        return false;
    }


    //////////////////////   Interface   //////////////////////

    public interface CursorMoveCallback {
        void onCursorMoved(int start, int end);
    }

    public interface OffsetMeasure {
        int getLastLineRelativeOffset();

        int getNextLineRelativeOffset();
    }

    public interface TextChangeListener {
        void onTextChanged(String before, String after);
    }
}
