package com.arko.controller.OperationalDashboard;

import com.arko.model.DAO.PassengerDAO;
import com.arko.model.DAO.StationDAO;
import com.arko.model.POJO.Passenger;
import com.arko.model.POJO.Station;
import com.arko.utils.SessionManager;
import com.arko.view.OperationalDashboard.InputFormPanel;
import com.arko.view.OperationalDashboard.OperationalDashboard;

import javax.swing.*;
import java.util.List;

public class InputFormController {

    private final InputFormPanel                panel;
    private final PassengerDAO                  passengerDAO;
    private final StationDAO                    stationDAO;
    private final PassengerWaitlistController   waitlistController;
    private final OperationalDashboard          operationalDashboard;

    //CONSTRUCTOR CALLED IN OP DASHBOARD SETS UP FORM
    public InputFormController(InputFormPanel panel,
                               PassengerWaitlistController waitlistController,
                               OperationalDashboard operationalDashboard) { //only the created panel in main is passed
        this.panel = panel;
        this.passengerDAO = new PassengerDAO(); //controller handles creation of its own DAO
        this.stationDAO = new StationDAO();
        this.waitlistController = waitlistController;
        this.operationalDashboard = operationalDashboard;

        populateDestinations();
        panel.btnEnter.addActionListener(e -> handleRegistration());
    }

    // POPULATES DESTINATION DROPDOWN (INPUT PANEL FORM)
    public void populateDestinations() {
        panel.cmbDestination.removeAllItems(); //prevents duplicates when no. of stations update

        List<Station> stations = stationDAO.getAllStations(); //fetches all stations from db
        int currentStationID = SessionManager.getInstance().getCurrentStationId(); //fetches logged stationID from staff

        for (Station station : stations) {
            if (station.getStationID() != currentStationID)
                panel.cmbDestination.addItem(station);
        }

    }

    // ACTION CALLED BY ACTION LISTENER IN ENTER BUTTON (INPUT FORM PANEL)
    private void handleRegistration() {
        try {
            // ── 1. Data extraction ────────────────────────────────────────────
            String firstName = panel.txtFirstName.getText().trim();
            String lastName = panel.txtLastName.getText().trim();
            String mi = panel.txtMI.getText().trim();
            String ageStr = panel.txtAge.getText().trim();
            String sex = panel.cmbSex.getSelectedItem().toString();
            String classification = panel.cmbClassification.getSelectedItem().toString();
            String contact = panel.txtContact.getText().trim();
            Station destination = (Station) panel.cmbDestination.getSelectedItem();

            // ── 2. Required field check ───────────────────────────────────────
            if (firstName.isEmpty() || lastName.isEmpty()
                    || ageStr.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Please fill in all required fields (*).",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── 3. Age: must be a number greater than 5 ───────────────────────
            int age;
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Age must be a valid number.",
                        "Invalid Age", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (age <= 5) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Passenger age must be greater than 5.",
                        "Invalid Age", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── 4. Contact: must be exactly 10 digits ─────────────────────────
            if (!contact.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Contact number must be exactly 10 digits (e.g. 9957778888).\n" +
                                "No spaces, dashes, or letters allowed.",
                        "Invalid Contact Number", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── 5. Destination must differ from origin (current station) ───────
            int currentStationId = SessionManager.getInstance().getCurrentStationId();
            if (destination == null || destination.getStationID() == currentStationId) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Destination cannot be the same as the boarding station.",
                        "Invalid Destination", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── 6. POJO mapping ───────────────────────────────────────────────
            Passenger newPassenger = new Passenger(
                    firstName, lastName, mi, contact,
                    age, sex.charAt(0), classification,
                    destination.getStationID()
            );

            // ── 7. DAO execution ──────────────────────────────────────────────
            Passenger saved = passengerDAO.insertPassenger(newPassenger);

            if (saved != null) {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Registration Successful!\n" +
                                "Passenger: " + saved.getFirstName() + " " + saved.getLastName() + "\n" +
                                "Boarding Code: " + saved.getBoardingCode(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                waitlistController.refreshWaitlist();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(operationalDashboard,
                        "Database Error: Could not save passenger.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(operationalDashboard,
                    "An unexpected error occurred: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // RESETS FORM (INPUT FORM PANEL)
    private void clearForm() {
        panel.txtFirstName.setText("");
        panel.txtLastName.setText("");
        panel.txtMI.setText("");
        panel.txtAge.setText("");
        panel.txtContact.setText("");
        panel.cmbSex.setSelectedIndex(0);
        panel.cmbClassification.setSelectedIndex(0);
        panel.cmbDestination.setSelectedIndex(0);
        panel.txtFirstName.requestFocus();
    }

}