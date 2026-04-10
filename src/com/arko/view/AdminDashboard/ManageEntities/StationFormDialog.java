package com.arko.view.AdminDashboard.ManageEntities;

import com.arko.model.POJO.Station;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StationFormDialog extends JDialog {

    // Form Fields - Exposed for Controller access
    public JTextField txtName;
    public JTextField txtCode;
    public JComboBox<String> cbStatus;

    public JButton btnSave;
    public JButton btnCancel;

    private boolean isSaved = false;
    private final boolean isUpdateMode;

    public StationFormDialog(Window owner, Station existingStation) {
        super(owner, existingStation == null ? "Add New Station" : "Update Station", ModalityType.APPLICATION_MODAL);
        this.isUpdateMode = (existingStation != null);

        setSize(450, 320);
        setLocationRelativeTo(owner);
        setResizable(false);
        UIStyler.styleDialogShell(this);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        UIStyler.styleDialogContentPanel(contentPane);

        // --- FORM PANEL ---
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        UIStyler.styleDialogFormPanel(formPanel);

        txtName   = new JTextField();
        txtCode   = new JTextField();
        cbStatus  = new JComboBox<>(new String[]{"Operational", "Under Maintenance", "Closed"});

        addFormField(formPanel, "Station Name *", txtName);
        addFormField(formPanel, "Station Code *", txtCode);
        addFormField(formPanel, "Status *",       cbStatus);

        // --- BUTTON PANEL ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        UIStyler.styleDialogButtonPanel(buttonPanel);

        btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(130, 35));
        UIStyler.styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton(isUpdateMode ? "Update Station" : "Save Station");
        btnSave.setPreferredSize(new Dimension(130, 35));
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
            txtName.setText(existingStation.getStationName());
            txtCode.setText(existingStation.getStationCode());
            cbStatus.setSelectedItem(existingStation.getOperationalStatus());
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
}