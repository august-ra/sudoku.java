import Sudoku.*;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;

public class MainFrame extends JFrame {
    private final static BasicStroke stroke = new BasicStroke(3.0f);
    private final static BasicStroke thin   = new BasicStroke(1.0f);

    private final Generator game = new Generator();

    //private JScrollPane scrollPane;
    private JBoard pnlBoard;
    //private JPanel pnlBottom;
    //private JPanel   pnlStyle;
    private JComboBox<String> cmbSubsquareForm;
    private JComboBox<String> cmbDiagonals;
    private JComboBox<String> cmbSubsquares;
    private JToggleButton btnLotsOfSubsquares;
    //private JPanel   pnlSize;
    private JComboBox<Integer> cmbSquareAmount;
    private JFormattedTextField txtSquareSize;
    private JComboBox<SquareSize> cmbSubsquareSizes;
    private JToggleButton btnRotate;
    //private JPanel   pnlAdditional;
    private JTextField    txtCellSize;
    private JComboBox<String> cmbDigitsStyle;
    private JComboBox<String> cmbLanguage;
    private JButton       btnStart;

    // on-form settings
    private int amount, form, diagonals, subsquares, size, sx, sy;
    private boolean lotOfSquares, rotate;
    private int digitsStyle, digitsLang;

    private boolean sizeUpdated = false;

    // calculated values
    private int[][] maskFigures, maskSquares;
    private int[][] digits;

    private String[] a;          // digits' alphabet

    private int bWidth, bHeight; // board's size in pixels
    private int sizeX, sizeY;    // board's size in cells
    private int px, py;          // paddings from top-left corner
    private float sizeCell;      // cell's size

    private Point[]     p;       // top-left squares' beginnings
    private Area[] joints;       // figures' joints
    private Point[]     s;       // top-left subsquares' corners
    private boolean[][] c;       // rounding squares' corners

    private boolean[] dLeftRight;
    private boolean[] sLeftRight;

    private int step;

    // drawing

    private class JBoard extends JPanel {
        Graphics2D          g;
        private Font        f;
        private FontMetrics m;

        private void setStroke(int i, int s) {
            if (i % s == 0)
                g.setStroke(stroke);
            else
                g.setStroke(thin);
        }

        private void drawLines(boolean horizontal,
                               boolean vertical) {
            for (int n = 0; n < amount; n++) {
                for (int i = 1; i < size; i++) {
                    if (vertical) {
                        int x = Converters.toInt(sizeCell * (p[n].x + i));

                        setStroke(i, sx);
                        g.drawLine(x + px, Converters.toInt(sizeCell * p[n].y) + py, x + px, Converters.toInt(sizeCell * (p[n].y + size)) + py);
                    }
                    if (horizontal) {
                        int y = Converters.toInt(sizeCell * (p[n].y + i));

                        setStroke(i, sy);
                        g.drawLine(Converters.toInt(sizeCell * p[n].x) + px, y + py, Converters.toInt(sizeCell * (p[n].x + size)) + px, y + py);
                    }
                }
            }
        }

        private void drawDigit(int x, int y, int z) {
            if (g == null)
                return;

            if (z == 0)
                return;

            String s = a[z];

            float x1 = sizeCell * x;
            float y1 = sizeCell * y;

            float x3 = x1 + (sizeCell - m.stringWidth(s)) / 2;
            float y3 = y1 + (sizeCell - m.getHeight()) / 2 + m.getAscent();

            g.drawString(s, x3 + px, y3 + py);
        }

