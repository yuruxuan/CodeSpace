package coding.yu.codespace;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

/**
 * Created by yu on 9/20/2019.
 */
public class CursorFlicker {

    private static final int MSG_SHOW = 1;
    private static final int MSG_HIDE = 2;
    private static final int TIME_FLICKER_DURING_MS = 800;

    private boolean mNeedToShow = true;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MSG_SHOW) {
                sendEmptyMessageDelayed(MSG_HIDE, TIME_FLICKER_DURING_MS);
            }

            if (msg.what == MSG_HIDE) {
                sendEmptyMessageDelayed(MSG_SHOW, TIME_FLICKER_DURING_MS);
            }
        }
    };

    public boolean isNeedToShow() {
        return mNeedToShow;
    }

    public void reshowForce() {
        mNeedToShow = true;
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(MSG_SHOW, TIME_FLICKER_DURING_MS);
    }


}
