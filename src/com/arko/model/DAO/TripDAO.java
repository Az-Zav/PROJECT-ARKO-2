package com.arko.model.DAO;

import com.arko.model.POJO.Trip;
import com.arko.model.database.DBConnection;
import com.arko.utils.OperationalDashboard.AppConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripDAO {

    /**
     * Initialises a new trip and returns the generated TripID.
     * Status is set to DOCKED at the given start station.
     */
    public int createTripReturnID(int vesselId, int startStationId, String direction) {
        String sql = "INSERT INTO trip (VesselID, DepartureTime, TripStatus, CurrentStationID, TripDirection) " +
                "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, vesselId);
            ps.setString(2, AppConstants.TripStatus.DOCKED.toDbString());
            ps.setInt(3, startStationId);
            ps.setString(4, direction);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Creates the return trip at terminus with the opposite direction.
     * Status is DOCKED so the vessel appears in the terminus dropdown immediately.
     */
    public int createReturnTripReturnID(int vesselId, String returnDirection, int terminusStationId) {
        String sql = "INSERT INTO trip (VesselID, DepartureTime, TripStatus, CurrentStationID, TripDirection) " +
                "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, vesselId);
            ps.setString(2, AppConstants.TripStatus.DOCKED.toDbString());
            ps.setInt(3, terminusStationId);
            ps.setString(4, returnDirection);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Marks a trip as COMPLETED.
     */
    public boolean completeTrip(int tripId) {
        String sql = "UPDATE trip SET TripStatus = ? WHERE TripID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, AppConstants.TripStatus.COMPLETED.toDbString());
            ps.setInt(2, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns true if every passenger on this trip has ARRIVED status.
     */
    public boolean allPassengersArrived(int tripId) {
        String sql = "SELECT COUNT(*) FROM passengers " +
                "WHERE TripID = ? AND PassengerStatus != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            ps.setString(2, AppConstants.PassengerStatus.ARRIVED.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Fetches the TripDirection for a given trip.
     */
    public String getTripDirection(int tripId) {
        String sql = "SELECT TripDirection FROM trip WHERE TripID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("TripDirection");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * READ: The River Map Tracking engine.
     *
     * Next station is computed inline via a CASE-based JOIN on the station table —
     * no separate per-vessel query is made. This eliminates the N+1 problem that
     * previously called StationDAO.getStationCodeById() once per vessel per refresh.
     *
     * Next station ID logic:
     *   UPSTREAM   → CurrentStationID - 1
     *   DOWNSTREAM → CurrentStationID + 1
     *
     * The JOIN returns NULL for ns.StationCode when the computed ID falls outside
     * the station table (end of line) or when the trip has no direction (IDLE).
     * These cases are handled via CASE WHEN in the SELECT clause.
     */
    public List<Trip> getFullFleetTracking() {
        List<Trip> fleet = new ArrayList<>();

        String sql =
                "SELECT " +
                        "  v.VesselName, v.VesselID, v.CurrentLoad AS VesselLoad, " +
                        "  t.TripID, t.TripStatus, t.TripDirection, t.CurrentStationID, " +
                        "  cs.StationCode AS CurrentCode, " +
                        // Compute next station code inline — no extra query per vessel
                        "  CASE " +
                        "    WHEN t.TripDirection IS NULL THEN '---' " +
                        "    WHEN ns.StationCode IS NULL  THEN 'END OF LINE' " +
                        "    ELSE ns.StationCode " +
                        "  END AS NextCode " +
                        "FROM vessel v " +
                        "LEFT JOIN trip t " +
                        "  ON v.VesselID = t.VesselID AND t.TripStatus != ? " +
                        // Current station JOIN
                        "LEFT JOIN station cs ON t.CurrentStationID = cs.StationID " +
                        // Next station JOIN — ID computed by CASE, no schema change
                        "LEFT JOIN station ns ON ns.StationID = CASE " +
                        "  WHEN t.TripDirection = 'UPSTREAM'   THEN t.CurrentStationID - 1 " +
                        "  WHEN t.TripDirection = 'DOWNSTREAM' THEN t.CurrentStationID + 1 " +
                        "  ELSE NULL " +
                        "END " +
                        "WHERE v.VesselStatus = 'Operational' AND v.IsActive = 1 " +
                        "ORDER BY v.VesselID ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, AppConstants.TripStatus.COMPLETED.toDbString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Trip t = new Trip();
                t.setVesselName(rs.getString("VesselName"));
                t.setVesselID(rs.getInt("VesselID"));
                t.setCurrentLoad(rs.getInt("VesselLoad"));
                t.setTripID(rs.getInt("TripID"));

                String status = rs.getString("TripStatus");
                t.setTripStatus(status == null ? AppConstants.TripStatus.IDLE.name() : status);

                t.setTripDirection(rs.getString("TripDirection"));
                t.setCurrentStationID(rs.getInt("CurrentStationID"));
                t.setFromStationCode(
                        rs.getString("CurrentCode") != null ? rs.getString("CurrentCode") : "---");

                // Next station code populated from SQL — no Java calculation needed
                String nextCode = rs.getString("NextCode");
                t.setNextStationCode(nextCode != null ? nextCode : "---");

                fleet.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fleet;
    }

    /**
     * Moves a vessel to IN TRANSIT status on departure.
     */
    public boolean departTrip(int tripId) {
        String sql = "UPDATE trip SET TripStatus = ? WHERE TripID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, AppConstants.TripStatus.IN_TRANSIT.toDbString());
            ps.setInt(2, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generic status and location update — called when a vessel arrives.
     */
    public boolean updateTripStatusAndLocation(int tripId, String status, int currentStationId) {
        String sql = "UPDATE trip SET TripStatus = ?, CurrentStationID = ? WHERE TripID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.toUpperCase());
            ps.setInt(2, currentStationId);
            ps.setInt(3, tripId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}