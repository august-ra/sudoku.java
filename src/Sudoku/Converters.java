package Sudoku;

public class Converters {
    public static String toStr(Integer d) {
        return d.toString();
    }

    public static int toInt(String s) {
        try {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int toInt(Float d) {
        return d.intValue();
    }

    public static float toFloat(Integer d) {
        return d.floatValue();
    }
}
