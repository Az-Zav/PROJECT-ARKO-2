package com.arko.view.AdminDashboard.AccountSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AccountSettingsPanel extends JPanel {

    // --- ARKO THEME COLORS ---
    private static final Color BG_COLOR = new Color(240, 242, 245);
    private static final Color PRIMARY_PURPLE = new Color(76, 59, 148);
    private static final Color TEXT_DARK = new Color(40, 40, 40);
    private static final Color TEXT_DIM = new Color(110, 117, 125);

    // --- CLASS VARIABLES ---
    private CardLayout cardLayout;
    private JPanel cardDisplay;

    public AccountSettingsPanel() {
        // LEVEL 1: Setup the outer container
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // Build the nested components
        JPanel headerPanel = createHeaderPanel();
        JPanel bodyContainer = createBodyContainer();

        // Assemble Level 1
        add(headerPanel, BorderLayout.NORTH);
        add(bodyContainer, BorderLayout.CENTER);
    }

    // --- LEVEL 2: HEADER (GridBagLayout) ---
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false); // Let the grey background show through
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST; // Pin text to the left
        gbc.insets = new Insets(5, 0, 5, 30); // Padding between text elements
        header.setPreferredSize(new Dimension (0, 200));

        // 1. Primary Title (Name)
        JLabel lblName = new JLabel("Victor Jazz B. Condicion");
        lblName.setFont(new Font("Inter", Font.BOLD, 28));
        lblName.setForeground(TEXT_DARK);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across the tags and data below
        header.add(lblName, gbc);

        // 2. Secondary Tag (Staff ID)
        gbc.gridwidth = 1; // Reset span
        gbc.gridy = 1;

        JLabel lblStaffIdTag = new JLabel("Staff ID:");
        lblStaffIdTag.setFont(new Font("Inter", Font.PLAIN, 14));
        lblStaffIdTag.setForeground(TEXT_DIM);
        header.add(lblStaffIdTag, gbc);

        // 3. Tertiary Data (ID Number)
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel lblStaffIdData = new JLabel("A01-00-000000");
        lblStaffIdData.setFont(new Font("Inter", Font.PLAIN, 14));
        lblStaffIdData.setForeground(TEXT_DARK);
        header.add(lblStaffIdData, gbc);

        // 4. Email Tag & Data
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblEmailTag = new JLabel("Email Address:");
        lblEmailTag.setFont(new Font("Inter", Font.PLAIN, 14));
        lblEmailTag.setForeground(TEXT_DIM);
        header.add(lblEmailTag, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        JLabel lblEmailData = new JLabel("avcondicion@gmail.com");
        lblEmailData.setFont(new Font("Inter", Font.PLAIN, 14));
        lblEmailData.setForeground(TEXT_DARK);
        header.add(lblEmailData, gbc);

        return header;
    }

    // --- LEVEL 2: BODY CONTAINER (BorderLayout) ---
    private JPanel createBodyContainer() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        // Push the body down 30px away from the header
        body.setBorder(new EmptyBorder(30, 0, 0, 0));

        // Build the nested components for the body
        JPanel tabSwitcher = createTabSwitcher();
        cardDisplay = createCardDisplay(); // Assign to class variable so tabs can control it

        // Assemble Level 2
        body.add(tabSwitcher, BorderLayout.NORTH);
        body.add(cardDisplay, BorderLayout.CENTER);

        return body;
    }

    // --- LEVEL 3: NAVIGATION TABS ---
    private JPanel createTabSwitcher() {
        // FlowLayout.LEFT keeps buttons neatly aligned next to each other
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        tabs.setOpaque(false);
        tabs.setBorder(new EmptyBorder(0, 0, 15, 0)); // Space between tabs and the white card

        // Create Tab Buttons
        JButton btnDetails = createTabButton("Account Details", true);
        JButton btnSecurity = createTabButton("Security Settings", false);

        // Add Switching Logic
        btnDetails.addActionListener(e -> cardLayout.show(cardDisplay, "DETAILS"));
        btnSecurity.addActionListener(e -> cardLayout.show(cardDisplay, "SECURITY"));

        tabs.add(btnDetails);
        tabs.add(btnSecurity);

        return tabs;
    }

    // --- LEVEL 3: THE WHITE INFORMATION CARD (CardLayout) ---
    private JPanel createCardDisplay() {
        cardLayout = new CardLayout();
        JPanel cardHolder = new JPanel(cardLayout);
        cardHolder.setBackground(Color.WHITE);

        // This is the padding inside the white card so text doesn't touch edges
        cardHolder.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Add the sub-panels (You will build these separate classes next)
        // For testing, we just use blank JPanels with different background colors
        JPanel detailsSubPanel = new JPanel();
        detailsSubPanel.setBackground(Color.WHITE);
        detailsSubPanel.add(new JLabel("Account Details Form Goes Here"));

        JPanel securitySubPanel = new JPanel();
        securitySubPanel.setBackground(new Color(250, 240, 240)); // Slight red tint to prove it switched
        securitySubPanel.add(new JLabel("Password Change Form Goes Here"));

        // Register the cards
        cardHolder.add(detailsSubPanel, "DETAILS");
        cardHolder.add(securitySubPanel, "SECURITY");

        return cardHolder;
    }

    // --- HELPER METHOD: STYLING TAB BUTTONS ---
    private JButton createTabButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isActive) {
            btn.setForeground(PRIMARY_PURPLE);
            // Optional: Add an underline border to active tab
            btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_PURPLE));
            btn.setBorderPainted(true);
        } else {
            btn.setForeground(TEXT_DIM);
        }

        return btn;
    }


    // Paste the main method I gave you previously right here
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Account Settings - Level 1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.add(new AccountSettingsPanel());
            frame.setVisible(true);
        });
    }
}