package com.arko.view.AdminDashboard;

import com.arko.utils.Login.UserSession;
import com.arko.view.AdminDashboard.ManageEntities.ManageUsersPanel;

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
        com.arko.view.AdminDashboard.ManageEntities.ManageUsersPanel usersPanel = new ManageUsersPanel();

        // 2. Initialize the Controller (The Wiring)
        // This automatically sets up the Table, Buttons, and DAO
        new com.arko.controller.ManageEntities.ManageUsersController(usersPanel);

        // 3. Register the REAL panel instead of a blank one
        cardPanel.add(new JPanel(), "VESSELS");
        cardPanel.add(usersPanel, "USERS");
        cardPanel.add(new JPanel(), "STATIONS");
        cardPanel.add(new JPanel(), "REPORTS");

        // 4. Sidebar setup
        AdminSidebar sidebar = new AdminSidebar(session, cardLayout, cardPanel);

        add(sidebar, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);
    }
}