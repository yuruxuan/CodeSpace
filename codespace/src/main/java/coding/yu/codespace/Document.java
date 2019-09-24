package coding.yu.codespace;

import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yu on 9/20/2019.
 */
public class Document {

    private static final String TAG = "Document";

    private StringBuilder mText = new StringBuilder();

    private List<String> mLines = new ArrayList<>();

    public Document() {
        mLines.add("");
    }

    public void clear() {
        mText.delete(0, mText.length());
        analyzeLines();
    }

    public void setText(String s) {
        mText.delete(0, mText.length());
        mText.append(s);
        analyzeLines();
    }

    public int length() {
        return mText.length();
    }

    public void insert(int offset, char c) {
        mText.insert(offset, c);
        analyzeLinesIfNeed(null);
    }

    public void insert(int offset, String s) {
        mText.insert(offset, s);
        analyzeLinesIfNeed(null);
    }

    public void append(String s) {
        mText.append(s);
        analyzeLinesIfNeed(null);
    }

    public void append(char c) {
        mText.append(c);
        analyzeLinesIfNeed(null);
    }

    public void replace(int start, int end, String s) {
        mText.replace(start, end, s);
        analyzeLines();
    }

    public void deleteLast() {
        mText.deleteCharAt(mText.length() - 1);
        analyzeLinesIfNeed(null);
    }

    public void delete(int start, int end) {
        mText.delete(start, end);
        analyzeLinesIfNeed(null);
    }

    public CharSequence getText() {
        return mText.toString();
    }


    //////////////////////   Line   //////////////////////

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

    private void analyzeLinesIfNeed(String s) {
        analyzeLines();
    }

    /**
     * Split text by '\n'. '\n' is end line of text, and it's also start of line on UI
     * So if '\n' is end of text, we need put a empty string to line array.
     * */
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
