package coding.yu.codespace;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ActionCallbackCompat {

    private boolean fromInsert;
    private CodeSpace mCodeSpace;
    private Context mContext;
    private ActionCallback mActionCallback = new ActionCallback();
    private ActionCallback2 mActionCallback2 = new ActionCallback2();

    public ActionMode startActionMode(CodeSpace codeSpace, boolean fromInsert) {
        mCodeSpace = codeSpace;
        mContext = codeSpace.getContext();
        this.fromInsert = fromInsert;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return codeSpace.startActionMode(mActionCallback2, ActionMode.TYPE_FLOATING);
        } else {
            return codeSpace.startActionMode(mActionCallback);
        }
    }

    class ActionCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(android.R.string.selectTextMode);
            TypedArray array = mContext.getTheme().obtainStyledAttributes(new int[]{
                    android.R.attr.actionModeSelectAllDrawable,
                    android.R.attr.actionModeCutDrawable,
                    android.R.attr.actionModeCopyDrawable,
                    android.R.attr.actionModePasteDrawable,
            });
            menu.add(0, 0, 0, mContext.getString(android.R.string.selectAll))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    .setAlphabeticShortcut('a')
                    .setIcon(array.getDrawable(0));

            if (!fromInsert) {
                menu.add(0, 1, 0, mContext.getString(android.R.string.cut))
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                        .setAlphabeticShortcut('x')
                        .setIcon(array.getDrawable(1));

                menu.add(0, 2, 0, mContext.getString(android.R.string.copy))
                        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                        .setAlphabeticShortcut('c')
                        .setIcon(array.getDrawable(2));
            }

            menu.add(0, 3, 0, mContext.getString(android.R.string.paste))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    .setAlphabeticShortcut('v')
                    .setIcon(array.getDrawable(3));
            array.recycle();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == 0) {
                if (TextUtils.isEmpty(mCodeSpace.getDocument().toString())) {
                    mCodeSpace.finishActionMode();
                    return true;
                }
                mCodeSpace.getDocument().selectAll();
                mCodeSpace.notifySelectionChangeInvalidate();
                mCodeSpace.showSelectionHandle();
                mCodeSpace.dismissInsertionHandle();
                return true;
            }

            if (item.getItemId() == 1) {
                int start = mCodeSpace.getDocument().getSelectionStart();
                int end = mCodeSpace.getDocument().getSelectionEnd();
                String str = mCodeSpace.getDocument().toString();
                String selection = str.substring(start, end);
                copyToClipboard(mCodeSpace.getContext(), selection);

                mCodeSpace.getDocument().recordBeforeEdit();
                mCodeSpace.getDocument().delete(start, end);
                mCodeSpace.getDocument().recordAfterEdit();
                mCodeSpace.getDocument().notifyTextChangedIfNeed();

                mCodeSpace.getDocument().removeSelect();
                mCodeSpace.getDocument().setSelection(start, start);
                mCodeSpace.notifySelectionChangeInvalidate();
                mCodeSpace.dismissSelectionHandle();
                mCodeSpace.finishActionMode();
                return true;
            }

            if (item.getItemId() == 2) {
                int start = mCodeSpace.getDocument().getSelectionStart();
                int end = mCodeSpace.getDocument().getSelectionEnd();
                String str = mCodeSpace.getDocument().toString();
                String selection = str.substring(start, end);
                copyToClipboard(mCodeSpace.getContext(), selection);
                mCodeSpace.getDocument().removeSelect();
                mCodeSpace.getDocument().setSelection(end, end);
                mCodeSpace.notifySelectionChangeInvalidate();
                mCodeSpace.dismissSelectionHandle();
                mCodeSpace.finishActionMode();
                return true;
            }

            if (item.getItemId() == 3) {
                String content = readFromClipboard(mCodeSpace.getContext());
                if (!TextUtils.isEmpty(content)) {
                    int start = mCodeSpace.getDocument().getSelectionStart();
                    int end = mCodeSpace.getDocument().getSelectionEnd();

                    mCodeSpace.getDocument().recordBeforeEdit();
                    mCodeSpace.getDocument().replace(start, end, content);
                    mCodeSpace.getDocument().recordAfterEdit();
                    mCodeSpace.getDocument().notifyTextChangedIfNeed();

                    mCodeSpace.getDocument().removeSelect();
                    mCodeSpace.getDocument().setSelection(start + content.length(), start + content.length());
                    mCodeSpace.notifySelectionChangeInvalidate();
                    mCodeSpace.dismissInsertionHandle();
                    mCodeSpace.dismissSelectionHandle();
                }
                mCodeSpace.finishActionMode();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

    private void copyToClipboard(Context context, String content) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Label", content);
        cm.setPrimaryClip(clipData);
    }

    private String readFromClipboard(Context context) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clipData = cm.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                String content = item.getText().toString();
                return content;
            }
        }
        return "";
    }


    @SuppressLint("NewApi")
    class ActionCallback2 extends ActionMode.Callback2 {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return mActionCallback.onCreateActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mActionCallback.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mActionCallback.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionCallback.onDestroyActionMode(mode);
        }

        @Override
        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            super.onGetContentRect(mode, view, outRect);
            CodeSpace codeSpace = (CodeSpace) view;
            Rect rect = new Rect();
            if (fromInsert) {
                rect.set(codeSpace.getCursorRect());
            } else {
                rect.set(codeSpace.getSelectionRegion());
            }
            rect.offset(-mCodeSpace.getScrollX(), -mCodeSpace.getScrollY());
            outRect.set(rect);
        }
    }
}
