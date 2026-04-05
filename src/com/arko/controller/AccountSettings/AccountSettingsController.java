package com.arko.controller.AccountSettings;

import com.arko.controller.Login.AuthController;
import com.arko.controller.Login.ForgotPasswordController;
import com.arko.model.DAO.StaffDAO;
import com.arko.model.POJO.Staff;
import com.arko.utils.SessionManager;
import com.arko.utils.Login.LoginResult;
import com.arko.utils.Login.PasswordUtil;
import com.arko.view.AdminDashboard.AccountSettings.AccountSettingsPanel;
import com.arko.view.AdminDashboard.AccountSettings.ChangePasswordDialog;
import com.arko.view.AdminDashboard.AccountSettings.ConfirmPasswordDialog;

import javax.swing.*;
import java.awt.*;

public class AccountSettingsController {

    private final AccountSettingsPanel panel;
    private final StaffDAO staffDAO;
    private final AuthController authController;

    public AccountSettingsController(AccountSettingsPanel panel) {
        this.panel = panel;
        this.staffDAO = new StaffDAO();
        this.authController = new AuthController();

        initController();
        populateStaffInformation();
    }

    private void initController() {
        panel.getBtnChangePassword().addActionListener(e -> handleChangePasswordRequest());
    }

    public void populateStaffInformation() {
        if (SessionManager.getInstance().isLoggedIn()) {
            Staff staff = SessionManager.getInstance().getCurrentStaff();

            panel.lblStaffName.setText(staff.getFullName());
            panel.lblStaffIDData.setText(String.valueOf(staff.getStaffID()));
            panel.lblStaffEmailData.setText(staff.getEmail());
            panel.lblStaffContactData.setText(staff.getContactNumber());
            panel.lblStaffRoleData.setText(staff.getRole());
            panel.lblStaffStationData.setText(staff.getStationCode());
        }
    }

    // ─── SECURITY CHAIN: STEP 1 (VERIFICATION) ──────────────────────────────

    private void handleChangePasswordRequest() {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        ConfirmPasswordDialog confirmDialog = new ConfirmPasswordDialog(parentWindow);

        confirmDialog.btnVerify.addActionListener(e -> handleVerification(confirmDialog));
        confirmDialog.btnForgot.addActionListener(e -> handleForgotInDialog(confirmDialog));
        confirmDialog.btnCancel.addActionListener(e -> confirmDialog.dispose());

        confirmDialog.setVisible(true);
    }

    private void handleVerification(ConfirmPasswordDialog dialog) {
        String password = new String(dialog.txtCurrentPassword.getPassword());
        String username = SessionManager.getInstance().getCurrentStaff().getUsername();

        if (password.isEmpty()) {
            dialog.lblError.setText("Please enter your password.");
            return;
        }

        LoginResult result = authController.verifyCredentials(username, password);

        if (result.isValid()) {
            dialog.dispose();
            showChangePasswordDialog();
        } else {
            dialog.lblError.setText("Incorrect password. Please try again.");
            dialog.txtCurrentPassword.setText("");
        }
    }

    private void handleForgotInDialog(ConfirmPasswordDialog dialog) {
        String username = SessionManager.getInstance().getCurrentStaff().getUsername();

        ForgotPasswordController forgotController = new ForgotPasswordController();
        String error = forgotController.handleReset(username);

        if (error != null) {
            JOptionPane.showMessageDialog(dialog, error, "Reset Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(dialog,
                    "A temporary password has been sent to your email.\n" +
                            "Please use it to verify your identity.",
                    "Email Sent", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ─── SECURITY CHAIN: STEP 2 (CHANGE PASSWORD) ───────────────────────────

    private void showChangePasswordDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(panel);
        ChangePasswordDialog changeDialog = new ChangePasswordDialog(parentWindow);

        changeDialog.btnCancel.addActionListener(e -> changeDialog.dispose());

        changeDialog.btnSave.addActionListener(e -> {
            String newPass     = new String(changeDialog.txtNewPassword.getPassword());
            String confirmPass = new String(changeDialog.txtConfirmPassword.getPassword());

            if (newPass.isEmpty()) {
                changeDialog.lblError.setText("New password cannot be empty.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                changeDialog.lblError.setText("Passwords do not match.");
                return;
            }

            String validationError = PasswordUtil.validate(newPass);
            if (validationError != null) {
                changeDialog.lblError.setText("<html>" + validationError + "</html>");
                return;
            }

            int staffId = SessionManager.getInstance().getCurrentStaffId();
            boolean updated = authController.updatePassword(staffId, newPass);

            if (updated) {
                JOptionPane.showMessageDialog(changeDialog,
                        "Your password has been successfully updated.",
                        "Security Update", JOptionPane.INFORMATION_MESSAGE);
                changeDialog.dispose();
            } else {
                changeDialog.lblError.setText("Database error. Could not update password.");
            }
        });

        changeDialog.setVisible(true);
    }
}