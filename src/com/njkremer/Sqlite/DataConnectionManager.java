package com.njkremer.Sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This is the main class used for connecting to the SQLite database. Use the {@linkplain #init(String)} or
 * {@linkplain #init(String, String)} methods to specify the path to the database file.
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
     * program. Logging defaults to {@linkplain org.apache.log4j.Level.WARN}
     * 
     * @param databaseName The path relative to java property <code>user.dir</code> to the database.
     */
    public static void init(String databaseName) {
        init(databaseName, System.getProperty("user.dir"), DEFAULT_LOGGER_LEVEL);
    }

    /**
     * Initializes the database connection with the passed in absolute path to the database and database name.
     * Logging defaults to {@linkplain org.apache.log4j.Level.WARN}
     * 
     * @param databaseName The absolute path to the database.
     * @param pathToDatabase The filename of the database.
     */
    public static void init(String databaseName, String pathToDatabase) {
        init(databaseName, pathToDatabase, DEFAULT_LOGGER_LEVEL);
    }

    /**
     * Initializes the database connection with the passed in relative path (starting at the user.dir) of the java
     * program. Logging is set to provided level.
     * @param databaseName The absolute path to the database.
     * @param loggerLevel The level at which the ORM should log messages to console.
     */
    public static void init(String databaseName, Level loggerLevel) {
        init(databaseName, System.getProperty("user.dir"), loggerLevel);
    }

    /**
     * Initializes the database connection with the passed in absolute path to the database and database name.
     * Logging is set to provided level.
     * 
     * @param databaseName The absolute path to the database.
     * @param pathToDatabase The filename of the database.
     * @param loggerLevel The level at which the ORM should log messages to console.
     */
    public static void init(String databaseName, String pathToDatabase, Level loggerLevel) {
        try {
            if (connection == null) {
                Class.forName("org.sqlite.JDBC");
                String separator = File.separator;
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s%s%s", pathToDatabase, separator, databaseName));
                connection.setAutoCommit(true);

                initializeLogging(loggerLevel);
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

    private static void initializeLogging(Level loggerLevel) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(loggerLevel);
    }

    private static Connection connection;

    private static final Level DEFAULT_LOGGER_LEVEL = Level.WARN;
}
