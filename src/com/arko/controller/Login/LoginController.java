package com.arko.controller.Login;

import com.arko.model.POJO.Staff;
import com.arko.utils.Login.DashboardAccess;
import com.arko.utils.SessionManager;
import com.arko.utils.Login.LoginResult;
import com.arko.utils.Login.StaffRoles;
import com.arko.view.Login.ChangePasswordFrame;
import com.arko.view.MainAppShell;

import javax.swing.*;
import java.awt.*;

public class LoginController {

    private final MainAppShell appShell;
    private final AuthController authController;

    public LoginController(JTextField txtUser, JPasswordField txtPass,
                           JButton btnLogin, JLabel errorLabel,
                           MainAppShell appShell,
                           AuthController authController) {
        this.appShell = appShell;
        this.authController = authController;
        handleLogin(txtUser, txtPass, btnLogin, errorLabel);
    }

    private void handleLogin(JTextField txtUser, JPasswordField txtPass,
                             JButton btnLogin, JLabel errorLabel) {
        btnLogin.addActionListener(e -> {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());

            String inputError = validateInputs(username, password);
            if (inputError != null) {
                errorLabel.setText(inputError);
                return;
            }

            errorLabel.setText(" ");
            LoginOutcome outcome = login(username, password);

            if (!outcome.success) {
                // Check if loginResult exists AND if there are seconds to count down
                if (outcome.loginResult != null && outcome.loginResult.getLockRemainingSeconds() > 0) {
                    startCountdown(errorLabel, outcome.loginResult.getLockRemainingSeconds(), btnLogin);
                } else {
                    errorLabel.setText(outcome.message);
                }
                txtPass.setText("");
                return;
            }

            // Check if account requires password change
            if (outcome.nextScreen == NextScreen.CHANGE_PASSWORD) {
                Window owner = SwingUtilities.getWindowAncestor(btnLogin);
                ChangePasswordFrame changeDlg = new ChangePasswordFrame(
                        owner, authController, outcome.loginResult, this);
                changeDlg.setVisible(true);
                return;
            }

            // FIX: actually route to the dashboard
            route(outcome.loginResult, btnLogin);
        });
    }

    // ACCOUNT LOCK COUNTER
    private void startCountdown(JLabel label, long seconds, JButton btnLogin) {
        Timer timer = new Timer(1000, null); // 1-second interval
        final long[] timeRemaining = {seconds};

        timer.addActionListener(e -> {
            if (timeRemaining[0] > 0) {
                label.setText("Account locked. Try again in " + timeRemaining[0] + "s");
                timeRemaining[0]--;
            } else {
                label.setText("Account unlocked. You may try again.");
                ((Timer)e.getSource()).stop();
            }
        });

        timer.start();
        btnLogin.setEnabled(true);
    }


    public enum NextScreen {
        NONE,             // stay put — display the error message
        CHANGE_PASSWORD,  // first-login password change required
        ADMIN_DASHBOARD,  // role == ADMIN
        STAFF_DASHBOARD   // role == STAFF
    }


    public static class LoginOutcome {
        public boolean    success;
        public String     message;
        public NextScreen nextScreen;
        public LoginResult loginResult; // null on failure
    }

    public String validateInputs(String username, String password) {
        if (username == null || username.isBlank())
            return "Please enter your username.";
        if (password == null || password.isBlank())
            return "Please enter your password.";
        return null; // null → inputs are valid, proceed to login()
    }

    public LoginOutcome login(String username, String password) {
        LoginOutcome outcome = new LoginOutcome();

        // [STEP 1] Hit the database via AuthController
        LoginResult result = authController.verifyCredentials(username.trim(), password);

        // CRITICAL: Attach the result immediately so the Controller can read
        // error details (like countdown timers) regardless of success/fail.
        outcome.loginResult = result;

        // [STEP 2] Handle failure path
        if (!result.isValid()) {
            outcome.success    = false;
            outcome.message    = result.getErrorMessage();
            outcome.nextScreen = NextScreen.NONE;
            return outcome;
        }

        // [STEP 3] Handle success path — decide next screen
        outcome.success     = true;
        outcome.loginResult = result;

        if (result.isMustChangePassword()) {
            // Force password reset before accessing any dashboard
            outcome.message    = "Password change required before access.";
            outcome.nextScreen = NextScreen.CHANGE_PASSWORD;
            return outcome;
        }

        if (StaffRoles.isAdmin(result.getRole())) {
            outcome.message    = "Welcome, Admin!";
            outcome.nextScreen = NextScreen.ADMIN_DASHBOARD;
        } else {
            outcome.message    = "Welcome, Staff!";
            outcome.nextScreen = NextScreen.STAFF_DASHBOARD;
        }

        return outcome;
    }

    public void route(LoginResult loginResult, JComponent caller) {
        try {
            Staff staff = loginResult.getAuthenticatedStaff();
            if (staff == null) {
                staff = authController.findStaffByUsername(loginResult.getUsername());
            }
            if (staff == null) {
                JOptionPane.showMessageDialog(caller, "Could not load user profile.");
                return;
            }
            SessionManager.getInstance().login(staff);
            DashboardAccess.route(appShell);
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(caller, "Error initializing session: " + e.getMessage());
        }
    }

    /** Expose the AuthController so ChangePasswordController can reuse it. */
    public AuthController getAuthController() {
        return authController;
    }
}

