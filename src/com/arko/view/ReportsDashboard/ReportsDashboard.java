package com.arko.view.ReportsDashboard;

import com.arko.controller.ReportsDashboard.ReportsController;
import com.arko.utils.SessionManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReportsDashboard extends JFrame {

    private final Color CONTENT_BG = new Color(240, 242, 245);

    public ReportsDashboard() {
        setTitle("PROJECT ARKO | VIEW REPORTS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        getContentPane().setBackground(CONTENT_BG);
        setLayout(new BorderLayout(0, 0));

        // ── Step 2: Build all panels first ───────────────────────────────────
        // Panels are dumb display components — they don't know about each other.
        // The controller is what connects them, so we build panels first,
        // then hand them all to the filter panel which owns the controller.
        RidershipTimelinePanel timelinePanel       = new RidershipTimelinePanel();
        ClassificationPanel    classificationPanel = new ClassificationPanel();

        boolean isAdmin = SessionManager.getInstance().isCurrentStaffAdmin();
        StationAnalyticsPanel  analyticsPanel      = new StationAnalyticsPanel(isAdmin);
        ReportsFilterPanel     filterPanel         = new ReportsFilterPanel();

        // ── Step 3: Instantiate the controller ───────────────────────────────
        // ReportsController receives all four panels.
        // It wires action listeners onto filterPanel and calls updateChart()
        // on the three chart panels when filters change.
        // This mirrors how OperationalDashboard wires its controllers:
        //   new InputFormController(panel, waitlistController, dashboard)
        ReportsController controller = new ReportsController(
                filterPanel,
                timelinePanel,
                classificationPanel,
                analyticsPanel
        );

        // ── Step 4: Assemble content area layout ──────────────────────────────
        JPanel contentArea = new JPanel(new GridBagLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // ROW 0: Filter panel — full width, fixed height (no vertical stretch)
        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 3;
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        contentArea.add(filterPanel, gbc);

        // ROW 1: Timeline (left, wider) + Classification donut (right, narrower)
        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.weightx = 0.6; gbc.weighty = 0.45;
        contentArea.add(timelinePanel, gbc);

        gbc.gridx = 2; gbc.gridwidth = 1;
        gbc.weightx = 0.4;
        contentArea.add(classificationPanel, gbc);

        // ROW 2: Analytics panel — full width, takes remaining vertical space
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3;
        gbc.weightx = 1.0; gbc.weighty = 0.55;
        contentArea.add(analyticsPanel, gbc);

        add(contentArea, BorderLayout.CENTER);
    }
}