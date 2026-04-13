package com.arko.utils.OperationalDashboard;

import com.arko.model.database.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Determines whether a given station is a terminus based on MIN/MAX StationID.
 *
 * Cache removed — queries the DB fresh on each call.
 * Rationale: terminus checks only happen on ARRIVED (user action), not on a
 * hot path like a timer. The query is trivially cheap (one aggregate, one row).
 * A per-JVM cache would serve stale data if stations are added/removed via
 * the admin feature on another station without restarting this instance.
 *
 * Direction convention:
 *   UPSTREAM   = toward lower StationIDs  → upstream terminus   = MIN StationID
 *   DOWNSTREAM = toward higher StationIDs → downstream terminus = MAX StationID
 */
public final class TerminusChecker {

    private TerminusChecker() {}

    /** Returns true if the given station is either terminus (MIN or MAX ID). */
    public static boolean isTerminus(int stationId) {
        int[] bounds = fetchBounds();
        return stationId == bounds[0] || stationId == bounds[1];
    }

    /**
     * Returns true if this station is the upstream terminus (MIN StationID).
     * A vessel arriving here going UPSTREAM has reached the end of the line.
     */
    public static boolean isUpstreamTerminus(int stationId) {
        return stationId == fetchBounds()[0];
    }

    /**
     * Returns true if this station is the downstream terminus (MAX StationID).
     * A vessel arriving here going DOWNSTREAM has reached the end of the line.
     */
    public static boolean isDownstreamTerminus(int stationId) {
        return stationId == fetchBounds()[1];
    }

    /**
     * Returns the direction the vessel should travel on its return trip.
     * UPSTREAM vessel at MIN → return DOWNSTREAM
     * DOWNSTREAM vessel at MAX → return UPSTREAM
     */
    public static String getReturnDirection(int stationId) {
        int[] bounds = fetchBounds();
        if (stationId == bounds[0]) return "DOWNSTREAM";
        if (stationId == bounds[1]) return "UPSTREAM";
        throw new IllegalArgumentException("Station " + stationId + " is not a terminus.");
    }

    /**
     * Queries MIN and MAX StationID fresh from the DB.
     * Returns int[]{min, max}. Returns {-1, -1} on failure.
     * This queries only operational stations that are considered terminus (last by operations, not by route)
     * This approach does not account for true terminus stations that are unoperational
     */
    private static int[] fetchBounds() {
        String sql = "SELECT MIN(StationID) AS MinID, MAX(StationID) AS MaxID FROM station " +
                "WHERE OperationalStatus = 'Operational' AND IsActive = 1 ";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new int[]{ rs.getInt("MinID"), rs.getInt("MaxID") };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{ -1, -1 }; //fall back to prevent null crash incase query failed
    }
}