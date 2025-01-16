package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteSetup {
    private static final String DB_URL = "jdbc:sqlite:userdb.sqlite";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("Connected to SQLite database.");
                String createTableSQL = "CREATE TABLE IF NOT EXISTS Users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT NOT NULL UNIQUE," +
                        "password TEXT NOT NULL" +
                        ");";
                Statement stmt = conn.createStatement();
                stmt.execute(createTableSQL);
                System.out.println("Table 'Users' created successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to SQLite: " + e.getMessage());
        }
    }
}
