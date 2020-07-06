package net.toeikanta.multiplex;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    private final String dbFileName = "/database.db";
    private PlotVoting plotVoting;
    private String connUrl = "";

    public DatabaseHandler(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
        this.connect();
        this.createInitTables();
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
                //use this for production
//            String url = "jdbc:sqlite:" + plotVoting.getDataFolder() + dbFileName;
                //use this for test
            String url = "jdbc:sqlite:memory:";

            this.connUrl = url;
            Logger.print(url);
            conn = DriverManager.getConnection(url);
            Logger.print("Connection to SQLite has been established.");
            conn.close();
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

    public void createInitTables(){
        // SQL statement for creating a new table
        String type = "CREATE TABLE IF NOT EXISTS type (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	name text NOT NULL,\n"
                + "	start_date date NOT NULL,\n"
                + "	closed boolean NOT NULL\n"
                + ");";
//        String player = "CREATE TABLE IF NOT EXISTS player (\n"
//                + "	id integer PRIMARY KEY,\n"
//                + "	name text NOT NULL,\n"
//                + "	name text NOT NULL\n"
//                + ");";
        String vote = "CREATE TABLE IF NOT EXISTS vote (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	vote_date date NOT NULL,\n"
                + "	player_name text NOT NULL,\n"
                + "	type_id integer NOT NULL\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(type);
            Logger.print("check database type passed");
            stmt.execute(vote);
            Logger.print("check database vote passed ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
