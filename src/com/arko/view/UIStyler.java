package com.arko.view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIStyler {

    // Global Colors
    public static final Color PRIMARY = new Color(0x483696);
    public static final Color PRIMARY_HOVER = new Color(0x7554CA);
    public static final Color TEXT_LIGHT = new Color(0xFAFAFA);
    public static final Color DISABLED_BG = new Color(0xf0f0f0);
    public static final Color DISABLED_FG = Color.WHITE;
    public static final Color BG_LIGHT = new Color(255, 255, 255);
    public static final Color BG_GREY = new Color(230, 230, 230);
    public static final Color BORDER_COLOR = new Color(234, 234, 234);
    public static final Color GRID_COLOR = new Color(213, 213, 213);
    public static final Color HEADER_BG = new Color(0xF8F8F8);

    // Universal UI Styling
    public static void styleUI(JComponent field) {
        if (field instanceof JTextField tf) {
            tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(5, 8, 5, 8)));
            tf.setBackground(BG_GREY);
            tf.setForeground(PRIMARY);
            tf.setFont(new Font("Inter", Font.PLAIN, 12));
            tf.setPreferredSize(new Dimension(120, 28));
        } else if (field instanceof JLabel lbl) {
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(PRIMARY);
            lbl.setForeground(TEXT_LIGHT);
            lbl.setFont(new Font("Inter", Font.BOLD, 13));
            lbl.setBorder(new LineBorder(PRIMARY, 1));
            lbl.setPreferredSize(new Dimension(150, 28));
        }
    }

    // Reusable JComboBox styling
    public static void styleComboBox(JComboBox<?> combo, Color bg, Color fg, Color hoverBg, Color hoverFg) {
        combo.setBackground(bg);
        combo.setForeground(fg);
        combo.setFont(new Font("Inter", Font.PLAIN, 12));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        combo.setFocusable(false);
        combo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        combo.setPreferredSize(new Dimension(150, 28));

        // Arrow button & value background
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton("\u25BC");
                arrow.setFont(combo.getFont());
                arrow.setForeground(combo.getForeground());
                arrow.setBackground(combo.getBackground());
                arrow.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                arrow.setContentAreaFilled(false);
                return arrow;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(combo.getBackground());
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });

        // Renderer for items
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setOpaque(true);
                lbl.setFont(combo.getFont());
                lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                if (index == -1) { // non-dropdown display
                    lbl.setBackground(combo.getBackground());
                    lbl.setForeground(combo.getForeground());
                } else if (isSelected) { // hover/selected
                    lbl.setBackground(hoverBg);
                    lbl.setForeground(hoverFg);
                } else {
                    lbl.setBackground(combo.getBackground());
                    lbl.setForeground(lbl.getForeground());
                }

                return lbl;
            }
        });

        // Enable/disable behavior
        combo.addPropertyChangeListener("enabled", evt -> {
            boolean enabled = (boolean) evt.getNewValue();
            combo.setBackground(enabled ? bg : DISABLED_BG);
            combo.setForeground(enabled ? fg : DISABLED_FG);
            combo.repaint();
        });
    }

    // Reusable JButton styling
    public static void styleButton(JButton btn, Color activeBg, Color hoverBg, Color activeFg, Color disabledBg, Color disabledFg) {
        btn.setFocusPainted(false);
        btn.setBackground(activeBg);
        btn.setForeground(activeFg);
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 28));
        btn.setOpaque(true);

        btn.addPropertyChangeListener("enabled", evt -> {
            boolean enabled = (boolean) evt.getNewValue();
            btn.setBackground(enabled ? activeBg : disabledBg);
            btn.setForeground(enabled ? activeFg : disabledFg);
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(activeBg);
            }
        });
    }

    // --- BASE TABLE STYLE ---
    public static void baseTableStyle(JTable t, boolean showGrid, Color headerBg, Color headerFg, int rowHeight) {
        t.setRowHeight(rowHeight);
        t.setShowGrid(showGrid);
        t.setGridColor(GRID_COLOR);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        t.setRowSelectionAllowed(false);
        t.setColumnSelectionAllowed(false);
        t.setCellSelectionEnabled(false);
        t.setFont(new Font("Inter", Font.PLAIN, 12));
        t.setBackground(BG_LIGHT);
        t.setForeground(PRIMARY);

        JTableHeader header = t.getTableHeader();
        header.setBackground(headerBg);
        header.setForeground(headerFg);
        header.setFont(new Font("Inter", Font.BOLD, 10));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);

        // Remove blue hover/focus on header
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value.toString());
                lbl.setOpaque(true);
                lbl.setBackground(headerBg);
                lbl.setForeground(headerFg);
                lbl.setFont(new Font("Inter", Font.BOLD, 10));
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, GRID_COLOR),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                return lbl;
            }
        });
    }

    // --- DISTRIBUTION TABLE ---
    public static void styleDistributionTable(JTable t) {
        baseTableStyle(t, true, HEADER_BG, PRIMARY, 30);
    }

    // Custom cell renderer for DistributionPanel
    public static DefaultTableCellRenderer distributionRenderer(Color headerBg, Color primary) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(noFocusBorder);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Inter", Font.PLAIN, 11));

                if (column == 0) {
                    setBackground(headerBg);
                    setForeground(primary);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (row == (column - 1)) {
                    setBackground(primary);
                    setForeground(primary);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                    if (value == null) setText("0");
                }

                if (isSelected && column != 0) setBackground(BG_LIGHT);
                return this;
            }
        };
    }

    // --- MANIFEST TABLE ---
    public static void styleManifestTable(JTable t) {
        baseTableStyle(t, false, HEADER_BG, PRIMARY, 40);

        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(235, 245, 255));

        // center text renderer for all columns except Action (index 5)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 5) return null; // Action column handled elsewhere
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                // REMOVE dotted/focus border
                setBorder(BorderFactory.createEmptyBorder());

                return this;
            }
        };

        for (int i = 0; i < t.getColumnCount(); i++) {
            if (i != 5) t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    // --- COLUMN WIDTHS ---
    public static void configureColumnWidths(JTable t) {
        TableColumnModel columnModel = t.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120); // Code
        columnModel.getColumn(1).setPreferredWidth(140); // Name
        columnModel.getColumn(2).setPreferredWidth(50);  // From
        columnModel.getColumn(3).setPreferredWidth(50);  // To
        columnModel.getColumn(4).setPreferredWidth(80);  // Dir
        columnModel.getColumn(5).setPreferredWidth(100); // Action
    }
}