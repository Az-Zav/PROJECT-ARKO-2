package com.arko.view.AdminDashboard.ManageEntities;

import com.arko.model.POJO.Staff;

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

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- FORM PANEL ---
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        formPanel.setBackground(Color.WHITE);

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
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        btnCancel = new JButton("Cancel");
        styleButton(btnCancel, new Color(108, 117, 125));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton(isUpdateMode ? "Update Data" : "Save User");
        styleButton(btnSave, new Color(76, 59, 148)); // ARKO Primary Color
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
        label.setFont(new Font("Inter", Font.BOLD, 12));
        label.setForeground(new Color(80, 80, 80));

        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        if (field instanceof JTextField) {
            ((JTextField) field).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        }

        panel.add(label);
        panel.add(field);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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