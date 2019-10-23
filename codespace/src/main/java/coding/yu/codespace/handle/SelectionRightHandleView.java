package coding.yu.codespace.handle;

import android.view.View;

import coding.yu.codespace.R;

public class SelectionRightHandleView extends HandleView {

    // Android system handle image has alpha, size = 11dp
    private static final int IMAGE_PADDING_START_END_DP = 11;

    public SelectionRightHandleView(View parent, int color) {
        super(parent, R.drawable.cs_text_select_handle_right_mtrl_alpha, color);
    }

    @Override
    public int getAnchorX() {
        return -dp2px(IMAGE_PADDING_START_END_DP);
    }

    @Override
    public int getAnchorY() {
        return 0;
    }

    public int getPaddingSpace() {
        return dp2px(IMAGE_PADDING_START_END_DP);
    }
}
