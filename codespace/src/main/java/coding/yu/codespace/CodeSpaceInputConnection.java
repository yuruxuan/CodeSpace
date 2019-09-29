package coding.yu.codespace;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputContentInfo;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpaceInputConnection extends BaseInputConnection {

    private static final String TAG = "CodeSpaceInputConnectio";

    private CodeSpace mCodeSpace;
    private Document mDocument;
    private String mCurrentIME;

    public CodeSpaceInputConnection(CodeSpace targetView) {
        super(targetView, true);
        this.mCodeSpace = targetView;
        this.mDocument = targetView.getDocument();
    }

    public void setCurrentIME(String pkg) {
        Log.i("Yu", "Current IME:" + pkg);
        this.mCurrentIME = pkg;
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        Log.d("Yu", "setComposingRegion:" + start + " " + end);
        mDocument.setComposingRegion(start, end);
        mCodeSpace.invalidate();
        return true;
    }

    @Override
    public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
        Log.d("Yu", "commitContent:" + inputContentInfo + " " + flags + " " + opts);
        return super.commitContent(inputContentInfo, flags, opts);
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

        if (newCursorPosition != 1) {
            Log.e(TAG, "commitText newCursorPosition != 1, need check!");
            int position = mDocument.getCursorPosition();
            mDocument.setComposingRegion(position, position);
            mCodeSpace.invalidate();
            return true;
        }

        Log.e("Yu", "commitText >>> " + mDocument.getComposingIndexStart()
                + " " + mDocument.getComposingIndexEnd()
                + " " + mDocument.getCursorPosition());

        boolean composing = mDocument.isComposingTextExist();
        int start = composing ? mDocument.getComposingIndexStart() : mDocument.getCursorPosition();
        int end = composing ? mDocument.getComposingIndexEnd() : mDocument.getCursorPosition();

        mDocument.replace(start, end, text.toString());
        mDocument.moveCursor(end + text.length() - mDocument.getComposingLength(), false);
        mDocument.setComposingRegion(mDocument.getCursorPosition(), mDocument.getCursorPosition());

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
    public boolean performEditorAction(int actionCode) {
        Log.d("Yu", "performEditorAction:" + actionCode);
        return super.performEditorAction(actionCode);
    }

    @Override
    public void closeConnection() {
        Log.d("Yu", "closeConnection");
        super.closeConnection();
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data) {
        Log.d("Yu", "performPrivateCommand:" + action);
        return super.performPrivateCommand(action, data);
    }

    @Override
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        Log.d("Yu", "requestCursorUpdates:" + cursorUpdateMode);
        return super.requestCursorUpdates(cursorUpdateMode);
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        Log.d("Yu", "sendKeyEvent:" + event.toString());

        boolean result = mDocument.handleKeyEvent(event);
        if (result) {
            mCodeSpace.invalidate();
        }
        return result;
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        Log.d("Yu", "getSelectedText:" + flags);
        return null;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        Log.d("Yu", "setComposingText:" + text + " " + newCursorPosition);

        if (TextUtils.isEmpty(text)) {
            Log.w(TAG, "setComposingText text is empty, wtf...");
            mDocument.setComposingRegion(mDocument.getCursorPosition(), mDocument.getCursorPosition());
            mCodeSpace.invalidate();
            return true;
        }

        if (newCursorPosition != 1) {
            Log.w(TAG, "setComposingText newCursorPosition != 1, need check!");
            mDocument.setComposingRegion(mDocument.getCursorPosition(), mDocument.getCursorPosition());
            mCodeSpace.invalidate();
            return true;
        }

        int start;
        int end;
        if (mDocument.isComposingTextExist()) {
            start = mDocument.getComposingIndexStart();
            end = mDocument.getComposingIndexEnd();
        } else {
            start = mDocument.getCursorPosition();
            end = mDocument.getCursorPosition() + text.length();

            String str = mDocument.toString();
            if (str.length() > start && str.charAt(start) == '\n') {
                end--;
            }
        }

        mDocument.replace(start, end, text.toString());
        mDocument.setComposingRegion(start, start + text.length());
        mDocument.moveCursor(start + text.length(), false);


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
        mDocument.setComposingRegion(mDocument.getCursorPosition(), mDocument.getCursorPosition());
        mCodeSpace.invalidate();
        return true;
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        Log.d("Yu", "getCursorCapsMode:" + reqModes);
        return super.getCursorCapsMode(reqModes);
    }

    // This is not a good solution.
    // In some case, the IME can not provide the correct composing region by getTextBeforeCursor()
    // and getTextAfterCursor(), such as sogou pinyin IME. So I decide to return a empty
    // string, and the IME will not incorrectly invoke setComposingText().
    // BUT Google IME is perfect.
    @Override
    public CharSequence getTextBeforeCursor(int length, int flags) {
        if (isStupidIME(mCurrentIME)) {
            return "";
        }
        int start = Math.max(0, mDocument.getCursorPosition() - length);
        String s = mDocument.toString().substring(start, mDocument.getCursorPosition());
        Log.e("Yu", "getTextBeforeCursor return:" + s);
        return s;
    }

    @Override
    public CharSequence getTextAfterCursor(int length, int flags) {
        if (isStupidIME(mCurrentIME)) {
            return "";
        }
        String str = mDocument.toString();
        int end = Math.min(str.length(), mDocument.getCursorPosition() + length);
        String s = str.substring(mDocument.getCursorPosition(), end);
        Log.e("Yu", "getTextAfterCursor return:" + s);
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

    private boolean isStupidIME(String pkg) {
        if (TextUtils.equals("com.sohu.inputmethod.sogou", pkg)) {
            return true;
        }
        return false;
    }
}
