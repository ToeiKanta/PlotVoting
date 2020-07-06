package net.toeikanta.multiplex;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {
    private final String dbFileName = "/database.db";
    private PlotVoting plotVoting;

    public DatabaseHandler(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
        this.connect();
    }

    public void connect() {
        Connection conn = null;
        try {
            // create databaseFile if it's not exist.
            File databaseFile = new File(plotVoting.getDataFolder() +  dbFileName);
            if(!databaseFile.exists()){
                databaseFile.mkdirs();
            }
            // create a connection to the database
//            String url = "jdbc:sqlite:" + plotVoting.getDataFolder() + dbFileName;
            String url = "jdbc:sqlite:memory:";
//            System.out.println(url);
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
