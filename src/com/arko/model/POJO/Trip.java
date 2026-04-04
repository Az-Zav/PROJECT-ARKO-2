package com.arko.model.POJO;

import java.sql.Timestamp;

public class Trip {
    // --- DATABASE FIELDS ---
    private int       tripID;
    private String    tripDirection;
    private Timestamp departureTime;
    private Timestamp arrivalTime;
    private String    tripStatus;
    private int       vesselID;
    private int       currentStationID;

    // --- UI DISPLAY FIELDS (non-DB, populated from JOINs in TripDAO) ---
    private String vesselName;
    private String fromStationCode; // Code of the station the vessel is currently at
    private String nextStationCode; // Computed inline in SQL — no extra query needed
    private int    currentLoad;

    // Constructor
    public Trip(String tripDirection, int vesselID, int currentStationID) {
        this.tripDirection    = tripDirection;
        this.vesselID         = vesselID;
        this.currentStationID = currentStationID;
    }

    public Trip() {}

    // --- Getters and Setters ---
    public int    getTripID()              { return tripID; }
    public void   setTripID(int id)        { this.tripID = id; }

    public String getTripDirection()                   { return tripDirection; }
    public void   setTripDirection(String direction)   { this.tripDirection = direction; }

    public Timestamp getDepartureTime()                { return departureTime; }
    public void      setDepartureTime(Timestamp ts)    { this.departureTime = ts; }

    public Timestamp getArrivalTime()                  { return arrivalTime; }
    public void      setArrivalTime(Timestamp ts)      { this.arrivalTime = ts; }

    public String getTripStatus()                      { return tripStatus; }
    public void   setTripStatus(String status) {
        this.tripStatus = (status != null) ? status.toUpperCase() : null;
    }

    public int  getVesselID()              { return vesselID; }
    public void setVesselID(int id)        { this.vesselID = id; }

    public int  getCurrentStationID()          { return currentStationID; }
    public void setCurrentStationID(int id)    { this.currentStationID = id; }

    public String getVesselName()                      { return vesselName; }
    public void   setVesselName(String vesselName)     { this.vesselName = vesselName; }

    public String getFromStationCode()                 { return fromStationCode; }
    public void   setFromStationCode(String code)      { this.fromStationCode = code; }

    /**
     * The predicted next station code, computed inline in TripDAO.getFullFleetTracking()
     * via a CASE-based JOIN — no additional query is made per vessel.
     * Null-safe: "---" for IDLE, "END OF LINE" when beyond route bounds.
     */
    public String getNextStationCode()                 { return nextStationCode; }
    public void   setNextStationCode(String code)      { this.nextStationCode = code; }

    public int  getCurrentLoad()           { return currentLoad; }
    public void setCurrentLoad(int load)   { this.currentLoad = load; }
}