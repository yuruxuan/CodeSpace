package coding.yu.codespace.highlight;

/**
 * Created by yu on 9/24/2019.
 */
public class LightColor extends ColorStyle {

    private static final int COLOR_KEYWORD = 0xff365ccf;
    private static final int COLOR_TYPE = 0xff7e0454;
    private static final int COLOR_KEYWORD2 = 0xff438080;
    private static final int COLOR_STRING = 0xff468344;
    private static final int COLOR_COMMENT = 0xffaaaaaa;
    private static final int COLOR_COMMON_TEXT = 0xff333333;

    @Override
    public int getKeywordColor() {
        return COLOR_KEYWORD;
    }

    @Override
    public int getTypeColor() {
        return COLOR_TYPE;
    }

    @Override
    public int getKeyword2Color() {
        return COLOR_KEYWORD2;
    }

    @Override
    public int getStringColor() {
        return COLOR_STRING;
    }

    @Override
    public int getCommentColor() {
        return COLOR_COMMENT;
    }

    @Override
    public int getCommonTextColor() {
        return COLOR_COMMON_TEXT;
    }
}
