/**
 *
 */

package com.arko.model.DAO;

import com.arko.model.database.DBConnection;
import com.arko.model.POJO.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffDAO {


    // ── Called by AuthController.verifyCredentials() ──────────────
    public Staff findByUsername(String username) throws SQLException {
        String sql = "SELECT StaffID, Username, Password, FirstName, LastName, Role, Email " +
                "FROM Staff WHERE Username = ? LIMIT 1";

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
                staff.setRole(rs.getString("Role"));
                staff.setEmail(rs.getString("Email"));
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
}