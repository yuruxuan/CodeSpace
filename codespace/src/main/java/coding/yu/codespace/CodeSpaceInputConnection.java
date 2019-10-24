package coding.yu.codespace;

import android.text.Editable;
import android.util.Log;
import android.view.inputmethod.BaseInputConnection;

/**
 * Created by yu on 9/18/2019.
 */
public class CodeSpaceInputConnection extends BaseInputConnection {

    private static final String TAG = "CodeSpace";

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
        Log.i(TAG, "Current IME:" + pkg);
        this.mCurrentIME = pkg;
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        boolean result = super.setComposingRegion(start, end);
        mCodeSpace.invalidate();
        return result;
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
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
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
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
    public boolean finishComposingText() {
        boolean result = super.finishComposingText();
        mCodeSpace.notifySelectionChangeInvalidate();
        mCodeSpace.postScrollFollowCursor();
        return result;
    }

    @Override
    public CharSequence getTextBeforeCursor(int length, int flags) {
        CharSequence charSequence = super.getTextBeforeCursor(length, flags);
        return needFixComposing() ? "" : charSequence;
    }

    @Override
    public CharSequence getTextAfterCursor(int length, int flags) {
        CharSequence charSequence = super.getTextAfterCursor(length, flags);
        return needFixComposing() ? "" : charSequence;
    }

    private boolean needFixComposing() {
        return mCurrentIME.equals("com.sohu.inputmethod.sogou");
    }
}
