package com.arko.model.DAO;

import com.arko.model.POJO.Station;
import com.arko.model.database.DBConnection;
import com.arko.utils.OperationalDashboard.AppConstants;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StationDAO {

    // --- CREATE ---
    public boolean createStation(Station station) {
        String sql = "INSERT INTO station (StationName, StationCode) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, station.getStationName());
            ps.setString(2, station.getStationCode());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- READ ---
    public List<Station> getAllStations() {
        List<Station> stations = new ArrayList<>();
        String sql = "SELECT * FROM station ORDER BY StationID ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) stations.add(mapResultSetToStation(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stations;
    }

    public Station getStationById(int id) {
        String sql = "SELECT * FROM station WHERE StationID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToStation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //HELPER TO POPULATE JCOMBO IN INPUT FORM CONTROLLER
    public List<String> getAllStationCodes() {
        List<String> codes = new ArrayList<>();
        String sql = "SELECT StationCode FROM station ORDER BY StationID ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) codes.add(rs.getString("StationCode"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return codes;
    }

    /**
     * Returns the ordered list of station IDs that remain ahead of the vessel
     * in its current direction of travel.
     *
     * DOWNSTREAM: stations with StationID > currentStationId (ascending)
     * UPSTREAM:   stations with StationID < currentStationId (descending)
     *
     * Used by BoardingCalculation to identify S_future (downstream stations)
     * for the reservation sum in the boarding limit formula.
     */

    // USED IN BOARDING CALCULATION
    public List<Integer> getRemainingStationIds(int currentStationId, String direction) {
        List<Integer> ids = new ArrayList<>();

        String sql = "DOWNSTREAM".equalsIgnoreCase(direction)
                ? "SELECT StationID FROM station WHERE StationID > ? ORDER BY StationID ASC"
                : "SELECT StationID FROM station WHERE StationID < ? ORDER BY StationID DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentStationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("StationID"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * Returns a map of stationId → waiting passenger count for all stations
     * ahead of the vessel in a single batch query.
     *
     * Only counts passengers whose PassengerDirection matches the vessel direction —
     * upstream passengers at downstream stations are irrelevant for boarding planning.
     *
     * This replaces N individual queries (one per downstream station) with one,
     * keeping the boarding limit calculation efficient even on a remote DB.
     *
     * @param remainingStationIds  ordered list from getRemainingStationIds()
     * @param direction            "UPSTREAM" or "DOWNSTREAM"
     * @return map of StationID → count (stations with 0 waiting are absent from the map)
     */
    public Map<Integer, Integer> getDownstreamWaitingCounts(
            List<Integer> remainingStationIds, String direction) {

        Map<Integer, Integer> counts = new HashMap<>();
        if (remainingStationIds == null || remainingStationIds.isEmpty()) return counts;

        // Build IN clause: (?, ?, ?, ...)
        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < remainingStationIds.size(); i++) {
            inClause.append(i == 0 ? "?" : ", ?");
        }

        String sql = "SELECT OriginStationID, COUNT(*) AS WaitingCount " +
                "FROM passengers " +
                "WHERE PassengerStatus = ? " +
                "AND PassengerDirection = ? " +
                "AND OriginStationID IN (" + inClause + ") " +
                "GROUP BY OriginStationID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, AppConstants.PassengerStatus.WAITING.name());
            ps.setString(2, direction);
            int paramIdx = 3;
            for (int stationId : remainingStationIds) {
                ps.setInt(paramIdx++, stationId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                counts.put(rs.getInt("OriginStationID"), rs.getInt("WaitingCount"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    // --- UPDATE ---
    public boolean updateStation(Station station) {
        String sql = "UPDATE station SET StationName = ?, StationCode = ?, " +
                "OperationalStatus = ? WHERE StationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, station.getStationName());
            ps.setString(2, station.getStationCode());
            ps.setString(3, station.getOperationalStatus());
            ps.setInt(4, station.getStationID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- DELETE ---
    public boolean deleteStation(int stationId) {
        String sql = "DELETE FROM station WHERE StationID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves waiting passenger counts grouped by origin and destination.
     * Used by the Distribution Matrix.
     */
    public List<int[]> getWaitingDistribution() {
        List<int[]> distribution = new ArrayList<>();
        String sql = "SELECT OriginStationID, DestinationStationID, COUNT(*) AS WaitingCount " +
                "FROM passengers " +
                "WHERE PassengerStatus = ? " +
                "GROUP BY OriginStationID, DestinationStationID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, AppConstants.PassengerStatus.WAITING.name());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                distribution.add(new int[]{
                        rs.getInt("OriginStationID"),
                        rs.getInt("DestinationStationID"),
                        rs.getInt("WaitingCount")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return distribution;
    }

    /**
     * Retrieves the total number of stations in the database.
     * Useful for dynamically determining the midpoint or terminus boundaries.
     * * @return the total count of stations, or 0 if an error occurs.
     */
    public int getTotalStationCount() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM station";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // --- HELPER ---
    private Station mapResultSetToStation(ResultSet rs) throws SQLException {
        Station s = new Station();
        s.setStationID(rs.getInt("StationID"));
        s.setStationName(rs.getString("StationName"));
        s.setStationCode(rs.getString("StationCode"));
        s.setOperationalStatus(rs.getString("OperationalStatus"));
        return s;
    }
}