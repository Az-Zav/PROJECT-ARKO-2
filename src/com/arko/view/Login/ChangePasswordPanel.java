package com.arko.view.Login;

import com.arko.controller.Login.AuthController;
import com.arko.controller.Login.ChangePasswordController;
import com.arko.controller.Login.LoginController;
import com.arko.utils.Login.LoginResult;
import com.arko.view.UIStyler;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable change-password form; used inside {@link LoginPanel} (card)
 */
public class ChangePasswordPanel extends JPanel {

    private final AuthController authController;
    private final LoginController loginController;
    private final LoginPanel loginPanel;
    private final Runnable afterSuccessfulSave;

    private ChangePasswordController controller;

    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel errorLabel;

    private boolean showNewPassword;
    private boolean showConfirmPassword;

    /**
     * @param loginPanel          when non-null, a "Back to login" control is shown and routes to the login card
     * @param afterSuccessfulSave run after {@link ChangePasswordController#routeAfterChange} (e.g. dialog {@code dispose})
     */
    public ChangePasswordPanel(AuthController authController,
                               LoginController loginController,
                               LoginPanel loginPanel,
                               Runnable afterSuccessfulSave) {
        this.authController = authController;
        this.loginController = loginController;
        this.loginPanel = loginPanel;
        this.afterSuccessfulSave = afterSuccessfulSave != null ? afterSuccessfulSave : () -> { };
        setOpaque(false);
        buildUI();
    }

    public void bind(LoginResult loginResult) {
        this.controller = new ChangePasswordController(authController, loginResult, loginController);
        clearForm();
    }

    public void clearForm() {
        if (newPasswordField != null) {
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            errorLabel.setText(" ");
        }
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.weightx = 1.0;
        outer.weighty = 1.0;
        outer.anchor = GridBagConstraints.CENTER;

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(520, 540));
        card.setBackground(UIStyler.BG_LIGHT);
        card.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel heading = new JLabel("Change Your Password");
        heading.setFont(new Font("Inter", Font.BOLD, 28));
        heading.setForeground(UIStyler.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(heading, gbc);

        JLabel subheading = new JLabel("You must set a new password before continuing.");
        subheading.setFont(new Font("Inter", Font.PLAIN, 13));
        subheading.setForeground(Color.GRAY);
        gbc.gridy = 1;
        subheading.setPreferredSize(new Dimension(420, 20));
        gbc.insets = new Insets(0, 0, 30, 0);
        card.add(subheading, gbc);

        ImageIcon eyeOpen = loadIcon("/com/resources/Icons/show.png");
        ImageIcon eyeClosed = loadIcon("/com/resources/Icons/hidden.png");

        int fieldWidth = 420;
        int fieldHeight = 40;

        JLabel newPassLabel = new JLabel("New Password");
        UIStyler.styleFormLabel(newPassLabel);
        newPassLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(newPassLabel, gbc);

        newPasswordField = new JPasswordField();
        UIStyler.styleFormField(newPasswordField);
        newPasswordField.setFont(new Font("Inter", Font.PLAIN, 16));
        newPasswordField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        char newEcho = newPasswordField.getEchoChar();

        JButton eyeNew = new JButton();
        eyeNew.setIcon(eyeClosed);
        eyeNew.setFocusPainted(false);
        eyeNew.setBorderPainted(false);
        eyeNew.setContentAreaFilled(false);
        eyeNew.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeNew.addActionListener(e -> {
            showNewPassword = !showNewPassword;
            newPasswordField.setEchoChar(showNewPassword ? (char) 0 : newEcho);
            eyeNew.setIcon(showNewPassword ? eyeOpen : eyeClosed);
        });

        JPanel newPassRow = new JPanel(new BorderLayout(5, 0));
        newPassRow.setOpaque(false);
        newPassRow.add(newPasswordField, BorderLayout.CENTER);
        newPassRow.add(eyeNew, BorderLayout.EAST);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(newPassRow, gbc);

        JLabel confirmLabel = new JLabel("Confirm Password");
        UIStyler.styleFormLabel(confirmLabel);
        confirmLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(confirmLabel, gbc);

        confirmPasswordField = new JPasswordField();
        UIStyler.styleFormField(confirmPasswordField);
        confirmPasswordField.setFont(new Font("Inter", Font.PLAIN, 16));
        confirmPasswordField.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        char confirmEcho = confirmPasswordField.getEchoChar();

        JButton eyeConfirm = new JButton();
        eyeConfirm.setIcon(eyeClosed);
        eyeConfirm.setFocusPainted(false);
        eyeConfirm.setBorderPainted(false);
        eyeConfirm.setContentAreaFilled(false);
        eyeConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeConfirm.addActionListener(e -> {
            showConfirmPassword = !showConfirmPassword;
            confirmPasswordField.setEchoChar(showConfirmPassword ? (char) 0 : confirmEcho);
            eyeConfirm.setIcon(showConfirmPassword ? eyeOpen : eyeClosed);
        });

        JPanel confirmPassRow = new JPanel(new BorderLayout(5, 0));
        confirmPassRow.setOpaque(false);
        confirmPassRow.add(confirmPasswordField, BorderLayout.CENTER);
        confirmPassRow.add(eyeConfirm, BorderLayout.EAST);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(confirmPassRow, gbc);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(200, 30, 30));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(errorLabel, gbc);

        JButton saveBtn = new JButton("Save New Password");
        saveBtn.setPreferredSize(new Dimension(fieldWidth, 44));
        UIStyler.stylePrimaryButton(saveBtn);
        saveBtn.setFont(new Font("Inter", Font.BOLD, 15));
        saveBtn.addActionListener(e -> onSaveClicked());
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(saveBtn, gbc);

        if (loginPanel != null) {
            JButton backBtn = new JButton("Back to login");
            backBtn.setPreferredSize(new Dimension(fieldWidth, 36));
            backBtn.setFocusPainted(false);
            backBtn.setFont(new Font("Inter", Font.PLAIN, 14));
            backBtn.setForeground(UIStyler.PRIMARY);
            backBtn.setBackground(UIStyler.BG_LIGHT);
            backBtn.setBorder(BorderFactory.createLineBorder(UIStyler.PRIMARY, 1));
            backBtn.addActionListener(e -> loginPanel.showLoginCard());
            gbc.gridy = 8;
            gbc.insets = new Insets(0, 0, 0, 0);
            card.add(backBtn, gbc);
        }

        add(card, outer);
    }

    private void onSaveClicked() {
        if (controller == null) {
            errorLabel.setText("Session error. Please try logging in again.");
            return;
        }

        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        String error = controller.validate(newPass, confirmPass);
        if (error != null) {
            errorLabel.setText(error);
            return;
        }

        boolean saved = controller.saveNewPassword(newPass);
        if (!saved) {
            errorLabel.setText("Failed to save password. Please try again.");
            return;
        }

        controller.routeAfterChange(this);
        afterSuccessfulSave.run();
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL url = getClass().getResource(path);
        if (url != null) {
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        System.out.println("Icon not found: " + path);
        return null;
    }
}
