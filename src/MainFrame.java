import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;

public class MainFrame extends JFrame {
    private final static BasicStroke stroke = new BasicStroke(3.0f);
    private final static BasicStroke thin = new BasicStroke(1.0f);

    //private JScrollPane scrollPane;
    private JBoard pnlBoard;
    //private JPanel pnlBottom;
    //private JPanel   pnlStyle;
    private JComboBox<String> cmbSubsquareForm;
    private JComboBox<String> cmbDiagonals;
    private JComboBox<String> cmbSquares;
    private JToggleButton btnLotsOfSquares;
    //private JPanel   pnlSize;
    private JComboBox<Integer> cmbSquareAmount;
    private JFormattedTextField txtSquareSize;
    private JComboBox<SquareSize> cmbSubsquareSizes;
    private JToggleButton btnRotate;
    private JButton button;

    private int[][] maskFigures, maskSquares;

    // on-form settings
    private int amount, form, diagonals, squares, size, sx, sy;
    private boolean lotOfSquares, rotate;

    private boolean sizeUpdated = false;

    // calculated values
    private int bWidth, bHeight, sizeX, sizeY, px, py; // paddings from top-left corner
    private Point[] p;
    private boolean[][] c;
    //private int psx, psy;
    private double sizeCell;

    // class for sizes

    private class SquareSize {
        private int x;
        private int y;

        private SquareSize(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private SquareSize() {
            this.x = 0;
            this.y = 0;
        }

        public String toString() {
            if (x > 0 && y > 0)
                return toStr(this.x) + " × " + toStr(this.y);
            else
                return "_______";
        }
    }

    // conversation

    private static String toStr(int d) {
        return Integer.toString(d);
    }

