package com.arko.model.DAO;

import com.arko.model.POJO.Vessel;
import com.arko.model.database.DBConnection;
import com.arko.utils.OperationalDashboard.AppConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VesselDAO {

    // --- CREATE ---
    public boolean createVessel(Vessel vessel) {
        String sql = "INSERT INTO vessel (VesselName, MaxCapacity, VesselStatus, CurrentLoad) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vessel.getVesselName());
            ps.setInt(2, vessel.getMaxCapacity());
            ps.setString(3, vessel.getVesselStatus());
            ps.setInt(4, vessel.getCurrentLoad());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- READ ---
    public List<Vessel> getAllVessels() {
        List<Vessel> vessels = new ArrayList<>();
        String sql = "SELECT * FROM vessel ORDER BY VesselID ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) vessels.add(mapResultSetToVessel(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vessels;
    }

    // --- DELETE ---
    public boolean deleteVessel(int vesselID) {
        String sql = "DELETE FROM vessel WHERE VesselID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vesselID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches vessels visible to a station for the Control Panel dropdown.
     *
     * A vessel is shown if ANY of these conditions are true:
     *   1. It is already at this exact station (CurrentStationID = stationId)
     *   2. It is UPSTREAM and its CurrentStationID > stationId
     *      — it is somewhere downstream of us, travelling toward lower-numbered stations
     *   3. It is DOWNSTREAM and its CurrentStationID < stationId
     *      — it is somewhere upstream of us, travelling toward higher-numbered stations
     *   4. It has no active trip and VesselStatus = 'Operational'
     *      — idle vessel that any station can claim
     *
     * This covers cases where a vessel skips intermediate stations:
     * e.g. a vessel at HUL(8) going UPSTREAM is visible at VAL(7), QUI(6), QUB(5) etc.
     */
    public List<Vessel> getVesselsForStation(int stationId) {
        List<Vessel> list = new ArrayList<>();
        String sql = "SELECT v.*, t.TripID, t.TripDirection " +   // Added TripDirection
                "FROM vessel v " +
                "LEFT JOIN trip t ON v.VesselID = t.VesselID " +
                "AND t.TripStatus != ? " +
                "WHERE t.CurrentStationID = ? " +                                    // Already here
                "OR (t.TripDirection = 'UPSTREAM'   AND t.CurrentStationID > ?) " + // Heading toward lower IDs
                "OR (t.TripDirection = 'DOWNSTREAM' AND t.CurrentStationID < ?) " + // Heading toward higher IDs
                "OR (t.TripID IS NULL AND v.VesselStatus = 'Operational')";          // Idle vessels

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, AppConstants.TripStatus.COMPLETED.name());
            ps.setInt(2, stationId);  // Exact match
            ps.setInt(3, stationId);  // UPSTREAM condition
            ps.setInt(4, stationId);  // DOWNSTREAM condition
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vessel v = new Vessel();
                v.setVesselID(rs.getInt("VesselID"));
                v.setVesselName(rs.getString("VesselName"));
                v.setMaxCapacity(rs.getInt("MaxCapacity"));
                v.setVesselStatus(rs.getString("VesselStatus"));
                v.setCurrentLoad(rs.getInt("CurrentLoad"));
                v.setCurrentTripID(rs.getInt("TripID"));
                v.setTripDirection(rs.getString("TripDirection")); // Populated for direction-aware boarding
                list.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- UPDATE ---
    public boolean updateVessel(Vessel vessel) {
        String sql = "UPDATE vessel SET VesselName = ?, MaxCapacity = ?, " +
                "VesselStatus = ?, CurrentLoad = ? WHERE VesselID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vessel.getVesselName());
            ps.setInt(2, vessel.getMaxCapacity());
            ps.setString(3, vessel.getVesselStatus());
            ps.setInt(4, vessel.getCurrentLoad());
            ps.setInt(5, vessel.getVesselID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Connection-aware overload for use inside a TransactionRunner block.
     * Called from PassengerManifestController.handleArrival() so that
     * markAsArrived + decrementVesselLoad share one atomic transaction.
     */
    public void decrementVesselLoad(int vesselId, Connection conn) throws SQLException {
        String sql = "UPDATE vessel SET CurrentLoad = GREATEST(CurrentLoad - 1, 0) WHERE VesselID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vesselId);
            ps.executeUpdate();
        }
    }

    /**
     * Resets vessel load to 0 after a completed trip.
     * Called by ControlPanelController after terminus trip completion,
     * when all passengers have alighted and the vessel becomes idle.
     */
    public boolean resetVesselLoad(int vesselId) {
        String sql = "UPDATE vessel SET CurrentLoad = 0 WHERE VesselID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vesselId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increments vessel load by n when passengers board.
     */
    public boolean incrementVesselLoad(int vesselId, int count) {
        String sql = "UPDATE vessel SET CurrentLoad = CurrentLoad + ? WHERE VesselID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, count);
            ps.setInt(2, vesselId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- HELPER ---
    private Vessel mapResultSetToVessel(ResultSet rs) throws SQLException {
        Vessel v = new Vessel();
        v.setVesselID(rs.getInt("VesselID"));
        v.setVesselName(rs.getString("VesselName"));
        v.setMaxCapacity(rs.getInt("MaxCapacity"));
        v.setVesselStatus(rs.getString("VesselStatus"));
        v.setCurrentLoad(rs.getInt("CurrentLoad"));
        return v;
    }
}