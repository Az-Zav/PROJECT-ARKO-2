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

    public static final javax.swing.border.Border NO_FOCUS_BORDER = BorderFactory.createEmptyBorder();

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
    public static final Color PAGE_BG = new Color(240, 242, 245);
    public static final Color TEXT_DARK = new Color(40, 40, 40);
    public static final Color TEXT_MUTED = new Color(110, 117, 125);
    public static final Color SIDEBAR_BG = new Color(76, 59, 148);
    public static final Color SIDEBAR_ACTIVE_BG = new Color(55, 42, 110);
    public static final Color SECONDARY_BTN_BG = new Color(108, 117, 125);
    public static final Color SECONDARY_BTN_HOVER = new Color(91, 99, 107);

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

    public static void stylePrimaryButton(JButton btn) {
        styleButton(btn, PRIMARY, PRIMARY_HOVER, TEXT_LIGHT, DISABLED_BG, DISABLED_FG);
    }

    public static void styleSecondaryButton(JButton btn) {
        styleButton(btn, SECONDARY_BTN_BG, SECONDARY_BTN_HOVER, TEXT_LIGHT, DISABLED_BG, DISABLED_FG);
    }

    public static void styleFormField(JComponent field) {
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        if (field instanceof JTextField tf) {
            styleUI(tf);
            tf.setFont(new Font("Inter", Font.PLAIN, 13));
        } else if (field instanceof JPasswordField pf) {
            stylePasswordField(pf);
            pf.setFont(new Font("Inter", Font.PLAIN, 13));
        } else if (field instanceof JComboBox<?> cb) {
            styleComboBox(cb, BG_GREY, PRIMARY, PRIMARY_HOVER, TEXT_LIGHT);
            cb.setFont(new Font("Inter", Font.PLAIN, 13));
        }
    }

    public static void styleFormLabel(JLabel label) {
        label.setFont(new Font("Inter", Font.BOLD, 12));
        label.setForeground(new Color(80, 80, 80));
    }

    public static void stylePagePanel(JPanel panel) {
        panel.setBackground(PAGE_BG);
    }

    public static void stylePageTitle(JLabel label) {
        label.setFont(new Font("Inter", Font.BOLD, 24));
        label.setForeground(TEXT_DARK);
    }

    public static void styleTableContainer(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(new Color(230, 232, 235), 1, true));
    }

    public static void styleTableScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    public static void styleStripedDataTable(JTable t, int actionColumnIndex) {
        t.setRowHeight(40);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(new Font("Inter", Font.PLAIN, 14));

        JTableHeader header = t.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Inter", Font.BOLD, 12));
        header.setForeground(TEXT_MUTED);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 232, 235)));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(new EmptyBorder(0, 5, 0, 5));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return this;
            }
        };

        for (int i = 0; i < t.getColumnCount(); i++) {
            if (i != actionColumnIndex) {
                t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    public static void styleDialogShell(JDialog dialog) {
        dialog.getContentPane().setBackground(Color.WHITE);
    }

    public static void styleDialogContentPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
    }

    public static void styleDialogFormPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
    }

    public static void styleDialogButtonPanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 0, 0, 0));
    }

    public static void styleSidebarPanel(JPanel panel) {
        panel.setBackground(SIDEBAR_BG);
    }

    public static void styleSidebarProfilePanel(JPanel panel) {
        panel.setBackground(SIDEBAR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 10));
    }

    public static void styleSidebarNameLabel(JLabel label) {
        label.setForeground(TEXT_LIGHT);
        label.setFont(new Font("Inter", Font.BOLD, 18));
    }

    public static void styleSidebarRoleLabel(JLabel label) {
        label.setForeground(TEXT_LIGHT);
        label.setFont(new Font("Inter", Font.PLAIN, 16));
    }

    public static void styleSidebarNavButton(JButton btn) {
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(TEXT_LIGHT);
        btn.setFont(new Font("Inter", Font.PLAIN, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);
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
                setBorder(NO_FOCUS_BORDER);
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
    public static void stylePasswordField(JPasswordField pf) {
        styleUI(pf);
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getViewport().setBackground(BG_LIGHT);
    }

    /** Default table look for admin CRUD grids (uses shared header/row colors). */
    public static void styleAdminEntityTable(JTable t) {
        baseTableStyle(t, true, HEADER_BG, PRIMARY, 32);
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