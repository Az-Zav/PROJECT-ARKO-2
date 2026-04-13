package com.arko.model.POJO;

// ============================================================
// FILE        : Staff.java
// PACKAGE     : Model
// ROLE        : Full staff record DTO — mirrors the Staff table.
//               Used when an ADMIN loads/creates/edits a user.
//               For login-only flows, the lighter LoginResult is
//               used instead.
//
// CONNECTIONS :
//   ← Controller.AdminController  populates this when listing users
//   → UI.AdminDashboard / ManageUsersPanel  displays staff details
//
// COLUMNS MAPPED:
//   StaffID, Username, FirstName, LastName, Email,
//   ContactNumber, Role, StationID, IsActive,
//   MustChangePassword, TwoFactorEnabled, TwoFactorSecret
// ============================================================

public class Staff {
    // Database Fields
    private int    staffID; // Primary Key
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String role;                // ADMIN | STAFF
    private int    stationID;           // -1 for all stations
    private String email;
    private boolean active = true;

    // Non-DB field — populated via JOIN in StaffDAO
    private String stationCode;
    private String fullName;

    // Constructor for creating a new staff member (Admin feature)
    public Staff(String username, String password, String firstName, String lastName,
                 String contactNumber, String role, int stationID) {
        this.username      = username;
        this.password      = password;
        this.firstName     = firstName;
        this.lastName      = lastName;
        this.contactNumber = contactNumber;
        this.role          = role;
        this.stationID     = stationID;
    }

    // Empty constructor for DAO retrieval mapping
    public Staff() {}

    // --- Getters and Setters ---
    public int    getStaffID()                    { return staffID; }
    public void   setStaffID(int id)              { this.staffID = id; }

    public String getUsername()                   { return username; }
    public void   setUsername(String username)    { this.username = username; }

    public String getPassword()                   { return password; }
    public void   setPassword(String password)    { this.password = password; }

    public String getFirstName()                  { return firstName; }
    public void   setFirstName(String firstName)  { this.firstName = firstName; }

    public String getLastName()                   { return lastName; }
    public void   setLastName(String lastName)    { this.lastName = lastName; }

    public String getFullName() {
        return fullName != null && !fullName.isBlank()
                ? fullName
                : ((firstName == null ? "" : firstName)
                + " "
                + (lastName  == null ? "" : lastName)).trim();
    }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContactNumber()                      { return contactNumber; }
    public void   setContactNumber(String contactNumber)  { this.contactNumber = contactNumber; }

    public String getRole()                       { return role; }
    public void   setRole(String role)            { this.role = role; }

    public int    getStationID()                  { return stationID; }
    public void   setStationID(int id)            { this.stationID = id; }

    public String getStationCode()                        { return stationCode; }
    public void   setStationCode(String stationCode)      { this.stationCode = stationCode; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

