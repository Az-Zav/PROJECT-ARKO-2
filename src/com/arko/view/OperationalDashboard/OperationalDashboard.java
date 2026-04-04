package com.arko.view.OperationalDashboard;

import com.arko.controller.OperationalDashboard.*;
import com.arko.model.POJO.Staff;
import com.arko.utils.Login.UserSession;
import com.arko.utils.OperationalDashboard.BoardingSession;
import com.arko.utils.SessionManager;
import com.arko.view.ReportsDashboard.ReportsDashboardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class OperationalDashboard extends JFrame {

    private final Color BG_COLOR    = new Color(240, 242, 245);
    private final Color HEADER_DARK = new Color(18, 24, 38);

    private HamburgerSidebar sidebar;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private UserSession session;
    private JLabel lblClock; // Field for the digital clock


    public OperationalDashboard(UserSession session) {
        this.session = session;

        setTitle("PROJECT ARKO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0));

        // --- SIDEBAR SETUP ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        sidebar = new HamburgerSidebar(cardLayout, cardPanel, session, this);

        // --- HEADER ---
        add(createHeader(), BorderLayout.NORTH);

        // Start the clock timer
        startClock();

        // --- CONTENT AREA ---
        JPanel contentArea = new JPanel(new BorderLayout(20, 20));
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP ROW ---
        JPanel topRow = new JPanel(new GridLayout(1, 3, 20, 0));
        topRow.setOpaque(false);

        WaitlistPanel waitlistPanel = new WaitlistPanel();
        topRow.add(waitlistPanel);

        ManifestPanel manifestPanel = new ManifestPanel();
        topRow.add(manifestPanel);

        RiverMapPanel riverMapPanel = new RiverMapPanel();
        MapTrackingController.init(riverMapPanel);
        topRow.add(riverMapPanel);

        // --- BOTTOM ROW ---
        JPanel bottomRow = new JPanel(new GridBagLayout());
        bottomRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 20);

        JPanel leftStack = new JPanel(new BorderLayout(0, 20));
        leftStack.setOpaque(false);

        InputFormPanel inputFormPanel = new InputFormPanel();
        leftStack.add(inputFormPanel, BorderLayout.CENTER);

        StationControlsPanel stationControlsPanel = new StationControlsPanel();
        leftStack.add(stationControlsPanel, BorderLayout.SOUTH);

        gbc.weightx = 0.33;
        gbc.weighty = 1.0;
        bottomRow.add(leftStack, gbc);

        DistributionPanel distributionPanel = new DistributionPanel();
        gbc.weightx = 0.67;
        gbc.insets = new Insets(0, 0, 0, 0);
        bottomRow.add(distributionPanel, gbc);

        // --- CONTROLLER WIRING ---
        BoardingSession boardingSession = new BoardingSession();

        // 1. Core controllers
        ControlPanelController controlPanelController =
                new ControlPanelController(stationControlsPanel, boardingSession, this);

        PassengerWaitlistController passengerWaitlistController =
                new PassengerWaitlistController(waitlistPanel, boardingSession, this);

        PassengerManifestController passengerManifestController =
                new PassengerManifestController(manifestPanel, boardingSession);

        // 2. Link ControlPanel to sub-controllers
        controlPanelController.setWaitlistController(passengerWaitlistController);
        controlPanelController.setManifestController(passengerManifestController);

        // 3. Link Waitlist to others
        passengerWaitlistController.setControlPanelController(controlPanelController);
        passengerWaitlistController.setManifestController(passengerManifestController);

        // 4. Link Manifest back to ControlPanel
        passengerManifestController.setControlPanelController(controlPanelController);

        // 5. Input Form
        new InputFormController(inputFormPanel, passengerWaitlistController, this);

        // 6. Distribution — owns its own refresh cycle, no cross-controller dependency
        PassengerDistributionController distributionController =
                new PassengerDistributionController(distributionPanel);
        distributionController.startAutoRefresh();

        // --- ASSEMBLE ---
        JPanel centerContent = new JPanel(new GridLayout(2, 1, 0, 20));
        centerContent.setOpaque(false);
        centerContent.add(topRow);
        centerContent.add(bottomRow);

        contentArea.add(centerContent, BorderLayout.CENTER);

        // Wrap dashboard into card
        JPanel dashboardWrapper = new JPanel(new BorderLayout());
        dashboardWrapper.setOpaque(false);
        dashboardWrapper.add(contentArea, BorderLayout.CENTER);

        cardPanel.add(dashboardWrapper, "DASHBOARD");
        cardPanel.add(new JPanel(), "PROFILE");
        cardPanel.add(new ReportsDashboardPanel(), "REPORTS");

        // Main container (sidebar + content)
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.add(sidebar, BorderLayout.WEST);
        mainContainer.add(cardPanel, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "DASHBOARD");
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_DARK);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left — branding + user info
        Staff staff = SessionManager.getInstance().getCurrentStaff();
        String userName    = (staff != null) ? staff.getFirstName() + " " + staff.getLastName() : "Unknown";
        String stationCode = SessionManager.getInstance().getCurrentStationCode();

        // Hamburger button
        JButton hamburger = new JButton("☰");
        hamburger.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        hamburger.setForeground(new Color(0xFFFFFF));
        hamburger.setContentAreaFilled(false);
        hamburger.setBorderPainted(false);
        hamburger.setFocusPainted(false);
        hamburger.setCursor(new Cursor(Cursor.HAND_CURSOR));
        hamburger.addActionListener(e -> sidebar.toggle());

        JLabel lblInfo = new JLabel("ARKO  |  STAFF — " + userName + "  |  " + stationCode);
        lblInfo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblInfo.setForeground(Color.WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(hamburger);
        leftPanel.add(lblInfo);

        // --- RIGHT PANEL (Digital Clock) ---
        lblClock = new JLabel();
        lblClock.setFont(new Font("Monospaced", Font.BOLD, 16)); // Monospaced prevents "jumping" numbers
        lblClock.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblClock);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private void startClock() {
        // Formatter for DD:MM:YYYY - HH:MM:SS (24-hour/military time)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd:MM:yyyy - HH:mm:ss");

        Timer timer = new Timer(1000, e -> {
            lblClock.setText(LocalDateTime.now().format(dtf));
        });
        timer.setInitialDelay(0);
        timer.start();
    }
}