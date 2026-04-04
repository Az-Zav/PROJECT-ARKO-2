package com.arko.model.POJO;

public class Station {
    // Database Fields
    private int stationID;           // PK - Auto-increment
    private String stationName;      // e.g., "Guadalupe"
    private String stationCode;      // e.g., "GUA" (3 characters as per VARCHAR(3))
    private String operationalStatus; // e.g., "Operational", "Closed", "Under Repair"

    // Constructor for ADDING a new station (Admin Feature)
    public Station(String stationName, String stationCode, String operationalStatus) {
        this.stationName = stationName;
        this.stationCode = stationCode;
        this.operationalStatus = operationalStatus;

        /* EXCLUDED FIELDS:
           - stationID: Auto-incremented by the database.
        */
    }

    // Empty Constructor for Retrieving from DAO
    public Station() {}

    // --- Getters and Setters ---
    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    // Standard toString for debugging or populating JComboBoxes
    @Override
    public String toString() {
        return (stationName != null) ? stationName : "Unknown Station";
    }
}