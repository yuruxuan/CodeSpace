package coding.yu.codespace.handle;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;

import coding.yu.codespace.R;

public class SelectionLeftHandleView extends HandleView {

    private static final int IMAGE_PADDING_START_END_DP = 11;

    private int mAnchorX;

    public SelectionLeftHandleView(View parent, int color) {
        super(parent, R.drawable.cs_text_select_handle_left_mtrl_alpha, color);

        Resources resources = parent.getContext().getResources();
        Drawable drawable = resources.getDrawable(R.drawable.cs_text_select_handle_left_mtrl_alpha);
        mAnchorX = - drawable.getIntrinsicWidth() + dp2px(IMAGE_PADDING_START_END_DP);
    }

    @Override
    public int getAnchorX() {
        return mAnchorX;
    }

    @Override
    public int getAnchorY() {
        return 0;
    }

    public int getPaddingSpace() {
        return dp2px(IMAGE_PADDING_START_END_DP);
    }
}
