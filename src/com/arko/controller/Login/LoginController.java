package com.arko.controller.Login;

import com.arko.model.POJO.Staff;
import com.arko.utils.Login.DashboardAccess;
import com.arko.utils.Login.LoginResult;
import com.arko.utils.Login.UserSession;

import javax.swing.*;

public class LoginController {

    public LoginController(JTextField txtUser, JPasswordField txtPass,
                           JButton btnLogin, JLabel errorLabel) {

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
                    startCountdown(errorLabel, outcome.loginResult.getLockRemainingSeconds());
                } else {
                    errorLabel.setText(outcome.message);
                }
                txtPass.setText("");
                return;
            }

            // Check if account requires password change
            if (outcome.nextScreen == NextScreen.CHANGE_PASSWORD) {
                // Open the ChangePasswordFrame instead of the dashboard
                // We pass the authController and the loginResult (which contains the StaffID)
                UI.ChangePasswordFrame changeFrame = new UI.ChangePasswordFrame(authController, outcome.loginResult);
                changeFrame.setVisible(true);

                // Close the current login window
                SwingUtilities.getWindowAncestor(btnLogin).dispose();
                return;
            }

            // FIX: actually route to the dashboard
            route(outcome.loginResult, btnLogin);
        });
    }

    // ACCOUNT LOCK COUNTER
    private void startCountdown(JLabel label, long seconds) {
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

    //  Auth dependency (created once per controller instance)
    private final AuthController authController = new AuthController();

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

        // Route by role
        if ("ADMIN".equalsIgnoreCase(result.getRole())) {
            outcome.message    = "Welcome, Admin!";
            outcome.nextScreen = NextScreen.ADMIN_DASHBOARD;
        } else {
            // Covers STAFF
            outcome.message    = "Welcome, Staff!";
            outcome.nextScreen = NextScreen.STAFF_DASHBOARD;
        }

        return outcome;
    }

    public void route(LoginResult loginResult, JComponent caller) {
        // 1. Fetch the full Staff POJO from the database to ensure SessionManager has everything
        try {
            Staff staff = authController.findStaffByUsername(loginResult.getUsername());

            // 2. Initialize the Global Session
            com.arko.utils.SessionManager.getInstance().login(staff);

            // 3. Create the legacy UserSession object for the existing DashboardAccess logic
            UserSession session = new UserSession(
                    loginResult.getStaffId(),
                    loginResult.getUsername(),
                    loginResult.getFullName(),
                    loginResult.getRole()
            );

            // 4. Open the correct dashboard and close login window
            DashboardAccess.route(session, caller);

        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(caller, "Error initializing session: " + e.getMessage());
        }
    }

    /** Expose the AuthController so ChangePasswordController can reuse it. */
    public AuthController getAuthController() {
        return authController;
    }
}

