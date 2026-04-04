package com.arko.utils.OperationalDashboard;

/**
 * Canonical trip status values.
 * Use .name() when writing to DB, and TripStatus.valueOf() when reading.
 * IN_TRANSIT maps to the DB string "IN TRANSIT" — use toDbString() for DB writes.
 */
public enum TripStatus {
    DOCKED,
    IN_TRANSIT,
    COMPLETED,
    IDLE;

    /**
     * Returns the exact string stored in the database.
     * IN_TRANSIT is stored as "IN TRANSIT" (with a space), not "IN_TRANSIT".
     * All others match their enum name exactly.
     */
    public String toDbString() {
        return this == IN_TRANSIT ? "IN TRANSIT" : this.name();
    }

    /**
     * Parses a raw DB string back into a TripStatus safely.
     * Returns IDLE if the value is null or unrecognized.
     */
    public static TripStatus fromDbString(String raw) {
        if (raw == null) return IDLE;
        switch (raw.trim().toUpperCase()) {
            case "DOCKED":     return DOCKED;
            case "IN TRANSIT": return IN_TRANSIT;
            case "COMPLETED":  return COMPLETED;
            default:           return IDLE;
        }
    }
}