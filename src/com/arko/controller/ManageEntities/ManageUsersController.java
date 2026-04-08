package com.arko.controller.ManageEntities;

import com.arko.model.DAO.StaffDAO;
import com.arko.model.DAO.StationDAO;
import com.arko.model.POJO.Staff;
import com.arko.model.POJO.Station;
import com.arko.view.AdminDashboard.ManageEntities.ManageUsersPanel;
import com.arko.utils.SessionManager;
import com.arko.view.AdminDashboard.ManageEntities.StaffFormDialog;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class ManageUsersController {

    private final ManageUsersPanel panel;
    private final StaffDAO staffDAO;
    private final StationDAO stationDAO;

    public ManageUsersController(ManageUsersPanel panel) {
        this.panel = panel;
        this.staffDAO = new StaffDAO();
        this.stationDAO = new StationDAO();

        // 1. Setup the Actions Column (Index 4: ID, User, Name, Role, ACTIONS)
        panel.table.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        panel.table.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        // 2. Bind the "Add User" Button
        panel.btnAdd.addActionListener(e -> handleAddUser());

        // 3. Initial Data Load
        refreshTable();
    }

    /**
     * Fetches fresh data from the DAO and repopulates the JTable.
     */
    public void refreshTable() {
        panel.tableModel.setRowCount(0);
        List<Staff> staffList = staffDAO.getAllStaff();

        for (Staff s : staffList) {
            // If StationCode is null (Admin), display "GLOBAL"
            String stationDisplay = (s.getStationCode() == null) ? "GLOBAL" : s.getStationCode();

            panel.tableModel.addRow(new Object[]{
                    s.getStaffID(),
                    s.getUsername(),
                    s.getFullName(),
                    s.getRole(),
                    s.getEmail(),
                    stationDisplay, // Index 5: The new Station Column
                    s               // Index 6: Action Buttons
            });
        }
    }

    private void handleAddUser() {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        StaffFormDialog dialog = new StaffFormDialog(parentWindow, null);

        populateStationDropdown(dialog, -1);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Staff newStaff = extractStaffFromDialog(dialog, new Staff());

            // Ensure staff is not assigned global
            if (!isStaffDataValid(newStaff, -1)) {
                return; // Validation failed, error message already shown by the helper
            }


            // 1. Generate a temporary password
            String tempPassword = com.arko.utils.Login.PasswordUtil.generateTemporaryPassword();

            // 2. Hash it and set it to the staff object before saving to DB
            // Assuming your DAO/POJO handles the password field
            newStaff.setPassword(com.arko.utils.Login.PasswordUtil.hashPassword(tempPassword));

            if (staffDAO.insertStaff(newStaff)) {
                refreshTable();

                // 3. Get the current Admin's name for the email footer/context
                // Assuming SessionManager has a method to get the current staff's name
                String adminName = SessionManager.getInstance().getCurrentStaff().getFullName();

                // 4. Trigger the email (The call you asked for)
                com.arko.utils.Email.EmailService.sendCredentialsEmail(
                        newStaff.getEmail(),
                        newStaff.getFullName(),
                        newStaff.getUsername(),
                        tempPassword,
                        adminName,
                        true, // Flag: isNewAccount = true
                        null  // Callback (optional)
                );

                JOptionPane.showMessageDialog(panel,
                        "User created successfully. A temporary password has been sent to " + newStaff.getEmail() + ".",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to save user. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleUpdateUser(Staff existingStaff) {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        StaffFormDialog dialog = new StaffFormDialog(parentWindow, existingStaff);

        populateStationDropdown(dialog, existingStaff.getStationID());
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            Staff updatedStaff = extractStaffFromDialog(dialog, existingStaff);

            // Ensure staff is not assigned global
            if (!isStaffDataValid(updatedStaff, updatedStaff.getStaffID())) {
                return; // Validation failed
            }

            if (staffDAO.updateStaff(updatedStaff)) {
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to update user profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Validation Helper: Multiple Validation checks for update and add
     */
    private boolean isStaffDataValid(Staff staff, int excludeID) {
        // 1. Database Check: Username Uniqueness
        if (staffDAO.isUsernameTaken(staff.getUsername(), excludeID)) {
            showWarning("The username '" + staff.getUsername() + "' is already taken.");
            return false;
        }

        // 2. Phone Number Format (Exactly 10 digits)
        if (staff.getContactNumber() == null || !staff.getContactNumber().matches("\\d{10}")) {
            showWarning("Phone number must be exactly 10 digits (e.g., 9999999999).");
            return false;
        }

        // 3. Special Characters Check
        String nameRegex = "^[a-zA-Z0-9 ]+$";
        if (!staff.getUsername().matches(nameRegex) ||
                !staff.getFirstName().matches(nameRegex) ||
                !staff.getLastName().matches(nameRegex)) {
            showWarning("Username and names cannot contain special characters.");
            return false;
        }

        // 4. Role-based Station Logic
        if ("STAFF".equalsIgnoreCase(staff.getRole()) && staff.getStationID() == -1) {
            showWarning("Operational Staff must be assigned to a specific station.");
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(staff.getRole()) && staff.getStationID() != -1) {
            showWarning("Admin cannot be assigned to a specific station. Please set to 'Global'.");
            return false;
        }

        return true;
    }

    // Show warning Error
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(panel, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Extracts form data into a Staff POJO.
     */
    private Staff extractStaffFromDialog(StaffFormDialog dialog, Staff staff) {
        staff.setUsername(dialog.txtUsername.getText().trim());
        staff.setFirstName(dialog.txtFirstName.getText().trim());
        staff.setLastName(dialog.txtLastName.getText().trim());
        staff.setEmail(dialog.txtEmail.getText().trim());
        staff.setContactNumber(dialog.txtContactNumber.getText().trim());
        staff.setRole((String) dialog.cbRole.getSelectedItem());

        StaffFormDialog.StationComboItem selectedStation = (StaffFormDialog.StationComboItem) dialog.cbStation.getSelectedItem();
        staff.setStationID(selectedStation != null ? selectedStation.getId() : -1);

        return staff;
    }

    /**
     * Dynamically loads stations from DB and populates the Dialog's combobox.
     */
    private void populateStationDropdown(StaffFormDialog dialog, int selectedStationId) {
        // 1. Clear existing items to prevent duplicates if the dialog is reused
        dialog.cbStation.removeAllItems();

        // 2. Add the default "Global" option for Admins or unassigned staff
        StaffFormDialog.StationComboItem globalItem = new StaffFormDialog.StationComboItem(-1, "Global / Admin (No Station)");
        dialog.cbStation.addItem(globalItem);

        // Default selection to Global if no specific ID is provided
        if (selectedStationId == -1) {
            dialog.cbStation.setSelectedItem(globalItem);
        }

        // 3. Fetch stations from the database using StationDAO
        List<Station> stations = stationDAO.getAllStations();

        // 4. Map Station POJOs to ComboItem objects for the UI
        for (Station s : stations) {
            // We display "CODE - Name" (e.g., "GUA - Guadalupe") for better UX
            String displayText = s.getStationCode() + " - " + s.getStationName();
            StaffFormDialog.StationComboItem item = new StaffFormDialog.StationComboItem(s.getStationID(), displayText);

            dialog.cbStation.addItem(item);

            // 5. Handle Auto-Selection for "Update Mode"
            if (s.getStationID() == selectedStationId) {
                dialog.cbStation.setSelectedItem(item);
            }
        }
    }

    private void handleDeleteUser(Staff s) {
        // Prevent self-deletion
        int currentUserId = SessionManager.getInstance().getCurrentStaffId();
        if (s.getStaffID() == currentUserId) {
            JOptionPane.showMessageDialog(panel, "You cannot delete your own account.", "Action Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(panel,
                "Are you sure you want to delete " + s.getUsername() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (staffDAO.deleteStaff(s.getStaffID())) {
                refreshTable();
            }
        }
    }

    // ── INNER CLASSES FOR CUSTOM TABLE BUTTONS ──────────────────────

    /**
     * Renders two buttons (Edit/Delete) inside the table cell.
     */
    private class ActionCellRenderer extends JPanel implements TableCellRenderer {
        private final JButton btnEdit = new JButton("✎");
        private final JButton btnDelete = new JButton("🗑");

        public ActionCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
            setOpaque(true);
            styleButton(btnEdit, new Color(40, 167, 69)); // Green
            styleButton(btnDelete, new Color(220, 53, 69)); // Red
            add(btnEdit);
            add(btnDelete);
        }

        private void styleButton(JButton b, Color c) {
            b.setFocusPainted(false);
            b.setFont(new Font("Inter", Font.BOLD, 12));
            b.setForeground(c);
            b.setBorder(BorderFactory.createLineBorder(c, 1));
            b.setBackground(Color.WHITE);
            b.setPreferredSize(new Dimension(30, 25));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    /**
     * Handles clicks on the Edit/Delete buttons inside the table cell.
     */
    private class ActionCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        private final JButton btnEdit = new JButton("✎");
        private final JButton btnDelete = new JButton("🗑");
        private Staff currentStaff;

        public ActionCellEditor() {
            container.setOpaque(true);
            btnEdit.addActionListener(e -> {
                stopCellEditing();
                handleUpdateUser(currentStaff);
            });
            btnDelete.addActionListener(e -> {
                stopCellEditing();
                handleDeleteUser(currentStaff);
            });
            container.add(btnEdit);
            container.add(btnDelete);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentStaff = (Staff) value;
            container.setBackground(table.getSelectionBackground());
            return container;
        }

        @Override
        public Object getCellEditorValue() {
            return currentStaff;
        }
    }
}