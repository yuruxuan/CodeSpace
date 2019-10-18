package coding.yu.codespace.handle;

import android.view.View;

import coding.yu.codespace.R;

public class SelectionRightHandleView extends HandleView {

    public SelectionRightHandleView(View parent, int color) {
        super(parent, R.drawable.cs_text_select_handle_right_mtrl_alpha, color);
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
