package com.arko.model.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility for running multiple DAO operations atomically in a single transaction.
 *
 * Usage:
 *   TransactionRunner.run(conn -> {
 *       // multiple ps.executeUpdate() calls here
 *       // if any throw, everything rolls back
 *   });
 *
 * The caller does not manage the connection — TransactionRunner opens it,
 * sets autoCommit = false, commits on success, and rolls back on failure.
 * The connection is always closed in the finally block.
 */
public final class TransactionRunner {

    private TransactionRunner() {}

    @FunctionalInterface
    public interface TransactionWork {
        void execute(Connection conn) throws SQLException;
    }

    /**
     * Executes the given work block inside a transaction.
     * Commits on success, rolls back on any SQLException.
     *
     * @param work the block of DAO operations to run atomically
     * @throws SQLException if the work fails and the rollback also fails (rare)
     */
    public static void run(TransactionWork work) throws SQLException {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            work.execute(conn);
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw e; // Re-throw so the caller can handle or display it
        } finally {
            try {
                conn.setAutoCommit(true); // Restore default before closing
                conn.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }
}