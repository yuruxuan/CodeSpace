package coding.yu.codespace.touch;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import coding.yu.codespace.CodeSpace;
import coding.yu.codespace.ime.IMEHelper;

/**
 * Created by yu on 9/18/2019.
 */
public class TouchGestureListener extends GestureDetector.SimpleOnGestureListener {

    private CodeSpace mCodeSpace;

    public static GestureDetector setup(CodeSpace codeSpace) {
        TouchGestureListener listener = new TouchGestureListener(codeSpace);
        GestureDetector gestureDetector = new GestureDetector(codeSpace.getContext(), listener);
        gestureDetector.setIsLongpressEnabled(true);
        return gestureDetector;
    }

    public TouchGestureListener(CodeSpace codeSpace) {
        super();
        this.mCodeSpace = codeSpace;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        IMEHelper.show(mCodeSpace);
        mCodeSpace.onSingleTapUp((int)e.getX(), (int) e.getY());

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void onShowPress(MotionEvent e) {
        super.onShowPress(e);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return super.onDoubleTap(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onContextClick(MotionEvent e) {
        return super.onContextClick(e);
    }
}
