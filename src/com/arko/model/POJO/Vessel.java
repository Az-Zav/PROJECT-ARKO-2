package com.arko.model.POJO;

public class Vessel {
    // Database Fields
    private int    vesselID;
    private String vesselName;
    private int    maxCapacity;
    private String vesselStatus;
    private int    currentLoad;
    private boolean active = true;

    // Bridge fields — not in the vessel table, populated via JOIN in VesselDAO
    private int    currentTripID;
    private String tripDirection; // Populated from the active trip's TripDirection

    // Constructor for Admin Feature
    public Vessel(String vesselName, int maxCapacity, String vesselStatus) {
        this.vesselName  = vesselName;
        this.maxCapacity = maxCapacity;
        this.vesselStatus = vesselStatus;
    }

    // Empty Constructor for DAO Retrieval
    public Vessel() {}

    // --- Getters and Setters ---
    public int    getVesselID()                  { return vesselID; }
    public void   setVesselID(int id)            { this.vesselID = id; }

    public String getVesselName()                { return vesselName; }
    public void   setVesselName(String name)     { this.vesselName = name; }

    public int    getMaxCapacity()               { return maxCapacity; }
    public void   setMaxCapacity(int cap)        { this.maxCapacity = cap; }

    public String getVesselStatus()              { return vesselStatus; }
    public void   setVesselStatus(String status) { this.vesselStatus = status; }

    public int    getCurrentLoad()               { return currentLoad; }
    public void   setCurrentLoad(int load)       { this.currentLoad = load; }

    public int    getCurrentTripID()             { return currentTripID; }
    public void   setCurrentTripID(int id)       { this.currentTripID = id; }

    public String getTripDirection()             { return tripDirection; }
    public void   setTripDirection(String dir)   { this.tripDirection = dir; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return vesselName;
    }
}