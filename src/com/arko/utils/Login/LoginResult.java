package com.arko.utils.Login;

import com.arko.model.POJO.Staff;

/**
 * Plain DTO returned by {@link com.arko.controller.Login.AuthController#verifyCredentials}.
 * Short-lived: used for the login / change-password transition only.
 */
public class LoginResult {

    // ── Authentication outcome ───────────────────────────────
    private boolean valid;          // true  → credentials OK and account usable
    private String  errorMessage;   // non-null when valid == false

    // ── Staff identity (populated only when valid == true) ───
    private int    staffId;
    private String username;
    private String fullName;        // "FirstName LastName"
    private String role;            // e.g. "ADMIN", "STAFF", "HEAD", "PCG"

    // ── Account flags ────────────────────────────────────────
    private boolean mustChangePassword; // true → force ChangePassword screen before dashboard
    private boolean active;             // mirrors Staff.IsActive column
    private boolean twoFactorEnabled;   // true when Google Authenticator is required
    private long lockRemainingSeconds;

    /** Full staff row when {@link #valid} — avoids a second DB fetch after login. */
    private Staff authenticatedStaff;

    // ── Getters & Setters ────────────────────────────────────
    // (No logic — just get/set pairs)

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getLockRemainingSeconds() { return lockRemainingSeconds; }
    public void setLockRemainingSeconds(long seconds) { this.lockRemainingSeconds = seconds; }

    public Staff getAuthenticatedStaff() { return authenticatedStaff; }
    public void setAuthenticatedStaff(Staff authenticatedStaff) { this.authenticatedStaff = authenticatedStaff; }
}
