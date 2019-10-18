package coding.yu.codespace.handle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import coding.yu.codespace.R;

public class SelectionLeftHandleView extends HandleView {

    private int mAnchorX;
    private int mAnchorY;

    public SelectionLeftHandleView(View parent, int color) {
        super(parent, R.drawable.cs_text_select_handle_left_mtrl_alpha, color);

        Resources resources = parent.getContext().getResources();
        Drawable drawable = resources.getDrawable(R.drawable.cs_text_select_handle_left_mtrl_alpha);
        mAnchorX = - drawable.getIntrinsicWidth();
        Log.e("Yu", "mAnchorX:" + mAnchorX);
    }

    @Override
    public int getAnchorX() {
        return 0;
    }

    @Override
    public int getAnchorY() {
        return 0;
    }
}
