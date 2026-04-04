package com.arko.utils.Login;

/**
 * ROLE:        Plain data carrier (DTO) returned by
 *              AuthController.verifyCredentials().
 *
 * CONNECTIONS :
 *      ← Auth.AuthController                   populates this object
 *      → Controller.LoginController            reads valid/role to decide next screen
 *      → Controller.ChangePasswordController   reads staffId for updatePassword
 *      → Session.UserSession                   receives staffId/username/fullName/role
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
}
