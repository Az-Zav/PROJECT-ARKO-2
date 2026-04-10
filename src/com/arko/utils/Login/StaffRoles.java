package com.arko.utils.Login;

/**
 * Normalized role checks for routing and UI (DB may store mixed case, e.g. "Admin").
 */
public final class StaffRoles {

    public static final String ADMIN = "ADMIN";

    private StaffRoles() {}

    public static boolean isAdmin(String role) {
        return role != null && ADMIN.equalsIgnoreCase(role.trim());
    }
}