        @Override
        protected void paintComponent(Graphics g2) {
            super.paintComponent(g2);

            g = (Graphics2D) g2;

            px = py = 10;

            int w = getWidth() - 2 * px;
            int h = getHeight() - 2 * py;

            if (amount == 1)
                sizeCell = Math.min(Converters.toFloat(w) / size, Converters.toFloat(h) / size);
            else
                sizeCell = Math.min(Converters.toFloat(w) / sizeX, Converters.toFloat(h) / sizeY);

            if (sizeCell < 30)
                sizeCell = 30;
            else if (sizeCell > 65)
                sizeCell = 65;

            txtCellSize.setText(new DecimalFormat("#.00").format(sizeCell));

            bWidth = Converters.toInt(sizeCell * sizeX);
            bHeight = Converters.toInt(sizeCell * sizeY);

            px = (w - bWidth) / 2 + px;
            py = (h - bHeight) / 2 + py;

            // background
            g.setColor(Color.WHITE);
            //g.fillRoundRect(px, py, bWidth, bHeight, 15, 15);

            for (int n = 0; n < amount; n++) {
                int x1 = Converters.toInt(sizeCell * p[n].x);
                int x2 = Converters.toInt(sizeCell * (p[n].x + size));
                int y1 = Converters.toInt(sizeCell * p[n].y);
                int y2 = Converters.toInt(sizeCell * (p[n].y + size));
                int wi = x2 - x1;
                int he = y2 - y1;

                if (c[n][0] && c[n][1] && c[n][2] && c[n][3])
                    g.fillRect(x1 + px, y1 + py, wi, he);
                else // ! amount == 5 && n == 1
                    g.fillRoundRect(x1 + px, y1 + py, wi, he, 15, 15);
            }

            // diagonals and squares
            if (form == 0) {
                if (subsquares > 0) {
                    g.setColor(Color.CYAN);

                    for (int n = 0; n < amount; n++) {
                        for (int x = 0; x < size; x++) {
                            int x1 = Converters.toInt(sizeCell * (p[n].x + x));
                            int x2 = Converters.toInt(sizeCell * (p[n].x + x + 1));
                            int wi = x2 - x1;

                            for (int y = 0; y < size; y++) {
                                int y1 = Converters.toInt(sizeCell * (p[n].y + y));
                                int y2 = Converters.toInt(sizeCell * (p[n].y + y + 1));
                                int he = y2 - y1;

                                if (maskSquares[p[n].x + x][p[n].y + y] > 0)
                                    g.fillRect(x1 + px, y1 + py, wi, he);
                            }
                        }
                    }
                }

                if (diagonals > 0) {
                    for (int n = 0; n < amount; n++) {
                        for (int i = 0, j = size - 1; i < size; i++, j--) {
                            int x1 = Converters.toInt(sizeCell * (p[n].x + i));
                            int x2 = Converters.toInt(sizeCell * (p[n].x + j));
                            int x3 = Converters.toInt(sizeCell * (p[n].x + i + 1));
                            int x4 = Converters.toInt(sizeCell * (p[n].x + j + 1));
                            int y1 = Converters.toInt(sizeCell * (p[n].y + i));
                            int y2 = Converters.toInt(sizeCell * (p[n].y + j));
                            int y3 = Converters.toInt(sizeCell * (p[n].y + i + 1));
                            int y4 = Converters.toInt(sizeCell * (p[n].y + j + 1));

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
                int x1 = Converters.toInt(sizeCell * p[n].x);
                int x2 = Converters.toInt(sizeCell * (p[n].x + size));
                int y1 = Converters.toInt(sizeCell * p[n].y);
                int y2 = Converters.toInt(sizeCell * (p[n].y + size));
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

                        int x1 = Converters.toInt(sizeCell * (p[n].x + x));
                        int x2 = Converters.toInt(sizeCell * (p[n].x + x + 1));

                        int y1 = Converters.toInt(sizeCell * (p[n].y + y));
                        int y2 = Converters.toInt(sizeCell * (p[n].y + y + 1));

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
                    drawLines(true, true);
                }
                else {
                    drawLines(true, false);
                    drawLines(false, true);
                }
            }
            else { // form == 1 && maskFigures != null
                for (int n = 0; n < amount; n++) {
                    // vertical lines
                    for (int y = 0; y < size; y++) {
                        for (int x = 0; x < size - 1; x++) {
                            if (maskFigures[p[n].x + x][p[n].y + y] == -1 && maskFigures[p[n].x + x + 1][p[n].y + y] == -1)
                                continue;

                            int x1 = Converters.toInt(sizeCell * (p[n].x + x + 1));
                            int y1 = Converters.toInt(sizeCell * (p[n].y + y));
                            int y2 = Converters.toInt(sizeCell * (p[n].y + y + 1));

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

                            int x1 = Converters.toInt(sizeCell * (p[n].x + x));
                            int x2 = Converters.toInt(sizeCell * (p[n].x + x + 1));
                            int y1 = Converters.toInt(sizeCell * (p[n].y + y + 1));

                            if (maskFigures[p[n].x + x][p[n].y + y] != maskFigures[p[n].x + x][p[n].y + y + 1])
                                g.setStroke(stroke);
                            else
                                g.setStroke(thin);

                            g.drawLine(x1 + px, y1 + py, x2 + px, y1 + py);
                        }
                    }
                }
            }

            // digits
            {
                String fontName = g.getFont().getFontName();
                String txt      = Converters.toStr(size);

                int fontSize = Converters.toInt(sizeCell) - 5;
                f = new Font(fontName, Font.BOLD, fontSize);
                m = g.getFontMetrics(f);

                if (digitsStyle == 0)
                    while (m.stringWidth(txt) >= sizeCell - 5) {
                        fontSize--;
                        f = new Font(fontName, Font.BOLD, fontSize);
                        m = g.getFontMetrics(f);
                    }

                g.setFont(f);
                g.setColor(Color.GREEN);

                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        int z = digits[x][y];

                        drawDigit(x, y, z);
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

    // this class

    public static void main(String[] args) {
        MainFrame app = new MainFrame();
        //app.setExtendedState(JFrame.MAXIMIZED_BOTH);
        app.setVisible(true);
        app.pack();
    }

    // generation

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
                case 1:
                    sizeX = size;
                    sizeY = size;
                    p = new Point[] {new Point(0, 0)};
                    joints = new Area[] {new Area(0, 0, 0, 0)};
                    c = new boolean[][] {{false, false, false, false}};
                    break;
                case 2:
                    sizeX = size * 2 - cx;
                    sizeY = size * 2 - cy;
                    p = new Point[] {new Point(0, 0),
                                     new Point(size - cx, size - cy)
                    };
                    joints = new Area[] {new Area(0, 0, 0, 0),
                                         new Area(size - cx, size - cy, size, size)
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
                    joints = new Area[] {new Area(0, 0, 0, 0),
                                         new Area(size - cx, size - cy, size, size),
                                         new Area(size - cx, size * 2 - cy * 2, size, size * 2 - cy)
                    };
                    c = new boolean[][] {{false, false,  true, false},
                                         { true, false, false,  true},
                                         {false,  true, false, false}
                    };
                    break;
                case 4:
                    sizeX = size * 2 - cx;
                    sizeY = size * 4 - cy * 3;
                    p = new Point[] {new Point(0, 0),
                                     new Point(size - cx, size - cy),
                                     new Point(0, size * 2 - cy * 2),
                                     new Point(size - cx, size * 3 - cy * 3)
                    };
                    joints = new Area[] {new Area(0, 0, 0, 0),
                                         new Area(size - cx, size - cy, size, size),
                                         new Area(size - cx, size * 2 - cy * 2, size, size * 2 - cy),
                                         new Area(size - cx, size * 3 - cy * 3, size, size * 3 - cy * 2)
                    };
                    c = new boolean[][] {{false, false,  true, false},
                                         { true, false, false,  true},
                                         {false,  true,  true, false},
                                         { true, false, false, false}
                    };
                    break;
                case 5:
                    sizeX = size * 3 - cx * 2;
                    sizeY = size * 3 - cy * 2;
                    p = new Point[] {new Point(0, 0),
                                     new Point(size - cx, size - cy),
                                     new Point(0, size * 2 - cy * 2),
                                     new Point(size * 2 - cx * 2, 0),
                                     new Point(size * 2 - cx * 2, size * 2 - cy * 2)
                    };
                    joints = new Area[] {new Area(0, 0, 0, 0),
                                         new Area(size - cx, size - cy, size, size),
                                         new Area(size - cx, size * 2 - cy * 2, size, size * 2 - cy),
                                         new Area(size * 2 - cx * 2, size - cy, size * 2 - cx, size),
                                         new Area(size * 2 - cx * 2, size * 2 - cy * 2, size * 2 - cx, size * 2 - cy)
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

        if (form == 1 || subsquares == 0 || sx == 1 || sy == 1)
            return arr;

        ArrayList<Point> list = new ArrayList<>();

        // 1 subsquare
        if (size == 4) {
            list.add(new Point(1, 1));

            for (int n = 0; n < amount; n++)
                for (int x = 0; x < sx; x++)
                    for (int y = 0; y < sy; y++)
                        arr[p[n].x + x + 1][p[n].y + y + 1] = 1;
        }
        // 2 subsquares
        else if (sx == 2 || sy == 2) {
            int x1 = sx / 2;
            int x2 = size - x1 - sx;

            int y1 = sy / 2;
            int y2 = size - y1 - sy;

            if (!sLeftRight[0]) {
                int x3 = x1;
                x1 = x2;
                x2 = x3;
            }

            list.add(new Point(x1, y1));
            list.add(new Point(x2, y2));

            for (int n = 0; n < amount; n++) {
                for (int y = 0; y < sy; y++) {
                    for (int x = 0; x < sx; x++) {
                        arr[p[n].x + x1 + x][p[n].y + y1 + y] = 1;
                        arr[p[n].x + x2 + x][p[n].y + y2 + y] = 2;
                    }
                }
            }
        }
        // 2-4 subsquares
        else if (!lotOfSquares) {
            int x1 = sx / 2;
            int x2 = size - x1 - sx;

            int y1 = sy / 2;
            int y2 = size - y1 - sy;

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

            if (sLeftRight[0]) {
                list.add(new Point(x1, y1));
                list.add(new Point(x2, y2));
            }
            if (sLeftRight[1]) {
                list.add(new Point(x2, y1));
                list.add(new Point(x1, y2));
            }

            for (int n = 0; n < amount; n++) {
                for (int y = 0; y < sy; y++) {
                    for (int x = 0; x < sx; x++) {
                        if (sLeftRight[0]) {
                            arr[p[n].x + x1 + x][p[n].y + y1 + y] = 1;
                            arr[p[n].x + x2 + x][p[n].y + y2 + y] = 2;
                        }
                        if (sLeftRight[1]) {
                            arr[p[n].x + x2 + x][p[n].y + y1 + y] = 3;
                            arr[p[n].x + x1 + x][p[n].y + y2 + y] = 4;
                        }
                    }
                }
            }
        }
        // 2-17 subsquares
        else {
            for (int i = 1, a = 1, b = size; i < size; i += sx + 1, a++, b++) {
                if (sLeftRight[0])
                    list.add(new Point(i, i));

                if (sLeftRight[1] && !(sLeftRight[0] && size % 2 == 0 && a == size / 2))
                    list.add(new Point(size - i - sx, i));

                for (int n = 0; n < amount; n++) {
                    for (int y = 0; y < sy; y++) {
                        int y1 = i + y;

                        for (int x = 0; x < sx; x++) {
                            int x1 = i + x;

                            if (sLeftRight[0])
                                arr[p[n].x + x1][p[n].y + y1] = a;

                            // center subsquare
                            if (sLeftRight[0] && size % 2 == 0 && a == size / 2)
                                continue;

                            if (sLeftRight[1]) {
                                int x2 = size - i - sx + x;

                                arr[p[n].x + x2][p[n].y + y1] = b;
                            }
                        }
                    }
                }
            }
        }

        s = list.toArray(new Point[0]);
        return arr;
    }

    private String[] getAlphabet() {
        String[] a = new String[size + 1];

        if (digitsStyle == 0) {
            for (int z = 1; z <= size; z++)
                a[z] = Converters.toStr(z);
        }
        else {
            char[] chars;
            int    len;

            if (digitsLang == 0) {
                chars = new char[] {
                        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
                        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                        'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
                };
                len = 25;
            }
            else {
                chars = new char[] {
                        'А', 'Б', 'В', 'Г', 'Д', 'Е', /*'Ё',*/ 'Ж', 'З',
                        'И', /*'Й',*/ 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р',
                        'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ',
                        'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я'
                };
                len = 31;
            }

            int z = 1, z1 = -1, z2 = 0;

            if (digitsStyle == 1)
                for (; z <= size && z <= 9; z++)
                    a[z] = Converters.toStr(z);

            if (size > 9 || digitsStyle == 2) {
                for (; z <= size; z++) {
                    if (z1 == -1)
                        a[z] = "" + chars[z2];
                    else
                        a[z] = "" + chars[z1] + chars[z2];

                    z2++;

                    if (z2 >= len) {
                        z2 = 0;
                        z1++;
                    }
                }
            }
        }

        return a;
    }

    private class Page {
        int[][][] data;
        int       state;

        private Page() {
            data = new int[sizeX][sizeY][size + 1];
            state = step;
        }

        private Page(int[][][] d) {
            data = d;
            state = step;
        }

        int[][][] copy() {
            int[][][] res = new int[sizeX][sizeY][size + 1];

            for (int x = 0; x < sizeX; x++)
                for (int y = 0; y < sizeY; y++)
                    System.arraycopy(data[x][y], 0, res[x][y], 0, data[x][y].length);
            //for (int z = 0; z <= size; z++)
            //    res[x][y][z] = data[x][y][z];

            return res;
        }

        private Page setDigit(int x, int y, int d, boolean newPage) {
            if (newPage) {
                // use the variant
                data[x][y][d + 1] = 1;

                // [x][y][0] = digit
                // [x][y][digit] = filled {false, true}
                int[][][] res = copy();

                // set digit
                res[x][y][0] = d + 1;

                // current cell for other digits
                for (int d1 = 1; d1 <= size; d1++)
                    res[x][y][d1] = 1;

                // current column for the digit
                for (int y1 = 0; y1 < size; y1++)
                    res[x][y1][d + 1] = 1;

                // current row for the digit
                for (int x1 = 0; x1 < size; x1++)
                    res[x1][y][d + 1] = 1;

                // current figure for the digit
                if (form == 1) {
                    int z = maskFigures[x][y];

                    for (int x1 = 0; x1 < size; x1++)
                        for (int y1 = 0; y1 < size; y1++)
                            if (maskFigures[x1][y1] == z)
                                res[x1][y1][d + 1] = 1;
                }
                else {
                    int x2 = x / sy * sy;
                    int y2 = y / sx * sx;

                    for (int x1 = 0; x1 < sx; x1++)
                        for (int y1 = 0; y1 < sy; y1++)
                            res[x1 + x2][y1 + y2][d + 1] = 1;
                }

                // current diagonal for the digit
                if (dLeftRight[0] && x == y)
                    for (int x1 = 0; x1 < size; x1++)
                        res[x1][x1][d + 1] = 1;
                if (dLeftRight[1] && x == size - y - 1)
                    for (int x1 = 0, y1 = size - 1; x1 < size; x1++, y1--)
                        res[x1][y1][d + 1] = 1;

                // current subsquare for the digit
                // TODO: fill variant into subsquare
//                if (subsquares > 0) {
//                    for (Point elem : s)
//                        for (int x1 = 0; x1 < sx; x1++)
//                            for (int y1 = 0; y1 < sy; y1++)
//                                if (res[elem.x + x1][elem.y + y1][d + 1] == d)
//                }

                pnlBoard.drawDigit(x, y, d + 1);

                return new Page(res);
            }
            else {
                // set digit
                data[x][y][0] = d + 1;

                return this;
            }
        }

        private int[][] getGame() {
            int[][] res = new int[sizeX][sizeY];

            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    res[x][y] = data[x][y][0];

            return res;
        }

        private ArrayList<String> printArray() {
            ArrayList<String> list = new ArrayList<>();

            for (int y = 0; y < size; y++) {
                StringBuilder s = new StringBuilder();

                for (int x = 0; x < size; x++)
                    s.append(" ").append(data[x][y][0]);

                list.add(s.toString());
            }

            return list;
        }

        private boolean safeFill(int n, int x, int y, int d) {
            // cell(x, y)
            if (data[x][y][0] != 0)
                return false;

            // row(y)
            for (int x1 = 0; x1 < size; x1++)
                if (data[x1][y][0] == d)
                    return false;

            // column(x)
            for (int y1 = 0; y1 < size; y1++)
                if (data[x][y1][0] == d)
                    return false;

            // figure
            if (form == 1) {
                int z = maskFigures[x][y];

                for (int x1 = 0; x1 < size; x1++)
                    for (int y1 = 0; y1 < size; y1++)
                        if (maskFigures[p[n].x + x1][p[n].y + y1] == z
                                && data[p[n].x + x1][p[n].y + y1][0] == d)
                            return false;
            }
            else {
                int x2 = (x - p[n].x) / sx * sx + p[n].x;
                int y2 = (y - p[n].y) / sy * sy + p[n].y;

                for (int x1 = 0; x1 < sx; x1++)
                    for (int y1 = 0; y1 < sy; y1++)
                        if (data[x1 + x2][y1 + y2][0] == d)
                            return false;
            }

            // diagonal(left)
            if (dLeftRight[0] && x == y)
                for (int x1 = 0; x1 < size; x1++)
                    if (data[x1][x1][0] == d)
                        return false;

            // diagonal(right)
            if (dLeftRight[1] && size - x - 1 == y)
                for (int x1 = 0; x1 < size; x1++)
                    if (data[size - x1 - 1][x1][0] == d)
                        return false;

            // subsquares
//            if (subsquares > 0)
//                for (Point elem : s)
//                    for (int x1 = 0; x1 < sx; x1++)
//                        for (int y1 = 0; y1 < sy; y1++)
//                            if (data[elem.x + x1][elem.y + y1][0] == d)
//                                return false;

            data[x][y][0] = d;

            return true;
        }

        private boolean isPossibleCell(int x, int y, int d) {
            return data[x][y][d + 1] == 0;
        }

        private boolean isFilledDiagonal(int d, boolean left) {
            if (left) {
                for (int x = 0; x < size; x++)
                    if (data[x][x][0] == d + 1)
                        return true;
            }
            else {
                for (int x = 0; x < size; x++)
                    if (data[size - x - 1][x][0] == d + 1)
                        return true;
            }

            return false;
        }

        private boolean isFilledSubsquare(int d, Point elem) {
            for (int x = 0; x < sx; x++)
                for (int y = 0; y < sx; y++)
                    if (data[elem.x + x][elem.y + y][0] == d + 1)
                        return true;

            return false;
        }

        private boolean isFilledColumn(int x, int d) {
            for (int y1 = 0; y1 < size; y1++)
                if (data[x][y1][0] == d + 1)
                    return true;

            return false;
        }
    }

    private class Pages {
        ArrayList<Page> data;
        Page            last;

        private Pages() {
            data = new ArrayList<>();

            last = new Page();
            data.add(last);
        }

        private void deleteLast() {
            data.remove(last);

            int z = data.size() - 1;

            step = last.state;

            if (z >= 0)
                last = data.get(z);
            else {
                last = null;
                System.out.println("Nothing is in 'pages'!");
                System.exit(-1);
            }
        }

        private void setDigit(int x, int y, int d, boolean newPage) {
            last = last.setDigit(x, y, d, newPage);
            data.add(last);
        }

        private int[][] getGame() {
            return last.getGame();
        }

        private ArrayList<String> printArray() {
            return last.printArray();
        }

        private boolean safeFill(int n, int x, int y, int d) {
            return last.safeFill(n, x, y, d);
        }

        private boolean isPossibleCell(int x, int y, int d) {
            return last.isPossibleCell(x, y, d);
        }

        private boolean isFilledDiagonal(int d, boolean left) {
            return last.isFilledDiagonal(d, left);
        }

        private boolean isFilledSubsquare(int d, Point elem) {
            return last.isFilledSubsquare(d, elem);
        }

        private boolean isFilledColumn(int x, int d) {
            return last.isFilledColumn(x, d);
        }
    }

    private ArrayList<String> printArray(int[][] arr) {
        ArrayList<String> list = new ArrayList<>();

        for (int y = 0; y < size; y++) {
            StringBuilder s = new StringBuilder();

            for (int x = 0; x < size; x++)
                s.append(" ").append(arr[x][y]);

            list.add(s.toString());
        }

        return list;
    }

    private class Generator {
        Pages pages;

        final Random rnd = new Random();

        private int[][] setUpDigits(boolean real) {
            if (!real) {
                int[][] arr = new int[sizeX][sizeY];

                int max;

                if (diagonals == 3 && subsquares > 0 || subsquares == 3)
                    max = size;
                else
                    max = size * size / 2;

                for (int n = 0; n < amount; n++) {
                    int max2 = max;

                    for (int x = joints[n].x1; x < joints[n].x2; x++)
                        for (int y = joints[n].y1; y < joints[n].y2; y++)
                            if (arr[x][y] > 0)
                                max2--;

                    for (int z = 0; z < max2; ) {
                        int x = rnd.nextInt(size) + p[n].x;
                        int y = rnd.nextInt(size) + p[n].y;

                        if (x >= joints[n].x1 && x < joints[n].x2
                                && y >= joints[n].y1 && y < joints[n].y2)
                            continue;

                        if (maskFigures[x][y] == -1)
                            continue;

                        if (safeFill(arr, n, x, y, rnd.nextInt(size) + 1))
                            z++;
                    }
                }

                return arr;
            }
            else {
                for (int n = 0; n < amount; n++) {
                    pages = new Pages();

                    // step 0 - center
                    if (diagonals == 3 && size % 2 == 1) {
                        int d = rnd.nextInt(size);
                        int c = size / 2;

                        if (pages.isPossibleCell(p[n].x + c, p[n].y + c, d))
                            pages.setDigit(p[n].x + c, p[n].y + c, d, true);
                    }

                    for (int d = 0; d < size; d++) {
                        // setting last digit is easy
                        if (d == size - 1) {
                            for (int x = 0; x < size; x++)
                                for (int y = 0; y < size; y++)
                                    if (pages.isPossibleCell(p[n].x + x, p[n].y + y, d))
                                        pages.setDigit(p[n].x + x, p[n].y + y, d, false);
                            break;
                        }

                        for (step = 1; step < 3; step++) {
                            if (step == 1 && dLeftRight[0]) {
                                ArrayList<Integer> list = new ArrayList<>();

                                for (int x = 0, y = 0; x < size; x++, y++)
                                    if (pages.isPossibleCell(x, y, d))
                                        list.add(x);

                                int z = list.size();

                                if (z > 0) {
                                    int x = list.get(rnd.nextInt(z));

                                    pages.setDigit(p[n].x + x, p[n].y + x, d, true);
                                }
                            }
                            else if (step == 2 && dLeftRight[1]) {
                                ArrayList<Integer> list = new ArrayList<>();

                                for (int x = 0, y = size - 1; x < size; x++, y--)
                                    if (pages.isPossibleCell(x, y, d))
                                        list.add(y);

                                int z = list.size();

                                if (z > 0) {
                                    int x = list.get(rnd.nextInt(z));
                                    int y = size - x - 1;

                                    pages.setDigit(p[n].x + x, p[n].y + y, d, true);
                                }
                            }
                            else if (step == 3 && subsquares > 0) {
                                for (Point elem : s)
                                    if (!pages.isFilledSubsquare(d, elem))
                                        ; // TODO: make checking and adding
                            }
                            else if (step == 4) {
                                for (int x = 0; x < size; x++) {
                                    if (pages.isFilledColumn(p[n].x + x, d))
                                        continue;

                                    ArrayList<Integer> list = new ArrayList<>();

                                    for (int y = 0; y < size; y++)
                                        if (pages.isPossibleCell(x, y, d))
                                            list.add(y);

                                    int z = list.size();

                                    if (z > 0) {
                                        int y = list.get(rnd.nextInt(z));

                                        pages.setDigit(x, y, d, true);
                                    }
                                    else {
                                        // draw back
                                        x -= 2;
                                        pages.deleteLast();

                                        if (x < 0) {
                                            System.out.println("Step was " + Converters.toStr(step));
                                            step -= 2;
                                            System.out.println("Step is " + Converters.toStr(step));

                                            if (step <= 0) {
                                                d -= 2;

                                                if (d < 0) {
                                                    System.out.println("It couldn't set " + Converters.toStr(d + 1));
                                                    return pages.getGame();
                                                }

                                                x = size - 1;
                                                pages.deleteLast();
                                            }
                                            else {
                                                d -= 1;

                                                if (d < 0) {
                                                    System.out.println("It couldn't set " + Converters.toStr(d + 1));
                                                    return pages.getGame();
                                                }

                                                x = size - 1;
                                                pages.deleteLast();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return pages.getGame();
            }
        }

        private boolean safeFill(int[][] game, int n, int x, int y, int d) {
            // cell(x, y)
            if (game[x][y] != 0)
                return false;

            // row(y)
            for (int x1 = 0; x1 < size; x1++)
                if (game[p[n].x + x1][y] == d)
                    return false;

            // column(x)
            for (int y1 = 0; y1 < size; y1++)
                if (game[x][p[n].y + y1] == d)
                    return false;

            // figure
            if (form == 1) {
                int z = maskFigures[x][y];

                for (int x1 = 0; x1 < size; x1++)
                    for (int y1 = 0; y1 < size; y1++)
                        if (maskFigures[p[n].x + x1][p[n].y + y1] == z
                                && game[p[n].x + x1][p[n].y + y1] == d)
                            return false;
            }
            else {
                int x2 = (x - p[n].x) / sx * sx + p[n].x;
                int y2 = (y - p[n].y) / sy * sy + p[n].y;

                for (int x1 = 0; x1 < sx; x1++)
                    for (int y1 = 0; y1 < sy; y1++)
                        if (game[x1 + x2][y1 + y2] == d)
                            return false;
            }

            // diagonal(left)
            if (dLeftRight[0] && x == y)
                for (int x1 = 0; x1 < size; x1++)
                    if (game[p[n].x + x1][p[n].y + x1] == d)
                        return false;

            // diagonal(right)
            if (dLeftRight[1] && x == size - y - 1)
                for (int x1 = 0, y1 = size - 1; x1 < size; x1++, y1--)
                    if (game[p[n].x + x1][p[n].y + y1] == d)
                        return false;

            // subsquares
            if (subsquares > 0)
                for (Point elem : s)
                    if (x >= elem.x && x < elem.x + sx
                            && y >= elem.y && y < elem.y + sy)
                        for (int x1 = 0; x1 < sx; x1++)
                            for (int y1 = 0; y1 < sy; y1++)
                                if (game[p[n].x + elem.x + x1][p[n].y + elem.y + y1] == d)
                                    return false;

            game[x][y] = d;

            return true;
        }
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

                if (x < px || x > bWidth + px)
                    return;
                else
                    x = Converters.toInt((x - px) / sizeCell);

                if (y < py || y > bHeight + py)
                    return;
                else
                    y = Converters.toInt((y - py) / sizeCell);

                if (maskFigures[x][y] == -1)
                    return;

                x++;
                y++;

                if (e.getButton() == MouseEvent.BUTTON1) {
                    JOptionPane.showMessageDialog(null,
                            Converters.toStr(x) + " ; " + Converters.toStr(y),
                            "some",
                            JOptionPane.INFORMATION_MESSAGE);
                    // TODO: edit digits in cells
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(pnlBoard, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane);

        form = 0;

        cmbSubsquareForm = new JComboBox<>(new String[] {
                "squares",
                "random"
        });

        cmbSubsquareForm.addActionListener((ActionEvent e) -> {
            form = cmbSubsquareForm.getSelectedIndex();

            if (form == 0 && isPrime(size)) {
                size = 9;
                txtSquareSize.setText("9");
            }

            sizeChanged();
            squareChanged();
        });

        diagonals = 0;
        dLeftRight = new boolean[] {false, false};

        cmbDiagonals = new JComboBox<>(new String[] {
                "neither diagonals",
                "left diagonal",
                "right diagonal",
                "both diagonals"
        });

        cmbDiagonals.addActionListener((ActionEvent e) -> {
            diagonals = cmbDiagonals.getSelectedIndex();
            dLeftRight = getLeftRight(diagonals);

            maskFigures = getMaskFigures();
            maskSquares = getMaskSquares();

            digits = game.setUpDigits(false);

            pnlBoard.repaint();
        });

        subsquares = 0;
        sLeftRight = new boolean[] {false, false};

        cmbSubsquares = new JComboBox<>(new String[] {
                "neither squares",
                "left square",
                "right square",
                "both squares"
        });

        cmbSubsquares.addActionListener((ActionEvent e) -> {
            subsquares = cmbSubsquares.getSelectedIndex();
            sLeftRight = getLeftRight(subsquares);

            maskSquares = getMaskSquares();

            buttonsUpdate();

            digits = game.setUpDigits(false);

            pnlBoard.repaint();
        });

        lotOfSquares = false;

        btnLotsOfSubsquares = new JToggleButton("and to 4");
        btnLotsOfSubsquares.setEnabled(false);

        btnLotsOfSubsquares.addActionListener((ActionEvent e) -> {
            buttonsUpdate();

            squareChanged();
        });

        JPanel pnlStyle = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlStyle.add(new JLabel("Figures on a board are"));
        pnlStyle.add(cmbSubsquareForm);
        pnlStyle.add(new JLabel("including"));
        pnlStyle.add(cmbDiagonals);
        pnlStyle.add(new JLabel("and"));
        pnlStyle.add(cmbSubsquares);
        pnlStyle.add(btnLotsOfSubsquares);
        //pnlStyle.add(new JLabel("."));

        amount = 1;

        cmbSquareAmount = new JComboBox<>(new Integer[] {1, 2, 3, 4, 5});
        cmbSquareAmount.setEnabled(true);

        cmbSquareAmount.addActionListener((ActionEvent e) -> {
            amount = (int) cmbSquareAmount.getSelectedItem();

            sizeChanged();
        });

        NumberFormat    format    = NumberFormat.getInstance();
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
            size = Converters.toInt(txtSquareSize.getText());

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
                    size = Converters.toInt(txtSquareSize.getText());

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

        txtCellSize = new JTextField("");
        txtCellSize.setColumns(5);
        txtCellSize.setHorizontalAlignment(JTextField.CENTER);
        txtCellSize.setEnabled(false);

        digitsStyle = 0;

        cmbDigitsStyle = new JComboBox<>(new String[] {
                "digits",
                "digits and letters",
                "letters"
        });

        cmbDigitsStyle.addActionListener((ActionEvent e) -> {
            int old = digitsStyle;

            digitsStyle = cmbDigitsStyle.getSelectedIndex();

            if (digitsStyle == 1 && size == 9) {
                cmbDigitsStyle.setSelectedIndex(0);
                digitsStyle = 0;

                Toolkit.getDefaultToolkit().beep();
            }

            if (old == digitsStyle)
                return;

            cmbLanguage.setEnabled(digitsStyle != 0);

            a = getAlphabet();

            pnlBoard.repaint();
        });

        digitsLang = 0;

        cmbLanguage = new JComboBox<>(new String[] {
                "english",
                "russian"
        });

        cmbLanguage.addActionListener((ActionEvent e) -> {
            digitsLang = cmbLanguage.getSelectedIndex();

            a = getAlphabet();

            pnlBoard.repaint();
        });

        btnStart = new JButton("START A GAME WITH THE SETTINGS");

        btnStart.addActionListener((ActionEvent e) -> {
            btnStart.setText("STOP THE GAME");

            JOptionPane.showMessageDialog(this, "STARTED");
        });

        JPanel pnlAdditional = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlAdditional.add(new JLabel("Size of cells are"));
        pnlAdditional.add(txtCellSize);
        pnlAdditional.add(new JLabel("and there are"));
        pnlAdditional.add(cmbDigitsStyle);
        pnlAdditional.add(cmbLanguage);
        pnlAdditional.add(new JLabel("into cells"));
        pnlAdditional.add(btnStart);

        JPanel pnlBottom = new JPanel();
        pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.PAGE_AXIS));
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pnlBottom.add(pnlStyle);
        pnlBottom.add(Box.createRigidArea(new Dimension(8, 0)));
        pnlBottom.add(pnlSize);
        pnlBottom.add(Box.createRigidArea(new Dimension(8, 0)));
        pnlBottom.add(pnlAdditional);

        add(pnlBottom, BorderLayout.SOUTH);

        JRootPane rootPane = SwingUtilities.getRootPane(this);
        rootPane.setDefaultButton(btnStart);

        rootPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    System.exit(0);
                else
                    super.keyTyped(e);
            }
        });

        maskFigures = getMaskFigures();
        maskSquares = getMaskSquares();

        a = getAlphabet();
        digits = game.setUpDigits(false);

        pnlBoard.repaint();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void pack() {
        super.pack();

        //pnlBoard.setPreferredSize(new Dimension(pnlBoard.getWidth()-30, pnlBoard.getWidth()-30));
        btnRotate.setPreferredSize(new Dimension(btnRotate.getWidth(), btnRotate.getHeight()));
        btnLotsOfSubsquares.setPreferredSize(new Dimension(btnLotsOfSubsquares.getWidth(), btnLotsOfSubsquares.getHeight()));

        super.pack();

        this.setMinimumSize(new Dimension(this.getWidth(), 300));
        //this.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));

    }

    /**
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
    }
    **/

    // testing

    private boolean isPrime(int d) {
        return (d ==  5 || d ==  7 || d == 11 || d == 13 || d == 17 ||
                d == 19 || d == 23 || d == 29 || d == 31 || d == 37 ||
                d == 41 || d == 43 || d == 47 || d == 53 || d == 59 ||
                d == 61 || d == 67 || d == 71 || d == 73 || d == 79 ||
                d == 83 || d == 89 || d == 97);
    }

    private void buttonsUpdate() {
        //Object item = cmbSquareSizes.getSelectedItem();
        SquareSize item = (SquareSize) cmbSubsquareSizes.getSelectedItem();

        boolean isRandom = (form == 1);

        if (!isRandom && amount == 1 && subsquares != 0 && item != null && sx == sy) {
            lotOfSquares = btnLotsOfSubsquares.isSelected();

            if (!lotOfSquares)
                btnLotsOfSubsquares.setText("and to 4");
            else
                btnLotsOfSubsquares.setText("and a lot");
            btnLotsOfSubsquares.setEnabled(true);
        }
        else {
            lotOfSquares = false;

            btnRotate.setText("and to 4");
            btnLotsOfSubsquares.setEnabled(false);
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
        else if (isPrime(size)) {
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
            final Toolkit   toolkit = Toolkit.getDefaultToolkit();
            final Insets    insets  = toolkit.getScreenInsets(getGraphicsConfiguration());
            final Dimension size    = toolkit.getScreenSize();
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
                cmbSubsquares.setEnabled(false);

                cmbSquareAmount.setEnabled(false);

                cmbSubsquareSizes.setEnabled(false);
            }
            else {
                cmbDiagonals.setEnabled(true);
                cmbSubsquares.setEnabled(true);

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

        a = getAlphabet();
        digits = game.setUpDigits(false);
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

            a = getAlphabet();
            digits = game.setUpDigits(false);

            pnlBoard.repaint();
        }
    }

    private void findSizes(int s) {
        SquareSize item = null;

        for (int y = 2; y < 11; y++) {
            if (s % y != 0)
                continue;

            int x = s / y;

            if (x < y)
                break;

            if (x - y > 7)
                continue;

            item = new SquareSize(x, y);
            cmbSubsquareSizes.insertItemAt(item, 0);

            if (x == y)
                break;
        }

        cmbSubsquareSizes.setSelectedItem(item);
    }

    private boolean[] getLeftRight(int value) {
        boolean[] arr = new boolean[] {false, false};

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
