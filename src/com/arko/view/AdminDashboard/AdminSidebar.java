package com.arko.view.AdminDashboard;

import com.arko.utils.Login.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminSidebar extends JPanel {

    private static final Color SIDEBAR_BG = new Color(76, 59, 148);   // matches HamburgerSidebar exactly
    private static final Color ACTIVE_BG  = new Color(55, 42, 110);   // slightly darker for active state
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_DIM   = new Color(200, 200, 210); // matches SideNavigationPanel

    public JButton btnVessels;
    public JButton btnStations;
    public JButton btnUsers;
    public JButton btnReports;
    public JButton btnLogout;

    private JButton activeButton = null;

    public AdminSidebar(UserSession session, CardLayout cardLayout, JPanel cardPanel) {
        setBackground(SIDEBAR_BG);
        setPreferredSize(new Dimension(250, 0)); // matches HamburgerSidebar width
        setLayout(new BorderLayout());

        // --- TOP: Profile Section (matches HamburgerSidebar profilePanel) ---
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(SIDEBAR_BG);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 10));

        String fullName = (session != null) ? session.getFullName() : "Admin";
        String role     = (session != null) ? session.getRole()     : "ADMIN";

        JLabel nameLabel = new JLabel(fullName);
        nameLabel.setForeground(TEXT_WHITE);
        nameLabel.setFont(new Font("Inter", Font.BOLD, 18)); // matches HamburgerSidebar

        JLabel roleLabel = new JLabel(role);
        roleLabel.setForeground(TEXT_WHITE);
        roleLabel.setFont(new Font("Inter", Font.PLAIN, 16)); // matches HamburgerSidebar

        profilePanel.add(nameLabel);
        profilePanel.add(roleLabel);

        add(profilePanel, BorderLayout.NORTH);

        // --- CENTER: Nav Buttons ---
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        btnVessels  = createButton("Manage Vessels",  loadIcon("/com/Icons/vessel.png"),  () -> cardLayout.show(cardPanel, "VESSELS"));
        btnUsers    = createButton("Manage Users",    loadIcon("/com/Icons/profile.png"), () -> cardLayout.show(cardPanel, "USERS"));
        btnStations = createButton("Manage Stations", loadIcon("/com/Icons/station.png"), () -> cardLayout.show(cardPanel, "STATIONS"));
        btnReports  = createButton("View Reports",    loadIcon("/com/Icons/report.png"),  () -> cardLayout.show(cardPanel, "REPORTS"));

        navPanel.add(btnVessels);
        navPanel.add(btnUsers);
        navPanel.add(btnStations);
        navPanel.add(btnReports);
        navPanel.add(Box.createVerticalGlue());

        add(navPanel, BorderLayout.CENTER);

        // --- BOTTOM: Logout (matches HamburgerSidebar logout block) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        btnLogout = createButton("Logout", loadIcon("/com/Icons/logout.png"), () -> {});
        bottomPanel.add(btnLogout);

        add(bottomPanel, BorderLayout.SOUTH);

        // Auto-select first item
        setActive(btnVessels);
    }

    // Copied directly from HamburgerSidebar.createButton()
    private JButton createButton(String text, ImageIcon icon, Runnable action) {
        JButton btn = new JButton(text, icon);

        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("Inter", Font.PLAIN, 18));
        btn.setFont(btn.getFont().deriveFont(16f)); // matches HamburgerSidebar exactly
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorderPainted(false);
        btn.setOpaque(true);

        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);

        btn.addActionListener(e -> action.run());

        // Hover effect — only applied when not the active button
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(ACTIVE_BG);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) btn.setBackground(SIDEBAR_BG);
            }
        });

        return btn;
    }

    // Copied from HamburgerSidebar.loadIcon()
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

    // --- PUBLIC API FOR CONTROLLER ---
    public void setActive(JButton selected) {
        if (activeButton != null) {
            activeButton.setBackground(SIDEBAR_BG);
        }
        activeButton = selected;
        activeButton.setBackground(ACTIVE_BG);
    }
}