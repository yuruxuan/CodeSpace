package coding.yu.codespace;

import android.view.KeyEvent;

/**
 * Created by yu on 9/23/2019.
 */
public class KeyCodeConverter {

    public static char convertOther(int code) {
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

    public static char convertNumSign(int code, boolean shiftPressed) {
        if (KeyEvent.KEYCODE_1 == code) {
            return shiftPressed ? '!' :  '1';
        }
        if (KeyEvent.KEYCODE_2 == code) {
            return shiftPressed ? '@' :  '2';
        }
        if (KeyEvent.KEYCODE_3 == code) {
            return shiftPressed ? '#' :  '3';
        }
        if (KeyEvent.KEYCODE_4 == code) {
            return shiftPressed ? '$' :  '4';
        }
        if (KeyEvent.KEYCODE_5 == code) {
            return shiftPressed ? '%' :  '5';
        }
        if (KeyEvent.KEYCODE_6 == code) {
            return shiftPressed ? '^' :  '6';
        }
        if (KeyEvent.KEYCODE_7 == code) {
            return shiftPressed ? '&' :  '7';
        }
        if (KeyEvent.KEYCODE_8 == code) {
            return shiftPressed ? '*' :  '8';
        }
        if (KeyEvent.KEYCODE_9 == code) {
            return shiftPressed ? '(' :  '9';
        }
        if (KeyEvent.KEYCODE_0 == code) {
            return shiftPressed ? ')' : '0';
        }
        if (KeyEvent.KEYCODE_GRAVE == code) {
            return shiftPressed ? '~' : '`';
        }
        if (KeyEvent.KEYCODE_MINUS == code) {
            return shiftPressed ? '_' : '-';
        }
        if (KeyEvent.KEYCODE_EQUALS == code) {
            return shiftPressed ? '+' : '=';
        }
        if (KeyEvent.KEYCODE_LEFT_BRACKET == code) {
            return shiftPressed ? '{' : '[';
        }
        if (KeyEvent.KEYCODE_RIGHT_BRACKET == code) {
            return shiftPressed ? '}' : ']';
        }
        if (KeyEvent.KEYCODE_BACKSLASH == code) {
            return shiftPressed ? '|' : '\\';
        }
        if (KeyEvent.KEYCODE_SEMICOLON == code) {
            return shiftPressed ? ':' : ';';
        }
        if (KeyEvent.KEYCODE_APOSTROPHE == code) {
            return shiftPressed ? '"' : '\'';
        }
        if (KeyEvent.KEYCODE_COMMA == code) {
            return shiftPressed ? '<' : ',';
        }
        if (KeyEvent.KEYCODE_PERIOD == code) {
            return shiftPressed ? '>' : '.';
        }
        if (KeyEvent.KEYCODE_SLASH == code) {
            return shiftPressed ? '?' : '/';
        }
        return 0;
    }

    public static char convertLetter(int code, boolean uppercase) {
        if (KeyEvent.KEYCODE_A == code) {
            return uppercase ? 'A' : 'a';
        }
        if (KeyEvent.KEYCODE_B == code) {
            return uppercase ? 'B' : 'b';
        }
        if (KeyEvent.KEYCODE_C == code) {
            return uppercase ? 'C' : 'c';
        }
        if (KeyEvent.KEYCODE_D == code) {
            return uppercase ? 'D' : 'd';
        }
        if (KeyEvent.KEYCODE_E == code) {
            return uppercase ? 'E' : 'e';
        }
        if (KeyEvent.KEYCODE_F == code) {
            return uppercase ? 'F' : 'f';
        }
        if (KeyEvent.KEYCODE_G == code) {
            return uppercase ? 'G' : 'g';
        }
        if (KeyEvent.KEYCODE_H == code) {
            return uppercase ? 'H' : 'h';
        }
        if (KeyEvent.KEYCODE_I == code) {
            return uppercase ? 'I' : 'i';
        }
        if (KeyEvent.KEYCODE_J == code) {
            return uppercase ? 'J' : 'j';
        }
        if (KeyEvent.KEYCODE_K == code) {
            return uppercase ? 'K' : 'k';
        }
        if (KeyEvent.KEYCODE_L == code) {
            return uppercase ? 'L' : 'l';
        }
        if (KeyEvent.KEYCODE_M == code) {
            return uppercase ? 'M' : 'm';
        }
        if (KeyEvent.KEYCODE_N == code) {
            return uppercase ? 'N' : 'n';
        }
        if (KeyEvent.KEYCODE_O == code) {
            return uppercase ? 'O' : 'o';
        }
        if (KeyEvent.KEYCODE_P == code) {
            return uppercase ? 'P' : 'p';
        }
        if (KeyEvent.KEYCODE_Q == code) {
            return uppercase ? 'Q' : 'q';
        }
        if (KeyEvent.KEYCODE_R == code) {
            return uppercase ? 'R' : 'r';
        }
        if (KeyEvent.KEYCODE_S == code) {
            return uppercase ? 'S' : 's';
        }
        if (KeyEvent.KEYCODE_T == code) {
            return uppercase ? 'T' : 't';
        }
        if (KeyEvent.KEYCODE_U == code) {
            return uppercase ? 'U' : 'u';
        }
        if (KeyEvent.KEYCODE_V == code) {
            return uppercase ? 'V' : 'v';
        }
        if (KeyEvent.KEYCODE_W == code) {
            return uppercase ? 'W' : 'w';
        }
        if (KeyEvent.KEYCODE_X == code) {
            return uppercase ? 'X' : 'x';
        }
        if (KeyEvent.KEYCODE_Y == code) {
            return uppercase ? 'Y' : 'y';
        }
        if (KeyEvent.KEYCODE_Z == code) {
            return uppercase ? 'Z' : 'z';
        }
        return 0;
    }
}
