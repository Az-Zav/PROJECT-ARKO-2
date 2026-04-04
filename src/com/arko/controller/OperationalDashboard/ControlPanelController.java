package com.arko.controller.OperationalDashboard;

import com.arko.model.DAO.PassengerDAO;
import com.arko.model.DAO.StationDAO;
import com.arko.model.DAO.TripDAO;
import com.arko.model.DAO.VesselDAO;
import com.arko.model.POJO.Vessel;
import com.arko.utils.OperationalDashboard.BoardingCalculation;
import com.arko.utils.OperationalDashboard.BoardingSession;
import com.arko.utils.SessionManager;
import com.arko.utils.OperationalDashboard.TerminusChecker;
import com.arko.view.OperationalDashboard.OperationalDashboard;
import com.arko.view.OperationalDashboard.StationControlsPanel;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class ControlPanelController {

    private final StationControlsPanel panel;
    private final OperationalDashboard operationalDashboard;
    private final VesselDAO            vesselDAO;
    private final StationDAO           stationDAO;
    private final TripDAO              tripDAO;
    private final PassengerDAO passengerDAO;
    private final BoardingSession      session;
    private boolean atTerminus = false;

    private PassengerWaitlistController waitlistController;
    private PassengerManifestController manifestController;

    // BoardingSession is created in OperationalDashboard and shared with
    // PassengerWaitlistController so both controllers see the same docking state
    public ControlPanelController(StationControlsPanel panel,
                                  BoardingSession session,
                                  OperationalDashboard operationalDashboard) {
        this.panel                  = panel;
        this.vesselDAO              = new VesselDAO();
        this.stationDAO             = new StationDAO();
        this.tripDAO                = new TripDAO();
        this.passengerDAO           = new PassengerDAO();
        this.session                = session;
        this.operationalDashboard   = operationalDashboard;

        initActionListeners();
        populateVessels();
    }

    // Called from OperationalDashboard after the other controllers are built
    public void setWaitlistController(PassengerWaitlistController waitlistController) {
        this.waitlistController = waitlistController;
    }

    public void setManifestController(PassengerManifestController manifestController) {
        this.manifestController = manifestController;
    }

    // ── Vessel dropdown ───────────────────────────────────────────────────────

    private void populateVessels() {
        panel.cmbVessel.removeAllItems();

        Vessel placeholder = new Vessel();
        placeholder.setVesselName("--- Select Vessel ---");
        placeholder.setVesselID(-1);
        panel.cmbVessel.addItem(placeholder);

        int currentStationID = SessionManager.getInstance().getCurrentStationId();
        List<Vessel> vessels = vesselDAO.getVesselsForStation(currentStationID);
        for (Vessel v : vessels) panel.cmbVessel.addItem(v);
    }

    // ── Action listeners ──────────────────────────────────────────────────────

    private void initActionListeners() {
        panel.cmbVessel.addActionListener(e -> {
            Vessel selected = (Vessel) panel.cmbVessel.getSelectedItem();
            panel.btnArrive.setEnabled(selected != null && selected.getVesselID() != -1);
            panel.btnDepart.setEnabled(false);
        });

        panel.btnArrive.addActionListener(e -> handleArriveAction());
        panel.btnDepart.addActionListener(e -> handleDepartAction());

        panel.btnArrive.setEnabled(false);
        panel.btnDepart.setEnabled(false);
    }

    // ── ARRIVE ────────────────────────────────────────────────────────────────

    private void handleArriveAction() {
        Vessel vessel = (Vessel) panel.cmbVessel.getSelectedItem();
        if (vessel == null || vessel.getVesselID() == -1) return;

        int    currentStationId = SessionManager.getInstance().getCurrentStationId();
        int    tripId           = vessel.getCurrentTripID();
        String direction        = vessel.getTripDirection();

        // Idle vessel — create a brand-new trip and assign it a starting direction
        if (tripId <= 0) {
            direction = determineInitialDirection(currentStationId);
            tripId    = tripDAO.createTripReturnID(vessel.getVesselID(), currentStationId, direction);
            vessel.setCurrentTripID(tripId);
            vessel.setTripDirection(direction);
        }

        boolean docked = tripDAO.updateTripStatusAndLocation(tripId, "DOCKED", currentStationId);
        if (!docked) {
            JOptionPane.showMessageDialog(operationalDashboard,
                    "Database Error: Could not dock " + vessel.getVesselName(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calculate boarding limit and mark the session as active
        int calculatedLimit = calculateBoardingLimit(vessel, direction);
        session.markDocked(vessel, calculatedLimit, direction);

        // Seed the boarded counter from DB so mid-route arrivals resume accurately
        int alreadyBoarded = passengerDAO.getBoardedCountForTrip(tripId);
        session.seedBoardedCount(alreadyBoarded);

        // Terminus check — direction-aware so a DOWNSTREAM vessel at MAX triggers,
        // and an UPSTREAM vessel at MIN triggers, but not the reverse
        atTerminus = isDirectionAwareTerminus(currentStationId, direction);

        // Swap button label and state based on terminus result
        if (atTerminus) {
            panel.btnDepart.setText("COMPLETE TRIP");
            panel.btnDepart.setEnabled(false); // enabled only after all passengers ARRIVED
        } else {
            panel.btnDepart.setText("DEPART");
            panel.btnDepart.setEnabled(true);
        }

        panel.btnArrive.setEnabled(false);
        panel.cmbVessel.setEnabled(false);

        updateBoardingDisplay();

        // If at terminus, run the same allPassengersArrived check immediately —
        // covers the case where the vessel arrives with zero passengers on board,
        // in which case recalculateAndRefresh() is never called by the manifest
        // and COMPLETE TRIP would stay permanently disabled.
        if (atTerminus) {
            boolean allArrived = tripDAO.allPassengersArrived(session.getDockedVessel().getCurrentTripID());
            panel.btnDepart.setEnabled(allArrived);
        }

        if (manifestController != null) manifestController.refreshManifest();
        if (waitlistController  != null) waitlistController.refreshWaitlist();

        MapTrackingController.refreshMap();

        JOptionPane.showMessageDialog(operationalDashboard,
                vessel.getVesselName() + " has DOCKED successfully.\n" +
                        "Direction: " + direction +
                        (atTerminus ? "\n[TERMINUS] All passengers must ARRIVE before completing." : ""),
                "Arrival Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── DEPART / COMPLETE TRIP ────────────────────────────────────────────────

    private void handleDepartAction() {
        Vessel vessel = (Vessel) panel.cmbVessel.getSelectedItem();
        if (vessel == null || vessel.getVesselID() == -1) return;

        if (atTerminus) {
            handleTerminusCompletion(vessel);
        } else {
            handleRegularDepart(vessel);
        }
    }

    private void handleRegularDepart(Vessel vessel) {
        boolean departed = tripDAO.departTrip(vessel.getCurrentTripID());
        if (!departed) {
            JOptionPane.showMessageDialog(operationalDashboard,
                    "Database Error: Could not depart " + vessel.getVesselName(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        session.markDeparted();
        resetAfterDepart();

        MapTrackingController.refreshMap();

        JOptionPane.showMessageDialog(operationalDashboard,
                vessel.getVesselName() + " has DEPARTED.",
                "Departed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleTerminusCompletion(Vessel vessel) {
        int tripId = vessel.getCurrentTripID();

        // Guard rail — every passenger on the manifest must have ARRIVED
        // before the trip can be marked complete
        if (!tripDAO.allPassengersArrived(tripId)) {
            JOptionPane.showMessageDialog(operationalDashboard,
                    "Cannot complete trip — some passengers have not yet ARRIVED.\n" +
                            "Mark all passengers as ARRIVED in the manifest first.",
                    "Passengers Still On Board", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean completed = tripDAO.completeTrip(tripId);
        if (!completed) {
            JOptionPane.showMessageDialog(operationalDashboard,
                    "Database Error: Could not complete trip for " + vessel.getVesselName(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        /// Reset vessel load to 0 — all passengers have arrived, vessel is now empty
        vesselDAO.resetVesselLoad(vessel.getVesselID());

        session.markDeparted();
        atTerminus = false;
        resetAfterDepart();

        MapTrackingController.refreshMap();

        JOptionPane.showMessageDialog(operationalDashboard,
                vessel.getVesselName() + " trip COMPLETED.\n" +
                        "The vessel is now idle and available at any station.",
                "Trip Completed", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    // Called by PassengerManifestController after each passenger alights.
    // Recalculates the boarding limit (more room opened up) and re-enables
    // board buttons if space is now available. Also checks if COMPLETE TRIP
    // should be unlocked when at terminus.
    public void recalculateAndRefresh() {
        Vessel v = session.getDockedVessel();
        if (v == null) return;

        int newLimit = calculateBoardingLimit(v, v.getTripDirection());
        session.setBoardingLimit(newLimit);
        updateBoardingDisplay();

        if (waitlistController != null) waitlistController.refreshWaitlist();

        // At terminus, unlock COMPLETE TRIP the moment the last passenger arrives
        if (atTerminus) {
            boolean allArrived = tripDAO.allPassengersArrived(v.getCurrentTripID());
            panel.btnDepart.setEnabled(allArrived);
        }
    }

    // Called by PassengerWaitlistController after each successful board action
    public void updateBoardingDisplay() {
        panel.lblBoardingCount.setText(session.getBoardingProgressText());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    // Determines starting direction for an idle vessel based on its position
    // relative to the midpoint of all stations.
    // Vessels in the lower half default to DOWNSTREAM, upper half to UPSTREAM.
    private String determineInitialDirection(int currentStationId) {
        int midpoint = stationDAO.getTotalStationCount() / 2;
        return (currentStationId <= midpoint) ? "DOWNSTREAM" : "UPSTREAM";
    }

    // Returns true only when the vessel has reached the END of its current direction.
    // A DOWNSTREAM vessel at the MAX station → terminus.
    // An UPSTREAM vessel at the MIN station → terminus.
    // Cross-checks are intentionally false: DOWNSTREAM at MIN is not a terminus.
    private boolean isDirectionAwareTerminus(int stationId, String direction) {
        if (direction == null) return false;
        if ("UPSTREAM".equalsIgnoreCase(direction))   return TerminusChecker.isUpstreamTerminus(stationId);
        if ("DOWNSTREAM".equalsIgnoreCase(direction)) return TerminusChecker.isDownstreamTerminus(stationId);
        return false;
    }

    private int calculateBoardingLimit(Vessel vessel, String direction) {
        if (waitlistController == null) return 0;

        int currentStationId          = SessionManager.getInstance().getCurrentStationId();
        List<Integer> remainingIds    = stationDAO.getRemainingStationIds(currentStationId, direction);
        Map<Integer, Integer> demand  = stationDAO.getDownstreamWaitingCounts(remainingIds, direction);
        int currentDemand             = waitlistController.getWaitlistCount();

        return BoardingCalculation.calculate(vessel, remainingIds, demand, currentDemand);
    }

    // Shared reset after any departure (regular or terminus completion)
    private void resetAfterDepart() {
        panel.btnDepart.setText("DEPART");
        panel.btnDepart.setEnabled(false);
        panel.btnArrive.setEnabled(false); // disabled until vessel re-selected
        panel.cmbVessel.setEnabled(true);

        updateBoardingDisplay();

        // session.markDeparted() has already run before this method is called,
        // so isDocked() is false — refreshManifest() hits its early return and clears the table
        if (manifestController  != null) manifestController.refreshManifest();

        populateVessels(); // repopulate so returned/idle vessels appear

        if (waitlistController != null) waitlistController.refreshWaitlist();
    }
}