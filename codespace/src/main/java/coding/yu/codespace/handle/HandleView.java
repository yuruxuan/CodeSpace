package coding.yu.codespace.handle;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.graphics.drawable.DrawableCompat;

public abstract class HandleView extends AppCompatImageView {

    private static final int HANDLE_OFFSET_Y_PX = 3;

    private View mParent;
    private PopupWindow mContainer;

    public HandleView(View parent, int drawableId, int color) {
        super(parent.getContext());
        mParent = parent;

        Drawable drawable = mParent.getContext().getDrawable(drawableId);
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        setImageDrawable(wrappedDrawable);
        setBackgroundColor(Color.TRANSPARENT);

        mContainer = new PopupWindow(getContext());
        mContainer.setSplitTouchEnabled(true);
        mContainer.setClippingEnabled(false);
        mContainer.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mContainer.setContentView(this);
    }

    public abstract int getAnchorX();

    public abstract int getAnchorY();

    public boolean isShowing() {
        return mContainer.isShowing();
    }

    public void show(int x, int y) {
        mContainer.showAtLocation(mParent, Gravity.NO_GRAVITY,
                x + getAnchorX(), y + getAnchorY() + HANDLE_OFFSET_Y_PX);
    }

    public void update(int x, int y) {
        mContainer.update(x + getAnchorX(), y + getAnchorY() + HANDLE_OFFSET_Y_PX,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void dismiss() {
        mContainer.dismiss();
    }

    int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
