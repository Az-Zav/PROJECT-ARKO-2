package com.arko.controller.Login;

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
                errorLabel.setText(outcome.message);
                txtPass.setText("");
                return;
            }

            // FIX: actually route to the dashboard
            route(outcome.loginResult, btnLogin);
        });
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
            // Covers STAFF, HEAD, PCG
            outcome.message    = "Welcome, Staff!";
            outcome.nextScreen = NextScreen.STAFF_DASHBOARD;
        }

        return outcome;
    }

    public void route(LoginResult loginResult, JComponent caller) {
        // Build the session object from auth result
        UserSession session = new UserSession(
                loginResult.getStaffId(),
                loginResult.getUsername(),
                loginResult.getFullName(),
                loginResult.getRole()
        );
        // Open the correct dashboard and close login window
        DashboardAccess.route(session, caller);
    }

    /** Expose the AuthController so ChangePasswordController can reuse it. */
    public AuthController getAuthController() {
        return authController;
    }
}

