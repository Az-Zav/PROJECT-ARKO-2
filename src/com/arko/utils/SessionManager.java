package com.arko.utils;

import com.arko.model.POJO.Staff;
import com.arko.utils.Login.StaffRoles;

import java.time.LocalDate;

/**
 * Singleton Utility to manage the currently logged-in user session.
 */
public class SessionManager {

    private static SessionManager instance;
    private Staff currentStaff;

    // Reports Filter default States
    private String reportsPeriodType = "DAILY";
    private LocalDate reportsAnchorDate = LocalDate.now();
    private int reportsStationId = -1;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Staff staff) {
        this.currentStaff = staff;
    }

    /** Clears identity and resets report filter state to defaults (single logout/reset contract). */
    public void resetAll() {
        this.currentStaff = null;
        resetReportFilters();
    }

    public void resetReportFilters() {
        this.reportsPeriodType = "DAILY";
        this.reportsAnchorDate = LocalDate.now();
        this.reportsStationId = -1;
    }

    public void logout() {
        resetAll();
    }

    public boolean isLoggedIn() {
        return currentStaff != null;
    }

    // --- ACCURATE FIELD ACCESSORS ---

    public Staff getCurrentStaff() {
        return currentStaff;
    }

    public int getCurrentStaffId() {
        return (currentStaff != null) ? currentStaff.getStaffID() : -1;
    }

    /** Display name for the logged-in user (full name when available). */
    public String getCurrentFullName() {
        return (currentStaff != null) ? currentStaff.getFullName() : "Guest";
    }

    public String getCurrentUserRole() {
        return (currentStaff != null) ? currentStaff.getRole() : "NONE";
    }

    public int getCurrentStationId() {
        return (currentStaff != null) ? currentStaff.getStationID() : -1;
    }

    public String getCurrentStationCode() {
        return (currentStaff != null) ? currentStaff.getStationCode() : "N/A";
    }

    public boolean isCurrentStaffAdmin() {
        return currentStaff != null && StaffRoles.isAdmin(currentStaff.getRole());
    }

    // ── Reports Filter State Getters/Setters ──────────────────────────────────

    public String getReportsPeriodType() {
        return reportsPeriodType;
    }

    public void setReportsPeriodType(String periodType) {
        this.reportsPeriodType = periodType;
    }

    public LocalDate getReportsAnchorDate() {
        return reportsAnchorDate;
    }

    public void setReportsAnchorDate(LocalDate anchorDate) {
        this.reportsAnchorDate = anchorDate;
    }

    public int getReportsStationId() {
        return reportsStationId;
    }

    public void setReportsStationId(int stationId) {
        this.reportsStationId = stationId;
    }
}