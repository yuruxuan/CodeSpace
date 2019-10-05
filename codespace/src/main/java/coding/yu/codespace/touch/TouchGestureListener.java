package coding.yu.codespace.touch;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import coding.yu.codespace.CodeSpace;
import coding.yu.codespace.ime.IMEHelper;

/**
 * Created by yu on 9/18/2019.
 */
public class TouchGestureListener extends GestureDetector.SimpleOnGestureListener {

    private CodeSpace mCodeSpace;

    private boolean isScrollingX;
    private boolean isScrollingY;

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
        Log.e("Yu", "onDown:" + e.toString());
        isScrollingX = false;
        isScrollingY = false;
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.e("Yu", "onSingleTapUp:" + e.toString());
        IMEHelper.show(mCodeSpace);
        mCodeSpace.onSingleTapUp((int) e.getX(), (int) e.getY());

        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        mCodeSpace.onLongPress((int) e.getX(), (int) e.getY());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int dX = (int) distanceX;
        int dY = (int) distanceY;

        if (!isScrollingX && !isScrollingY) {
            if (Math.abs(dX) > Math.abs(dY)) {
                isScrollingX = true;
            } else {
                isScrollingY = true;
            }
        }
        Log.e("Yu", "onScroll: " + isScrollingX + " " + isScrollingY);
        int targetX = Math.max(0, mCodeSpace.getScrollX() + dX);
        int targetY = Math.max(0, mCodeSpace.getScrollY() + dY);

        if (isScrollingX) {
            mCodeSpace.scrollTo(targetX, mCodeSpace.getScrollY());
        }
        if (isScrollingY){
            mCodeSpace.scrollTo(mCodeSpace.getScrollX(), targetY);
        }
        return true;
    }

    @Override
    public boolean onScrollFinish(MotionEvent e) {

        return super.onScrollFinish(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.e("Yu", "onFling:" + e2.toString());
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
        Log.e("Yu", "onSingleTapConfirmed:" + e.toString());
        return super.onSingleTapConfirmed(e);
    }
}
