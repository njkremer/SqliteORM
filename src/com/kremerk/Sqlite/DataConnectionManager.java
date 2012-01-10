package com.kremerk.Sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataConnectionManager {
    public static Connection getConnection() {
        return connection;
    }

    public static void init(String databaseName) {
        init(databaseName, System.getProperty("user.dir"));
    }

    public static void init(String databaseName, String pathToDatabase) {
        try {
            if(connection == null) {
                Class.forName("org.sqlite.JDBC");
                // TODO this should probably be done with env variables and
                // File.Seperator
                String separator = File.separator;
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s%s%s", 
                        pathToDatabase, 
                        separator, 
                        databaseName));
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
