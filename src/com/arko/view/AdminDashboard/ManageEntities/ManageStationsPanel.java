package com.arko.view.AdminDashboard.ManageEntities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ManageStationsPanel extends JPanel {

    public JTable table;
    public DefaultTableModel tableModel;
    public JButton btnAdd;

    public ManageStationsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADER & TOOLBAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Manage Stations");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 24));
        lblTitle.setForeground(new Color(40, 40, 40));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);

        btnAdd = new JButton("+ Add Station");
        styleAddButton(btnAdd);
        toolbar.add(btnAdd);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);

        // 2. TABLE SETUP
        // Columns: Station ID | Name | Code | Status | Actions
        String[] columns = {"Station ID", "Name", "Code", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Actions column is editable
            }
        };

        table = new JTable(tableModel);
        styleTable(table);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(new LineBorder(new Color(230, 232, 235), 1, true));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // 3. ASSEMBLE
        add(headerPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(new Font("Inter", Font.PLAIN, 14));

        JTableHeader header = t.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Inter", Font.BOLD, 12));
        header.setForeground(new Color(110, 117, 125));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 232, 235)));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
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
            if (i != 4) { // Skip Actions column
                t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        t.getColumnModel().getColumn(0).setPreferredWidth(80);  // Station ID
        t.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        t.getColumnModel().getColumn(2).setPreferredWidth(80);  // Code
        t.getColumnModel().getColumn(3).setPreferredWidth(120); // Status
        t.getColumnModel().getColumn(4).setPreferredWidth(120); // Actions
    }

    private void styleAddButton(JButton btn) {
        btn.setPreferredSize(new Dimension(130, 36));
        btn.setBackground(new Color(76, 59, 148));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(55, 42, 110));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(76, 59, 148));
            }
        });
    }
}