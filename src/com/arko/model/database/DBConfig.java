package com.arko.model.database;

/**
 * Central database configuration.
 *
 * For local testing (current):
 *   HOST = "localhost"
 *
 * For LAN demo (hotspot host machine):
 *   Change HOST to the hotspot IP, e.g. "192.168.137.1"
 *   All other fields stay the same.
 *
 * This is the ONLY file that needs to change when switching
 * between local and LAN deployment. No business logic is touched.
 */
public final class DBConfig {

    private DBConfig() {}

    public static final String HOST     = "localhost";      
    public static final int    PORT     = 3306;              
    public static final String DATABASE = "project_arko_db";
    public static final String USER     = "root";             
    public static final String PASSWORD = "rootPwSql";       

    // Assembled URL — do not modify
    public static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=Asia/Manila";
}
