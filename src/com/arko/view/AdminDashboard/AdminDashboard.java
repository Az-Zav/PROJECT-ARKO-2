package com.arko.view.AdminDashboard;

import com.arko.controller.AccountSettings.AccountSettingsController;
import com.arko.controller.ManageEntities.ManageStationsController;
import com.arko.controller.ManageEntities.ManageUsersController;
import com.arko.controller.ManageEntities.ManageVesselsController;
import com.arko.controller.ReportsDashboard.ReportsController;
import com.arko.utils.Login.UserSession;
import com.arko.view.AdminDashboard.AccountSettings.AccountSettingsPanel;
import com.arko.view.AdminDashboard.ManageEntities.ManageStationsPanel;
import com.arko.view.AdminDashboard.ManageEntities.ManageUsersPanel;
import com.arko.view.AdminDashboard.ManageEntities.ManageVesselsPanel;
import com.arko.view.ReportsDashboard.ReportsDashboard;
import com.arko.view.ReportsDashboard.ReportsDashboardPanel;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private static final Color BG_COLOR = new Color(240, 242, 245);

    public AdminDashboard(UserSession session) {
        setTitle("PROJECT ARKO — Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);

        // 1. Card panel (content area)
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_COLOR);

        // 1. Create the Panel (The View)
        AccountSettingsPanel accountPanel = new AccountSettingsPanel();
        ManageUsersPanel usersPanel = new ManageUsersPanel();
        ManageVesselsPanel vesselsPanel = new ManageVesselsPanel();
        ManageStationsPanel stationsPanel = new ManageStationsPanel();
        ReportsDashboardPanel reportsPanel = new ReportsDashboardPanel(); // ADD


        // 2. Initialize the Controller (The Wiring)
        // This automatically sets up the Table, Buttons, and DAO
        new AccountSettingsController(accountPanel);
        new ManageUsersController(usersPanel);
        new ManageVesselsController(vesselsPanel);
        new ManageStationsController(stationsPanel);

        // 3. Register the REAL panel instead of a blank one
        cardPanel.add(accountPanel, "ACCOUNT_SETTINGS");
        cardPanel.add(vesselsPanel, "VESSELS");
        cardPanel.add(usersPanel, "USERS");
        cardPanel.add(stationsPanel, "STATIONS");
        cardPanel.add(reportsPanel, "REPORTS");


        // 4. Sidebar setup
        AdminSidebar sidebar = new AdminSidebar(session, cardLayout, cardPanel, this);

        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }
}