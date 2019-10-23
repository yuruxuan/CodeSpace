package coding.yu.codespace;

import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;

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

    private SpannableStringBuilder mText = new SpannableStringBuilder();

    private List<String> mLines = new ArrayList<>();

    private CLexer mLaxer = new CLexer();
    private List<Token> mTokenList = new ArrayList<>();
    private Token mLastToken;

    private OffsetMeasure mOffsetMeasure;

    public void setOffsetMeasure(OffsetMeasure offsetMeasure) {
        this.mOffsetMeasure = offsetMeasure;
    }

    //////////////////////   Edit  //////////////////////

    public void clear() {
        mText.delete(0, mText.length());
        analyze();
    }

    public void setText(String s) {
        mText.delete(0, mText.length());
        mText.append(s);
        analyze();
    }

    public int length() {
        return mText.length();
    }

    public void insert(int offset, char c) {
        mText.insert(offset, String.valueOf(c));
        analyze();
    }

    public void insert(int offset, String s) {
        mText.insert(offset, s);
        analyze();
    }

    public void append(String s) {
        mText.append(s);
        analyze();
    }

    public void append(char c) {
        mText.append(c);
        analyze();
    }

    public void replace(int start, int end, String s) {
        mText.replace(start, end, s);
        analyze();
    }

    public void delete(int start, int end) {
        mText.delete(start, end);
        analyze();
    }

    @Override
    public String toString() {
        return mText.toString();
    }

    public SpannableStringBuilder getText() {
        return mText;
    }

    //////////////////////   Composing Text  //////////////////////

//    public void setComposingRegion(int start, int end) {
//        this.mComposingIndexStart = start;
//        this.mComposingIndexEnd = end;
//    }

    public int getComposingIndexStart() {
        return BaseInputConnection.getComposingSpanStart(mText);
    }

    public int getComposingIndexEnd() {
        return BaseInputConnection.getComposingSpanEnd(mText);
    }

    public int getComposingLength() {
        return getComposingIndexEnd() - getComposingIndexStart();
    }

    public boolean isComposingTextExist() {
        int start = getComposingIndexStart();
        int end = getComposingIndexEnd();
        if (start >= 0 && end >= 0 && start < end) {
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

    public void analyze() {
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
                mLines.add(mText.subSequence(from, to).toString());
                from = to;
            }
        }

        if (from != to) {
            mLines.add(mText.subSequence(from, to).toString());
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
        if (line >= 0 && line < mLines.size()) {
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
        return Selection.getSelectionStart(mText);
    }

    private void setCursorPosition(int position) {
        Selection.setSelection(mText, position);
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

        setCursorPosition(absOffset);
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
        int start = Selection.getSelectionStart(mText);
        int end = Selection.getSelectionEnd(mText);
        if (start >= 0 && end >= 0 && start < end) {
            return true;
        }
        return false;
    }

    public void setSelection(int start, int end) {
        start = Math.min(start, mText.length());
        start = Math.max(start, 0);
        end = Math.min(end, mText.length());
        end = Math.max(end, 0);

        Selection.setSelection(mText, start, end);
    }

    public int getSelectionStart() {
        return Selection.getSelectionStart(mText);
    }

    public int getSelectionEnd() {
        return Selection.getSelectionEnd(mText);
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
            } else if ((ch = KeyCodeConverter.convertLetter(event.getKeyCode(),
                    event.isCapsLockOn() || event.isShiftPressed())) != 0) {
                insert(getCursorPosition(), ch);
            } else if ((ch = KeyCodeConverter.convertNumSign(event.getKeyCode(),
                    event.isShiftPressed())) != 0) {
                insert(getCursorPosition(), ch);
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
            if (getCursorPosition() - 1 >= 0) {
                delete(getCursorPosition() - 1, getCursorPosition());
            }
            return true;
        }

        return false;
    }


    //////////////////////   Interface   //////////////////////

    public interface OffsetMeasure {
        int getLastLineRelativeOffset();

        int getNextLineRelativeOffset();
    }

    public interface TextChangeListener {
        void onTextChanged(String before, String after);
    }
}
