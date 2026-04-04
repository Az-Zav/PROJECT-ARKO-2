package com.arko.controller.OperationalDashboard;

import com.arko.model.DAO.PassengerDAO;
import com.arko.model.DAO.VesselDAO;
import com.arko.model.POJO.Passenger;
import com.arko.model.POJO.Vessel;
import com.arko.model.database.TransactionRunner;
import com.arko.utils.OperationalDashboard.BoardingSession;
import com.arko.view.OperationalDashboard.ManifestPanel;

import java.sql.SQLException;
import java.util.List;

public class PassengerManifestController {

    private final ManifestPanel manifestPanel;
    private final PassengerDAO  passengerDAO;
    private final VesselDAO     vesselDAO;
    private final BoardingSession session;
    private ControlPanelController controlPanelController;

    public PassengerManifestController(ManifestPanel panel, BoardingSession session) {
        this.manifestPanel = panel;
        this.session       = session;
        this.passengerDAO  = new PassengerDAO();
        this.vesselDAO     = new VesselDAO();

        // Wire the "Arrived" button inside the manifest table
        this.manifestPanel.setArrivalCallback(this::handlePassengerAlighting);
    }

    public void setControlPanelController(ControlPanelController ctrl) {
        this.controlPanelController = ctrl;
    }

    public void refreshManifest() {
        manifestPanel.manifestModel.setRowCount(0);
        manifestPanel.lblTripId.setText("Trip: —"); // for the trip label

        // CLEAN ARCHITECTURE: Get the trip ID from the shared session
        if (!session.isDocked() || session.getDockedVessel() == null) return;

        manifestPanel.lblTripId.setText("Trip #" + session.getDockedVessel().getCurrentTripID());

        int tripId = session.getDockedVessel().getCurrentTripID();
        List<Passenger> manifest = passengerDAO.getManifestForTrip(tripId);

        for (Passenger p : manifest) {
            manifestPanel.manifestModel.addRow(new Object[]{
                    p.getBoardingCode(),
                    p.getFullName(),
                    p.getOriginCode(),
                    p.getDestinationCode(),
                    p.getPassengerDirection(),
                    p // The hidden object for the button editor
            });
        }
    }

    /**
     * Called when a passenger clicks "ARRIVED" (gets off the boat).
     */
    private void handlePassengerAlighting(Passenger p) {
        Vessel vessel = session.getDockedVessel();
        if (vessel == null) return;

        try {
            // Atomic Transaction: Update passenger status AND boat load at once
            TransactionRunner.run(conn -> {
                boolean marked = passengerDAO.markAsArrived(p.getPassengerID(), conn);
                if (!marked) throw new SQLException("Failed to update passenger status.");

                vesselDAO.decrementVesselLoad(vessel.getVesselID(), conn);
            });

            // Update local memory
            vessel.setCurrentLoad(Math.max(0, vessel.getCurrentLoad() - 1));

            // UI REFRESH CYCLE
            refreshManifest();

            // IMPORTANT: Since the boat is now emptier, recalculate the boarding limit!
            if (controlPanelController != null) {
                controlPanelController.recalculateAndRefresh();
            }

            MapTrackingController.refreshMap();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


/*package com.arko.controller.OperationalDashboard;

import com.arko.main.Main;
import com.arko.model.DAOs.PassengerDAO;
import com.arko.model.DAOs.VesselDAO;
import com.arko.model.POJOs.Passenger;
import com.arko.model.POJOs.Vessel;
import com.arko.model.database.TransactionRunner;
import com.arko.view.OperationalDashboard.ManifestPanel;
import com.arko.view.OperationalDashboard.StationControlsPanel;
import java.sql.SQLException;
import java.util.List;

/**
 * Instance class accessed via getInstance().
 * Manages manifest table population and passenger arrival logic.

public class PassengerManifestController {

    private static PassengerManifestController instance;

    private final ManifestPanel        manifestPanel;
    private final StationControlsPanel stationControlsPanel;
    private final PassengerDAO         passengerDAO;
    private final VesselDAO            vesselDAO;
    private final Main                 main;

    // Injected after ControlPanelController is built
    private ControlPanelController controlPanelController;

    private PassengerManifestController(Main main) {
        this.main                 = main;
        this.manifestPanel        = main.manifestPanel;
        this.stationControlsPanel = main.stationControlsPanel;
        this.passengerDAO         = new PassengerDAO();
        this.vesselDAO            = new VesselDAO();

        manifestPanel.arriveButtonEditor.setArrivalCallback(this::handleArrival);
    }

    public static void init(Main main) {
        instance = new PassengerManifestController(main);
    }

    public static PassengerManifestController getInstance() {
        if (instance == null) throw new IllegalStateException(
                "PassengerManifestController not initialised. Call init(main) first.");
        return instance;
    }

    public void setControlPanelController(ControlPanelController ctrl) {
        this.controlPanelController = ctrl;
    }

    // ── Public methods ────────────────────────────────────────────────────────

    public void refreshManifest() {
        Object selected = stationControlsPanel.comboVessel.getSelectedItem();
        manifestPanel.manifestModel.setRowCount(0);

        if (!(selected instanceof Vessel)) return;
        Vessel vessel = (Vessel) selected;
        if (vessel.getCurrentTripID() == 0) return;

        List<Passenger> manifest = passengerDAO.getManifestForTrip(vessel.getCurrentTripID());
        for (Passenger p : manifest) {
            manifestPanel.manifestModel.addRow(new Object[]{
                    p.getBoardingCode(),
                    p.getFullName(),
                    p.getOriginCode(),
                    p.getDestinationCode(),
                    p.getPassengerDirection(),
                    p
            });
        }
    }

    public void clearManifest() {
        manifestPanel.manifestModel.setRowCount(0);
    }

    // ── Arrival callback ──────────────────────────────────────────────────────

    /**
     * Marks the passenger as ARRIVED and decrements the vessel load atomically
     * in a single transaction — both succeed or both roll back.
     *
     * After a successful arrival:
     *   - Manifest is refreshed
     *   - Control panel re-evaluates boarding preview (updates Boarding: label)
     *   - At terminus, control panel checks if COMPLETE TRIP should be enabled

    private void handleArrival(Passenger p) {
        Object selected = stationControlsPanel.comboVessel.getSelectedItem();
        if (!(selected instanceof Vessel)) return;
        Vessel vessel = (Vessel) selected;

        try {
            TransactionRunner.run(conn -> {
                // 1. Mark passenger ARRIVED — uses connection-aware overload
                boolean marked = passengerDAO.markAsArrived(p.getPassengerID(), conn);
                if (!marked) throw new SQLException("ARRIVE_REJECTED");

                // 2. Decrement vessel load atomically with the arrival
                vesselDAO.decrementVesselLoad(vessel.getVesselID(), conn);
            });
        } catch (SQLException e) {
            if (!"ARRIVE_REJECTED".equals(e.getMessage())) {
                System.err.println("Error in handleArrival: " + e.getMessage());
                e.printStackTrace();
            }
            return; // Arrival failed — don't refresh UI
        }

        // Update local vessel object to stay in sync with DB
        vessel.setCurrentLoad(Math.max(0, vessel.getCurrentLoad() - 1));

        refreshManifest();

        // Re-evaluate boarding preview via ControlPanelController —
        // this updates both Boarding: and Boarded: labels correctly
        // without needing BoardingCalculation here directly
        if (controlPanelController != null) {
            controlPanelController.refreshBoardingPreview();
            controlPanelController.checkDepartButtonState();
        }
    }
}
        */
