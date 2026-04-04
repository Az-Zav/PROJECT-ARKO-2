package com.arko.controller.OperationalDashboard;

import com.arko.model.DAO.PassengerDAO;
import com.arko.model.POJO.Passenger;
import com.arko.model.POJO.Vessel;
import com.arko.utils.OperationalDashboard.BoardingSession;
import com.arko.utils.SessionManager;
import com.arko.view.OperationalDashboard.OperationalDashboard;
import com.arko.view.OperationalDashboard.WaitlistPanel;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class PassengerWaitlistController {

    private final WaitlistPanel   panel;
    private final PassengerDAO    passengerDAO;
    private final BoardingSession session;
    private boolean isDownstream = false;
    private final OperationalDashboard operationalDashboard;

    private ControlPanelController      controlPanelController;
    private PassengerManifestController manifestController;

    // session is the same object that ControlPanelController holds —
    // both controllers share one instance, created in OperationalDashboard
    public PassengerWaitlistController(WaitlistPanel panel, BoardingSession session, OperationalDashboard operationalDashboard) {
        this.panel        = panel;
        this.passengerDAO = new PassengerDAO();
        this.session      = session;
        this.operationalDashboard = operationalDashboard;

        panel.tableWaitlist.getColumn("Action").setCellRenderer(new BoardButtonRenderer());
        panel.tableWaitlist.getColumn("Action").setCellEditor(new BoardButtonEditor());

        panel.btnDirectionToggle.addActionListener(e -> {
            isDownstream = !isDownstream;
            panel.btnDirectionToggle.setText(isDownstream ? "DOWNSTREAM" : "UPSTREAM");
            refreshWaitlist();
        });

        refreshWaitlist();
    }

    public void setControlPanelController(ControlPanelController controlPanelController) {
        this.controlPanelController = controlPanelController;
    }

    public void setManifestController(PassengerManifestController manifestController) {
        this.manifestController = manifestController;
    }

    public void refreshWaitlist() {
        panel.tableWaitlistModel.setRowCount(0);

        int stationId = SessionManager.getInstance().getCurrentStationId();
        List<Passenger> list = passengerDAO.getWaitlistByDirection(stationId, isDownstream);

        int queueNum = 1;
        for (Passenger p : list) {
            panel.tableWaitlistModel.addRow(new Object[]{
                    queueNum++,
                    p.getFirstName() + " " + p.getLastName(),
                    p.getDestinationCode(),
                    p
            });
        }
    }

    // Returns current waitlist row count — used by ControlPanelController
    // as the currentDemand input to BoardingCalculation.calculate()
    public int getWaitlistCount() {
        return panel.tableWaitlistModel.getRowCount();
    }

    // Single source of truth for whether a board button should be enabled.
    // Two conditions must both be true:
    //   1. A vessel is docked (ARRIVE was pressed and succeeded)
    //   2. The passenger's direction matches the docked vessel's direction
    //   3. The boarding limit has not been reached
    private boolean isButtonEnabled(Passenger passenger) {
        if (!session.isDocked()) return false;
        if (passenger == null)   return false;

        String dockedDir    = session.getDockedDirection();
        String passengerDir = passenger.getPassengerDirection();

        if (dockedDir == null || passengerDir == null || session.isBoardingLimitReached()) return false;
        return dockedDir.equalsIgnoreCase(passengerDir);
    }

    // ── BOARD Button Renderer ─────────────────────────────────────────────────

    private class BoardButtonRenderer extends JButton implements TableCellRenderer {

        public BoardButtonRenderer() {
            setOpaque(true);
            setText("BOARD");
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Passenger p = (value instanceof Passenger) ? (Passenger) value : null;
            setEnabled(isButtonEnabled(p));
            return this;
        }
    }

    // ── BOARD Button Editor ───────────────────────────────────────────────────

    private class BoardButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button;
        private Passenger currentPassenger;

        public BoardButtonEditor() {
            button = new JButton("BOARD");
            button.addActionListener(e -> handleBoardClick());
        }

        private void handleBoardClick() {
            fireEditingStopped();

            if (!session.isDocked()) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "No vessel is currently docked. Mark a vessel as ARRIVED first.",
                        "No Active Trip", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (session.isBoardingLimitReached()) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Boarding limit reached. No more passengers can board this trip.",
                        "Limit Reached", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Vessel selected = session.getDockedVessel();
            if (selected == null) return;

            String vesselDirection    = selected.getTripDirection();
            String passengerDirection = currentPassenger.getPassengerDirection();
            if (vesselDirection == null || !vesselDirection.equalsIgnoreCase(passengerDirection)) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "This passenger is travelling " + passengerDirection +
                                " and cannot board a " + vesselDirection + " vessel.",
                        "Direction Mismatch", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = passengerDAO.boardSinglePassengerAtomic(
                    currentPassenger.getPassengerID(),
                    selected.getCurrentTripID(),
                    selected.getVesselID(),
                    selected.getMaxCapacity()
            );

            if (success) {
                selected.setCurrentLoad(selected.getCurrentLoad() + 1);
                session.incrementBoarded(1);

                if (controlPanelController != null) controlPanelController.updateBoardingDisplay();
                if (manifestController     != null) manifestController.refreshManifest();

                refreshWaitlist();
                MapTrackingController.refreshMap();
            } else {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Could not board passenger. Vessel may be full or passenger already boarded.",
                        "Board Failed", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value,
                boolean isSelected, int row, int col) {
            currentPassenger = (Passenger) value;
            button.setEnabled(isButtonEnabled(currentPassenger));
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return currentPassenger;
        }
    }
}