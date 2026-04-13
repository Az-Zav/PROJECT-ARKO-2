package com.arko.model.DAO;

import com.arko.model.POJO.Passenger;
import com.arko.model.POJO.Trip;
import com.arko.model.database.DBConnection;
import com.arko.model.database.TransactionRunner;
import com.arko.utils.OperationalDashboard.AppConstants;
import com.arko.utils.SessionManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * DAO Class for passenger entity,
 * contains DATA ACCESS METHODS (SQL QUERIES) FOR THE DATABASE
 */

public class PassengerDAO {

// ──────────────────────────────────OPERATIONAL DASHBOARD METHODS──────────────────────────────────

    //──────────────────────────────────INPUT FORM PANEL──────────────────────────────────

    /**
     *inserts passenger object into the database
     *
     * @param p passenger object packaged from extracted form fields
     * @return passenger object
     */
    public Passenger insertPassenger(Passenger p) {         //prepared statement sql query
        String sql = "INSERT INTO passengers (FirstName, LastName, MiddleInitial, ContactNumber, " +
                "Age, Sex, PassengerDirection, PassengerStatus, Classification, " +
                "BoardingCode, OriginStationID, DestinationStationID, StaffStampID, RegistrationTimeStamp) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)"; // RegistrationTimestamp filled automatically in the query

        try (Connection conn = DBConnection.getConnection()) {  //try-with resources call for db connection
            int originId = SessionManager.getInstance().getCurrentStationId(); //fetched from session manager
            int staffId  = SessionManager.getInstance().getCurrentStaffId();    // fetched from session manager

            String direction = (p.getDestinationStationID() > originId) ? "DOWNSTREAM" : "UPSTREAM"; // compare desintationID with originID to determine direction
            String bCode     = generateBoardingCode(conn, originId);

            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); //Stores sql in PreparedStatement object and returns autogenerate keys (PassengerID)

            //Sets the '?' placeholders in the sql query
            // first argument = position of '?' placeholder, second parameter is getters to extract fields from passed passenger object
            ps.setString(1, p.getFirstName());
            ps.setString(2, p.getLastName());
            ps.setString(3, p.getMiddleInitial());
            ps.setString(4, p.getContactNumber());
            ps.setInt(5, p.getAge());
            ps.setString(6, String.valueOf(p.getSex()));
            ps.setString(7, direction);
            ps.setString(8, AppConstants.PassengerStatus.WAITING.name());
            ps.setString(9, p.getClassification());
            ps.setString(10, bCode);
            ps.setInt(11, originId);
            ps.setInt(12, p.getDestinationStationID());
            ps.setInt(13, staffId);

            int affectedRows = ps.executeUpdate(); //returns 1 if successful execution in db
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) { //checks next line
                        //store additional information to local Passenger POJO
                        p.setPassengerID(rs.getInt(1));
                        p.setPassengerDirection(direction);
                        p.setBoardingCode(bCode);
                        p.setPassengerStatus(AppConstants.PassengerStatus.WAITING.name());
                        return p; // outputs passenger object filled
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Helper method to create boarding code (e.g. GUA-2026-3-31-001)
     *
     * @param conn connection object
     * @param stationId
     * @return concatenated boarding code
     * @throws SQLException
     */
    private String generateBoardingCode(Connection conn, int stationId) throws SQLException {
        String sCode    = SessionManager.getInstance().getCurrentStationCode(); //fetch from session manager
        String datePart = LocalDate.now().toString(); // local method to return current date in YYYY-MM-DD format

        String countSql = "SELECT COUNT(*) FROM passengers " +
                "WHERE OriginStationID = ? AND DATE(RegistrationTimeStamp) = CURDATE()";    // count passengers saved in db registered in current date
        int count = 1;

        try (PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setInt(1, stationId);    //pass stationID to query for station filtering
            ResultSet rs = ps.executeQuery();
            if (rs.next()) count = rs.getInt(1) + 1;    //for last 3-digit identifier in bcode that refreshes daily
        }
        return String.format("%s-%s-%03d", sCode, datePart, count); //concatenated boarding code
    }



    //──────────────────────────────────PASSENGER WAITLIST PANEL──────────────────────────────────

    /**
     * Queries passengers based on direction
     *
     * @param originId
     * @param isDownstream
     * @return ArrayList of passenger object
     */
    public List<Passenger> getWaitlistByDirection(int originId, boolean isDownstream) {
        List<Passenger> list = new ArrayList<>();
        String direction = isDownstream ? "DOWNSTREAM" : "UPSTREAM";

        //JOINS PASSENGER TO STATION CODE WHERE DESTINATIONSTATIONID = STATIONID
        String sql = "SELECT p.*, s.StationCode " +
                "FROM passengers p " +
                "JOIN station s ON p.DestinationStationID = s.StationID " +
                "WHERE p.OriginStationID = ? " +
                "AND p.PassengerDirection = ? " +
                "AND p.PassengerStatus = ? " +
                "ORDER BY p.RegistrationTimeStamp ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, originId);
            ps.setString(2, direction);
            ps.setString(3, AppConstants.PassengerStatus.WAITING.name());
            ResultSet rs = ps.executeQuery();

            // STORE QUERIED DATA IN RS AND TRANSFER TO PASSEMGER OBJECT
            while (rs.next()) {
                Passenger p = new Passenger();
                p.setPassengerID(rs.getInt("PassengerID"));
                p.setFirstName(rs.getString("FirstName"));
                p.setLastName(rs.getString("LastName"));
                p.setMiddleInitial(rs.getString("MiddleInitial"));
                p.setBoardingCode(rs.getString("BoardingCode"));
                p.setDestinationStationID(rs.getInt("DestinationStationID"));
                p.setDestinationCode(rs.getString("StationCode"));
                p.setPassengerStatus(rs.getString("PassengerStatus"));
                p.setPassengerDirection(rs.getString("PassengerDirection"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     *
     * UPDATE WAITING PASSENGERS' STATUS TO IN TRANSIT AND INCREMENTS VESSEL LOAD IN DB
     *
     * @param passengerId
     * @param tripId
     * @param vesselId
     * @param maxCapacity
     * @return  boolean if successful or failed
     */
    public boolean boardSinglePassengerAtomic(int passengerId, int tripId,
                                              int vesselId, int maxCapacity) {
        // Passenger UPDATE — only succeeds if still WAITING and vessel has room
        String boardSql =
                "UPDATE passengers SET PassengerStatus = ?, TripID = ? " +
                        "WHERE PassengerID = ? " +
                        "AND PassengerStatus = ? " +
                        "AND (SELECT CurrentLoad FROM vessel WHERE VesselID = ? AND IsActive = 1) < ?";

        // Vessel load UPDATE — runs only if passenger UPDATE succeeded
        String loadSql =
                "UPDATE vessel SET CurrentLoad = CurrentLoad + 1 WHERE VesselID = ?";

        final boolean[] boarded = {false};

        try {
            TransactionRunner.run(conn -> {
                // 1. Attempt to board the passenger with capacity guard
                try (PreparedStatement ps = conn.prepareStatement(boardSql)) {
                    ps.setString(1, AppConstants.PassengerStatus.BOARDED.name());
                    ps.setInt(2, tripId);
                    ps.setInt(3, passengerId);
                    ps.setString(4, AppConstants.PassengerStatus.WAITING.name());
                    ps.setInt(5, vesselId);
                    ps.setInt(6, maxCapacity);

                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        // Either already boarded or vessel full — abort cleanly
                        // TransactionRunner will roll back since we throw here
                        throw new SQLException("BOARD_REJECTED");
                    }
                }

                // 2. Increment vessel load only if boarding succeeded
                try (PreparedStatement ps = conn.prepareStatement(loadSql)) {
                    ps.setInt(1, vesselId);
                    ps.executeUpdate();
                }

                boarded[0] = true;
            });
        } catch (SQLException e) {
            if (!"BOARD_REJECTED".equals(e.getMessage())) {
                // Unexpected error — log it
                System.err.println("Error in boardSinglePassengerAtomic: " + e.getMessage());
                e.printStackTrace();
            }
            // BOARD_REJECTED is a controlled flow — no logging needed
        }

        return boarded[0];
    }

    /**
     * Fetches all passengers on a specific trip for the Manifest UI.
     */
    public List<Passenger> getManifestForTrip(int tripId) {
        List<Passenger> manifest = new ArrayList<>();

        String sql = "SELECT p.*, " +
                "CONCAT(p.FirstName, ' ', p.MiddleInitial, '. ', p.LastName) AS FullName, " +
                "os.StationCode AS OriginCode, " +
                "ds.StationCode AS DestCode " +
                "FROM passengers p " +
                "JOIN station os ON p.OriginStationID      = os.StationID " +
                "JOIN station ds ON p.DestinationStationID = ds.StationID " +
                "WHERE p.TripID = ? " +
                "ORDER BY p.RegistrationTimeStamp ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Passenger p = new Passenger();
                p.setPassengerID(rs.getInt("PassengerID"));
                p.setBoardingCode(rs.getString("BoardingCode"));
                p.setFullName(rs.getString("FullName"));
                p.setPassengerDirection(rs.getString("PassengerDirection"));
                p.setOriginStationID(rs.getInt("OriginStationID"));
                p.setDestinationStationID(rs.getInt("DestinationStationID"));
                p.setPassengerStatus(rs.getString("PassengerStatus"));
                p.setOriginCode(rs.getString("OriginCode"));
                p.setDestinationCode(rs.getString("DestCode"));
                manifest.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return manifest;
    }

    /**
     * Updates passenger status to ARRIVED and sets the completion timestamp.
     * Called inside a transaction from PassengerManifestController.
     */
    public boolean markAsArrived(int passengerId, Connection conn) throws SQLException {
        String sql = "UPDATE passengers SET PassengerStatus = ?, " +
                "CompletionTimeStamp = CURRENT_TIMESTAMP WHERE PassengerID = ? AND PassengerStatus = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, AppConstants.PassengerStatus.ARRIVED.name());
            ps.setInt(2, passengerId);
            ps.setString(3, AppConstants.PassengerStatus.BOARDED.name());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Marks a passenger arrived at {@code alightStationId}, updating destination and direction when
     * alighting away from the booked destination. Must be called inside an existing transaction.
     */
    public boolean markAsArrivedAtStation(int passengerId, int alightStationId, int originStationId,
                                          Connection conn) throws SQLException {
        String direction = (alightStationId > originStationId) ? "DOWNSTREAM" : "UPSTREAM";
        String sql = "UPDATE passengers SET PassengerStatus = ?, CompletionTimeStamp = CURRENT_TIMESTAMP, " +
                "DestinationStationID = ?, PassengerDirection = ? " +
                "WHERE PassengerID = ? AND PassengerStatus = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, AppConstants.PassengerStatus.ARRIVED.name());
            ps.setInt(2, alightStationId);
            ps.setString(3, direction);
            ps.setInt(4, passengerId);
            ps.setString(5, AppConstants.PassengerStatus.BOARDED.name());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Returns the number of passengers already BOARDED on a given trip.
     * Used by BoardingSession.seedBoardedCount() on ARRIVED to resume
     * an accurate counter after a crash or restart mid-session.
     */
    public int getBoardedCountForTrip(int tripId) {
        String sql = "SELECT COUNT(*) FROM passengers " +
                "WHERE TripID = ? AND PassengerStatus = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            ps.setString(2, AppConstants.PassengerStatus.BOARDED.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── REPORTS QUERIES ──────────────────────────────────

    /**
     * Checks if any ARRIVED passengers exist for a given date.
     * Used by ReportsController on first load to decide whether to show
     * today's data or fall back to yesterday.
     */
    public boolean hasArrivedDataForDate(LocalDate date) {
        String sql =    "SELECT COUNT(*) FROM passengers " +
                "WHERE PassengerStatus = 'ARRIVED' " +
                "AND DATE(CompletionTimeStamp) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns ridership counts grouped by time unit for the timeline bar chart.
     *
     * Each int[] in the list is {timeUnit, count}:
     *   DAILY   → timeUnit = HOUR(CompletionTimeStamp),    0–23
     *   WEEKLY  → timeUnit = DAYOFWEEK(CompletionTimeStamp), 2–7 (Mon=2, Sat=7)
     *   MONTHLY → timeUnit = WEEK(CompletionTimeStamp),    ISO week number
     *   YEARLY  → timeUnit = MONTH(CompletionTimeStamp),   1–12
     *
     * stationId = -1 means all stations (no WHERE on OriginStationID).
     * Only rows within [startDate, endDate] are counted.
     */
    public ArrayList<int[]> getRidershipTimeline(
            LocalDate startDate, LocalDate endDate,
            String periodType, int stationId) {

        ArrayList<int[]> results = new ArrayList<>();

        // Choose the SQL grouping function based on period type
        String timeUnitExpr;
        if      ("DAILY".equals(periodType))   timeUnitExpr = "HOUR(CompletionTimeStamp)";
        else if ("WEEKLY".equals(periodType))  timeUnitExpr = "DAYOFWEEK(CompletionTimeStamp)";
        else if ("MONTHLY".equals(periodType)) timeUnitExpr = "WEEK(CompletionTimeStamp)";
        else                                   timeUnitExpr = "MONTH(CompletionTimeStamp)";

        // Two SQL strings — one filtered by station, one not.
        // We cannot pass NULL as a parameter to skip a WHERE clause in JDBC,
        // so we branch in Java instead.
        String sql;
        if (stationId == -1) {
            sql = "SELECT " + timeUnitExpr + " AS timeUnit, COUNT(*) AS count " +
                    "FROM passengers " +
                    "WHERE PassengerStatus = 'ARRIVED' " +
                    "AND DATE(CompletionTimeStamp) BETWEEN ? AND ? " +
                    "GROUP BY timeUnit ORDER BY timeUnit";
        } else {
            sql = "SELECT " + timeUnitExpr + " AS timeUnit, COUNT(*) AS count " +
                    "FROM passengers " +
                    "WHERE PassengerStatus = 'ARRIVED' " +
                    "AND DATE(CompletionTimeStamp) BETWEEN ? AND ? " +
                    "AND OriginStationID = ? " +
                    "GROUP BY timeUnit ORDER BY timeUnit";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            if (stationId != -1) ps.setInt(3, stationId); // replaces ? in query for specific station

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(new int[]{ rs.getInt("timeUnit"), rs.getInt("count") });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Returns passenger counts grouped by Classification for the donut chart.
     *
     * Each String[] in the list is {classification, count}.
     * Respects the station filter via OriginStationID.
     * stationId = -1 means all stations.
     */
    public ArrayList<String[]> getClassificationBreakdown(
            LocalDate startDate, LocalDate endDate, int stationId) {

        ArrayList<String[]> results = new ArrayList<>();

        String sql;
        if (stationId == -1) {
            sql = "SELECT Classification, COUNT(*) AS count " +
                    "FROM passengers " +
                    "WHERE PassengerStatus = 'ARRIVED' " +
                    "AND DATE(CompletionTimeStamp) BETWEEN ? AND ? " +
                    "GROUP BY Classification";
        } else {
            sql = "SELECT Classification, COUNT(*) AS count " +
                    "FROM passengers " +
                    "WHERE PassengerStatus = 'ARRIVED' " +
                    "AND DATE(CompletionTimeStamp) BETWEEN ? AND ? " +
                    "AND OriginStationID = ? " +
                    "GROUP BY Classification";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            if (stationId != -1) ps.setInt(3, stationId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                results.add(new String[]{
                        rs.getString("Classification"),
                        rs.getString("count")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Returns boarding and alighting counts per station for the grouped bar chart.
     *
     * Runs two queries — one on OriginStationID (boarding) and one on
     * DestinationStationID (alighting) — then returns both lists separately.
     * The controller merges them into a single dataset by station name.
     *
     * Each String[] is {stationName, count}.
     * stationId = -1 means all stations (admin view).
     * stationId = specific ID means single station (staff view).
     *
     * Result is a two-element ArrayList<ArrayList<String[]>>:
     *   index 0 = boarding rows
     *   index 1 = alighting rows
     */
    /**
     * Returns boarding and alighting counts per station for the grouped bar chart.
     * Updated to comply with MySQL ONLY_FULL_GROUP_BY mode.
     */
    public ArrayList<ArrayList<String[]>> getBoardingAlightingData(
            LocalDate startDate, LocalDate endDate, int stationId) {

        ArrayList<String[]> boardingRows  = new ArrayList<>();
        ArrayList<String[]> alightingRows = new ArrayList<>();

        // ── Boarding query (OriginStationID) ─────────────────────────────────
        String boardingSql;
        if (stationId == -1) {
            // UPDATED: Added s.StationID to GROUP BY to allow ORDER BY s.StationID
            boardingSql = "SELECT s.StationName, COUNT(*) AS count " +
                    "FROM passengers p " +
                    "JOIN station s ON p.OriginStationID = s.StationID " +
                    "WHERE p.PassengerStatus = 'ARRIVED' " +
                    "AND DATE(p.CompletionTimeStamp) BETWEEN ? AND ? " +
                    "GROUP BY s.StationID, s.StationName ORDER BY s.StationID";
        } else {
            boardingSql = "SELECT s.StationName, COUNT(*) AS count " +
                    "FROM passengers p " +
                    "JOIN station s ON p.OriginStationID = s.StationID " +
                    "WHERE p.PassengerStatus = 'ARRIVED' " +
                    "AND DATE(p.CompletionTimeStamp) BETWEEN ? AND ? " +
                    "AND p.OriginStationID = ? " +
                    "GROUP BY s.StationName";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(boardingSql)) {

            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            if (stationId != -1) ps.setInt(3, stationId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                boardingRows.add(new String[]{
                        rs.getString("StationName"),
                        rs.getString("count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ── Alighting query (DestinationStationID) ────────────────────────────
        String alightingSql;
        if (stationId == -1) {
            // UPDATED: Added s.StationID to GROUP BY to allow ORDER BY s.StationID
            alightingSql = "SELECT s.StationName, COUNT(*) AS count " +
                    "FROM passengers p " +
                    "JOIN station s ON p.DestinationStationID = s.StationID " +
                    "WHERE p.PassengerStatus = 'ARRIVED' " +
                    "AND DATE(p.CompletionTimeStamp) BETWEEN ? AND ? " +
                    "GROUP BY s.StationID, s.StationName ORDER BY s.StationID";
        } else {
            alightingSql = "SELECT s.StationName, COUNT(*) AS count " +
                    "FROM passengers p " +
                    "JOIN station s ON p.DestinationStationID = s.StationID " +
                    "WHERE p.PassengerStatus = 'ARRIVED' " +
                    "AND DATE(p.CompletionTimeStamp) BETWEEN ? AND ? " +
                    "AND p.DestinationStationID = ? " +
                    "GROUP BY s.StationName";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(alightingSql)) {

            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            if (stationId != -1) ps.setInt(3, stationId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                alightingRows.add(new String[]{
                        rs.getString("StationName"),
                        rs.getString("count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<ArrayList<String[]>> combined = new ArrayList<>();
        combined.add(boardingRows);
        combined.add(alightingRows);
        return combined;
    }

    // ── TRIP MANIFEST QUERIES ─────────────────────────────────────────────────

    /**
     * Returns all trips whose DepartureTime falls within the date window.
     * JOINs vessel for the name. stationId = -1 means all stations.
     *
     * Each Trip carries: tripID, tripDirection, tripStatus,
     * departureTime, vesselName, and a passenger count.
     */
    public ArrayList<Trip> getTripsForDateRange(
            LocalDate startDate, LocalDate endDate, int stationId) {

        ArrayList<Trip> results = new ArrayList<>();

        String sql;
        if (stationId == -1) {
            // Admin: all trips in the date window
            sql = "SELECT t.TripID, t.TripDirection, t.TripStatus, " +
                    "       t.DepartureTime, v.VesselName, " +
                    "       COUNT(p.PassengerID) AS passengerCount " +
                    "FROM trip t " +
                    "JOIN vessel v ON t.VesselID = v.VesselID " +
                    "LEFT JOIN passengers p ON p.TripID = t.TripID " +
                    "WHERE DATE(t.DepartureTime) BETWEEN ? AND ? " +
                    "GROUP BY t.TripID, t.TripDirection, t.TripStatus, " +
                    "         t.DepartureTime, v.VesselName " +
                    "ORDER BY t.DepartureTime ASC";
        } else {
            // Staff: only trips where their station was an origin OR destination
            sql = "SELECT t.TripID, t.TripDirection, t.TripStatus, " +
                    "       t.DepartureTime, v.VesselName, " +
                    "       COUNT(p.PassengerID) AS passengerCount " +
                    "FROM trip t " +
                    "JOIN vessel v ON t.VesselID = v.VesselID " +
                    "LEFT JOIN passengers p ON p.TripID = t.TripID " +
                    "WHERE DATE(t.DepartureTime) BETWEEN ? AND ? " +
                    "AND t.TripID IN ( " +
                    "    SELECT DISTINCT TripID FROM passengers " +
                    "    WHERE OriginStationID = ? OR DestinationStationID = ? " +
                    ") " +
                    "GROUP BY t.TripID, t.TripDirection, t.TripStatus, " +
                    "         t.DepartureTime, v.VesselName " +
                    "ORDER BY t.DepartureTime ASC";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            if (stationId != -1) {
                ps.setInt(3, stationId);
                ps.setInt(4, stationId); // same value for OR condition
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Trip t = new Trip();
                t.setTripID(rs.getInt("TripID"));
                t.setTripDirection(rs.getString("TripDirection"));
                t.setTripStatus(rs.getString("TripStatus"));
                t.setDepartureTime(rs.getTimestamp("DepartureTime"));
                t.setVesselName(rs.getString("VesselName"));
                t.setCurrentLoad(rs.getInt("passengerCount"));
                results.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Returns all passengers for a given trip for the detail table.
     * Reuses the same mapping as getManifestForTrip() but also pulls
     * Classification so the reports view can display it.
     */
    public List<Passenger> getPassengersForTrip(int tripId) {
        List<Passenger> list = new ArrayList<>();

        String sql = "SELECT p.PassengerID, p.BoardingCode, p.Classification, " +
                "       p.PassengerStatus, p.PassengerDirection, " +
                "       CONCAT(p.FirstName, ' ', p.MiddleInitial, '. ', p.LastName) AS FullName, " +
                "       os.StationCode AS OriginCode, " +
                "       ds.StationCode AS DestCode " +
                "FROM passengers p " +
                "JOIN station os ON p.OriginStationID      = os.StationID " +
                "JOIN station ds ON p.DestinationStationID = ds.StationID " +
                "WHERE p.TripID = ? " +
                "ORDER BY p.RegistrationTimeStamp ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Passenger p = new Passenger();
                p.setPassengerID(rs.getInt("PassengerID"));
                p.setBoardingCode(rs.getString("BoardingCode"));
                p.setFullName(rs.getString("FullName"));
                p.setClassification(rs.getString("Classification"));
                p.setPassengerStatus(rs.getString("PassengerStatus"));
                p.setPassengerDirection(rs.getString("PassengerDirection"));
                p.setOriginCode(rs.getString("OriginCode"));
                p.setDestinationCode(rs.getString("DestCode"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


}