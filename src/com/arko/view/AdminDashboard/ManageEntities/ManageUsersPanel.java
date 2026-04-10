package com.arko.view.AdminDashboard.ManageEntities;

import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ManageUsersPanel extends JPanel {

    // Expose these so the Controller can bind data and listeners
    public JTable table;
    public DefaultTableModel tableModel;
    public JButton btnAdd;

    public ManageUsersPanel() {
        setLayout(new BorderLayout(10, 10));
        UIStyler.stylePagePanel(this);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADER & TOOLBAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Manage Users");
        UIStyler.stylePageTitle(lblTitle);

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
        UIStyler.styleTableContainer(tableContainer);

        JScrollPane scrollPane = new JScrollPane(table);
        UIStyler.styleTableScrollPane(scrollPane);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // 3. ASSEMBLE
        add(headerPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);

    }

    // Matches the styling from WaitlistPanel
    private void styleTable(JTable t) {
        UIStyler.styleStripedDataTable(t, 6);

        t.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        t.getColumnModel().getColumn(3).setPreferredWidth(100); // Role
        t.getColumnModel().getColumn(4).setPreferredWidth(200); // Email
        t.getColumnModel().getColumn(5).setPreferredWidth(120); // Actions
    }

    private void styleAddButton(JButton btn) {
        btn.setPreferredSize(new Dimension(120, 36));
        UIStyler.stylePrimaryButton(btn);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
    }
}