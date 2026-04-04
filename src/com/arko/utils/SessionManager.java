package com.arko.utils;

import com.arko.model.POJO.Staff;

/**
 * Singleton Utility to manage the currently logged-in user session.
 * Accessible globally to retrieve staff ID, role, or station details.
 */
public class SessionManager {

    private static SessionManager instance;
    private Staff currentStaff;

    // Private constructor for Singleton pattern
    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Initializes the session after a successful login.
     * @param staff The Staff POJO retrieved from the database.
     */
    public void login(Staff staff) {
        this.currentStaff = staff;
    }

    /**
     * Clears the current session data.
     */
    public void logout() {
        this.currentStaff = null;
    }

    public boolean isLoggedIn() {
        return currentStaff != null;
    }

    // --- CONVENIENCE GETTERS ---

    public Staff getCurrentStaff() {
        return currentStaff;
    }

    public int getCurrentStaffId() {
        return (currentStaff != null) ? currentStaff.getStaffID() : -1;
    }

    public String getCurrentUsername() {
        return (currentStaff != null) ? currentStaff.getUsername() : "Guest";
    }

    public String getCurrentUserRole() {
        return (currentStaff != null) ? currentStaff.getRole() : "NONE";
    }

    /**
     * Returns the StationID assigned to the current user.
     * Useful for filtering data in the Operational Dashboard.
     */
    public int getCurrentStationID() {
        return (currentStaff != null) ? currentStaff.getStationID() : -1;
    }

    public String getCurrentStationCode() {
        return (currentStaff != null) ? currentStaff.getStationCode() : "N/A";
    }

    public boolean isAdmin() {
        return currentStaff != null && "ADMIN".equalsIgnoreCase(currentStaff.getRole());
    }
}