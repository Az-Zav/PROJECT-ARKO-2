/**
 *
 */

package com.arko.model.DAO;

import com.arko.model.database.DBConnection;
import com.arko.model.POJO.Staff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    //FOR MANAGE USERS FEATURE;
    //READ
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        // LEFT JOIN ensures we still see Admins even if their StationID is NULL
        String sql = "SELECT s.*, st.StationCode " +
                "FROM Staff s " +
                "LEFT JOIN station st ON s.StationID = st.StationID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Staff s = new Staff();
                s.setStaffID(rs.getInt("StaffID"));
                s.setUsername(rs.getString("Username"));
                s.setFirstName(rs.getString("FirstName"));
                s.setLastName(rs.getString("LastName"));
                s.setRole(rs.getString("Role"));
                s.setEmail(rs.getString("Email"));
                s.setStationID(rs.getInt("StationID"));

                // Map the joined column
                s.setStationCode(rs.getString("StationCode"));

                staffList.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    //CREATE
    public boolean insertStaff(Staff s) {
        String sql = "INSERT INTO Staff (Username, Password, FirstName, LastName, " +
                "Email, ContactNumber, Role, StationID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getUsername());
            ps.setString(2, s.getPassword());
            ps.setString(3, s.getFirstName());
            ps.setString(4, s.getLastName());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getContactNumber());
            ps.setString(7, s.getRole());
            ps.setInt(8, s.getStationID());

            if (ps.executeUpdate() > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        s.setStaffID(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //UPDATE
    public boolean updateStaff(Staff s) {
        String sql = "UPDATE Staff SET Username = ?, FirstName = ?, LastName = ?, " +
                "Email = ?, ContactNumber = ?, Role = ?, StationID = ? " +
                "WHERE StaffID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.getUsername());
            ps.setString(2, s.getFirstName());
            ps.setString(3, s.getLastName());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getContactNumber());
            ps.setString(6, s.getRole());

            // FIX: Handle the -1 (Global) case
            if (s.getStationID() == -1) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, s.getStationID());
            }

            ps.setInt(8, s.getStaffID());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //DELETE
    public boolean deleteStaff(int staffID) {
        String sql = "DELETE FROM Staff WHERE StaffID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ── Called by AuthController.verifyCredentials() ──────────────
    public Staff findByUsername(String username) throws SQLException {
        String sql = "SELECT s.*, st.StationCode " +
                "FROM Staff s " +
                "LEFT JOIN station st ON s.StationID = st.StationID " +
                "WHERE s.Username = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Staff staff = new Staff();
                staff.setStaffID(rs.getInt("StaffID"));
                staff.setUsername(rs.getString("Username"));
                staff.setPassword(rs.getString("Password"));
                staff.setFirstName(rs.getString("FirstName"));
                staff.setLastName(rs.getString("LastName"));
                staff.setContactNumber(rs.getString("ContactNumber"));
                staff.setRole(rs.getString("Role"));
                staff.setEmail(rs.getString("Email"));
                staff.setStationID(rs.getInt("StationID"));
                staff.setStationCode(rs.getString("StationCode"));
                return staff;
            }
        }
    }

    // ── Called by AuthController.updatePassword() ─────────────────
    public boolean updatePassword(int staffId, String hashedPassword) throws SQLException {
        String sql = "UPDATE Staff SET Password = ? WHERE StaffID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setInt(2, staffId);
            return ps.executeUpdate() > 0;
        }
    }

    // HELPER METHOD TO MAP RESULTSET TO OBJECT
    private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffID(rs.getInt("StaffID"));
        s.setUsername(rs.getString("Username"));
        s.setFirstName(rs.getString("FirstName"));
        s.setLastName(rs.getString("LastName"));
        s.setContactNumber(rs.getString("ContactNumber"));
        s.setRole(rs.getString("Role"));
        s.setStationID(rs.getInt("StationID"));
        s.setEmail(rs.getString("Email"));

        // Check if StationCode was included in the SQL JOIN
        // This prevents errors if you call a query that doesn't have the JOIN
        try {
            s.setStationCode(rs.getString("StationCode"));
        } catch (SQLException e) {
            // If StationCode isn't in the ResultSet, just skip it
        }

        return s;
    }
}