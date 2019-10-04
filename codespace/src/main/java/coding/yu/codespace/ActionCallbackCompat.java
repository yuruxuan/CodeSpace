package coding.yu.codespace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ActionCallbackCompat {

    private Context mContext;
    private ActionCallback mActionCallback = new ActionCallback();
    private ActionCallback2 mActionCallback2 = new ActionCallback2();

    public ActionMode startActionMode(View view) {
        mContext = view.getContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return view.startActionMode(mActionCallback2, ActionMode.TYPE_FLOATING);
        } else {
            return view.startActionMode(mActionCallback);
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

            menu.add(0, 1, 0, mContext.getString(android.R.string.cut))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    .setAlphabeticShortcut('x')
                    .setIcon(array.getDrawable(1));

            menu.add(0, 2, 0, mContext.getString(android.R.string.copy))
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    .setAlphabeticShortcut('c')
                    .setIcon(array.getDrawable(2));

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
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
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
            // This rect is the selection rect.
            Log.e("Yu", "view:" + view + ", rect:" + outRect);
        }
    }
}
