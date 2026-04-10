package com.arko.view.AdminDashboard;

import com.arko.controller.AccountSettings.AccountSettingsController;
import com.arko.controller.Login.AuthController;
import com.arko.controller.ManageEntities.ManageStationsController;
import com.arko.controller.ManageEntities.ManageUsersController;
import com.arko.controller.ManageEntities.ManageVesselsController;
import com.arko.model.DAO.StaffDAO;
import com.arko.model.DAO.StationDAO;
import com.arko.model.DAO.VesselDAO;
import com.arko.view.AdminDashboard.AccountSettings.AccountSettingsPanel;
import com.arko.view.AdminDashboard.ManageEntities.ManageStationsPanel;
import com.arko.view.AdminDashboard.ManageEntities.ManageUsersPanel;
import com.arko.view.AdminDashboard.ManageEntities.ManageVesselsPanel;
import com.arko.view.MainAppShell;
import com.arko.view.ReportsDashboard.ReportsDashboardPanel;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JPanel {

    private static final Color BG_COLOR = new Color(240, 242, 245);

    public AdminDashboard(MainAppShell appShell) {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        StaffDAO staffDAO = new StaffDAO();
        StationDAO stationDAO = new StationDAO();
        VesselDAO vesselDAO = new VesselDAO();
        AuthController authController = new AuthController(staffDAO);

        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_COLOR);

        AccountSettingsPanel accountPanel = new AccountSettingsPanel();
        ManageUsersPanel usersPanel = new ManageUsersPanel();
        ManageVesselsPanel vesselsPanel = new ManageVesselsPanel();
        ManageStationsPanel stationsPanel = new ManageStationsPanel();
        ReportsDashboardPanel reportsPanel = new ReportsDashboardPanel();

        new AccountSettingsController(accountPanel, staffDAO, authController);
        new ManageUsersController(usersPanel, staffDAO, stationDAO);
        new ManageVesselsController(vesselsPanel, vesselDAO);
        new ManageStationsController(stationsPanel, stationDAO);

        cardPanel.add(accountPanel, "ACCOUNT_SETTINGS");
        cardPanel.add(vesselsPanel, "VESSELS");
        cardPanel.add(usersPanel, "USERS");
        cardPanel.add(stationsPanel, "STATIONS");
        cardPanel.add(reportsPanel, "REPORTS");

        AdminSidebar sidebar = new AdminSidebar(cardLayout, cardPanel, appShell);

        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }
}
