package com.arko.view.AdminDashboard.ManageEntities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ManageUsersPanel extends JPanel {

    // Expose these so the Controller can bind data and listeners
    public JTable table;
    public DefaultTableModel tableModel;
    public JButton btnAdd;

    public ManageUsersPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 242, 245)); // Match AdminDashboard background
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADER & TOOLBAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Manage Users");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 24));
        lblTitle.setForeground(new Color(40, 40, 40));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);

        btnAdd = new JButton("+ Add User");
        styleAddButton(btnAdd);
        toolbar.add(btnAdd);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);

        // 2. TABLE SETUP
        // The "Actions" column is reserved for your custom CellRenderer/Editor (Update/Delete)
        String[] columns = {"ID", "Username", "Full Name", "Role","Email","Station", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only make the 'Actions' column editable so buttons can be clicked
                return column == 6;
            }
        };

        table = new JTable(tableModel);
        styleTable(table);

        // Wrap table in a visually distinct container
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

    // Matches the styling from WaitlistPanel
    private void styleTable(JTable t) {
        t.setRowHeight(40);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(new Font("Inter", Font.PLAIN, 14));

        // --- HEADER STYLING ---
        JTableHeader header = t.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Inter", Font.BOLD, 12));
        header.setForeground(new Color(110, 117, 125));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 232, 235)));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // --- CELL STYLING ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(new EmptyBorder(0, 5, 0, 5));

                // Subtle alternating row colors for readability
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return this;
            }
        };

        for (int i = 0; i < t.getColumnCount(); i++) {
            // Apply the renderer to all columns EXCEPT the Actions column (index 4)
            // You will apply your custom ButtonRenderer to column 4 in the controller/view-setup.
            if (i != 6) {
                t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        t.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        t.getColumnModel().getColumn(3).setPreferredWidth(100); // Role
        t.getColumnModel().getColumn(4).setPreferredWidth(200); // Email
        t.getColumnModel().getColumn(5).setPreferredWidth(120); // Actions
    }

    private void styleAddButton(JButton btn) {
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setBackground(new Color(76, 59, 148)); // Matches AdminSidebar theme
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Simple hover effect
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