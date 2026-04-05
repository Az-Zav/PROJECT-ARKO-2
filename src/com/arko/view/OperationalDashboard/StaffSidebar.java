package com.arko.view.OperationalDashboard;

import com.arko.utils.Login.UserSession;
import com.arko.utils.SessionManager;

import javax.swing.*;
import java.awt.*;

public class StaffSidebar extends JPanel {

    private JPanel menuPanel;
    private boolean visible = false;
    public JButton btnLogout;

    public StaffSidebar(CardLayout cardLayout, JPanel cardPanel, UserSession session, OperationalDashboard operationalDashboard) {
        setLayout(new BorderLayout());

        // Sidebar container
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(76, 59, 148));
        menuPanel.setPreferredSize(new Dimension(250, 0));

        // Profile section
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(new Color(76, 59, 148));
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 10));

        // REPLACE HARDCODED STRINGS WITH SESSION DATA
        JLabel nameLabel = new JLabel(session.getFullName());
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Inter", Font.BOLD, 18));

        JLabel roleLabel = new JLabel(session.getRole());
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setFont(new Font("Inter", Font.PLAIN, 16));

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
                    operationalDashboard,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                SessionManager.getInstance().logout();
                new com.arko.view.Login.LoginPanel();
                SwingUtilities.getWindowAncestor(this).dispose();
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

        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.setBackground(new Color(76, 59, 148));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Inter", Font.PLAIN, 18));
        btn.setFont(btn.getFont().deriveFont(16f)); // keeps the font but size 16
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        // Align icon to the left
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);

        btn.addActionListener(e -> action.run());
        return btn;
    }

    // Toggle sidebar visibility
    public void toggle() {
        visible = !visible;
        menuPanel.setVisible(visible);
    }
}