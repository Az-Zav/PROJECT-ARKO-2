package com.arko.view.AdminDashboard.AccountSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountSettingsPanel extends JPanel {

    private static final Color BG_COLOR = new Color(240, 242, 245);
    private static final Color PRIMARY_PURPLE = new Color(76, 59, 148);
    private static final Color TEXT_DIM = new Color(110, 117, 125);

    private CardLayout cardLayout;
    private JPanel cardDisplay;

    // --- ADDED TRIGGER BUTTON ---
    private JButton btnChangePassword;

    public AccountSettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createBodyContainer(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        header.setOpaque(false);

        JButton btnDetails = createTabButton("Account Details", true);
        JButton btnSecurity = createTabButton("Password & Security", false);

        btnDetails.addActionListener(e -> {
            updateTabStyle(btnDetails, btnSecurity);
            cardLayout.show(cardDisplay, "DETAILS");
        });

        btnSecurity.addActionListener(e -> {
            updateTabStyle(btnSecurity, btnDetails);
            cardLayout.show(cardDisplay, "SECURITY");
        });

        header.add(btnDetails);
        header.add(new Box.Filler(new Dimension(20, 0), new Dimension(20, 0), new Dimension(20, 0)));
        header.add(btnSecurity);

        return header;
    }

    private JPanel createBodyContainer() {
        cardLayout = new CardLayout();
        cardDisplay = new JPanel(cardLayout);
        cardDisplay.setOpaque(false);

        // DETAILS CARD
        JPanel detailsSubPanel = new JPanel();
        detailsSubPanel.setBackground(Color.WHITE);
        detailsSubPanel.add(new JLabel("Profile Information Details..."));

        // SECURITY CARD (Now with a real button)
        JPanel securitySubPanel = new JPanel(new GridBagLayout());
        securitySubPanel.setBackground(Color.WHITE);

        btnChangePassword = new JButton("Change Password");
        styleMainButton(btnChangePassword);

        securitySubPanel.add(btnChangePassword);

        cardDisplay.add(detailsSubPanel, "DETAILS");
        cardDisplay.add(securitySubPanel, "SECURITY");

        return cardDisplay;
    }

    // --- GETTER FOR CONTROLLER ---
    public JButton getBtnChangePassword() {
        return btnChangePassword;
    }

    private void styleMainButton(JButton btn) {
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setBackground(PRIMARY_PURPLE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JButton createTabButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(isActive);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isActive) {
            btn.setForeground(PRIMARY_PURPLE);
            btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_PURPLE));
        } else {
            btn.setForeground(TEXT_DIM);
            btn.setBorder(null);
        }
        return btn;
    }

    private void updateTabStyle(JButton active, JButton inactive) {
        active.setForeground(PRIMARY_PURPLE);
        active.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_PURPLE));
        active.setBorderPainted(true);

        inactive.setForeground(TEXT_DIM);
        inactive.setBorder(null);
        inactive.setBorderPainted(false);
    }

}