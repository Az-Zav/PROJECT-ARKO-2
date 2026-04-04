package com.arko.controller.OperationalDashboard;

import com.arko.model.DAO.StationDAO;
import com.arko.view.OperationalDashboard.DistributionPanel;

import javax.swing.*;
import java.util.List;

public class PassengerDistributionController {

    private final DistributionPanel panel;
    private final StationDAO        stationDAO;
    private       Timer             refreshTimer;

    public PassengerDistributionController(DistributionPanel panel) {
        this.panel      = panel;
        this.stationDAO = new StationDAO();

        updateMatrix(); // Initial load on construction
    }

    // ── Auto-refresh ──────────────────────────────────────────────────────────

    // Call this from OperationalDashboard after construction to start live updates.
    // The timer re-queries the DB every 5 seconds — no UI disruption since
    // setRowCount(0) + re-add is safe on the EDT via the Swing Timer.
    public void startAutoRefresh() {
        if (refreshTimer != null && refreshTimer.isRunning()) refreshTimer.stop();
        refreshTimer = new Timer(5000, e -> updateMatrix());
        refreshTimer.start();
    }

    public void stopAutoRefresh() {
        if (refreshTimer != null && refreshTimer.isRunning()) refreshTimer.stop();
    }

    // ── Matrix update ─────────────────────────────────────────────────────────

    private void updateMatrix() {
        List<String> codes        = stationDAO.getAllStationCodes();
        List<int[]>  stats        = stationDAO.getWaitingDistribution();
        int          stationCount = codes.size();

        if (stationCount == 0) return;

        // Re-initialise the grid with live station codes from DB.
        // This keeps the matrix correct if stations are added/removed via Admin.
        panel.initStationGrid(codes);

        // Fill in the count values row by row.
        // Row i = origin station (codes index i, StationID = i+1 assuming sequential IDs).
        // Col j+1 = destination station (offset by 1 because col 0 is the ORIGIN header).
        for (int i = 0; i < stationCount; i++) {
            int originId = i + 1;

            for (int j = 0; j < stationCount; j++) {
                int destId = j + 1;
                int count  = 0;

                for (int[] entry : stats) {
                    if (entry[0] == originId && entry[1] == destId) {
                        count = entry[2];
                        break;
                    }
                }

                // col 0 is the origin label (set by initStationGrid), data starts at col 1
                panel.tableModel.setValueAt(count == 0 ? null : count, i, j + 1);
            }
        }
    }
}