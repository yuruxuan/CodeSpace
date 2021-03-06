package coding.yu.codespace.handle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import coding.yu.codespace.CodeSpace;
import coding.yu.codespace.R;

public class InsertionHandleView extends HandleView {

    private int mAnchorX;

    public InsertionHandleView(View parent, int color) {
        super(parent, R.drawable.cs_text_select_handle_middle_mtrl_alpha, color);

        Resources resources = parent.getContext().getResources();
        Drawable drawable = resources.getDrawable(R.drawable.cs_text_select_handle_middle_mtrl_alpha);
        mAnchorX = - drawable.getIntrinsicWidth() / 2;
    }

    @Override
    public int getAnchorX() {
        return mAnchorX;
    }

    @Override
    public int getAnchorY() {
        return 0;
    }
}
