package com.arko.controller.OperationalDashboard;

import com.arko.model.DAO.TripDAO;
import com.arko.model.POJO.Trip;
import com.arko.view.OperationalDashboard.RiverMapPanel;

import java.util.List;

public class MapTrackingController {

    // 1. The single, shared instance of this controller
    private static MapTrackingController instance;

    // 2. Normal, SAFE, non-static UI variables
    private final RiverMapPanel riverMapPanel;
    private final TripDAO tripDAO;

    // 3. Private constructor (forces the app to use init())
    private MapTrackingController(RiverMapPanel riverMapPanel) {
        this.riverMapPanel = riverMapPanel;
        this.tripDAO = new TripDAO();
    }

    // 4. Called EXACTLY ONCE when your OperationalDashboard starts
    public static void init(RiverMapPanel riverMapPanel) {
        if (instance == null) {
            instance = new MapTrackingController(riverMapPanel);
            refreshMap(); // Do an initial load

            // Auto-refresh every 4 seconds to reflect other stations' actions
            javax.swing.Timer autoRefresh = new javax.swing.Timer(4000, e -> refreshMap());
            autoRefresh.start();
        }
    }

    // 5. The safe, globally accessible refresh method!
    public static void refreshMap() {
        if (instance == null || instance.riverMapPanel == null) return;

        instance.riverMapPanel.tableRiverMapModel.setRowCount(0);
        List<Trip> fleet = instance.tripDAO.getFullFleetTracking();

        for (Trip t : fleet) {
            instance.riverMapPanel.tableRiverMapModel.addRow(new Object[]{
                    t.getTripID() == 0 ? "N/A" : t.getTripID(),
                    t.getVesselName(),
                    t.getFromStationCode(),
                    t.getNextStationCode(),
                    t.getCurrentLoad(),
                    t.getTripDirection() == null ? "---" : t.getTripDirection(),
                    t.getTripStatus()
            });
        }
    }
}

/*


import com.arko.main.Main;
import com.arko.model.DAOs.TripDAO;
import com.arko.model.POJOs.Trip;
import com.arko.view.OperationalDashboard.RiverMapPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.List;

/**
 * Instance class with a stoppable timer, accessed via getInstance().
 *
 * Next station computation was previously done in Java via a per-vessel
 * StationDAO.getStationCodeById() call (N+1 queries per refresh).
 * It is now computed inline in TripDAO.getFullFleetTracking() via a SQL JOIN,
 * and read directly from Trip.getNextStationCode(). No StationDAO needed here.

public class MapTrackingController {

    private static MapTrackingController instance;

    private final RiverMapPanel riverMapPanel;
    private final TripDAO       tripDAO;
    private       Timer         refreshTimer;

    private MapTrackingController(Main main) {
        this.riverMapPanel = main.riverMapPanel;
        this.tripDAO       = new TripDAO();
    }

    public static void init(Main main) {
        instance = new MapTrackingController(main);
    }

    public static MapTrackingController getInstance() {
        if (instance == null) throw new IllegalStateException(
                "MapTrackingController not initialised. Call init(main) first.");
        return instance;
    }

    // ── Static convenience shims ──────────────────────────────────────────────

    public static void refreshMap(Main main) {
        getInstance().refresh();
    }

    public static void startAutoRefresh(Main main) {
        init(main);
        getInstance().start();
    }

    public static void stopAutoRefresh() {
        if (instance != null) instance.stop();
    }

    // ── Instance methods ──────────────────────────────────────────────────────

    public void refresh() {
        List<Trip> fleet = tripDAO.getFullFleetTracking();

        SwingUtilities.invokeLater(() -> {
            riverMapPanel.riverMapModel.setRowCount(0);

            for (Trip t : fleet) {
                riverMapPanel.riverMapModel.addRow(new Object[]{
                        t.getTripID() == 0 ? "N/A" : t.getTripID(),
                        t.getVesselName(),
                        t.getFromStationCode(),
                        t.getNextStationCode(),     // From SQL JOIN — no extra query
                        t.getCurrentLoad(),
                        t.getTripDirection() == null ? "---" : t.getTripDirection(),
                        t.getTripStatus()
                });
            }
        });
    }

    public void start() {
        if (refreshTimer != null && refreshTimer.isRunning()) refreshTimer.stop();
        refreshTimer = new Timer(3000, e -> refresh());
        refreshTimer.start();
        refresh();
    }

    public void stop() {
        if (refreshTimer != null && refreshTimer.isRunning()) refreshTimer.stop();
    }
}

*/