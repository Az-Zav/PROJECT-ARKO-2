package com.arko.view.AdminDashboard.ManageEntities;

import com.arko.model.POJO.Vessel;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VesselFormDialog extends JDialog {

    // Form Fields - Exposed for Controller access
    public JTextField txtName;
    public JTextField txtMaxCapacity;
    public JComboBox<String> cbStatus;

    public JButton btnSave;
    public JButton btnCancel;

    private boolean isSaved = false;
    private final boolean isUpdateMode;

    public VesselFormDialog(Window owner, Vessel existingVessel) {
        super(owner, existingVessel == null ? "Add New Vessel" : "Update Vessel", ModalityType.APPLICATION_MODAL);
        this.isUpdateMode = (existingVessel != null);

        setSize(450, 320);
        setLocationRelativeTo(owner);
        setResizable(false);
        UIStyler.styleDialogShell(this);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        UIStyler.styleDialogContentPanel(contentPane);

        // --- FORM PANEL ---
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        UIStyler.styleDialogFormPanel(formPanel);

        txtName        = new JTextField();
        txtMaxCapacity = new JTextField();
        cbStatus       = new JComboBox<>(new String[]{"Operational", "Under Maintenance", "Decommissioned"});

        addFormField(formPanel, "Vessel Name *",  txtName);
        addFormField(formPanel, "Max Capacity *", txtMaxCapacity);
        addFormField(formPanel, "Status *",       cbStatus);

        // --- BUTTON PANEL ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        UIStyler.styleDialogButtonPanel(buttonPanel);

        btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(120, 35));
        UIStyler.styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton(isUpdateMode ? "Update Vessel" : "Save Vessel");
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
            txtName.setText(existingVessel.getVesselName());
            txtMaxCapacity.setText(String.valueOf(existingVessel.getMaxCapacity()));
            cbStatus.setSelectedItem(existingVessel.getVesselStatus());
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