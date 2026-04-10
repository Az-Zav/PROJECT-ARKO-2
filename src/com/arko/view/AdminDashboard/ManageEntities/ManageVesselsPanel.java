package com.arko.view.AdminDashboard.ManageEntities;

import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ManageVesselsPanel extends JPanel {

    public JTable table;
    public DefaultTableModel tableModel;
    public JButton btnAdd;

    public ManageVesselsPanel() {
        setLayout(new BorderLayout(10, 10));
        UIStyler.stylePagePanel(this);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADER & TOOLBAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Manage Vessels");
        UIStyler.stylePageTitle(lblTitle);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);

        btnAdd = new JButton("+ Add Vessel");
        styleAddButton(btnAdd);
        toolbar.add(btnAdd);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(toolbar, BorderLayout.EAST);

        // 2. TABLE SETUP
        // Columns: Vessel ID | Name | Max Capacity | Status | Actions
        String[] columns = {"Vessel ID", "Name", "Max Capacity", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Actions column is editable
            }
        };

        table = new JTable(tableModel);
        styleTable(table);

        JPanel tableContainer = new JPanel(new BorderLayout());
        UIStyler.styleTableContainer(tableContainer);

        JScrollPane scrollPane = new JScrollPane(table);
        UIStyler.styleTableScrollPane(scrollPane);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // 3. ASSEMBLE
        add(headerPanel, BorderLayout.NORTH);
        add(tableContainer, BorderLayout.CENTER);
    }

    private void styleTable(JTable t) {
        UIStyler.styleStripedDataTable(t, 4);

        t.getColumnModel().getColumn(0).setPreferredWidth(80);  // Vessel ID
        t.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        t.getColumnModel().getColumn(2).setPreferredWidth(120); // Max Capacity
        t.getColumnModel().getColumn(3).setPreferredWidth(120); // Status
        t.getColumnModel().getColumn(4).setPreferredWidth(120); // Actions
    }

    private void styleAddButton(JButton btn) {
        btn.setPreferredSize(new Dimension(130, 36));
        UIStyler.stylePrimaryButton(btn);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
    }
}