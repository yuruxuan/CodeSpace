package coding.yu.codespace;

import android.view.KeyEvent;

/**
 * Created by yu on 9/23/2019.
 */
public class KeyCodeConverter {

    public static char convert(int code) {
        if (KeyEvent.KEYCODE_0 == code) {
            return '0';
        }

        if (KeyEvent.KEYCODE_1 == code) {
            return '1';
        }

        if (KeyEvent.KEYCODE_2 == code) {
            return '2';
        }

        if (KeyEvent.KEYCODE_3 == code) {
            return '3';
        }

        if (KeyEvent.KEYCODE_4 == code) {
            return '4';
        }

        if (KeyEvent.KEYCODE_5 == code) {
            return '5';
        }

        if (KeyEvent.KEYCODE_6 == code) {
            return '6';
        }

        if (KeyEvent.KEYCODE_7 == code) {
            return '7';
        }

        if (KeyEvent.KEYCODE_8 == code) {
            return '8';
        }

        if (KeyEvent.KEYCODE_9 == code) {
            return '9';
        }

        if (KeyEvent.KEYCODE_ENTER == code) {
            return '\n';
        }

        if (KeyEvent.KEYCODE_SPACE == code) {
            return ' ';
        }

        if (KeyEvent.KEYCODE_TAB == code) {
            return '\t';
        }

        return 0;
    }
}
