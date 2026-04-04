package com.arko.utils;

import com.arko.model.POJO.Staff;
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

    public void logout() {
        this.currentStaff = null;
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

    public String getCurrentUserName() {
        return (currentStaff != null) ? currentStaff.getFirstName() + " " + currentStaff.getLastName() : "Guest";
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
        return currentStaff != null && "Admin".equalsIgnoreCase(currentStaff.getRole());
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