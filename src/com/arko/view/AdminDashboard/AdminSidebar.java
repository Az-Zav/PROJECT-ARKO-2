package com.arko.view.AdminDashboard;

import com.arko.model.POJO.Staff;
import com.arko.utils.SessionManager;
import com.arko.view.MainAppShell;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminSidebar extends JPanel {

    public JButton btnVessels;
    public JButton btnStations;
    public JButton btnUsers;
    public JButton btnReports;
    public JButton btnLogout;
    public JButton btnAccount;

    private JButton activeButton = null;

    public AdminSidebar(CardLayout cardLayout, JPanel cardPanel, MainAppShell appShell) {
        UIStyler.styleSidebarPanel(this);
        setPreferredSize(new Dimension(250, 0));
        setLayout(new BorderLayout());

        // --- TOP: Profile Section ---
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        UIStyler.styleSidebarProfilePanel(profilePanel);

        Staff staff = SessionManager.getInstance().getCurrentStaff();
        String fullName = (staff != null) ? staff.getFullName() : "Admin";
        String role     = (staff != null) ? staff.getRole()     : "ADMIN";

        JLabel nameLabel = new JLabel(fullName);
        UIStyler.styleSidebarNameLabel(nameLabel);

        JLabel roleLabel = new JLabel(role);
        UIStyler.styleSidebarRoleLabel(roleLabel);

        profilePanel.add(nameLabel);
        profilePanel.add(roleLabel);

        add(profilePanel, BorderLayout.NORTH);

        // --- CENTER: Nav Buttons ---
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        // BUG FIX: was calling setActive(btnReports) — should be setActive(btnAccount)
        btnAccount = createButton("Profile", loadIcon("/com/resources/Icons/profile.png"), () -> {
            cardLayout.show(cardPanel, "ACCOUNT_SETTINGS");
            setActive(btnAccount);
        });

        btnVessels = createButton("Manage Vessels", loadIcon("/com/resources/Icons/manageVessels.png"), () -> {
            cardLayout.show(cardPanel, "VESSELS");
            setActive(btnVessels);
        });

        btnUsers = createButton("Manage Users", loadIcon("/com/resources/Icons/manageUsers.png"), () -> {
            cardLayout.show(cardPanel, "USERS");
            setActive(btnUsers);
        });

        btnStations = createButton("Manage Stations", loadIcon("/com/resources/Icons/manageStations.png"), () -> {
            cardLayout.show(cardPanel, "STATIONS");
            setActive(btnStations);
        });

        btnReports = createButton("View Reports", loadIcon("/com/resources/Icons/report.png"), () -> {
            cardLayout.show(cardPanel, "REPORTS");
            setActive(btnReports);
        });

        navPanel.add(btnAccount);
        navPanel.add(btnVessels);
        navPanel.add(btnUsers);
        navPanel.add(btnStations);
        navPanel.add(btnReports);
        navPanel.add(Box.createVerticalGlue());

        add(navPanel, BorderLayout.CENTER);

        // --- BOTTOM: Logout ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        btnLogout = createButton("Logout", loadIcon("/com/resources/Icons/logout.png"), () -> {
            int confirm = JOptionPane.showConfirmDialog(
                    appShell,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                SessionManager.getInstance().logout();
                appShell.returnToLogin();
            }
        });
        bottomPanel.add(btnLogout);

        add(bottomPanel, BorderLayout.SOUTH);

        // FIX: Mark the first visible card's button as active on startup.
        // CardLayout shows the first-added card ("ACCOUNT_SETTINGS") by default,
        // so btnAccount should be highlighted immediately.
        setActive(btnAccount);
    }

    private JButton createButton(String text, ImageIcon icon, Runnable action) {
        JButton btn = new JButton(text, icon);
        UIStyler.styleSidebarNavButton(btn);

        btn.addActionListener(e -> action.run());

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeButton) btn.setBackground(UIStyler.SIDEBAR_ACTIVE_BG);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeButton) btn.setBackground(UIStyler.SIDEBAR_BG);
            }
        });

        return btn;
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            Image img = new ImageIcon(imgURL).getImage()
                    .getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } else {
            System.err.println("Icon not found: " + path);
            return null;
        }
    }

    public void setActive(JButton selected) {
        if (activeButton != null) {
            activeButton.setBackground(UIStyler.SIDEBAR_BG);
        }
        activeButton = selected;
        activeButton.setBackground(UIStyler.SIDEBAR_ACTIVE_BG);
    }
}