package com.arko.view.ReportsDashboard;

import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TripManifestPanel extends JPanel {

    // ── Filter bar controls — wired by TripManifestController ─────────────────
    public JComboBox<String> cmbStation;
    public JComboBox<String> cmbPeriod;
    public JButton           btnBack;
    public JButton           btnForward;
    public JButton           btnViewCharts;
    public JLabel            lblPeriodDisplay;

    // ── Trip summary table (top) ──────────────────────────────────────────────
    public JTable            tripTable;
    public DefaultTableModel tripModel;

    // ── Passenger detail table (bottom) ──────────────────────────────────────
    public JTable            passengerTable;
    public DefaultTableModel passengerModel;
    public JLabel            lblSelectedTrip;

    public TripManifestPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(buildFilterBar(), BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(2, 1, 0, 16));
        tables.setOpaque(false);
        tables.add(buildTripCard());
        tables.add(buildPassengerCard());
        add(tables, BorderLayout.CENTER);
    }

    // ── Filter bar ────────────────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setOpaque(false);

        cmbStation = new JComboBox<>();
        cmbPeriod  = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Yearly"});
        btnBack    = new JButton("◀");
        btnForward = new JButton("▶");
        btnViewCharts = new JButton("CHARTS");
        UIStyler.styleComboBox(cmbStation, UIStyler.BG_GREY, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT);
        UIStyler.styleComboBox(cmbPeriod, UIStyler.BG_GREY, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT);
        UIStyler.styleSecondaryButton(btnBack);
        UIStyler.styleSecondaryButton(btnForward);
        UIStyler.styleSecondaryButton(btnViewCharts);

        lblPeriodDisplay = new JLabel("—");
        lblPeriodDisplay.setFont(new Font("Inter", Font.PLAIN, 12));
        lblPeriodDisplay.setBorder(new EmptyBorder(0, 8, 0, 8));

        bar.add(new JLabel("Station:"));
        bar.add(cmbStation);
        bar.add(new JLabel("Period:"));
        bar.add(cmbPeriod);
        bar.add(btnBack);
        bar.add(lblPeriodDisplay);
        bar.add(btnForward);
        bar.add(btnViewCharts);

        return bar;
    }

    // ── Trip summary card ─────────────────────────────────────────────────────

    private JPanel buildTripCard() {
        ModernCard card = new ModernCard("Trip Summary");

        String[] cols = {"Trip ID", "Vessel", "Departure", "Direction", "Status", "Passengers"};
        tripModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tripTable = new JTable(tripModel);
        styleTable(tripTable, cols.length);

        JPanel tableContainer = new JPanel(new BorderLayout());
        UIStyler.styleTableContainer(tableContainer);

        JScrollPane scroll = new JScrollPane(tripTable);
        UIStyler.styleTableScrollPane(scroll);
        tableContainer.add(scroll, BorderLayout.CENTER);

        card.container.add(tableContainer, BorderLayout.CENTER);
        return card;
    }

    // ── Passenger detail card ─────────────────────────────────────────────────

    private JPanel buildPassengerCard() {
        ModernCard card = new ModernCard("Passenger Detail");

        lblSelectedTrip = new JLabel("Select a trip above to view its passengers.");
        lblSelectedTrip.setFont(new Font("Inter", Font.ITALIC, 12));
        lblSelectedTrip.setForeground(UIStyler.TEXT_MUTED);
        lblSelectedTrip.setBorder(new EmptyBorder(0, 0, 8, 0));

        String[] cols = {"Code", "Name", "From", "To", "Classification", "Direction", "Status"};
        passengerModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        passengerTable = new JTable(passengerModel);
        styleTable(passengerTable, cols.length);

        JPanel tableContainer = new JPanel(new BorderLayout());
        UIStyler.styleTableContainer(tableContainer);

        JScrollPane scroll = new JScrollPane(passengerTable);
        UIStyler.styleTableScrollPane(scroll);
        tableContainer.add(scroll, BorderLayout.CENTER);

        card.container.add(lblSelectedTrip, BorderLayout.NORTH);
        card.container.add(tableContainer,  BorderLayout.CENTER);
        return card;
    }

    // ── Shared table styling — mirrors ManageStationsPanel.styleTable() ───────

    private void styleTable(JTable t, int colCount) {
        UIStyler.styleStripedDataTable(t, -1);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
    }
}