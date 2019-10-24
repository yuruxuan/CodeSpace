package coding.yu.codespace;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputContentInfo;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpaceInputConnection extends BaseInputConnection {

    private static final String TAG = "CodeSpaceInputConnection";

    private CodeSpace mCodeSpace;
    private Document mDocument;
    private String mCurrentIME;

    public CodeSpaceInputConnection(CodeSpace targetView) {
        super(targetView, true);
        this.mCodeSpace = targetView;
        this.mDocument = targetView.getDocument();
    }

    @Override
    public Editable getEditable() {
        return mDocument.getText();
    }

    public void setCurrentIME(String pkg) {
        Log.i("Yu", "Current IME:" + pkg);
        this.mCurrentIME = pkg;
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        boolean result = super.setComposingRegion(start, end);
        mCodeSpace.invalidate();
        return result;
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
        boolean result = super.commitText(text, newCursorPosition);
        mDocument.analyze();
        mCodeSpace.notifySelectionChangeInvalidate();
        mCodeSpace.dismissInsertionHandle();
        mCodeSpace.dismissSelectionHandle();
        mCodeSpace.finishActionMode();
        mCodeSpace.postScrollFollowCursor();
        return result;
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
        return super.sendKeyEvent(event);
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        Log.d("Yu", "getSelectedText:" + flags);
        return super.getSelectedText(flags);
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        Log.d("Yu", "setComposingText:" + text + " " + newCursorPosition);
        boolean result = super.setComposingText(text, newCursorPosition);
        mDocument.analyze();
        mCodeSpace.notifySelectionChangeInvalidate();
        mCodeSpace.dismissInsertionHandle();
        mCodeSpace.dismissSelectionHandle();
        mCodeSpace.finishActionMode();
        mCodeSpace.postScrollFollowCursor();
        return result;
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        Log.d("Yu", "deleteSurroundingText:" + beforeLength + " " + afterLength);
        boolean result = super.deleteSurroundingText(beforeLength, afterLength);
        mDocument.analyze();
        mCodeSpace.notifySelectionChangeInvalidate();
        mCodeSpace.dismissInsertionHandle();
        mCodeSpace.dismissSelectionHandle();
        mCodeSpace.finishActionMode();
        mCodeSpace.postScrollFollowCursor();
        return result;
    }

    @Override
    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        Log.d("Yu", "deleteSurroundingTextInCodePoints " + beforeLength + " " + afterLength);
        return super.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
    }

    @Override
    public boolean finishComposingText() {
        Log.d("Yu", "finishComposingText");
        boolean result = super.finishComposingText();
        mCodeSpace.notifySelectionChangeInvalidate();
        mCodeSpace.postScrollFollowCursor();
        return result;
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        Log.d("Yu", "getCursorCapsMode:" + reqModes);
        return super.getCursorCapsMode(reqModes);
    }


    @Override
    public boolean setSelection(int start, int end) {
        Log.d("Yu", "setSelection:" + start + " " + end);
        return super.setSelection(start, end);
    }

    @Override
    public CharSequence getTextBeforeCursor(int length, int flags) {
        Log.d("Yu", "getTextBeforeCursor");
        CharSequence charSequence = super.getTextBeforeCursor(length, flags);
        return needFixComposing() ? "" : charSequence;
    }

    @Override
    public CharSequence getTextAfterCursor(int length, int flags) {
        Log.d("Yu", "getTextAfterCursor");
        CharSequence charSequence = super.getTextAfterCursor(length, flags);
        return needFixComposing() ? "" : charSequence;
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled) {
        Log.d("Yu", "reportFullscreenMode:" + enabled);
        return super.reportFullscreenMode(enabled);
    }


    private boolean needFixComposing() {
        return mCurrentIME.equals("com.sohu.inputmethod.sogou");
    }
}
