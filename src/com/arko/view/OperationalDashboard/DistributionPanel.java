package com.arko.view.OperationalDashboard;

import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DistributionPanel extends ModernCard {

    public JTable tableDistribution;
    public DefaultTableModel tableModel;

    public DistributionPanel() {
        super("Passenger Distribution (Waiting)");

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableDistribution = new JTable(tableModel);
        UIStyler.styleDistributionTable(tableDistribution);
        tableDistribution.setDefaultRenderer(Object.class,
                UIStyler.distributionRenderer(UIStyler.HEADER_BG, UIStyler.PRIMARY));

        JScrollPane scrollPane = new JScrollPane(tableDistribution);
        UIStyler.styleTableScrollPane(scrollPane);

        this.container.add(scrollPane, BorderLayout.CENTER);

        // Grid is populated by PassengerDistributionController on construction
    }

    public void initStationGrid(List<String> stations) {
        String[] columnLabels = new String[stations.size() + 1];
        columnLabels[0] = "ORIGIN";
        for (int i = 0; i < stations.size(); i++) {
            columnLabels[i + 1] = stations.get(i);
        }

        tableModel.setDataVector(new Object[stations.size()][columnLabels.length], columnLabels);

        for (int i = 0; i < stations.size(); i++) {
            tableModel.setValueAt(stations.get(i), i, 0);
        }

        UIStyler.distributionRenderer(UIStyler.HEADER_BG, UIStyler.PRIMARY);
    }
}