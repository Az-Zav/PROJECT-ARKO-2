package com.arko.view.ReportsDashboard;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ReportsFilterPanel extends JPanel {

    // ── Public fields — wired by ReportsController ────────────────────────────
    public JComboBox<String> cmbStation;
    public JComboBox<String> cmbPeriod;
    public JLabel            lblTotalRidership;
    public JLabel            lblPeriodDisplay;
    public JButton           btnBack;
    public JButton           btnForward;
    public JButton           btnExport;

    public ReportsFilterPanel() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        // ── ROW 1: Title and Export button ────────────────────────────────────
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblTitle = new JLabel("📊  VIEW REPORTS");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 20));
        lblTitle.setForeground(new Color(55, 30, 145));

        btnExport = new JButton("EXPORT PDF");

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(btnExport);

        topRow.add(lblTitle,      BorderLayout.WEST);
        topRow.add(actionsPanel,  BorderLayout.EAST);

        // ── ROW 2: Filters and navigation ────────────────────────────────────
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        // Total ridership badge
        lblTotalRidership = new JLabel("Total Ridership: —");
        lblTotalRidership.setOpaque(true);
        lblTotalRidership.setBackground(new Color(255, 193, 7));
        lblTotalRidership.setBorder(new EmptyBorder(6, 12, 6, 12));
        lblTotalRidership.setFont(new Font("Inter", Font.BOLD, 12));

        // Station dropdown — items populated by ReportsController
        cmbStation = new JComboBox<>();

        // Period type dropdown — fixed options
        cmbPeriod = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Yearly"});

        // Navigation: back button, period label, forward button
        btnBack    = new JButton("◀");
        btnForward = new JButton("▶");

        // Period display label — shows the current date window
        // e.g. "2026-03-29" for daily, "2026-03-23  →  2026-03-28" for weekly
        lblPeriodDisplay = new JLabel("—");
        lblPeriodDisplay.setFont(new Font("Inter", Font.PLAIN, 12));
        lblPeriodDisplay.setBorder(new EmptyBorder(0, 8, 0, 8));

        filterRow.add(lblTotalRidership);
        filterRow.add(cmbStation);
        filterRow.add(cmbPeriod);
        filterRow.add(btnBack);
        filterRow.add(lblPeriodDisplay);
        filterRow.add(btnForward);

        // ── Assemble ──────────────────────────────────────────────────────────
        add(topRow,    BorderLayout.NORTH);
        add(filterRow, BorderLayout.CENTER);
    }
}