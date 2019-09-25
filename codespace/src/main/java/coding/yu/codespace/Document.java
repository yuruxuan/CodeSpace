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

    private int mCursorStart;
    private int mCursorEnd;

    private CursorMoveCallback mCursorMoveCallback;

    public void setCursorMoveCallback(CursorMoveCallback callback) {
        this.mCursorMoveCallback = callback;
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

    public void deleteLast() {
        mText.deleteCharAt(mText.length() - 1);
        analyze(null);
    }

    public void delete(int start, int end) {
        mText.delete(start, end);
        analyze(null);
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

        Log.e("Yu", "mLines:" + mLines.size() + " " + mLines.toString());
    }

    private void analyzeKeyword() {
        mTokenList.clear();
        mLaxer.parse(mText.toString(), mTokenList);
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
        return mCursorStart;
    }

    public int getCursorStart() {
        return mCursorStart;
    }

    public int getCursorEnd() {
        return mCursorEnd;
    }

    private void setCursorPosition(int position) {
        this.mCursorStart = this.mCursorEnd = position;
    }

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

        boolean needUpdate = absOffset != mCursorStart;

        if (needUpdate) {
            setCursorPosition(absOffset);

            if (mCursorMoveCallback != null) {
                mCursorMoveCallback.onCursorMoved(offset, offset);
            }
        }
    }

    public void moveCursorLeft() {
        moveCursor(-1, true);
    }

    public void moveCursorRight() {
        moveCursor(1, true);
    }

    public void moveCursorUp() {

    }

    public void moveCursorDown() {

    }


    //////////////////////   KeyEvent   //////////////////////

    /**
     * KeyEvent is received from:
     * 1.Input Method
     * 2.Framework > Activity > View
     */
    public void handleKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (handleDirectionControl(event)) {
                // Do nothing...
            } else {
                char ch = KeyCodeConverter.convert(event.getKeyCode());
                if (ch != 0) {
                    insert(getCursorPosition(), ch);
                    moveCursorRight();
                }
            }
        }
    }

    private boolean handleDirectionControl(KeyEvent event) {
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

        return false;
    }

    public interface CursorMoveCallback {
        void onCursorMoved(int start, int end);
    }
}
