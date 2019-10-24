package coding.yu.codespace.indicator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yu on 10/24/2019.
 */
public abstract class BaseIndicator extends View {

    public BaseIndicator(Context context) {
        super(context);
    }

    public BaseIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void updateSizeInfo(float textSizePx);

    public abstract void updateLineCount(int count);

    public abstract void currentLineIndex(int index);

    public abstract void onCodeSpaceScroll(int x, int y);
}
