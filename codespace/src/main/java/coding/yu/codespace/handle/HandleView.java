package coding.yu.codespace.handle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

public abstract class HandleView extends View {

    private PopupWindow mPopupWindow;

    public HandleView(Context context) {
        super(context);
        init();
    }

    private void init() {

    }
}
