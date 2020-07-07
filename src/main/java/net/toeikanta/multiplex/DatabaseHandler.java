package net.toeikanta.multiplex;

import org.bukkit.entity.Player;
import java.io.*;
import java.sql.*;

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
            String url = "jdbc:sqlite:" + plotVoting.getDataFolder() + dbFileName;
                //use this for test
//            String url = "jdbc:sqlite:memory:";

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

    public void getTopVoteByType(String type,Player sender){
        String sql = "SELECT vote_date,player_name,type_name FROM plot WHERE type_name = ? ORDER BY ";

        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                sender.sendMessage(rs.getString("name"));
                Logger.print(rs.getInt("id") +  "\t" +
                        rs.getString("name") + "\t" +
                        rs.getBoolean("closed") + "\t" +
                        rs.getDate("start_date"));
            }
        } catch (SQLException e) {
            Logger.print(e.getMessage());
        }
    }

    public void selectAllType(Player sender){
        String sql = "SELECT * FROM types";

        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                sender.sendMessage(rs.getString("name"));
                Logger.print(rs.getString("name") + "\t" +
                        rs.getBoolean("closed") + "\t" +
                        rs.getDate("start_date"));
            }
        } catch (SQLException e) {
            Logger.print(e.getMessage());
        }
    }

    public void addType(String name, Player sender) {
        String sql = "INSERT INTO types(name,start_date,closed) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(connUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDate(2, new Date(System.currentTimeMillis()));
            pstmt.setBoolean(3, false);
            pstmt.executeUpdate();
            sender.sendMessage("add type '" + name +"' completed");
        } catch (SQLException e) {
            sender.sendMessage(e.getMessage());
            Logger.print(e.getMessage());
        }
    }

    public void createInitTables(){
        // SQL statement for creating a new table
        String types = "CREATE TABLE IF NOT EXISTS types (\n"
                + "	name text NOT NULL PRIMARY KEY,\n"
                + "	start_date date NOT NULL,\n"
                + "	closed boolean NOT NULL\n"
                + ");";
        String plots = "CREATE TABLE IF NOT EXISTS plots (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	x_pos real NOT NULL,\n"
                + "	y_pos real NOT NULL,\n"
                + "owner_name text NOT NULL,"
                + "type_name text NOT NULL,"
                + " FOREIGN KEY (type_name)\n"
                + "   REFERENCES types (name)\n"
                + "     ON UPDATE CASCADE\n"
                + "     ON DELETE CASCADE"
                + ");";
        String votes = "CREATE TABLE IF NOT EXISTS votes (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	vote_date date NOT NULL,\n"
                + "	player_name text NOT NULL,\n"
                + "	plot_id integer REFERENCES plots(id),\n"
                + "	type_name text NOT NULL, \n"
                + "   FOREIGN KEY (type_name) \n"
                + "   REFERENCES types (name) \n"
                + "     ON UPDATE CASCADE\n"
                + "     ON DELETE CASCADE"
                + ");";

        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(types);
            Logger.print("check database type passed");
            stmt.execute(votes);
            Logger.print("check database vote passed ");
            stmt.execute(plots);
            Logger.print("check database plot passed ");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
