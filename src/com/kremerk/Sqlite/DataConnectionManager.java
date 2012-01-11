package com.kremerk.Sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is the main class used for connecting to the SQLite database. Use the {@linkplain #init(String)} or
 * {@linkplain #init(String, String)} methods to specify the path to the database file.
 * 
 */
public class DataConnectionManager {
    /**
     * Gets the raw SQL Connection.
     * 
     * @return The raw {@linkplain Connection} to the database.
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Initializes the database connection with the passed in relative path (starting at the user.dir) of the java
     * program.
     * 
     * @param databaseName The path relative to java property <code>user.dir</code> to the database.
     */
    public static void init(String databaseName) {
        init(databaseName, System.getProperty("user.dir"));
    }

    /**
     * Initializes the database connection with the passed in absolute path to the database and database name.
     * 
     * @param databaseName The absolute path to the database.
     * @param pathToDatabase The filename of the database.
     */
    public static void init(String databaseName, String pathToDatabase) {
        try {
            if (connection == null) {
                Class.forName("org.sqlite.JDBC");
                String separator = File.separator;
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s%s%s", pathToDatabase, separator, databaseName));
                connection.setAutoCommit(true);
            }
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private DataConnectionManager() {
        // to enforce static usage...
    }

    private static Connection connection;
}
