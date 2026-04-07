package com.example.onlineauctionsystem.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DATA_DIR = System.getProperty("user.dir") + File.separator + "data";
    private static final String DB_URL = "jdbc:sqlite:" + DATA_DIR + File.separator + "database.db";

    public static Connection getConnection() throws SQLException {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return DriverManager.getConnection(DB_URL);
    }
}
