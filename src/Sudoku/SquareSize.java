package Sudoku;

public class SquareSize {
    public int x;
    public int y;

    public SquareSize(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public SquareSize() {
        this.x = 0;
        this.y = 0;
    }

    public String toString() {
        if (x > 0 && y > 0)
            return Converters.toStr(this.x) + " × " + Converters.toStr(this.y);
        else
            return "_______";
    }
}
