package com.arko.view.ReportsDashboard;

import com.arko.controller.ReportsDashboard.ReportsController;
import com.arko.utils.SessionManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReportsDashboardPanel extends JPanel {

    private final Color CONTENT_BG = new Color(240, 242, 245);

    public ReportsDashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(CONTENT_BG);

        RidershipTimelinePanel timelinePanel       = new RidershipTimelinePanel();
        ClassificationPanel    classificationPanel = new ClassificationPanel();

        boolean isAdmin = SessionManager.getInstance().isCurrentStaffAdmin();
        StationAnalyticsPanel  analyticsPanel      = new StationAnalyticsPanel(isAdmin);
        ReportsFilterPanel     filterPanel         = new ReportsFilterPanel();

        new ReportsController(
                filterPanel,
                timelinePanel,
                classificationPanel,
                analyticsPanel
        );

        JPanel contentArea = new JPanel(new GridBagLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 3;
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        contentArea.add(filterPanel, gbc);

        gbc.gridy = 1; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.weightx = 0.6; gbc.weighty = 0.45;
        contentArea.add(timelinePanel, gbc);

        gbc.gridx = 2; gbc.gridwidth = 1;
        gbc.weightx = 0.4;
        contentArea.add(classificationPanel, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3;
        gbc.weightx = 1.0; gbc.weighty = 0.55;
        contentArea.add(analyticsPanel, gbc);

        add(contentArea, BorderLayout.CENTER);
    }
}