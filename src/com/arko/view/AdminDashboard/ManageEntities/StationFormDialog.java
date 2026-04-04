package com.arko.view.AdminDashboard.ManageEntities;

import com.arko.model.POJO.Station;

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

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- FORM PANEL ---
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        formPanel.setBackground(Color.WHITE);

        txtName   = new JTextField();
        txtCode   = new JTextField();
        cbStatus  = new JComboBox<>(new String[]{"Operational", "Under Maintenance", "Closed"});

        addFormField(formPanel, "Station Name *", txtName);
        addFormField(formPanel, "Station Code *", txtCode);
        addFormField(formPanel, "Status *",       cbStatus);

        // --- BUTTON PANEL ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        btnCancel = new JButton("Cancel");
        styleButton(btnCancel, new Color(108, 117, 125));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new JButton(isUpdateMode ? "Update Station" : "Save Station");
        styleButton(btnSave, new Color(76, 59, 148));
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
        btn.setPreferredSize(new Dimension(130, 35));
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
}