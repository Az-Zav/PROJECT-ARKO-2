package com.arko.view.AdminDashboard.AccountSettings;

import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    public JPasswordField txtNewPassword;
    public JPasswordField txtConfirmPassword;
    public JButton btnSave;
    public JButton btnCancel;
    public JLabel lblError;

    // Independent states for each field
    private boolean showNewPass = false;
    private boolean showConfirmPass = false;

    public ChangePasswordDialog(Window owner) {
        super(owner, "Change Password", ModalityType.APPLICATION_MODAL);

        // Increased height slightly to accommodate the text area
        setSize(400, 580);
        setLocationRelativeTo(owner);
        setResizable(false);
        UIStyler.styleDialogShell(this);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        UIStyler.styleDialogContentPanel(contentPane);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 0. Header
        JLabel lblHeader = new JLabel("CHANGE PASSWORD");
        lblHeader.setFont(new Font("Inter", Font.BOLD, 18));
        lblHeader.setForeground(UIStyler.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(lblHeader, gbc);

        // 1. Instructions JTextArea
        JTextArea txtInstructions = new JTextArea(
                "Set a strong password to secure your account:\n" +
                        "• Minimum of 8 characters\n" +
                        "• At least one uppercase letter (A-Z)\n" +
                        "• At least one numerical digit (0-9)\n" +
                        "• At least one special character (!@#$%^&*)"
        );
        txtInstructions.setFont(new Font("Inter", Font.PLAIN, 12));
        txtInstructions.setForeground(new Color(100, 100, 100));
        txtInstructions.setEditable(false);
        txtInstructions.setFocusable(false);
        txtInstructions.setOpaque(false);
        txtInstructions.setLineWrap(true);
        txtInstructions.setWrapStyleWord(true);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(txtInstructions, gbc);

        // 2. New Password Field
        txtNewPassword = new JPasswordField();
        addPasswordField(formPanel, "New Password", txtNewPassword, gbc, 2, "new");

        // 3. Confirm Password Field
        txtConfirmPassword = new JPasswordField();
        addPasswordField(formPanel, "Confirm New Password", txtConfirmPassword, gbc, 3, "confirm");

        // 4. Error Label
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Inter", Font.PLAIN, 12));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(lblError, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(150, 35));
        UIStyler.styleSecondaryButton(btnCancel);
        btnCancel.setForeground(Color.BLACK);

        btnSave = new JButton("Update Password");
        btnSave.setPreferredSize(new Dimension(150, 35));
        UIStyler.stylePrimaryButton(btnSave);

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);
    }

    private void addPasswordField(JPanel panel, String labelStr, JPasswordField field, GridBagConstraints gbc, int row, String type) {
        gbc.gridy = row;
        gbc.insets = new Insets(0, 0, 15, 0);

        JPanel container = new JPanel(new BorderLayout(5, 5));
        container.setOpaque(false);

        JLabel label = new JLabel(labelStr);
        UIStyler.styleFormLabel(label);
        container.add(label, BorderLayout.NORTH);

        JPanel fieldWrapper = new JPanel(new BorderLayout(5, 0));
        fieldWrapper.setOpaque(false);
        UIStyler.styleFormField(field);
        fieldWrapper.add(field, BorderLayout.CENTER);

        JButton eyeBtn = createEyeButton(field, type);
        fieldWrapper.add(eyeBtn, BorderLayout.EAST);

        container.add(fieldWrapper, BorderLayout.CENTER);
        panel.add(container, gbc);
    }

    private JButton createEyeButton(JPasswordField field, String type) {
        JButton eyeBtn = new JButton();
        eyeBtn.setPreferredSize(new Dimension(30, 35));
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ImageIcon eyeOpen = new ImageIcon(loadImage("/com/resources/Icons/show.png", 20, 20));
        ImageIcon eyeClosed = new ImageIcon(loadImage("/com/resources/Icons/hidden.png", 20, 20));
        eyeBtn.setIcon(eyeClosed);

        char defaultEcho = field.getEchoChar();
        eyeBtn.addActionListener(e -> {
            if (type.equals("new")) {
                showNewPass = !showNewPass;
                field.setEchoChar(showNewPass ? (char) 0 : defaultEcho);
                eyeBtn.setIcon(showNewPass ? eyeOpen : eyeClosed);
            } else {
                showConfirmPass = !showConfirmPass;
                field.setEchoChar(showConfirmPass ? (char) 0 : defaultEcho);
                eyeBtn.setIcon(showConfirmPass ? eyeOpen : eyeClosed);
            }
        });
        return eyeBtn;
    }

    private Image loadImage(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage();
            return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        }
        return null;
    }
}