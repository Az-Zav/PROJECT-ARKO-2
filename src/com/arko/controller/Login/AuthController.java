package com.arko.controller.Login;

import com.arko.model.DAO.StaffDAO;
import com.arko.model.POJO.Staff;
import com.arko.utils.Login.LoginResult;
import com.arko.utils.Login.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthController {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_SECONDS = 30;

    private static final Map<String, Integer> FAILED_ATTEMPTS = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> LOCKED_USERS = new ConcurrentHashMap<>();

    private final StaffDAO staffDAO = new StaffDAO();

    public LoginResult verifyCredentials(String username, String password) {
        LoginResult loginResult = new LoginResult();
        String normalizedUsername = username == null ? "" : username.trim();

        LocalDateTime lockedUntil = LOCKED_USERS.get(normalizedUsername);
        if (lockedUntil != null) {

            long secondsLeft = java.time.Duration.between(LocalDateTime.now(), lockedUntil).getSeconds();

            if (secondsLeft > 0) {
                loginResult.setValid(false);
                loginResult.setLockRemainingSeconds(secondsLeft); // Pass the duration
                loginResult.setErrorMessage("Your account is temporarily locked.");
                return loginResult;
            }
            LOCKED_USERS.remove(normalizedUsername);
            FAILED_ATTEMPTS.remove(normalizedUsername);
        }

        try {
            Staff staff = staffDAO.findByUsername(normalizedUsername);

            if (staff == null) {
                loginResult.setValid(false);
                loginResult.setErrorMessage("Invalid username or password.");
                return loginResult;
            }

            if (!PasswordUtil.verifyPassword(password, staff.getPassword())) {
                recordFailedAttempt(normalizedUsername);
                loginResult.setValid(false);
                loginResult.setErrorMessage("Invalid username or password.");
                return loginResult;
            }

            clearFailedAttempts(normalizedUsername);

            loginResult.setValid(true);
            loginResult.setStaffId(staff.getStaffID());
            loginResult.setUsername(staff.getUsername());
            loginResult.setFullName(staff.getFullName());
            loginResult.setRole(staff.getRole());
            loginResult.setMustChangePassword(PasswordUtil.isTemporaryPassword(password));
            loginResult.setErrorMessage(null);

        } catch (SQLException e) {
            loginResult.setValid(false);
            loginResult.setErrorMessage("Database error: " + e.getMessage());
        }

        return loginResult;
    }

    // ── Called by ForgotPasswordController ────────────────────
    public Staff findStaffByUsername(String username) throws SQLException {
        return staffDAO.findByUsername(username);
    }

    public boolean updatePassword(int staffId, String newPassword) {
        try {
            return staffDAO.updatePassword(staffId, PasswordUtil.hashPassword(newPassword));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void recordFailedAttempt(String username) {
        int attempts = FAILED_ATTEMPTS.getOrDefault(username, 0) + 1;
        FAILED_ATTEMPTS.put(username, attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            LOCKED_USERS.put(username, LocalDateTime.now().plusSeconds(LOCK_SECONDS));
        }
    }

    private void clearFailedAttempts(String username) {
        FAILED_ATTEMPTS.remove(username);
        LOCKED_USERS.remove(username);
    }
}