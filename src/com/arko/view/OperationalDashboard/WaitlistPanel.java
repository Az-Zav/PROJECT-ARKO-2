package com.arko.view.OperationalDashboard;

import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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
        lblDirection.setFont(new Font("Inter", Font.BOLD, 10));
        lblDirection.setForeground(new Color(120, 120, 120));

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
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UIStyler.BG_LIGHT);

        // 3. ASSEMBLE (Using NORTH for toolbar and CENTER for table)
        this.container.add(toolbar, BorderLayout.NORTH);
        this.container.add(scrollPane, BorderLayout.CENTER);
    }

    private void toggleDirection() {
        isUpstream = !isUpstream;
        btnDirectionToggle.setText(isUpstream ? "UPSTREAM" : "DOWNSTREAM");
    }

    private void styleTable(JTable t) {
        t.setRowHeight(35);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(new Font("Inter", Font.PLAIN, 12));
        t.setBackground(UIStyler.BG_LIGHT);
        t.setForeground(UIStyler.PRIMARY);

        // --- CENTER ALIGNED HEADERS ---
        JTableHeader header = t.getTableHeader();
        header.setBackground(UIStyler.HEADER_BG);
        header.setFont(new Font("Inter", Font.BOLD, 11));
        header.setForeground(UIStyler.PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyler.BG_GREY));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // --- CELL RENDERER (Padding & Alignment) ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(new EmptyBorder(0, 5, 0, 5));
                setBackground(UIStyler.BG_LIGHT);
                setForeground(UIStyler.PRIMARY);
                return this;
            }
        };

        // Apply to all columns
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}