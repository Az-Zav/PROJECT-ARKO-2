package com.arko.utils.Login;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class    UserSession {

    private final int staffId;
    private final String username;
    private final String fullName;
    private final String role;
    private final LocalDateTime loginTime;

    public UserSession(int staffId, String username, String fullName, String role) {
        this.staffId = staffId;
        this.username = username;
        this.fullName = fullName;
        this.role = role == null ? "STAFF" : role.toUpperCase();
        this.loginTime = LocalDateTime.now();
    }

    public int getStaffId() {
        return staffId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getLoginTimeFormatted() {
        return loginTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}