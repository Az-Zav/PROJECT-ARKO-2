package com.arko.view.OperationalDashboard;

import com.arko.model.POJO.Station;
import com.arko.view.ModernCard;
import com.arko.view.UIStyler;

import javax.swing.*;
import java.awt.*;

public class InputFormPanel extends ModernCard {

    // --- ACCESSIBLE COMPONENTS FOR CONTROLLER ---
    public JTextField txtFirstName;
    public JTextField txtLastName;
    public JTextField txtMI;
    public JTextField txtAge;
    public JComboBox<String> cmbSex;
    public JComboBox<String> cmbClassification;
    public JTextField txtContact;
    public JComboBox<Station> cmbDestination;
    public JButton btnEnter;

    public InputFormPanel() {
        super("Passenger Input Form");

        // Initialize Components
        txtFirstName = new JTextField();
        txtLastName = new JTextField();
        txtMI = new JTextField();
        txtAge = new JTextField();
        txtContact = new JTextField();

        cmbSex = new JComboBox<>(new String[]{"M", "F"});
        cmbClassification = new JComboBox<>(new String[]{"Regular", "Student", "Senior", "PWD"});
        cmbDestination = new JComboBox<>();

        btnEnter = new JButton("ENTER");

        // Apply consistent styling
        UIStyler.styleFormField(txtFirstName);
        UIStyler.styleFormField(txtLastName);
        UIStyler.styleFormField(txtMI);
        UIStyler.styleFormField(txtAge);
        UIStyler.styleFormField(txtContact);

        UIStyler.styleComboBox(cmbSex, UIStyler.BG_GREY, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT);
        UIStyler.styleComboBox(cmbClassification, UIStyler.BG_GREY, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT);
        UIStyler.styleComboBox(cmbDestination, UIStyler.BG_GREY, UIStyler.PRIMARY, UIStyler.PRIMARY_HOVER, UIStyler.TEXT_LIGHT);

        UIStyler.stylePrimaryButton(btnEnter);

        // Layout setup
        JPanel formBody = new JPanel(new GridBagLayout());
        formBody.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // --- ROW 0: Name & Age ---
        gbc.gridy = 0;
        addLabeledField(formBody, "First Name*", txtFirstName, gbc, 0);
        addLabeledField(formBody, "Last Name*", txtLastName, gbc, 1);
        addLabeledField(formBody, "M.I.", txtMI, gbc, 2);
        addLabeledField(formBody, "Age*", txtAge, gbc, 3);

        // --- ROW 1: Classifications & Contact ---
        gbc.gridy = 1;
        addLabeledField(formBody, "Sex", cmbSex, gbc, 0);
        addLabeledField(formBody, "Classification", cmbClassification, gbc, 1);
        addLabeledField(formBody, "Contact Number*", txtContact, gbc, 2);
        addLabeledField(formBody, "Destination*", cmbDestination, gbc, 3);

        // --- ROW 2: ENTER BUTTON (Right Aligned) ---
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(15, 5, 5, 5);
        formBody.add(btnEnter, gbc);

        // Wrap formBody in a panel to center fields horizontally
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        // Add formBody to center horizontally
        wrapper.add(formBody, BorderLayout.CENTER);

        // Add the form panel to ModernCard container
        this.container.add(wrapper, BorderLayout.CENTER);
    }

    // Adds a label on top of a field and respects preferred size
    private void addLabeledField(JPanel p, String label, JComponent field, GridBagConstraints gbc, int x) {
        gbc.gridx = x;

        JPanel stack = new JPanel(new BorderLayout(1, 2)); // small gap only
        stack.setOpaque(false);

        JLabel lbl = new JLabel(label);
        UIStyler.styleFormLabel(lbl);
        lbl.setFont(new Font("Inter", Font.BOLD, 10));

        stack.add(lbl, BorderLayout.NORTH);
        stack.add(field, BorderLayout.CENTER);

        p.add(stack, gbc);
    }
}