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
    private final MapTrackingController mapTrackingController;
    private ControlPanelController controlPanelController;

    public PassengerManifestController(ManifestPanel panel, BoardingSession session,
                                       MapTrackingController mapTrackingController) {
        this.manifestPanel = panel;
        this.session       = session;
        this.passengerDAO  = new PassengerDAO();
        this.vesselDAO     = new VesselDAO();
        this.mapTrackingController = mapTrackingController;

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

            mapTrackingController.refreshMap();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
