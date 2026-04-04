package com.arko.controller.Login;

import com.arko.utils.Login.LoginResult;
import com.arko.utils.Login.PasswordUtil;

import javax.swing.*;

public class ChangePasswordController {

    private final AuthController authController;
    private final LoginResult loginResult;
    private final LoginController loginController; // needed for route()

    public ChangePasswordController(AuthController authController,
                                    LoginResult loginResult,
                                    LoginController loginController) {
        this.authController  = authController;
        this.loginResult     = loginResult;
        this.loginController = loginController;
    }

    // Returns null if valid, error message string if not
    public String validate(String newPass, String confirmPass) {
        if (newPass == null || newPass.isBlank())
            return "New password cannot be empty.";
        if (!newPass.equals(confirmPass))
            return "Passwords do not match.";
        return PasswordUtil.validate(newPass); // enforces uppercase, digit, special char rules
    }

    // Hashes and persists the new password via AuthController
    public boolean saveNewPassword(String newPass) {
        return authController.updatePassword(loginResult.getStaffId(), newPass);
    }

    // After saving, route to the correct dashboard using the same
    // path as a normal login — this initializes SessionManager too
    public void routeAfterChange(JComponent caller) {
        loginController.route(loginResult, caller);
    }
}