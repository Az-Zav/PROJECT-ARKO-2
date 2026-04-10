package com.arko.view.OperationalDashboard;

import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class WaitlistPanel extends ModernCard {

    public JTable tableWaitlist;
    public DefaultTableModel tableWaitlistModel;
    public JButton btnDirectionToggle;
    private boolean isUpstream = true;

    public WaitlistPanel() {
        super("Passenger Waitlist");

        // 1. COMPACT TOOLBAR (Optimizing space above)
        // We use a panel with zero vertical gaps to bring the table closer to the title
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        toolbar.setOpaque(false);

        JLabel lblDirection = new JLabel("Direction:");
        UIStyler.styleFormLabel(lblDirection);
        lblDirection.setFont(new Font("Inter", Font.BOLD, 10));
        lblDirection.setForeground(UIStyler.TEXT_MUTED);

        btnDirectionToggle = new JButton("UPSTREAM");
        UIStyler.styleButton(btnDirectionToggle, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT, UIStyler.DISABLED_BG, UIStyler.DISABLED_FG);

        toolbar.add(lblDirection);
        toolbar.add(btnDirectionToggle);

        // 2. UPDATED TABLE (Adding "Action" Column)
        String[] columns = {"No.", "Name", "TO", "Action"};
        tableWaitlistModel = new DefaultTableModel(columns, 0);
        tableWaitlist = new JTable(tableWaitlistModel);
        styleTable(tableWaitlist);

        JScrollPane scrollPane = new JScrollPane(tableWaitlist);
        UIStyler.styleTableScrollPane(scrollPane);

        // 3. ASSEMBLE (Using NORTH for toolbar and CENTER for table)
        this.container.add(toolbar, BorderLayout.NORTH);
        this.container.add(scrollPane, BorderLayout.CENTER);
    }

    private void toggleDirection() {
        isUpstream = !isUpstream;
        btnDirectionToggle.setText(isUpstream ? "UPSTREAM" : "DOWNSTREAM");
    }

    private void styleTable(JTable t) {
        UIStyler.styleStripedDataTable(t, -1);
        t.setRowHeight(35);
        t.setForeground(UIStyler.PRIMARY);
    }
}