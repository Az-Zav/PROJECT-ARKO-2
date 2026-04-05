package com.arko.view.AdminDashboard.AccountSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ConfirmPasswordDialog extends JDialog {

    public JPasswordField txtCurrentPassword;
    public JButton btnVerify;
    public JButton btnCancel;
    public JButton btnForgot;
    public JLabel lblError;
    private boolean showPassword = false;

    public ConfirmPasswordDialog(Window owner) {
        super(owner, "Security Verification", ModalityType.APPLICATION_MODAL);

        // Increased height to 420 to fit the instructions comfortably
        setSize(400, 420);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 0. Header
        JLabel lblHeader = new JLabel("VERIFY IT'S YOU");
        lblHeader.setFont(new Font("Inter", Font.BOLD, 18));
        lblHeader.setForeground(new Color(76, 59, 148));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(lblHeader, gbc);

        // 1. Instructions JTextArea
        JTextArea txtInstructions = new JTextArea(
                "To continue, please re-enter your current password. This helps us ensure that it's really you making these changes."
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

        // 2. Current Password Field (Helper now placed at gridy 2)
        txtCurrentPassword = new JPasswordField();
        addPasswordField(formPanel, "Current Password", txtCurrentPassword, gbc, 2);

        // 3. Error Label
        lblError = new JLabel(" ");
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Inter", Font.PLAIN, 12));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 5, 0);
        formPanel.add(lblError, gbc);

        // 4. Forgot Password Button
        btnForgot = new JButton("Forgot password?");
        btnForgot.setFont(new Font("Inter", Font.PLAIN, 12));
        btnForgot.setForeground(new Color(76, 59, 148));
        btnForgot.setContentAreaFilled(false);
        btnForgot.setBorderPainted(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.setHorizontalAlignment(SwingConstants.LEFT);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, -5, 0, 0); // Slight negative inset to align with text above
        formPanel.add(btnForgot, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnCancel = new JButton("Cancel");
        styleButton(btnCancel, new Color(220, 220, 220), Color.BLACK);

        btnVerify = new JButton("Verify");
        styleButton(btnVerify, new Color(76, 59, 148), Color.WHITE);

        btnPanel.add(btnCancel);
        btnPanel.add(btnVerify);

        contentPane.add(formPanel, BorderLayout.CENTER);
        contentPane.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);
    }

    private void addPasswordField(JPanel panel, String labelStr, JPasswordField field, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.insets = new Insets(0, 0, 15, 0);

        JPanel container = new JPanel(new BorderLayout(5, 5));
        container.setOpaque(false);

        JLabel label = new JLabel(labelStr);
        label.setFont(new Font("Inter", Font.BOLD, 12));
        label.setForeground(new Color(80, 80, 80));
        container.add(label, BorderLayout.NORTH);

        JPanel fieldWrapper = new JPanel(new BorderLayout(5, 0));
        fieldWrapper.setOpaque(false);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 35));
        fieldWrapper.add(field, BorderLayout.CENTER);

        JButton eyeBtn = createEyeButton(field);
        fieldWrapper.add(eyeBtn, BorderLayout.EAST);

        container.add(fieldWrapper, BorderLayout.CENTER);
        panel.add(container, gbc);
    }

    private JButton createEyeButton(JPasswordField field) {
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
            showPassword = !showPassword;
            field.setEchoChar(showPassword ? (char) 0 : defaultEcho);
            eyeBtn.setIcon(showPassword ? eyeOpen : eyeClosed);
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

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
    }
}