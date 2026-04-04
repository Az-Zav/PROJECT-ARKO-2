package com.arko.view.OperationalDashboard;

import com.arko.model.POJO.Vessel;
import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import java.awt.*;

public class StationControlsPanel extends ModernCard {

    // --- ACCESSIBLE COMPONENTS FOR CONTROLLER ---
    public JComboBox<Vessel> cmbVessel;
    public JLabel lblBoardingCount; //label boarding count
    public JButton btnArrive;
    public JButton btnDepart;

    public StationControlsPanel() {
        super("Station Vessel Controls");

        // 1. Initialize Components
        cmbVessel = new JComboBox<>();
        lblBoardingCount = new JLabel("Boarding: 0 / 0");
        btnArrive = new JButton("ARRIVE");
        btnDepart = new JButton("DEPART");

        // 2. Layout Setup (Single Row)
        JPanel controlRow = new JPanel(new GridBagLayout());
        controlRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Vessel Dropdown
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        UIStyler.styleComboBox(cmbVessel, UIStyler.PRIMARY, UIStyler.TEXT_LIGHT, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT);
        controlRow.add(cmbVessel, gbc);

        // Boarding Display
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        UIStyler.styleUI(lblBoardingCount);
        controlRow.add(lblBoardingCount, gbc);

        // Arrive Button
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        UIStyler.styleButton(btnArrive, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT, UIStyler.DISABLED_BG, UIStyler.DISABLED_FG);
        controlRow.add(btnArrive, gbc);

        // Depart Button
        gbc.gridx = 3;
        gbc.weightx = 0.3;
        UIStyler.styleButton(btnDepart, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT, UIStyler.DISABLED_BG, UIStyler.DISABLED_FG);
        controlRow.add(btnDepart, gbc);

        // 3. Assemble in ModernCard Container
        this.container.add(controlRow, BorderLayout.CENTER);
    }
}