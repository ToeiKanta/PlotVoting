package net.toeikanta.multiplex;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
//                    Statement stm = conn.createStatement();
//                    stm.execute("PRAGMA foreign_keys=ON");
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.print(ex.getMessage());
            }
        }
    }

    public void getTopPlotByType(String type_name,Player sender){
        String sql = "SELECT id,x_pos,y_pos,score,owner_name,type_name FROM plots WHERE type_name = '" + type_name + "' ORDER BY score DESC";

        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            // loop through the result set
            while (rs.next()) {
                sender.sendMessage("ID: " + rs.getString("id")+" :: "+
                        rs.getString("x_pos")+","+
                                rs.getString("y_pos")+ " score:: "+
                                rs.getString("score") + " owner:: "+ rs.getString("owner_name")
                        );
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

    public void registerPlot(String type_name, Player sender){
        String sql = "INSERT INTO plots(x_pos,y_pos,owner_name,type_name,score,world,z_pos) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(connUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, sender.getLocation().getX());
            pstmt.setDouble(2, sender.getLocation().getY());
            pstmt.setString(3, sender.getName());
            pstmt.setString(4, type_name);
            pstmt.setInt(5, 0);
            pstmt.setString(6, sender.getLocation().getWorld().getName());
            pstmt.setDouble(7, sender.getLocation().getZ());

            pstmt.executeUpdate();
            sender.sendMessage("register building with type '" + type_name +"' completed");
        } catch (SQLException e) {
            sender.sendMessage(e.getMessage());
            Logger.print(e.getMessage());
        }
    }

    private boolean isPlotExist(Integer plot_id){
        //check plot id exist
        String sql = "SELECT COUNT(*) AS count FROM plots WHERE id = " + plot_id;

        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stm = conn.createStatement()) {
             ResultSet resultSet = stm.executeQuery(sql);
             if(resultSet.getInt("count") > 0){
                 return true;
             }else{
                 return false;
             }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void plotTp(Integer plot_id, Player sender) {
        if (!this.isPlotExist(plot_id)) {
            sender.sendMessage("Plot id invalid.");
            return;
        }
        String sql = "SELECT * FROM plots WHERE id = " + plot_id;
        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stmt = conn.createStatement();
        ) {
            ResultSet pos = stmt.executeQuery(sql);
            String world = pos.getString("world");
            // บังคับ แค่ creative world เท่านั้น
            // ลบออกถ้าต้องการให้ไปที่ไหนก็ได้
            if(!world.equals(Command.creative_world_name)){
                sender.sendMessage(ChatColor.RED + "ขออภัย ระหว่างนี้ไม่อนุญาตให้วาร์ปไปพื้นที่นี้ได้");
                return;
            }
            World w = plotVoting.getServer().getWorld(world);
            Double x_pos = pos.getDouble("x_pos");
            Double y_pos = pos.getDouble("y_pos");
            Double z_pos = pos.getDouble("z_pos");
            sender.teleport(new Location(w, x_pos, y_pos, z_pos));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void votePlot(Integer plot_id, Player sender){
        if(!this.isPlotExist(plot_id)){
            sender.sendMessage("Plot id invalid.");
            return;
        }
        String sql = "INSERT INTO votes (vote_date,player_name,plot_id) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(connUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new Date(System.currentTimeMillis()));
            pstmt.setString(2, sender.getName());
            pstmt.setInt(3, plot_id);
            pstmt.executeUpdate();

            // update plot score
            String sql2 = "UPDATE plots SET score = score + 1 WHERE id = " + plot_id;
            try (Connection conn2 = DriverManager.getConnection(connUrl);
                 Statement stm = conn.createStatement()) {
                stm.executeUpdate(sql2);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
            sender.sendMessage("vote plot '" + plot_id +"' success");
        }catch (SQLException e){
            if(e.getMessage().contains("SQLITE_CONSTRAINT")){
                sender.sendMessage("คุณเคยโหวดพื้นที่นี้ไปแล้ว");
            }else{
                sender.sendMessage("เกิดปัญหาขัดข้อง");
            }
            Logger.print(e.getMessage());

        }
    }

    public void addType(String type_name, Player sender) {
        String sql = "INSERT INTO types(name,start_date,closed) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(connUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type_name);
            pstmt.setDate(2, new Date(System.currentTimeMillis()));
            pstmt.setBoolean(3, false);
            pstmt.executeUpdate();
            sender.sendMessage("add type '" + type_name +"' completed");
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
                + "	z_pos real NOT NULL,\n"
                + "	world text NOT NULL,\n"
                + "	score integer NOT NULL,\n"
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
                + "	plot_id integer NOT NULL, \n"
                + " UNIQUE(player_name,plot_id), \n"
                + "   FOREIGN KEY (plot_id) \n"
                + "   REFERENCES plots (id) \n"
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
