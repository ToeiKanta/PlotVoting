package net.toeikanta.multiplex;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.toeikanta.multiplex.libs.GUI;
import net.toeikanta.multiplex.libs.Libs;
import net.toeikanta.multiplex.libs.Logger;
import net.toeikanta.multiplex.libs.MathLibs;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
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
        String sql = "SELECT * FROM plots WHERE type_name = '" + type_name + "' ORDER BY score DESC";

        try (Connection conn = DriverManager.getConnection(connUrl);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            ItemStack[] topHeads = new ItemStack[45];
            int i = 0;
            // loop through the result set
            while (rs.next()) {
                String owner_name = rs.getString("owner_name");
                Integer score = rs.getInt("score");
                Double x_pos = rs.getDouble("x_pos");
                Integer plot_id = rs.getInt("id");
                Double y_pos = rs.getDouble("y_pos");
                Double z_pos = rs.getDouble("z_pos");
                String world = rs.getString("world");
                Date regis_date = rs.getDate("regis_date");
                World w = Bukkit.getWorld(world);
                Location location = new Location(w,x_pos,y_pos,z_pos);
                topHeads[i++] = GUI.getTopPlotHead(owner_name, i ,score, location, plot_id,regis_date);
            }
            Inventory top = GUI.getTopPlotGUI(topHeads, sender, type_name);
            sender.openInventory(top);
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
            int i = 1;
            while (rs.next()) {
                sender.sendMessage(
                        (i++) + ", name: " +
                        rs.getString("type_name") + ", closed: " +
                        rs.getBoolean("closed") + ", start_date: " +
                        rs.getDate("start_date"));
            }
        } catch (SQLException e) {
            Logger.print(e.getMessage());
        }
    }

    public void registerPlot(String type_name, Player sender){
        if(!isTypeExists(type_name)){
            sender.sendMessage(ChatColor.RED + "Type invalid");
            return;
        }

        String sql = "INSERT INTO plots(x_pos,y_pos,owner_name,type_name,score,world,z_pos,regis_date) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(connUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, sender.getLocation().getX());
            pstmt.setDouble(2, sender.getLocation().getY());
            pstmt.setString(3, sender.getName());
            pstmt.setString(4, type_name);
            pstmt.setInt(5, 0);
            pstmt.setString(6, sender.getLocation().getWorld().getName());
            pstmt.setDouble(7, sender.getLocation().getZ());
            pstmt.setDate(8, new Date(System.currentTimeMillis()));
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

    private boolean isTypeExists(String type_name){
        //check plot id exist
        String sql = "SELECT COUNT(*) AS count FROM types WHERE type_name = '" + type_name + "'";

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

    public void removePlot(Integer plot_id,Player sender){
        String sql = "DELETE FROM plots WHERE owner_name = '" + sender.getName() + "' AND id = " + plot_id ;

        try (Connection conn = DriverManager.getConnection(connUrl);
            Statement stm = conn.createStatement();
        ){
            stm.execute(sql);
            sender.sendMessage(ChatColor.GREEN + "ดำเนินการลบพิกัด ของ " + sender.getName() + " : ไอดี " + plot_id + " สำเร็จ!");
        } catch (SQLException e) {
            Logger.print(e.getMessage());
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
            if(!Libs.isWorldAllowed(world)){
               sender.sendMessage("ท่านไม่ได้รับอนุญาตให้วาร์ปไปโลกแห่งนั้นผ่่านวิธีนี้");
               return;
            }
            String owner_name = pos.getString("owner_name");
            String type_name = pos.getString("type_name");
            Integer score = pos.getInt("score");
            World w = plotVoting.getServer().getWorld(world);
            Double x_pos = pos.getDouble("x_pos");
            Double y_pos = pos.getDouble("y_pos");
            Double z_pos = pos.getDouble("z_pos");
            Date regis_date = pos.getDate("regis_date");
            sender.teleport(new Location(w, x_pos, y_pos, z_pos));
            sender.playSound(sender.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,100f,1f);
            sender.sendMessage(ChatColor.GRAY + "============== PlotVoting by MC-Multiplex ===========");
            sender.sendMessage(ChatColor.YELLOW + "ประเภทสิ่งก่อสร้าง : " + ChatColor.RED + type_name);
            sender.sendMessage(ChatColor.YELLOW + "ลงสมัครเมื่อวันที่ : " + ChatColor.RED + regis_date);
            sender.sendMessage(ChatColor.YELLOW + "ทำการวาร์ปไปที่พื้นที่ " + ChatColor.RED + " ID " + plot_id.toString() + ChatColor.YELLOW +" สำเร็จแล้ว");
            sender.sendMessage(ChatColor.YELLOW + "พื้นที่ของ : " + ChatColor.RED + owner_name);
            sender.sendMessage(ChatColor.YELLOW + "โลก : " + ChatColor.RED + world);
            sender.sendMessage(ChatColor.YELLOW + "พิกัด : " + ChatColor.RED + " (" + MathLibs.parseDouble(x_pos) + "," + MathLibs.parseDouble(y_pos) + "," + MathLibs.parseDouble(z_pos) + ")");
            sender.sendMessage(ChatColor.GOLD + "คะแนนโหวดทั้งหมด : " + ChatColor.WHITE + ChatColor.BOLD + score.toString());
            TextComponent clickVote = new TextComponent(">>>>>>>>>>> !! โหวด !! <<<<<<<<<<");
            clickVote.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            clickVote.setBold(true);
            clickVote.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pv vote " + plot_id));
            clickVote.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("คลิกเพื่อโหวดคะแนนให้กับ " + owner_name)
                            .color(net.md_5.bungee.api.ChatColor.BOLD)
                            .italic(true)
                            .underlined(true)
                            .create()));
            sender.sendMessage(ChatColor.GOLD + "ต้องการโหวดให้กับ " + owner_name + " คลิกด้านล่าง ");
            sender.spigot().sendMessage(clickVote);
            sender.sendMessage(ChatColor.GRAY + "=====================================================");
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
            sender.playSound(sender.getLocation().add(0,2,0), Sound.ENTITY_PLAYER_LEVELUP, 100f, 1);
            sender.sendMessage(ChatColor.GREEN + "ทำการโหวดให้กับพื้นที่ ไอดี '" + plot_id +"' สำเร็จ");
        }catch (SQLException e){
            if(e.getMessage().contains("SQLITE_CONSTRAINT")){
                sender.playSound(sender.getLocation().add(0,2,0), Sound.ENTITY_ZOMBIE_PIGMAN_HURT, 100f, 1);
                sender.sendMessage(ChatColor.RED + "คุณเคยโหวดพื้นที่นี้ไปแล้ว");
            }else{
                sender.sendMessage(ChatColor.RED + "เกิดปัญหาขัดข้อง");
            }
            Logger.print(e.getMessage());

        }
    }

    public void addType(String type_name, Player sender) {
        String sql = "INSERT INTO types(type_name,start_date,closed) VALUES(?,?,?)";

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
                + "	type_name text NOT NULL PRIMARY KEY,\n"
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
                + "regis_date date NOT NULL,\n"
                + "owner_name text NOT NULL,"
                + "type_name text NOT NULL,"
                + " FOREIGN KEY (type_name)\n"
                + "   REFERENCES types (type_name)\n"
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
