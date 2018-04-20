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
    private JComboBox<String> cmbStyle;
    private JComboBox<String> cmbDiagonals;
    private JComboBox<String> cmbSquares;
    private JCheckBox    chbSquaresForWholeBoard;
    //private JPanel   pnlSize;
    private JFormattedTextField txtBoardSize;
    private JComboBox<SquareSize> cmbSquareSizes;
    private JToggleButton btnRotate;
    private JButton button;

    private int[][] maskFigures, maskSquares;

    private int amount, form, diagonals, squares, size, sx, sy, board, px, py;
    private int psx, psy;
    private double cell;
    private boolean lotOfSquares, rotate;

    private boolean sizeUpdated = false;

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
            for (int i = 1; i < size; i++) {
                int s = board;
                int z = toInt(cell * i);

                if (vertical) {
                    setStroke(g, i, sx);

                    g.drawLine(z + px, py, z + px, s + py);
                }
                if (horizontal) {
                    setStroke(g, i, sy);

                    g.drawLine(px, z + py, s + px, z + py);
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
            board = w < h ? w : h;
            cell = toFloat(board) / size;

            if (cell < 30) {
                cell = 30;
                board = 30 * size;
            }
            else if (cell > 45) {
                cell = 45;
                board = 45 * size;
            }

            px = (w - board) / 2 + px;
            py = (h - board) / 2 + py;

            // background
            g.setColor(Color.WHITE);
            g.fillRoundRect(px, py, board, board, 15, 15);

            // diagonals and squares
            if (form == 0) {
                if (squares > 0) {
                    g.setColor(Color.CYAN);

                    for (int x = 0; x < size; x++) {
                        int x1 = toInt(cell * x);
                        int x2 = toInt(cell * (x + 1));

                        for (int y = 0; y < size; y++) {
                            int y1 = toInt(cell * y);
                            int y2 = toInt(cell * (y + 1));
                            int wi1 = x2 - x1;
                            int wi2 = y2 - y1;

                            if (maskSquares[x][y] > 0)
                                g.fillRect(x1 + px, y1 + py, wi1, wi2);
                        }
                    }
                }
                if (diagonals > 0) {
                    boolean[] dLeftRight = getLeftRight(diagonals);

                    g.setColor(Color.PINK);

                    for (int i = 0; i < size; i++) {
                        int x1 = toInt(cell * i);
                        int x2 = toInt(cell * (size - i - 1));
                        int x3 = toInt(cell * (i + 1));
                        int x4 = toInt(cell * (size - i));
                        int wi1 = x3 - x1;
                        int wi2 = x4 - x2;

                        if (dLeftRight[0]) {
                            g.fillRoundRect(x1 + px, x1 + py, wi1, wi1, 15, 15);

                            // left top corner
                            if (i > 0)
                                g.fillRect(x1 + px, x1 + py, 15, 15);
                            // left bottom corner
                            g.fillRect(x1 + px, x3 + py - 15, 15, 15);
                            // right top corner
                            g.fillRect(x3 + px - 15, x1 + py, 15, 15);
                            // right bottom corner
                            if (i < size - 1)
                                g.fillRect(x3 + px - 15, x3 + py - 15, 15, 15);
                        }
                        if (dLeftRight[1]) {
                            g.fillRoundRect(x2 + px, x1 + py, wi2, wi2, 15, 15);

                            // left top corner
                            g.fillRect(x2 + px, x1 + py, 15, 15);
                            // left bottom corner
                            if (i < size - 1)
                                g.fillRect(x2 + px, x3 + py - 15, 15, 15);
                            // right top corner
                            if (i > 0)
                                g.fillRect(x4 + px - 15, x1 + py, 15, 15);
                            // right bottom corner
                            g.fillRect(x4 + px - 15, x3 + py - 15, 15, 15);
                        }
                    }
                }
            }

            // bevel
            g.setColor(Color.LIGHT_GRAY);
            g.setStroke(stroke);
            g.drawRoundRect(px, py, board, board, 15, 15);

            // grid
            if (form == 0) {
                if (sx == sy) {
                    drawLines(g, true, true);
                }
                else {
                    drawLines(g, true, false);
                    drawLines(g, false, true);
                }
            }
            else { // form == 1 && maskFigures != null
                // vertical lines
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size - 1; x++) {
                        int zx = toInt(cell * (x + 1));
                        int z1 = toInt(cell * y);
                        int z2 = toInt(cell * (y + 1));

                        if (maskFigures[x][y] != maskFigures[x + 1][y])
                            g.setStroke(stroke);
                        else
                            g.setStroke(thin);

                        g.drawLine(zx + px, z1 + py, zx + px, z2 + py);
                    }
                }
                // horizontal lines
                for (int y = 0; y < size - 1; y++) {
                    for (int x = 0; x < size; x++) {
                        int z1 = toInt(cell * x);
                        int z2 = toInt(cell * (x + 1));
                        int zy = toInt(cell * (y + 1));

                        if (maskFigures[x][y] != maskFigures[x][y + 1])
                            g.setStroke(stroke);
                        else
                            g.setStroke(thin);

                        g.drawLine(z1 + px, zy + py, z2 + px, zy + py);
                    }
                }
            }
        }

        @Override
        public Dimension getMinimumSize() {
            int s = 30 * size + 20;
            return new Dimension(s, s);
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
        else {
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
        int[][] arr = new int[size][size];

        if (form == 1 || squares == 0 || sx == 1 || sy == 1)
            return arr;

        boolean[] sLeftRight = getLeftRight(squares);

        // 1 square
        if (size == 4) {
            for (int x = 0; x < sx; x++)
                for (int y = 0; y < sy; y++)
                    arr[x + 1][y + 1] = 1;
        }
        // 2 squares
        else if (sx == 2 || sy == 2) {
            int x1 = sx / 2;
            int x2 = size - x1 - 1;

            int y1 = sy / 2;
            int y2 = size - y1 - 1;

            if (sLeftRight[0]) {
                for (int y = 0; y < sy; y++) {
                    for (int x = 0; x < sx; x++) {
                        arr[x1 + x][y1 + y] = 1;
                        arr[x2 - x][y2 - y] = 2;
                    }
                }
            }
            else {
                for (int y = 0; y < sy; y++) {
                    for (int x = 0; x < sx; x++) {
                        arr[x2 - x][y1 + y] = 1;
                        arr[x1 + x][y2 - y] = 2;
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

            if ((sx == 3 ^ sy == 3) && (sx + sy) % 2 == 1)
                if (sy == 3) {
                    x1--;
                    x2++;
                }
                else {
                    y1--;
                    y2++;
                }

            for (int y = 0; y < sy; y++) {
                for (int x = 0; x < sx; x++) {
                    if (sLeftRight[0]) {
                        arr[x1 + x][y1 + y] = 1;
                        arr[x2 - x][y2 - y] = 2;
                    }
                    if (sLeftRight[1]) {
                        arr[x2 - x][y1 + y] = 3;
                        arr[x1 + x][y2 - y] = 4;
                    }
                }
            }
        }
        // 2-17 squares
        else {
            for (int i = 1, m = 1, n = size; i < size; i += sx + 1, m++, n++) {
                for (int y = 0; y < sy; y++) {
                    int y1 = i + y;

                    for (int x = 0; x < sx; x++) {
                        int x1 = i + x;

                        if (sLeftRight[0])
                            arr[x1][y1] = m;

                        if (sLeftRight[0] && size % 2 == 0 && m == size / 2)
                            continue;

                        if (sLeftRight[1]) {
                            int x2 = size - x1 - 1;

                            arr[x2][y1] = n;
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

                if (x < px || x > board+px)
                    return;
                else
                    x = toInt((x-px) / cell) + 1;

                if (y < py || y > board+py)
                    return;
                else
                    y = toInt((y-py) / cell) + 1;

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

        cmbStyle = new JComboBox<>(new String[] {
                "squares",
                "random"
        });

        cmbStyle.addActionListener((ActionEvent e) -> {
            form = cmbStyle.getSelectedIndex();

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

        chbSquaresForWholeBoard = new JCheckBox("and a lot");
        chbSquaresForWholeBoard.setEnabled(false);

        chbSquaresForWholeBoard.addActionListener((ActionEvent e) -> {
            lotOfSquares = chbSquaresForWholeBoard.isSelected();
            squareChanged();
        });

        JPanel pnlStyle = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlStyle.add(new JLabel("Figures on a board are"));
        pnlStyle.add(cmbStyle);
        pnlStyle.add(new JLabel("including"));
        pnlStyle.add(cmbDiagonals);
        pnlStyle.add(new JLabel("and"));
        pnlStyle.add(cmbSquares);
        pnlStyle.add(chbSquaresForWholeBoard);
        //pnlStyle.add(new JLabel("."));

        add(pnlStyle, BorderLayout.SOUTH);

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(4);
        formatter.setMaximum(100);

        size = 9;

        txtBoardSize = new JFormattedTextField(formatter);
        txtBoardSize.setColumns(5);
        txtBoardSize.setHorizontalAlignment(JFormattedTextField.CENTER);
        txtBoardSize.setText("9");

        txtBoardSize.addActionListener((ActionEvent e) -> {
            size = toInt(txtBoardSize.getText());

            sizeChanged();
            squareChanged();

            sizeUpdated = true;
        });

        txtBoardSize.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!sizeUpdated) {
                    size = toInt(txtBoardSize.getText());

                    sizeChanged();
                    squareChanged();
                }
                else {
                    sizeUpdated = false;
                }
            }
        });

        sx = sy = 3;

        cmbSquareSizes = new JComboBox<>(new SquareSize[] {new SquareSize(3, 3)});
        cmbSquareSizes.setPrototypeDisplayValue(new SquareSize());
        cmbSquareSizes.setEnabled(false);

        cmbSquareSizes.addActionListener(
                (ActionEvent e) -> squareChanged()
        );

        btnRotate = new JToggleButton("and doesn't need to rotate", false);
        btnRotate.setEnabled(false);

        btnRotate.addActionListener((ActionEvent e) -> {
            rotate = btnRotate.isSelected();
            squareChanged();
        });

        JPanel pnlSize = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlSize.add(new JLabel("Each board's side's size is"));
        pnlSize.add(txtBoardSize);
        pnlSize.add(new JLabel("what's equivalent to"));
        pnlSize.add(cmbSquareSizes);
        pnlSize.add(btnRotate);
        //pnlSize.add(new JLabel("."));

        add(pnlSize, BorderLayout.SOUTH);

        button = new JButton("look for");

        button.addActionListener(
                (ActionEvent e) -> findSizes(toInt(txtBoardSize.getText()))
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
    }**/

    private void buttonsUpdate() {
        //Object item = cmbSquareSizes.getSelectedItem();
        SquareSize item = (SquareSize) cmbSquareSizes.getSelectedItem();

        boolean isRandom = (form == 1);

        if (isRandom) {
            lotOfSquares = false;

            chbSquaresForWholeBoard.setSelected(false);
            chbSquaresForWholeBoard.setEnabled(false);

            btnRotate.setText("and doesn't need to rotate");
            btnRotate.setEnabled(false);
        }
        else if (!cmbSquareSizes.isEnabled() || item == null || sx == sy) {
            if (sx != sy)
                lotOfSquares = false;

            chbSquaresForWholeBoard.setEnabled(squares != 0);

            btnRotate.setText("and can't be rotated");
            btnRotate.setEnabled(false);
        }
        else {
            lotOfSquares = false;

            chbSquaresForWholeBoard.setSelected(false);
            chbSquaresForWholeBoard.setEnabled(false);

            if (!rotate)
                btnRotate.setText("without rotation");
            else
                btnRotate.setText("with rotation");
            btnRotate.setEnabled(true);
        }
    }

    private void sizeChanged() {
        cmbSquareSizes.removeAllItems();

        boolean isRandom = (form == 1);

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
                cmbSquareSizes.insertItemAt(item, 0);
                cmbSquareSizes.setSelectedItem(item);
            }
            else {
                size = 0;
            }
        }
        else {
            findSizes(size);
        }

        if (cmbSquareSizes.getItemCount() == 0) {
            if (!txtBoardSize.getText().isEmpty()) {
                txtBoardSize.setText("");
                txtBoardSize.requestFocusInWindow();
                cmbSquareSizes.setEnabled(false);
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

                cmbSquareSizes.setEnabled(false);
            }
            else {
                cmbDiagonals.setEnabled(true);
                cmbSquares.setEnabled(true);

                cmbSquareSizes.setEnabled(true);
                cmbSquareSizes.requestFocusInWindow();
            }

        }

        buttonsUpdate();

        maskFigures = getMaskFigures();
        maskSquares = getMaskSquares();
    }

    private void squareChanged() {
        SquareSize item = (SquareSize) cmbSquareSizes.getSelectedItem();

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
            cmbSquareSizes.insertItemAt(item, 0);

            if (z == j)
                break;
        }

        cmbSquareSizes.setSelectedItem(item);
    }

    private boolean[] getLeftRight(int value) {
        boolean[] arr = new boolean[2];

        switch (value) {
            case 1:
                arr[0] = true;
                break;
            case 2:
                arr[1] = true;
                break;
            default:
                arr[0] = true;
                arr[1] = true;
        }

        return arr;
    }
}
