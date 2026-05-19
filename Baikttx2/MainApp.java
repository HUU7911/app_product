import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainApp extends JFrame {

    // ─── Color Palette ───────────────────────────────────────────────────────
    private static final Color BG_MAIN       = new Color(15, 20, 35);
    private static final Color BG_PANEL      = new Color(22, 30, 50);
    private static final Color BG_CARD       = new Color(30, 40, 65);
    private static final Color BG_INPUT      = new Color(20, 28, 48);
    private static final Color ACCENT_BLUE   = new Color(64, 156, 255);
    private static final Color ACCENT_GREEN  = new Color(50, 210, 130);
    private static final Color ACCENT_ORANGE = new Color(255, 160, 50);
    private static final Color ACCENT_RED    = new Color(255, 80, 80);
    private static final Color ACCENT_PURPLE = new Color(160, 100, 255);
    private static final Color TEXT_PRIMARY  = new Color(230, 235, 245);
    private static final Color TEXT_MUTED    = new Color(130, 145, 170);
    private static final Color TABLE_HEADER  = new Color(40, 55, 90);
    private static final Color TABLE_ROW_ALT = new Color(25, 35, 58);
    private static final Color TABLE_ROW     = new Color(20, 28, 48);
    private static final Color BORDER_COLOR  = new Color(50, 65, 100);

    // ─── State ───────────────────────────────────────────────────────────────
    private final XManagerImpl manager = new XManagerImpl();
    private DefaultTableModel tableModel;
    private JTable productTable;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel countLabel;

    // ─── Form fields ─────────────────────────────────────────────────────────
    private JTextField fldName, fldCategory, fldPrice, fldQuantity, fldDescription;
    private JLabel selectedIdLabel;
    private int selectedProductId = -1;

    // ─── Sort state ──────────────────────────────────────────────────────────
    private boolean sortAsc = true;

    public MainApp() {
        setTitle("✦ XManager — Quản Lý Sản Phẩm");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1380, 820);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout(0, 0));

        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildCenter(),     BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);

        refreshTable(manager.getAllProducts());
        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        header.setPreferredSize(new Dimension(0, 68));

        // Logo / title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        left.setOpaque(false);
        JLabel logo = new JLabel("⬡ XMANAGER");
        logo.setFont(new Font("Monospaced", Font.BOLD, 22));
        logo.setForeground(ACCENT_BLUE);
        JLabel sub = new JLabel("Hệ thống quản lý kho hàng");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        left.add(logo);
        left.add(sub);

        // Search bar
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        center.setOpaque(false);
        searchField = createStyledTextField(30);
        searchField.putClientProperty("hint", "🔍  Tìm kiếm sản phẩm...");
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setPreferredSize(new Dimension(340, 36));
        searchField.setBackground(BG_INPUT);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(ACCENT_BLUE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { performSearch(); }
        });

        JButton btnClear = buildButton("✕", ACCENT_RED, BG_PANEL, 36, 36);
        btnClear.addActionListener(e -> { searchField.setText(""); performSearch(); });

        center.add(searchField);
        center.add(btnClear);

        // Right info
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 18));
        right.setOpaque(false);
        countLabel = new JLabel("100 sản phẩm");
        countLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        countLabel.setForeground(ACCENT_GREEN);
        right.add(countLabel);

        header.add(left, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CENTER (Table + Form side panel)
    // ═══════════════════════════════════════════════════════════════════════
    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTablePanel(), buildFormPanel());
        split.setDividerLocation(900);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(BG_MAIN);
        split.setOneTouchExpandable(false);
        return split;
    }

    // ── TABLE PANEL ─────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_MAIN);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 8, 7));

        // Toolbar buttons
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton btnSortAsc  = buildButton("↑ Giá tăng dần",  ACCENT_BLUE,   BG_CARD, 140, 34);
        JButton btnSortDesc = buildButton("↓ Giá giảm dần",  ACCENT_PURPLE, BG_CARD, 140, 34);
        JButton btnRefresh  = buildButton("⟳ Làm mới",       ACCENT_GREEN,  BG_CARD, 110, 34);
        JButton btnDelete   = buildButton("🗑 Xóa",           ACCENT_RED,    BG_CARD, 90,  34);

        btnSortAsc.addActionListener(e  -> refreshTable(manager.sortedX(1)));
        btnSortDesc.addActionListener(e -> refreshTable(manager.sortedX(-1)));
        btnRefresh.addActionListener(e  -> { searchField.setText(""); refreshTable(manager.getAllProducts()); });
        btnDelete.addActionListener(e   -> deleteSelected());

        toolbar.add(btnSortAsc);
        toolbar.add(btnSortDesc);
        toolbar.add(btnRefresh);
        toolbar.add(btnDelete);

        // Table
        String[] cols = {"ID", "Tên Sản Phẩm", "Danh Mục", "Giá (VNĐ)", "Số Lượng", "Mô Tả"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        productTable = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(64, 156, 255, 60));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW : TABLE_ROW_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                if (c instanceof JLabel lbl) lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };

        productTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        productTable.setRowHeight(32);
        productTable.setShowGrid(false);
        productTable.setIntercellSpacing(new Dimension(0, 2));
        productTable.setBackground(TABLE_ROW);
        productTable.setForeground(TEXT_PRIMARY);
        productTable.setSelectionBackground(new Color(64, 156, 255, 80));
        productTable.setSelectionForeground(Color.WHITE);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getTableHeader().setReorderingAllowed(false);

        // Header style
        JTableHeader header = productTable.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(ACCENT_BLUE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));

        // Column widths
        int[] widths = {50, 220, 110, 120, 80, 280};
        for (int i = 0; i < widths.length; i++)
            productTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Row click → populate form
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(TABLE_ROW);
        scroll.setBackground(BG_MAIN);
        styleScrollBar(scroll.getVerticalScrollBar());
        styleScrollBar(scroll.getHorizontalScrollBar());

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        return panel;
    }

    // ── FORM PANEL ──────────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_MAIN);
        outer.setBorder(BorderFactory.createEmptyBorder(14, 7, 8, 14));

        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        // Card title
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);
        JLabel title = new JLabel("✦  Thông Tin Sản Phẩm");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(ACCENT_BLUE);
        selectedIdLabel = new JLabel("[ Chưa chọn ]");
        selectedIdLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        selectedIdLabel.setForeground(TEXT_MUTED);
        selectedIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        cardHeader.add(title, BorderLayout.WEST);
        cardHeader.add(selectedIdLabel, BorderLayout.EAST);
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);

        JPanel sepWrapper = new JPanel(new BorderLayout());
        sepWrapper.setOpaque(false);
        sepWrapper.add(cardHeader, BorderLayout.CENTER);
        sepWrapper.add(sep, BorderLayout.SOUTH);
        sepWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        // Fields
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        fldName        = createStyledTextField(20);
        fldCategory    = createStyledTextField(20);
        fldPrice       = createStyledTextField(20);
        fldQuantity    = createStyledTextField(20);
        fldDescription = createStyledTextField(20);

        Object[][] rows = {
            {"Tên sản phẩm *", fldName},
            {"Danh mục *",     fldCategory},
            {"Giá (VNĐ) *",   fldPrice},
            {"Số lượng *",     fldQuantity},
            {"Mô tả",          fldDescription},
        };

        for (int i = 0; i < rows.length; i++) {
            gbc.gridx = 0; gbc.gridy = i * 2;
            gbc.weightx = 0; gbc.insets = new Insets(6, 0, 1, 0);
            JLabel lbl = new JLabel((String) rows[i][0]);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            lbl.setForeground(TEXT_MUTED);
            fields.add(lbl, gbc);

            gbc.gridx = 0; gbc.gridy = i * 2 + 1;
            gbc.weightx = 1; gbc.insets = new Insets(0, 0, 4, 0);
            fields.add((Component) rows[i][1], gbc);
        }

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btnAdd    = buildButton("＋  Thêm",       ACCENT_GREEN,  BG_CARD, 0, 40);
        JButton btnEdit   = buildButton("✎  Sửa",         ACCENT_ORANGE, BG_CARD, 0, 40);
        JButton btnDel    = buildButton("✕  Xóa",         ACCENT_RED,    BG_CARD, 0, 40);
        JButton btnClear  = buildButton("○  Làm trống",   ACCENT_BLUE,   BG_CARD, 0, 40);

        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnEdit.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnDel.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnClear.setFont(new Font("SansSerif", Font.BOLD, 13));

        btnAdd.addActionListener(e   -> addProduct());
        btnEdit.addActionListener(e  -> editProduct());
        btnDel.addActionListener(e   -> deleteSelected());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDel);
        btnPanel.add(btnClear);

        card.add(sepWrapper, BorderLayout.NORTH);
        card.add(fields,     BorderLayout.CENTER);
        card.add(btnPanel,   BorderLayout.SOUTH);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  STATUS BAR
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 14, 5, 14)));

        statusLabel = new JLabel("✓  Sẵn sàng — Đã tải " + manager.getAllProducts().size() + " sản phẩm từ Product.bin");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(ACCENT_GREEN);

        JLabel hint = new JLabel("Nhấn vào hàng để chọn sản phẩm");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(TEXT_MUTED);

        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(hint,        BorderLayout.EAST);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ═══════════════════════════════════════════════════════════════════════
    private void addProduct() {
        try {
            Product p = buildProductFromForm(-1);
            manager.addX(p);
            refreshTable(manager.getAllProducts());
            clearForm();
            setStatus("✓  Đã thêm sản phẩm: " + p.getName(), ACCENT_GREEN);
        } catch (Exception ex) {
            showError("Thêm thất bại", ex.getMessage());
        }
    }

    private void editProduct() {
        try {
            if (selectedProductId < 0) { showError("Chưa chọn sản phẩm", "Vui lòng chọn sản phẩm cần sửa từ bảng."); return; }
            Product p = buildProductFromForm(selectedProductId);
            manager.editX(p);
            refreshTable(manager.getAllProducts());
            clearForm();
            setStatus("✓  Đã cập nhật sản phẩm ID #" + p.getId(), ACCENT_ORANGE);
        } catch (Exception ex) {
            showError("Sửa thất bại", ex.getMessage());
        }
    }

    private void deleteSelected() {
        try {
            int row = productTable.getSelectedRow();
            if (row < 0) { showError("Chưa chọn sản phẩm", "Vui lòng chọn sản phẩm cần xóa từ bảng."); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            String name = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "<html>Bạn có chắc muốn xóa sản phẩm<br><b>" + name + "</b> (ID: " + id + ")?</html>",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            Product dummy = new Product(id, "", "", 0, 0, "");
            manager.delX(dummy);
            refreshTable(manager.getAllProducts());
            clearForm();
            setStatus("🗑  Đã xóa sản phẩm: " + name, ACCENT_RED);
        } catch (Exception ex) {
            showError("Xóa thất bại", ex.getMessage());
        }
    }

    private void performSearch() {
        try {
            String kw = searchField.getText().trim();
            List<Product> results = manager.searchX(kw);
            refreshTable(results);
            setStatus(kw.isEmpty()
                    ? "✓  Hiển thị tất cả sản phẩm"
                    : "🔍  Tìm \"" + kw + "\" → " + results.size() + " kết quả", ACCENT_BLUE);
        } catch (Exception ex) {
            showError("Tìm kiếm thất bại", ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    private Product buildProductFromForm(int id) {
        String name = fldName.getText().trim();
        String cat  = fldCategory.getText().trim();
        String priceStr = fldPrice.getText().trim().replace(",", "").replace(".", "");
        String qtyStr   = fldQuantity.getText().trim();
        String desc     = fldDescription.getText().trim();

        if (name.isEmpty()) throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        if (cat.isEmpty())  throw new IllegalArgumentException("Danh mục không được để trống!");
        if (priceStr.isEmpty()) throw new IllegalArgumentException("Giá không được để trống!");
        if (qtyStr.isEmpty())   throw new IllegalArgumentException("Số lượng không được để trống!");

        double price;
        int qty;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Giá phải là số hợp lệ!"); }
        try { qty = Integer.parseInt(qtyStr); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Số lượng phải là số nguyên!"); }

        if (price < 0) throw new IllegalArgumentException("Giá không được âm!");
        if (qty < 0)   throw new IllegalArgumentException("Số lượng không được âm!");

        return new Product(id, name, cat, price, qty, desc);
    }

    private void populateFormFromTable() {
        int row = productTable.getSelectedRow();
        if (row < 0) return;
        selectedProductId = (int) tableModel.getValueAt(row, 0);
        fldName.setText((String) tableModel.getValueAt(row, 1));
        fldCategory.setText((String) tableModel.getValueAt(row, 2));
        // Strip formatting from price
        String priceRaw = tableModel.getValueAt(row, 3).toString().replace(".", "").replace(",", "").replace(" VNĐ", "").trim();
        fldPrice.setText(priceRaw);
        fldQuantity.setText(tableModel.getValueAt(row, 4).toString());
        fldDescription.setText((String) tableModel.getValueAt(row, 5));
        selectedIdLabel.setText("ID: #" + selectedProductId);
    }

    private void clearForm() {
        fldName.setText("");
        fldCategory.setText("");
        fldPrice.setText("");
        fldQuantity.setText("");
        fldDescription.setText("");
        selectedProductId = -1;
        selectedIdLabel.setText("[ Chưa chọn ]");
        productTable.clearSelection();
    }

    private void refreshTable(List<Product> list) {
        tableModel.setRowCount(0);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategory(),
                nf.format((long) p.getPrice()) + " VNĐ",
                p.getQuantity(),
                p.getDescription()
            });
        }
        countLabel.setText(list.size() + " sản phẩm");
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void showError(String title, String msg) {
        setStatus("✗  " + msg, ACCENT_RED);
        JOptionPane.showMessageDialog(this,
                "<html><b>" + title + "</b><br><br>" + msg + "</html>",
                title, JOptionPane.ERROR_MESSAGE);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UI UTILITIES
    // ═══════════════════════════════════════════════════════════════════════
    private JTextField createStyledTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(BG_INPUT);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        tf.setPreferredSize(new Dimension(0, 36));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_BLUE, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        return tf;
    }

    private JButton buildButton(String text, Color fg, Color bg, int w, int h) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(fg.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 40));
                } else {
                    g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 20));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (w > 0) btn.setPreferredSize(new Dimension(w, h));
        else btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, h));
        return btn;
    }

    private void styleScrollBar(JScrollBar sb) {
        sb.setBackground(BG_PANEL);
        sb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            protected void configureScrollBarColors() {
                thumbColor = new Color(60, 80, 130);
                trackColor = BG_PANEL;
            }
            protected JButton createDecreaseButton(int o) { return createZeroButton(); }
            protected JButton createIncreaseButton(int o) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainApp();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Lỗi khởi động ứng dụng:\n" + e.getMessage(),
                        "Lỗi nghiêm trọng", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
