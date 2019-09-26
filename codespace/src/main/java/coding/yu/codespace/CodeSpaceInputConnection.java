package coding.yu.codespace;

import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpaceInputConnection extends BaseInputConnection {

    private static final String TAG = "CodeSpaceInputConnectio";

    private CodeSpace mCodeSpace;
    private Document mDocument;

    private int mComposingTextLength;

    public CodeSpaceInputConnection(CodeSpace targetView) {
        super(targetView, true);
        this.mCodeSpace = targetView;
        this.mDocument = targetView.getDocument();
    }

    @Override
    public boolean beginBatchEdit() {
        Log.d("Yu", "beginBatchEdit");
        return super.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit() {
        Log.d("Yu", "endBatchEdit");
        return super.endBatchEdit();
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        Log.d("Yu", "commitText:" + text + " " + newCursorPosition);

        mDocument.replace(
                mDocument.getCursorPosition() - mComposingTextLength,
                mDocument.getCursorPosition(),
                text.toString());

        mComposingTextLength = 0;

        if (newCursorPosition == 1) {
            mDocument.moveCursor(mDocument.getCursorPosition() + text.length(), false);
        } else {
            Log.e(TAG, "commitText newCursorPosition != 1, need check!");
        }

        Log.e("Yu", "Cursor pos:" + mDocument.getCursorPosition());

        mCodeSpace.invalidate();

        return true;
    }

    @Override
    public boolean performContextMenuAction(int id) {
        return super.performContextMenuAction(id);
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        Log.d("Yu", "sendKeyEvent:" + event.toString());

        mDocument.handleKeyEvent(event);
        mCodeSpace.invalidate();
        return true;
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        Log.d("Yu", "getSelectedText:" + flags);
        return super.getSelectedText(flags);
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        Log.d("Yu", "setComposingText:" + text + " " + newCursorPosition);

        mDocument.replace(
                mDocument.getCursorPosition() - mComposingTextLength,
                mDocument.getCursorPosition(),
                text.toString()
                );

        if (newCursorPosition == 1) {
            mDocument.moveCursor(mDocument.getCursorPosition() + text.length() - mComposingTextLength, false);
        } else {
            Log.e(TAG, "setComposingText newCursorPosition != 1, need check!");
        }

        mComposingTextLength = text.length();

        Log.e("Yu", "Cursor pos:" + mDocument.getCursorPosition());

        mCodeSpace.invalidate();
        return true;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        Log.d("Yu", "deleteSurroundingText:" + beforeLength + " " + afterLength);

        int start = mDocument.getCursorPosition() - beforeLength;
        int end = mDocument.getCursorPosition() + afterLength;
        if (start >= 0) {
            mDocument.delete(start, end);
            mDocument.moveCursor(start, false);
            mCodeSpace.invalidate();
        }
        return true;
    }

    @Override
    public boolean finishComposingText() {
        Log.d("Yu", "finishComposingText");
        mComposingTextLength = 0;
        return true;
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        Log.d("Yu", "getCursorCapsMode:" + reqModes);
        return super.getCursorCapsMode(reqModes);
    }

    @Override
    public CharSequence getTextBeforeCursor(int length, int flags) {
        int start = Math.max(0, mDocument.getCursorPosition() - length);
        String s = mDocument.toString().substring(start, mDocument.getCursorPosition());
        Log.d("Yu", "getTextBeforeCursor:" + s + " " + length);
        return s;
    }

    @Override
    public CharSequence getTextAfterCursor(int length, int flags) {
        String s = mDocument.toString();
        int end = Math.min(mDocument.toString().length(), mDocument.getCursorPosition() + 1);
        String logStr = mDocument.toString().substring(mDocument.getCursorPosition(), end);
        Log.d("Yu", "getTextAfterCursor:" + logStr + " " + length);
        return s;
    }

    @Override
    public boolean setSelection(int start, int end) {
        Log.d("Yu", "setSelection:" + start + " " + end);
        return super.setSelection(start, end);
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled) {
        Log.d("Yu", "reportFullscreenMode:" + enabled);
        return super.reportFullscreenMode(enabled);
    }


}