    private static int toInt(String s) {
        try {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int toInt(Double d) {
        return d.intValue();
    }

    private static double toFloat(Integer d) {
        return d.doubleValue();
    }

    // drawing

    private class JBoard extends JPanel {
        private void setStroke(Graphics2D g, int i, int s) {
            if (i % s == 0)
                g.setStroke(stroke);
            else
                g.setStroke(thin);
        }

        private void drawLines(Graphics2D g,
                               boolean horizontal,
                               boolean vertical) {
            for (int n = 0; n < amount; n++) {
                for (int i = 1; i < size; i++) {
                    if (vertical) {
                        int x = toInt(sizeCell * (p[n].x + i));

                        setStroke(g, i, sx);
                        g.drawLine(x + px, toInt(sizeCell * p[n].y) + py, x + px, toInt(sizeCell * (p[n].y + size)) + py);
                    }
                    if (horizontal) {
                        int y = toInt(sizeCell * (p[n].y + i));

                        setStroke(g, i, sy);
                        g.drawLine(toInt(sizeCell * p[n].x) + px, y + py, toInt(sizeCell * (p[n].x + size)) + px, y + py);
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g2) {
            super.paintComponent(g2);

            Graphics2D g = (Graphics2D) g2;

            px = py = 10;

            int w = getWidth() - 2 * px;
            int h = getHeight() - 2 * py;

            if (amount == 1)
                sizeCell = Math.min(toFloat(w)/size, toFloat(h)/size);
            else
                sizeCell = Math.min(toFloat(w)/sizeX, toFloat(h)/sizeY);

            if (sizeCell < 30)
                sizeCell = 30;
            else if (sizeCell > 45)
                sizeCell = 45;

            bWidth = toInt(sizeCell * sizeX);
            bHeight = toInt(sizeCell * sizeY);

            px = (w - bWidth) / 2 + px;
            py = (h - bHeight) / 2 + py;

            // background
            g.setColor(Color.WHITE);
            //g.fillRoundRect(px, py, bWidth, bHeight, 15, 15);

            for (int n = 0; n < amount; n++) {
                int x1 = toInt(sizeCell * p[n].x);
                int x2 = toInt(sizeCell * (p[n].x + size));
                int y1 = toInt(sizeCell * p[n].y);
                int y2 = toInt(sizeCell * (p[n].y + size));
                int wi = x2 - x1;
                int he = y2 - y1;

                if (c[n][0] && c[n][1] && c[n][2] && c[n][3])
                    g.fillRect(x1 + px, y1 + py, wi, he);
                else // ! amount == 5 && n == 1
                    g.fillRoundRect(x1 + px, y1 + py, wi, he, 15, 15);
            }

            boolean[] dLeftRight = getLeftRight(diagonals);

            // diagonals and squares
            if (form == 0) {
                if (squares > 0) {
                    g.setColor(Color.CYAN);

                    for (int n = 0; n < amount; n++) {
                        for (int x = 0; x < size; x++) {
                            int x1 = toInt(sizeCell * (p[n].x + x));
                            int x2 = toInt(sizeCell * (p[n].x + x + 1));
                            int wi = x2 - x1;

                            for (int y = 0; y < size; y++) {
                                int y1 = toInt(sizeCell * (p[n].y + y));
                                int y2 = toInt(sizeCell * (p[n].y + y + 1));
                                int he = y2 - y1;

                                if (maskSquares[p[n].x + x][p[n].y + y] > 0)
                                    g.fillRect(x1 + px, y1 + py, wi, he);
                            }
                        }
                    }
                }

                if (diagonals > 0) {
                    for (int n = 0; n < amount; n++) {
                        for (int i = 0; i < size; i++) {
                            int x1 = toInt(sizeCell * (p[n].x + i));
                            int x2 = toInt(sizeCell * (p[n].x + size - i - 1));
                            int x3 = toInt(sizeCell * (p[n].x + i + 1));
                            int x4 = toInt(sizeCell * (p[n].x + size - i));
                            int y1 = toInt(sizeCell * (p[n].y + i));
                            int y2 = toInt(sizeCell * (p[n].y + size - i - 1));
                            int y3 = toInt(sizeCell * (p[n].y + i + 1));
                            int y4 = toInt(sizeCell * (p[n].y + size - i));
                            int wi1 = x3 - x1;
                            int wi2 = x4 - x2;
                            int he1 = y3 - y1;
                            int he2 = y4 - y2;

                            if (dLeftRight[0]) {
                                g.setColor(Color.PINK);
                                g.fillRoundRect(x1 + px, y1 + py, wi1, he1, 15, 15);

                                // left top corner
                                if (i > 0)
                                    g.fillRect(x1 + px, y1 + py, 15, 15);
                                // right top corner
                                g.fillRect(x3 + px - 15, y1 + py, 15, 15);
                                // right bottom corner
                                if (i < size - 1)
                                    g.fillRect(x3 + px - 15, y3 + py - 15, 15, 15);
                                // left bottom corner
                                g.fillRect(x1 + px, y3 + py - 15, 15, 15);
                            }
                            if (dLeftRight[1]) {
                                g.setColor(Color.PINK);
                                g.fillRoundRect(x2 + px, y1 + py, wi2, he2, 15, 15);

                                // left top corner
                                g.fillRect(x2 + px, y1 + py, 15, 15);
                                // right top corner
                                if (i > 0)
                                    g.fillRect(x4 + px - 15, y1 + py, 15, 15);
                                // right bottom corner
                                g.fillRect(x4 + px - 15, y3 + py - 15, 15, 15);
                                // left bottom corner
                                if (i < size - 1)
                                    g.fillRect(x2 + px, y3 + py - 15, 15, 15);
                            }
                        }
                    }
                }
            }

            // bevel
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(stroke);
            //g.drawRoundRect(px, py, bWidth, bHeight, 15, 15);

            for (int n = 0; n < amount; n++) {
                int x1 = toInt(sizeCell * p[n].x);
                int x2 = toInt(sizeCell * (p[n].x + size));
                int y1 = toInt(sizeCell * p[n].y);
                int y2 = toInt(sizeCell * (p[n].y + size));
                int wi = x2 - x1;
                int he = y2 - y1;

                if (c[n][0] && c[n][1] && c[n][2] && c[n][3])
                    g.drawRect(x1 + px, y1 + py, wi, he);
                else // ! amount == 5 && n == 1
                    g.drawRoundRect(x1 + px, y1 + py, wi, he, 15, 15);
            }

            // corners' correction
            for (int n = 0; n < amount; n++) {
                for (int x = 0; x < size; x++) {
                    if (x == 1)
                        x = size - 1;

                    for (int y = 0; y < size; y++) {
                        if (x == 1)
                            x = size - 1;

                        int x1 = toInt(sizeCell * (p[n].x + x));
                        int x2 = toInt(sizeCell * (p[n].x + x + 1));

                        int y1 = toInt(sizeCell * (p[n].y + y));
                        int y2 = toInt(sizeCell * (p[n].y + y + 1));

                        if (dLeftRight[0])
                            g.setColor(Color.PINK);
                        else if (maskSquares[p[n].x + x][p[n].y + y] > 0)
                            g.setColor(Color.CYAN);
                        else
                            g.setColor(Color.WHITE);

                        // left top corner
                        if (x == 0 && y == 0 && c[n][0])
                            g.fillRect(x1 + px, y1 + py, 15, 15);
                        // right bottom corner
                        if (x == size - 1 && y == size - 1 && c[n][2])
                            g.fillRect(x2 + px - 15, y2 + py - 15, 15, 15);

                        if (dLeftRight[1])
                            g.setColor(Color.PINK);
                        else if (maskSquares[p[n].x + x][p[n].y + y] > 0)
                            g.setColor(Color.CYAN);
                        else
                            g.setColor(Color.WHITE);

                        // left bottom corner
                        if (x == 0 && y == size - 1 && c[n][3])
                            g.fillRect(x1 + px, y2 + py - 15, 15, 15);
                        // right top corner
                        if (x == size - 1 && y == 0 && c[n][1])
                            g.fillRect(x2 + px - 15, y1 + py, 15, 15);
                    }
                }
            }

            // grid
            g.setColor(Color.LIGHT_GRAY);

            if (form == 0 && amount == 1) {
                if (sx == sy) {
                    drawLines(g, true, true);
                }
                else {
                    drawLines(g, true, false);
                    drawLines(g, false, true);
                }
            }
            else { // form == 1 && maskFigures != null
                for (int n = 0; n < amount; n++) {
                    // vertical lines
                    for (int y = 0; y < size; y++) {
                        for (int x = 0; x < size - 1; x++) {
                            if (maskFigures[p[n].x + x][p[n].y + y] == -1 && maskFigures[p[n].x + x + 1][p[n].y + y] == -1)
                                continue;

                            int x1 = toInt(sizeCell * (p[n].x + x + 1));
                            int y1 = toInt(sizeCell * (p[n].y + y));
                            int y2 = toInt(sizeCell * (p[n].y + y + 1));

                            if (maskFigures[p[n].x + x][p[n].y + y] != maskFigures[p[n].x + x + 1][p[n].y + y])
                                g.setStroke(stroke);
                            else
                                g.setStroke(thin);

                            g.drawLine(x1 + px, y1 + py, x1 + px, y2 + py);
                        }
                    }
                    // horizontal lines
                    for (int y = 0; y < size - 1; y++) {
                        for (int x = 0; x < size; x++) {
                            if (maskFigures[p[n].x + x][p[n].y + y] == -1 && maskFigures[p[n].x + x][p[n].y + y + 1] == -1)
                                continue;

                            int x1 = toInt(sizeCell * (p[n].x + x));
                            int x2 = toInt(sizeCell * (p[n].x + x + 1));
                            int y1 = toInt(sizeCell * (p[n].y + y + 1));

                            if (maskFigures[p[n].x + x][p[n].y + y] != maskFigures[p[n].x + x][p[n].y + y + 1])
                                g.setStroke(stroke);
                            else
                                g.setStroke(thin);

                            g.drawLine(x1 + px, y1 + py, x2 + px, y1 + py);
                        }
                    }
                }
            }
        }

        @Override
        public Dimension getMinimumSize() {
            int w = 30 * sizeX + 20;
            int h = 30 * sizeY + 20;
            return new Dimension(w, h);
        }
    }

    public static void main(String[] args) {
        MainFrame app = new MainFrame();
        //app.setExtendedState(JFrame.MAXIMIZED_BOTH);
        app.setVisible(true);
        app.pack();
    }

    private int[][] getMaskFigures() {
        if (form == 1 && size <= 10) {
            sizeX = sizeY = size;

            switch (size) {
                case 4:
                    return new int[][] {{1, 2, 2, 2},
                                        {1, 3, 3, 2},
                                        {1, 3, 3, 4},
                                        {1, 4, 4, 4}};
                case 5:
                    return new int[][] {{1, 1, 1, 5, 5},
                                        {1, 1, 3, 5, 5},
                                        {2, 3, 3, 3, 5},
                                        {2, 2, 3, 4, 4},
                                        {2, 2, 4, 4, 4}};
                case 6:
                    return new int[][] {{1, 1, 1, 5, 5, 5},
                                        {1, 1, 3, 3, 5, 5},
                                        {1, 3, 3, 3, 4, 5},
                                        {2, 3, 4, 4, 4, 6},
                                        {2, 2, 4, 4, 6, 6},
                                        {2, 2, 2, 6, 6, 6}};
                case 7:
                    return new int[][] {{1, 1, 1, 1, 7, 7, 7},
                                        {1, 1, 3, 5, 5, 7, 7},
                                        {1, 3, 3, 3, 5, 5, 7},
                                        {2, 3, 3, 3, 5, 5, 7},
                                        {2, 4, 4, 4, 4, 5, 6},
                                        {2, 2, 4, 4, 4, 6, 6},
                                        {2, 2, 2, 6, 6, 6, 6}};
                case 8:
                    return new int[][] {{1, 1, 1, 1, 1, 8, 8, 8},
                                        {1, 1, 3, 3, 7, 7, 8, 8},
                                        {1, 3, 3, 3, 3, 7, 7, 8},
                                        {2, 3, 4, 4, 3, 7, 7, 8},
                                        {2, 4, 4, 5, 7, 7, 5, 8},
                                        {2, 4, 4, 5, 5, 5, 5, 6},
                                        {2, 2, 4, 4, 5, 5, 6, 6},
                                        {2, 2, 2, 6, 6, 6, 6, 6}};
                case 9:
                    return new int[][] {{1, 1, 1, 1, 4, 7, 9, 9, 9},
                                        {1, 1, 4, 1, 4, 7, 7, 9, 9},
                                        {1, 1, 4, 4, 4, 5, 7, 9, 9},
                                        {2, 2, 4, 5, 4, 5, 7, 7, 9},
                                        {3, 2, 4, 5, 5, 5, 6, 7, 9},
                                        {3, 2, 2, 5, 6, 5, 6, 7, 7},
                                        {3, 3, 2, 5, 6, 6, 6, 8, 8},
                                        {3, 3, 2, 2, 6, 8, 6, 8, 8},
                                        {3, 3, 3, 2, 6, 8, 8, 8, 8}};
                case 10:
                default:
                    return new int[][] {{1, 1, 1, 1, 6, 6, 8, 8, 8, 8},
                                        {1, 1, 1, 6, 6, 6, 6, 8, 8, 8},
                                        {1, 1, 2, 6, 6, 4, 6, 6, 8, 8},
                                        {1, 2, 2, 4, 4, 4, 7,10,10, 8},
                                        {2, 2, 4, 4, 4, 7, 7,10,10,10},
                                        {2, 2, 2, 4, 4, 7, 7, 7,10,10},
                                        {3, 2, 2, 4, 7, 7, 7,10,10, 9},
                                        {3, 3, 5, 5, 7, 5, 5,10, 9, 9},
                                        {3, 3, 3, 5, 5, 5, 5, 9, 9, 9},
                                        {3, 3, 3, 3, 5, 5, 9, 9, 9, 9}};
            }
        }
        else if (form == 0 && amount >= 1 && amount <= 5) {
            int cx, cy;

            if (diagonals == 0) {
                cx = sy == 2 ? sx - 1 : sx;
                cy = sx == 2 ? sy - 1 : sy;
            }
            else {
                int c = Math.min(sx, sy);

                cx = c;
                cy = c;
            }

            switch (amount) {
                case 1 :
                    sizeX = size;
                    sizeY = size;
                    p = new Point[] {new Point(0, 0)};
                    c = new boolean[][] {{false, false, false, false}};
                    break;
                case 2 :
                    sizeX = size * 2 - cx;
                    sizeY = size * 2 - cy;
                    p = new Point[] {new Point(0, 0),
                                     new Point(size - cx, size - cy)
                    };
                    c = new boolean[][] {{false, false,  true, false},
                                         { true, false, false, false}
                    };
                    break;
                case 3:
                    sizeX = size * 2 - cx;
                    sizeY = size * 3 - cy * 2;
                    p = new Point[] {new Point(0, 0),
                                     new Point(size - cx, size - cy),
                                     new Point(0, size * 2 - cy * 2)
                    };
                    c = new boolean[][] {{false, false,  true, false},
                                         { true, false, false,  true},
                                         {false,  true, false, false}
                    };
                    break;
                case 4 :
                    sizeX = size * 2 - cx;
                    sizeY = size * 4 - cy * 3;
                    p = new Point[] {new Point(0, 0),
                                     new Point(size - cx, size - cy),
                                     new Point(0, size * 2 - cy * 2),
                                     new Point(size - cx, size * 3 - cy * 3)
                    };
                    c = new boolean[][] {{false, false,  true, false},
                                         { true, false, false,  true},
                                         {false,  true,  true, false},
                                         { true, false, false, false}
                    };
                    break;
                case 5 :
                    sizeX = size * 3 - cx * 2;
                    sizeY = size * 3 - cy * 2;
                    p = new Point[] {new Point(0, 0),
                                     new Point(size - cx, size - cy),
                                     new Point(0, size * 2 - cy * 2),
                                     new Point(size * 2 - cx * 2, 0),
                                     new Point(size * 2 - cx * 2, size * 2 - cy * 2)
                    };
                    c = new boolean[][] {{false, false,  true, false},
                                         { true,  true,  true,  true},
                                         {false,  true, false, false},
                                         {false, false, false,  true},
                                         { true, false, false, false}
                    };
                    break;
                default:
                    sizeX = size;
                    sizeY = size;
                    p = new Point[] {new Point(0, 0)};
                    c = new boolean[][] {{false, false, false, false}};
            }

            int[][] arr = new int[sizeX][sizeY];

            // fill array
            for (int y = 0; y < sizeY; y++)
                for (int x = 0; x < sizeX; x++)
                    arr[x][y] = -1;

            for (int n = 0, z = 1; n < amount; n++) {
                Point pt = p[n];

                for (int y = 0; y < size; y++) {
                    if (y > 0 && y % sy == 0)
                        z += sx;

                    int z2 = z;

                    for (int x = 0; x < size; x++) {
                        if (x > 0 && x % sx == 0)
                            z2++;

                        if (arr[x + pt.x][y + pt.y] == -1)
                            arr[x + pt.x][y + pt.y] = z2;
                        else
                            arr[x + pt.x][y + pt.y] += z2;
                    }
                }
            }

            return arr;
        }
        else {
            sizeX = sizeY = size;

            int[][] arr = new int[size][size];

            int z = 1;

            for (int y = 0; y < size; y++) {
                if (y > 0 && y % sy == 0)
                    z += sx;

                int z2 = z;

                for (int x = 0; x < size; x++) {
                    if (x > 0 && x % sx == 0)
                        z2++;

                    arr[x][y] = z2;
                }
            }

            return arr;
        }
    }

    private int[][] getMaskSquares() {
        int[][] arr = new int[sizeX][sizeY];

        if (form == 1 || squares == 0 || sx == 1 || sy == 1)
            return arr;

        boolean[] sLeftRight = getLeftRight(squares);

        // 1 square
        if (size == 4) {
            for (int n = 0; n < amount; n++)
                for (int x = 0; x < sx; x++)
                    for (int y = 0; y < sy; y++)
                        arr[p[n].x + x + 1][p[n].y + y + 1] = 1;
        }
        // 2 squares
        else if (sx == 2 || sy == 2) {
            int x1 = sx / 2;
            int x2 = size - x1 - 1;

            int y1 = sy / 2;
            int y2 = size - y1 - 1;

            if (sLeftRight[0]) {
                for (int n = 0; n < amount; n++) {
                    for (int y = 0; y < sy; y++) {
                        for (int x = 0; x < sx; x++) {
                            arr[p[n].x + x1 + x][p[n].y + y1 + y] = 1;
                            arr[p[n].x + x2 - x][p[n].y + y2 - y] = 2;
                        }
                    }
                }
            }
            else {
                for (int n = 0; n < amount; n++) {
                    for (int y = 0; y < sy; y++) {
                        for (int x = 0; x < sx; x++) {
                            arr[p[n].x + x2 - x][p[n].y + y1 + y] = 1;
                            arr[p[n].x + x1 + x][p[n].y + y2 - y] = 2;
                        }
                    }
                }
            }
        }
        // 2-4 squares
        else if (!lotOfSquares) {
            int x1 = sx / 2;
            int x2 = size - x1 - 1;

            int y1 = sy / 2;
            int y2 = size - y1 - 1;

            if ((sx == 3 ^ sy == 3) && (sx + sy) % 2 == 1) {
                if (sy == 3) {
                    x1--;
                    x2++;
                }
                else {
                    y1--;
                    y2++;
                }
            }

            for (int n = 0; n < amount; n++) {
                for (int y = 0; y < sy; y++) {
                    for (int x = 0; x < sx; x++) {
                        if (sLeftRight[0]) {
                            arr[p[n].x + x1 + x][p[n].y + y1 + y] = 1;
                            arr[p[n].x + x2 - x][p[n].y + y2 - y] = 2;
                        }
                        if (sLeftRight[1]) {
                            arr[p[n].x + x2 - x][p[n].y + y1 + y] = 3;
                            arr[p[n].x + x1 + x][p[n].y + y2 - y] = 4;
                        }
                    }
                }
            }
        }
        // 2-17 squares
        else {
            for (int n = 0; n < amount; n++) {
                for (int i = 1, a = 1, b = size; i < size; i += sx + 1, a++, b++) {
                    for (int y = 0; y < sy; y++) {
                        int y1 = i + y;

                        for (int x = 0; x < sx; x++) {
                            int x1 = i + x;

                            if (sLeftRight[0])
                                arr[p[n].x + x1][p[n].y + y1] = a;

                            if (sLeftRight[0] && size % 2 == 0 && a == size / 2)
                                continue;

                            if (sLeftRight[1]) {
                                int x2 = size - x1 - 1;

                                arr[p[n].x + x2][p[n].y + y1] = b;
                            }
                        }
                    }
                }
            }
        }

        return arr;
    }

    private MainFrame() {
        super("Sudoku");

        pnlBoard = new JBoard();
        //pnlBoard.setBorder(BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED));
        pnlBoard.setPreferredSize(new Dimension(300, 300));
        //pnlBoard.setDoubleBuffered(true);

        pnlBoard.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                if (x < px || x > bWidth+px)
                    return;
                else
                    x = toInt((x-px) / sizeCell) + 1;

                if (y < py || y > bHeight+py)
                    return;
                else
                    y = toInt((y-py) / sizeCell) + 1;

                if (e.getButton() == MouseEvent.BUTTON1) {
                    JOptionPane.showMessageDialog(null,
                            toStr(x) + " ; " + toStr(y),
                            "some",
                            JOptionPane.INFORMATION_MESSAGE);
                    // TO-DO: edit digits in cells
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(pnlBoard, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane);//, BorderLayout.CENTER);

        form = 0;

        cmbSubsquareForm = new JComboBox<>(new String[] {
                "squares",
                "random"
        });

        cmbSubsquareForm.addActionListener((ActionEvent e) -> {
            form = cmbSubsquareForm.getSelectedIndex();

            sizeChanged();
            squareChanged();
        });

        diagonals = 0;

        cmbDiagonals = new JComboBox<>(new String[] {
                "neither diagonals",
                "left diagonal",
                "right diagonal",
                "both diagonals"
        });

        cmbDiagonals.addActionListener((ActionEvent e) -> {
            diagonals = cmbDiagonals.getSelectedIndex();

            maskFigures = getMaskFigures();
            maskSquares = getMaskSquares();

            pnlBoard.repaint();
        });

        squares = 0;

        cmbSquares = new JComboBox<>(new String[] {
                "neither squares",
                "left square",
                "right square",
                "both squares"
        });

        cmbSquares.addActionListener((ActionEvent e) -> {
            squares = cmbSquares.getSelectedIndex();
            maskSquares = getMaskSquares();

            buttonsUpdate();

            pnlBoard.repaint();
        });

        lotOfSquares = false;

        btnLotsOfSquares = new JToggleButton("and to 4");
        btnLotsOfSquares.setEnabled(false);

        btnLotsOfSquares.addActionListener((ActionEvent e) -> {
            buttonsUpdate();

            squareChanged();
        });

        JPanel pnlStyle = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlStyle.add(new JLabel("Figures on a board are"));
        pnlStyle.add(cmbSubsquareForm);
        pnlStyle.add(new JLabel("including"));
        pnlStyle.add(cmbDiagonals);
        pnlStyle.add(new JLabel("and"));
        pnlStyle.add(cmbSquares);
        pnlStyle.add(btnLotsOfSquares);
        //pnlStyle.add(new JLabel("."));

        add(pnlStyle, BorderLayout.SOUTH);

        amount = 1;

        cmbSquareAmount = new JComboBox<>(new Integer[] {1, 2, 3, 4, 5});
        cmbSquareAmount.setEnabled(true);

        cmbSquareAmount.addActionListener((ActionEvent e) -> {
            amount = (int) cmbSquareAmount.getSelectedItem();

            sizeChanged();
        });

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(4);
        formatter.setMaximum(100);

        size = 9;

        txtSquareSize = new JFormattedTextField(formatter);
        txtSquareSize.setColumns(5);
        txtSquareSize.setHorizontalAlignment(JFormattedTextField.CENTER);
        txtSquareSize.setText("9");

        txtSquareSize.addActionListener((ActionEvent e) -> {
            size = toInt(txtSquareSize.getText());

            sizeChanged();
            squareChanged();

            sizeUpdated = true;
        });

        txtSquareSize.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!sizeUpdated) {
                    size = toInt(txtSquareSize.getText());

                    sizeChanged();
                    squareChanged();
                }
                else {
                    sizeUpdated = false;
                }
            }
        });

        sx = sy = 3;

        cmbSubsquareSizes = new JComboBox<>(new SquareSize[] {new SquareSize(3, 3)});
        cmbSubsquareSizes.setPrototypeDisplayValue(new SquareSize());
        cmbSubsquareSizes.setEnabled(false);

        cmbSubsquareSizes.addActionListener(
                (ActionEvent e) -> squareChanged()
        );

        btnRotate = new JToggleButton("and isn't needed to rotate", false);
        btnRotate.setEnabled(false);

        btnRotate.addActionListener((ActionEvent e) -> {
            rotate = btnRotate.isSelected();
            squareChanged();
        });

        JPanel pnlSize = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlSize.add(new JLabel("There's"));
        pnlSize.add(cmbSquareAmount);
        pnlSize.add(new JLabel("squares's where each side's size is"));
        pnlSize.add(txtSquareSize);
        pnlSize.add(new JLabel("what's equivalent to"));
        pnlSize.add(cmbSubsquareSizes);
        pnlSize.add(btnRotate);
        //pnlSize.add(new JLabel("."));

        add(pnlSize, BorderLayout.SOUTH);

        button = new JButton("look for");

        button.addActionListener(
                (ActionEvent e) -> findSizes(toInt(txtSquareSize.getText()))
        );

        JPanel pnlBottom = new JPanel();
        pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.PAGE_AXIS));
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlBottom.add(pnlStyle);
        pnlBottom.add(Box.createRigidArea(new Dimension(8, 0)));
        pnlBottom.add(pnlSize);
        pnlBottom.add(Box.createRigidArea(new Dimension(8, 0)));
        pnlBottom.add(button);

        add(pnlBottom, BorderLayout.SOUTH);

        maskFigures = getMaskFigures();
        maskSquares = getMaskSquares();

        pnlBoard.repaint();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void pack() {
        super.pack();

        //pnlBoard.setPreferredSize(new Dimension(pnlBoard.getWidth()-30, pnlBoard.getWidth()-30));
        btnRotate.setPreferredSize(new Dimension(btnRotate.getWidth(), btnRotate.getHeight()));
        btnLotsOfSquares.setPreferredSize(new Dimension(btnLotsOfSquares.getWidth(), btnLotsOfSquares.getHeight()));

        super.pack();

        this.setMinimumSize(new Dimension(this.getWidth(), 300));
        //this.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));

    }

    /**private void findAllSizes() {
        int amount = 0;
        String str;
        Vector<String> list = new Vector<>();

        for (int i = 4; i < 101; i++) {
            if (i ==  5 || i ==  7 || i == 11 || i == 13 || i == 17 || i == 19
                    || i == 23 || i == 29 || i == 31 || i == 37 || i == 41 || i == 43
                    || i == 47 || i == 53 || i == 59 || i == 61 || i == 67 || i == 71
                    || i == 73 || i == 79 || i == 83 || i == 89 || i == 97)
                continue;

            for (int j = 2; j < 11; j++) {
                if (i % j != 0)
                    continue;

                int z = i / j;

                if (z < j)
                    break;

                if (z == 1 || z - j > 7)
                    continue;

                str = toStr(++amount) + "\t" + toStr(j) + "\t" + toStr(z) + "\t" + toStr(i) + "\n";
                list.add(str);

                if (z == j)
                    break;
            }
        }
    }

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(int[] ar) {
        Random rnd = new Random();

        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);

            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }**/

    private void buttonsUpdate() {
        //Object item = cmbSquareSizes.getSelectedItem();
        SquareSize item = (SquareSize) cmbSubsquareSizes.getSelectedItem();

        boolean isRandom = (form == 1);

        if (!isRandom && amount == 1 && squares != 0 && item != null && sx == sy) {
            lotOfSquares = btnLotsOfSquares.isSelected();

            if (!lotOfSquares)
                btnLotsOfSquares.setText("and to 4");
            else
                btnLotsOfSquares.setText("and a lot");
            btnLotsOfSquares.setEnabled(true);
        }
        else {
            lotOfSquares = false;

            btnRotate.setText("and to 4");
            btnLotsOfSquares.setEnabled(false);
        }

        if (isRandom) {
            btnRotate.setText("and isn't needed to rotate");
            btnRotate.setEnabled(false);
        }
        else if (item == null || sx == sy) {
            btnRotate.setText("and can't be rotated");
            btnRotate.setEnabled(false);
        }
        else {
            if (!rotate)
                btnRotate.setText("without rotation");
            else
                btnRotate.setText("with rotation");
            btnRotate.setEnabled(true);
        }
    }

    private void sizeChanged() {
        cmbSubsquareSizes.removeAllItems();

        boolean isRandom = (form == 1);

        // get subsquare's sizes
        if (size < 4) {
            size = 0;
        }
        else if (size == 5 || size == 7 || size == 11 || size == 13 || size == 17
                || size == 19 || size == 23 || size == 29 || size == 31 || size == 37
                || size == 41 || size == 43 || size == 47 || size == 53 || size == 59
                || size == 61 || size == 67 || size == 71 || size == 73 || size == 79
                || size == 83 || size == 89 || size == 97) {
            if (isRandom) {
                SquareSize item = new SquareSize(size, 1);
                cmbSubsquareSizes.insertItemAt(item, 0);
                cmbSubsquareSizes.setSelectedItem(item);
            }
            else {
                size = 0;
            }
        }
        else {
            findSizes(size);
        }

        // set control's states
        if (cmbSubsquareSizes.getItemCount() == 0) {
            if (!txtSquareSize.getText().isEmpty()) {
                txtSquareSize.setText("");
                txtSquareSize.requestFocusInWindow();

                cmbSquareAmount.setEnabled(false);

                cmbSubsquareSizes.setEnabled(false);
            }
        }
        else {
            // current size
            int w = pnlBoard.getWidth();
            int h = pnlBoard.getHeight();
            // recommended size
            Dimension d = pnlBoard.getMinimumSize();

            // screen's size
            final Toolkit toolkit = Toolkit.getDefaultToolkit();
            final Insets insets = toolkit.getScreenInsets(getGraphicsConfiguration());
            final Dimension size = toolkit.getScreenSize();
            size.width  -= insets.left + insets.right;
            size.height -= insets.top + insets.bottom;

            // new frame's size
            if (w < d.width) {
                w = this.getWidth() + d.width - w;
                if (w > size.width)
                    w = size.width;
            }
            else
                w = this.getWidth();
            if (h < d.height) {
                h = this.getHeight() + d.height - h;
                if (h > size.height)
                    h = size.height;
            }
            else
                h = this.getHeight();

            this.setSize(w, h);

            pnlBoard.setPreferredSize(d);

            if (isRandom) {
                cmbDiagonals.setEnabled(false);
                cmbSquares.setEnabled(false);

                cmbSquareAmount.setEnabled(false);

                cmbSubsquareSizes.setEnabled(false);
            }
            else {
                cmbDiagonals.setEnabled(true);
                cmbSquares.setEnabled(true);

                cmbSquareAmount.setEnabled(true);

                if (cmbSubsquareSizes.getItemCount() > 1) {
                    cmbSubsquareSizes.setEnabled(true);
                    cmbSubsquareSizes.requestFocusInWindow();
                }
            }
        }

        buttonsUpdate();

        maskFigures = getMaskFigures();
        maskSquares = getMaskSquares();
    }

    private void squareChanged() {
        SquareSize item = (SquareSize) cmbSubsquareSizes.getSelectedItem();

        if (item != null) {
            if (!rotate) {
                sx = item.x;
                sy = item.y;
            }
            else {
                sx = item.y;
                sy = item.x;
            }

            buttonsUpdate();

            maskFigures = getMaskFigures();
            maskSquares = getMaskSquares();

            pnlBoard.repaint();
        }
    }

    private void findSizes(int x) {
        SquareSize item = null;

        for (int j = 2; j < 11; j++) {
            if (x % j != 0)
                continue;

            int z = x / j;

            if (z < j)
                break;

            if (z - j > 7)
                continue;

            item = new SquareSize(z, j);
            cmbSubsquareSizes.insertItemAt(item, 0);

            if (z == j)
                break;
        }

        cmbSubsquareSizes.setSelectedItem(item);
    }

    private boolean[] getLeftRight(int value) {
        boolean[] arr = new boolean[2];

        arr[0] = false;
        arr[1] = false;

        switch (value) {
            case 1:
                arr[0] = true;
                break;
            case 2:
                arr[1] = true;
                break;
            case 3:
                arr[0] = true;
                arr[1] = true;
        }

        return arr;
    }
}
