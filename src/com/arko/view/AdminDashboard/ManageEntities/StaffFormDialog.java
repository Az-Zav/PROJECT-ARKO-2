package com.arko.view.AdminDashboard.ManageEntities;

import com.arko.model.POJO.Staff;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StaffFormDialog extends JDialog {

    // Form Fields - Exposed for Controller access
    public JTextField txtUsername;
    public JTextField txtFirstName;
    public JTextField txtLastName;
    public JTextField txtEmail;
    public JTextField txtContactNumber;
    public JComboBox<String> cbRole;
    public JComboBox<StationComboItem> cbStation; // Uses a helper class for ID + Name mapping

    public JButton btnSave;
    public JButton btnCancel;

    private boolean isSaved = false;
    private final boolean isUpdateMode;

    public StaffFormDialog(Window owner, Staff existingStaff) {
        super(owner, existingStaff == null ? "Add New User" : "Update User", ModalityType.APPLICATION_MODAL);
        this.isUpdateMode = (existingStaff != null);

        setSize(450, 550);
        setLocationRelativeTo(owner);
        setResizable(false);
        UIStyler.styleDialogShell(this);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        UIStyler.styleDialogContentPanel(contentPane);

        // --- FORM PANEL ---
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        UIStyler.styleDialogFormPanel(formPanel);

        txtUsername = new JTextField();
        txtFirstName = new JTextField();
        txtLastName = new JTextField();
        txtEmail = new JTextField();
        txtContactNumber = new JTextField();
        cbRole = new JComboBox<>(new String[]{"STAFF", "ADMIN"});
        cbStation = new JComboBox<>();

        addFormField(formPanel, "Username *", txtUsername);

        addFormField(formPanel, "First Name *", txtFirstName);
        addFormField(formPanel, "Last Name *", txtLastName);
        addFormField(formPanel, "Email", txtEmail);
        addFormField(formPanel, "Contact Number", txtContactNumber);
        addFormField(formPanel, "Role *", cbRole);
        addFormField(formPanel, "Assigned Station", cbStation);

        // --- BUTTON PANEL ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        UIStyler.styleDialogButtonPanel(buttonPanel);

        btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120, 35));
        UIStyler.styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton(isUpdateMode ? "Update Data" : "Save User");
        btnSave.setPreferredSize(new Dimension(120, 35));
        UIStyler.stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> {
            isSaved = true;
            dispose();
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);

        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);

        // --- PRE-FILL DATA (If Update Mode) ---
        if (isUpdateMode) {
            txtUsername.setText(existingStaff.getUsername());
            txtFirstName.setText(existingStaff.getFirstName());
            txtLastName.setText(existingStaff.getLastName());
            txtEmail.setText(existingStaff.getEmail());
            txtContactNumber.setText(existingStaff.getContactNumber());
            cbRole.setSelectedItem(existingStaff.getRole());
            // Note: cbStation selection is handled by the Controller after it populates the list
        }
    }

    private void addFormField(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        UIStyler.styleFormLabel(label);
        UIStyler.styleFormField(field);

        panel.add(label);
        panel.add(field);
    }

    public boolean isSaved() {
        return isSaved;
    }

    // --- HELPER CLASS FOR STATION COMBOBOX ---
    // Stores both the UI Display Name and the Database ID
    public static class StationComboItem {
        private final int id;
        private final String display;

        public StationComboItem(int id, String display) {
            this.id = id;
            this.display = display;
        }
        public int getId() { return id; }
        @Override
        public String toString() { return display; } // What the JComboBox shows
    }
}