package com.arko.controller.Login;

import com.arko.model.POJO.Staff;
import com.arko.utils.Login.PasswordUtil;
import com.arko.utils.Email.EmailService;

import java.sql.SQLException;

public class ForgotPasswordController {

    private final AuthController authController;

    public ForgotPasswordController(AuthController authController) {
        this.authController = authController;
    }

    // Returns null on success, error string on failure
    public String handleReset(String username) {
        if (username == null || username.isBlank())
            return "Please enter your username.";

        try {
            Staff staff = authController.findStaffByUsername(username.trim());

            if (staff == null)
                return "No account found with that username.";

            if (staff.getEmail() == null || staff.getEmail().isBlank())
                return "No email on file. Please contact your administrator.";

            String tempPassword = PasswordUtil.generateTemporaryPassword();
            boolean updated = authController.updatePassword(staff.getStaffID(), tempPassword);

            if (!updated)
                return "Failed to reset password. Please try again.";

            EmailService.sendCredentialsEmail(
                    staff.getEmail(),
                    staff.getFullName(),
                    staff.getUsername(),
                    tempPassword,
                    "System",
                    false,
                    null
            );

            return null; // null = success

        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }
}