package com.arko.view.OperationalDashboard;

import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class RiverMapPanel extends ModernCard {

    // --- ACCESSIBLE COMPONENTS ---
    public JTable tableRiverMap;
    public DefaultTableModel tableRiverMapModel;

    public RiverMapPanel() {
        super("River Map Tracking");

        // 1. Setup Table Model with the 7 required columns from your design
        //
        String[] columns = {"TRIP", "VESSEL", "FROM", "TO", "LOAD", "DIR", "STATUS"};
        tableRiverMapModel = new DefaultTableModel(columns, 0);
        tableRiverMap = new JTable(tableRiverMapModel);

        styleTable(tableRiverMap);
        configureColumnWidths(tableRiverMap);

        // 2. Wrap in ScrollPane
        JScrollPane scrollPane = new JScrollPane(tableRiverMap);
        UIStyler.styleTableScrollPane(scrollPane);

        // 3. Assemble in ModernCard Container
        this.container.add(scrollPane, BorderLayout.CENTER);
    }

    private void configureColumnWidths(JTable t) {
        TableColumnModel columnModel = t.getColumnModel();

        // Optimized for the high-density 7-column layout
        columnModel.getColumn(0).setPreferredWidth(50);  // TRIP
        columnModel.getColumn(1).setPreferredWidth(100); // VESSEL
        columnModel.getColumn(2).setPreferredWidth(60);  // FROM
        columnModel.getColumn(3).setPreferredWidth(60);  // TO
        columnModel.getColumn(4).setPreferredWidth(60);  // LOAD
        columnModel.getColumn(5).setPreferredWidth(40);  // DIR
        columnModel.getColumn(6).setPreferredWidth(100); // STATUS
    }

    private void styleTable(JTable t) {
        UIStyler.styleStripedDataTable(t, -1);
        t.setRowHeight(35);
        t.setSelectionBackground(new Color(235, 245, 255));

        // --- CELL RENDERER (Center Aligned) ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                // Color-code the STATUS column
                if (column == 6 && value != null) {
                    if (value.toString().equals("ARRIVED")) {
                        setForeground(new Color(136, 226, 80)); // Green for Arrived
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(new Color(0xC74340));
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                } else {
                    setForeground(Color.BLACK);
                }

                return this;
            }
        };

        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}