package com.arko.controller.OperationalDashboard;

import com.arko.model.DAO.TripDAO;
import com.arko.model.POJO.Trip;
import com.arko.view.OperationalDashboard.RiverMapPanel;

import javax.swing.*;
import java.util.List;

/**
 * Owns river map refresh for one {@link com.arko.view.OperationalDashboard.OperationalDashboard} instance.
 * Started/stopped with that dashboard's lifecycle.
 */
public class MapTrackingController {

    private final RiverMapPanel riverMapPanel;
    private final TripDAO       tripDAO;
    private       Timer         autoRefreshTimer;

    public MapTrackingController(RiverMapPanel riverMapPanel, TripDAO tripDAO) {
        this.riverMapPanel = riverMapPanel;
        this.tripDAO       = tripDAO;
    }

    public void start() {
        stop();
        refreshMap();
        autoRefreshTimer = new Timer(4000, e -> refreshMap());
        autoRefreshTimer.start();
    }

    public void stop() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
            autoRefreshTimer = null;
        }
    }

    public void refreshMap() {
        if (riverMapPanel == null) return;
        riverMapPanel.tableRiverMapModel.setRowCount(0);
        List<Trip> fleet = tripDAO.getFullFleetTracking();
        for (Trip t : fleet) {
            riverMapPanel.tableRiverMapModel.addRow(new Object[]{
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
