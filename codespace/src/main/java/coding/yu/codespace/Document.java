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

    public Document() {
        mLines.add("");
    }

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

    public CharSequence getText() {
        return mText.toString();
    }


    //////////////////////   Line & Keyword  //////////////////////

    /**
     * '\n' will locate at the end of line.
     */
    public int findLineByOffset(int offset) {
        if (offset < 0 || mText.length() > offset) {
            return -1;
        }

        // Maybe offset is equals mText.length()
        offset = Math.min(offset, mText.length() - 1);

        int count = 0;
        for (int i = 0; i <= offset; i++) {
            if (mText.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private void analyze(String s) {
        analyzeLines();
        analyzeKeyword();
    }

    /**
     * Split text by '\n'. '\n' is end line of text, and it's also start of line on UI
     * So if '\n' is end of text, we need put a empty string to line array.
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

        if (mText.length() == 0 || mText.charAt(mText.length() - 1) == '\n') {
            mLines.add("");
        }

        Log.e("Yu", "mLines:" + mLines.size() + " " + mLines.toString());
    }

    private void analyzeKeyword() {
        mTokenList.clear();
        mLaxer.parse(mText.toString(), mTokenList);
    }

    /**
     * This time draw transaction can not use last time draw transaction token.
     * So we need reset mLastToken when onDraw invoked.
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

    public int getLineCount() {
        return mLines.size();
    }

    public String getLineText(int line) {
        return mLines.get(line);
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
        return mText.length();
    }


    //////////////////////   KeyEvent   //////////////////////

    public void handleKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            char ch = KeyCodeConverter.convert(event.getKeyCode());
            if (ch != 0) {
                insert(getCursorPosition(), ch);
            }
        }
    }
}
