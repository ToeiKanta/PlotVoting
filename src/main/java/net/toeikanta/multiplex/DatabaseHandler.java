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
        // create databaseFile if it's not exist.
        File dir = new File(plotVoting.getDataFolder().toString());
        if(!dir.exists()){
            dir.mkdirs();
        }
        try {
            // create a connection to the database
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + plotVoting.getDataFolder() + dbFileName;
            Logger.print(url);
            conn = DriverManager.getConnection(url);
            Logger.print("Connection to SQLite has been established.");
        } catch (SQLException | ClassNotFoundException e) {
            Logger.print(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.print(ex.getMessage());
            }
        }
    }
}
