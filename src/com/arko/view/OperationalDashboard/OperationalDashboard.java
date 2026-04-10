package com.arko.view.OperationalDashboard;

import com.arko.controller.AccountSettings.AccountSettingsController;
import com.arko.controller.Login.AuthController;
import com.arko.controller.OperationalDashboard.*;
import com.arko.model.DAO.StaffDAO;
import com.arko.model.DAO.TripDAO;
import com.arko.model.POJO.Staff;
import com.arko.utils.OperationalDashboard.BoardingSession;
import com.arko.utils.SessionManager;
import com.arko.view.AdminDashboard.AccountSettings.AccountSettingsPanel;
import com.arko.view.MainAppShell;
import com.arko.view.ReportsDashboard.ReportsDashboardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OperationalDashboard extends JPanel {

    private final Color BG_COLOR    = new Color(240, 242, 245);
    private final Color HEADER_DARK = new Color(18, 24, 38);

    private final MainAppShell appShell;
    private StaffSidebar sidebar;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JLabel lblClock;

    private Timer clockTimer;
    private PassengerDistributionController distributionController;
    private MapTrackingController mapTrackingController;
    private BoardingSession boardingSession;

    public OperationalDashboard(MainAppShell appShell) {
        this.appShell = appShell;

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        sidebar = new StaffSidebar(cardLayout, cardPanel, appShell);

        add(createHeader(), BorderLayout.NORTH);
        startClock();

        JPanel contentArea = new JPanel(new BorderLayout(20, 20));
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topRow = new JPanel(new GridLayout(1, 3, 20, 0));
        topRow.setOpaque(false);

        WaitlistPanel waitlistPanel = new WaitlistPanel();
        topRow.add(waitlistPanel);

        ManifestPanel manifestPanel = new ManifestPanel();
        topRow.add(manifestPanel);

        RiverMapPanel riverMapPanel = new RiverMapPanel();
        TripDAO tripDAO = new TripDAO();
        mapTrackingController = new MapTrackingController(riverMapPanel, tripDAO);
        mapTrackingController.start();
        topRow.add(riverMapPanel);

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

        boardingSession = new BoardingSession();

        ControlPanelController controlPanelController =
                new ControlPanelController(stationControlsPanel, boardingSession, this, mapTrackingController);

        PassengerWaitlistController passengerWaitlistController =
                new PassengerWaitlistController(waitlistPanel, boardingSession, this, mapTrackingController);

        PassengerManifestController passengerManifestController =
                new PassengerManifestController(manifestPanel, boardingSession, mapTrackingController);

        controlPanelController.setWaitlistController(passengerWaitlistController);
        controlPanelController.setManifestController(passengerManifestController);

        passengerWaitlistController.setControlPanelController(controlPanelController);
        passengerWaitlistController.setManifestController(passengerManifestController);

        passengerManifestController.setControlPanelController(controlPanelController);

        new InputFormController(inputFormPanel, passengerWaitlistController, this);

        distributionController = new PassengerDistributionController(distributionPanel);
        distributionController.startAutoRefresh();

        JPanel centerContent = new JPanel(new GridLayout(2, 1, 0, 20));
        centerContent.setOpaque(false);
        centerContent.add(topRow);
        centerContent.add(bottomRow);

        contentArea.add(centerContent, BorderLayout.CENTER);

        JPanel dashboardWrapper = new JPanel(new BorderLayout());
        dashboardWrapper.setOpaque(false);
        dashboardWrapper.add(contentArea, BorderLayout.CENTER);

        AccountSettingsPanel accountSettingsPanel = new AccountSettingsPanel();
        StaffDAO staffDAO = new StaffDAO();
        AuthController authController = new AuthController(staffDAO);
        new AccountSettingsController(accountSettingsPanel, staffDAO, authController);

        cardPanel.add(dashboardWrapper,      "DASHBOARD");
        cardPanel.add(accountSettingsPanel,  "PROFILE");
        cardPanel.add(new ReportsDashboardPanel(), "REPORTS");

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setOpaque(false);
        mainContainer.add(sidebar, BorderLayout.WEST);
        mainContainer.add(cardPanel, BorderLayout.CENTER);

        add(mainContainer, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "DASHBOARD");
    }

    public void shutdown() {
        if (boardingSession != null) {
            boardingSession.reset();
        }
        if (mapTrackingController != null) {
            mapTrackingController.stop();
        }
        if (clockTimer != null) {
            clockTimer.stop();
            clockTimer = null;
        }
        if (distributionController != null) {
            distributionController.stopAutoRefresh();
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_DARK);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        Staff staff = SessionManager.getInstance().getCurrentStaff();
        String userName    = (staff != null) ? staff.getFullName() : "Unknown";
        String stationCode = SessionManager.getInstance().getCurrentStationCode();

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

        lblClock = new JLabel();
        lblClock.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblClock.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblClock);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private void startClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd:MM:yyyy - HH:mm:ss");
        if (clockTimer != null) {
            clockTimer.stop();
        }
        clockTimer = new Timer(1000, e -> lblClock.setText(LocalDateTime.now().format(dtf)));
        clockTimer.setInitialDelay(0);
        clockTimer.start();
    }
}
