package com.arko.view.OperationalDashboard;

import com.arko.model.POJO.Staff;
import com.arko.utils.SessionManager;
import com.arko.view.MainAppShell;
import com.arko.view.UIStyler;

import javax.swing.*;
import java.awt.*;

public class StaffSidebar extends JPanel {

    private JPanel menuPanel;
    private boolean visible = false;
    public JButton btnLogout;

    public StaffSidebar(CardLayout cardLayout, JPanel cardPanel, MainAppShell appShell) {
        setLayout(new BorderLayout());

        // Sidebar container
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        UIStyler.styleSidebarPanel(menuPanel);
        menuPanel.setPreferredSize(new Dimension(250, 0));

        // Profile section
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        UIStyler.styleSidebarProfilePanel(profilePanel);

        var sm = SessionManager.getInstance();
        Staff staff = sm.getCurrentStaff();
        JLabel nameLabel = new JLabel(staff != null ? staff.getFullName() : "Staff");
        UIStyler.styleSidebarNameLabel(nameLabel);

        JLabel roleLabel = new JLabel(staff != null ? staff.getRole() : "STAFF");
        UIStyler.styleSidebarRoleLabel(roleLabel);

        profilePanel.add(nameLabel);
        profilePanel.add(roleLabel);

        menuPanel.add(profilePanel);

        // Menu buttons
        menuPanel.add(createButton("Dashboard", loadIcon("/com/resources/Icons/dashboard.png"), () -> {
            cardLayout.show(cardPanel, "DASHBOARD");
        }));

        menuPanel.add(createButton("Profile", loadIcon("/com/resources/Icons/profile.png"), () -> {
            cardLayout.show(cardPanel, "PROFILE");
        }));

        menuPanel.add(createButton("Reports", loadIcon("/com/resources/Icons/report.png"), () -> {
            cardLayout.show(cardPanel, "REPORTS");
        }));

        menuPanel.add(Box.createVerticalGlue());

        // Logout button
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

        menuPanel.add(btnLogout);

        add(menuPanel, BorderLayout.WEST);
        menuPanel.setVisible(false);
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

    private JButton createButton(String text, ImageIcon icon, Runnable action) {
        JButton btn = new JButton(text, icon);
        UIStyler.styleSidebarNavButton(btn);

        btn.addActionListener(e -> action.run());
        return btn;
    }

    // Toggle sidebar visibility
    public void toggle() {
        visible = !visible;
        menuPanel.setVisible(visible);
    }
}