package com.arko.utils.OperationalDashboard;

/**
 * Canonical passenger status values.
 * Use .name() when writing to DB, and PassengerStatus.valueOf() when reading.
 * Eliminates the mixed-casing bug across DAOs ("Waiting" vs "BOARDED" vs "ARRIVED").
 */
public enum PassengerStatus {
    WAITING,
    BOARDED,
    ARRIVED
}