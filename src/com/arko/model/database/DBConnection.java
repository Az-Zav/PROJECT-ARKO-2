package com.arko.model.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection factory.
 *
 * Returns a fresh, independent connection on every call.
 * Each caller is responsible for closing it — all DAOs already do this
 * correctly via try-with-resources, which closes the connection automatically.
 *
 * Why fresh per call instead of a shared singleton:
 *   A single shared Connection is not thread-safe. Concurrent DAO calls
 *   from timers, user actions, or multiple stations can corrupt each other's
 *   queries on a shared connection. Fresh connections eliminate this entirely.
 *
 * Why this is safe on LAN:
 *   LAN round-trip is <1ms, so connection setup overhead is negligible.
 *   MySQL also manages its own internal connection recycling on the server side.
 *
 * For future migration to cloud DB:
 *   If connection overhead becomes a concern (e.g. Aiven free tier with
 *   100-300ms latency), replace this class with a HikariCP pool.
 *   All DAOs use try-with-resources and will work with HikariCP unchanged.
 */
public final class DBConnection {

    private DBConnection() {}

    /**
     * Opens and returns a new database connection using settings from DBConfig.
     * The caller MUST close this connection — use try-with-resources.
     *
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found.", e);
        }
        return DriverManager.getConnection(DBConfig.URL, DBConfig.USER, DBConfig.PASSWORD);
    }

    /**
     * No-op stub retained so Main.java's WindowAdapter call compiles cleanly.
     * With fresh-per-call connections there is no persistent connection to close.
     */
    public static void closeConnection() {
        // No persistent connection to close — each DAO closes its own connection
        // via try-with-resources. This method is intentionally empty.
        System.out.println("All connections are self-managed. Nothing to close globally.");
    }
}