package coding.yu.codespace;

import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpaceInputConnection extends BaseInputConnection {

    private static final String TAG = "CodeSpaceInputConnectio";

    private CodeSpace mCodeSpace;
    private Document mDocument;

    public CodeSpaceInputConnection(CodeSpace targetView) {
        super(targetView, true);
        this.mCodeSpace = targetView;
        this.mDocument = targetView.getDocument();
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        Log.d("Yu", "setComposingRegion:" + start + " " + end);
        mDocument.setComposingRegion(start, end);
        mCodeSpace.invalidate();
        return true;
    }

    @Override
    public boolean beginBatchEdit() {
//        Log.d("Yu", "beginBatchEdit");
        return super.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit() {
//        Log.d("Yu", "endBatchEdit");
        return super.endBatchEdit();
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        Log.d("Yu", "commitText:" + text + " " + newCursorPosition);

        mDocument.replace(
                mDocument.getComposingIndexStart(),
                mDocument.getComposingIndexEnd(),
                text.toString());

        if (newCursorPosition == 1) {
            mDocument.moveCursor(mDocument.getComposingIndexStart() + text.length(), false);
            mDocument.setComposingRegion(mDocument.getCursorPosition(), mDocument.getCursorPosition());
        } else {
            Log.e(TAG, "commitText newCursorPosition != 1, need check!");
        }

        Log.e("Yu", "Cursor pos:" + mDocument.getCursorPosition());

        mCodeSpace.invalidate();
        return true;
    }

    @Override
    public boolean commitCompletion(CompletionInfo text) {
        Log.v("Yu", "commitCompletion " + text);
        return super.commitCompletion(text);
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

        int start = mDocument.getComposingIndexStart();
        mDocument.replace(start, mDocument.getComposingIndexEnd(), text.toString());
        mDocument.setComposingRegion(start, mDocument.getComposingIndexStart() + text.length());

        if (newCursorPosition == 1) {
            mDocument.moveCursor(start + text.length(), false);
        } else {
            Log.e(TAG, "setComposingText newCursorPosition != 1, need check!");
        }

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
    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        Log.d("Yu", "deleteSurroundingTextInCodePoints " + beforeLength + " " + afterLength);
        return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
    }

    @Override
    public boolean finishComposingText() {
        Log.d("Yu", "finishComposingText");
        mDocument.setComposingRegion(0, 0);
        mCodeSpace.invalidate();
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
        String str = mDocument.toString();
        int end = Math.min(str.length(), mDocument.getCursorPosition() + length);
        String s = str.substring(mDocument.getCursorPosition(), end);
        Log.d("Yu", "getTextAfterCursor:" + s + " " + length);
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
