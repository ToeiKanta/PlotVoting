package net.toeikanta.multiplex;

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

import java.io.*;
import java.sql.*;

public class DatabaseHandler {
    // ตั้งช่ือไฟล์ database
    private final String dbFileName = "/database.db";
    private PlotVoting plotVoting;
    private String connUrl = ""; //ใช้เป็น URL ที่ไว้ connect database (ห้ามลบ)

    public DatabaseHandler(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
        this.connect();
        this.createInitTables();
    }

    //ทำการ connect database ครั้งแรก และ ตรวจสอบว่ามี database อยู่จริงหรือไม่
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

    // ดูอันดับ plots จากประเภท
    public void getTopPlotByType(String type_name,Player sender, Integer page_number){
        // แสดง plots row ทั้งหมด ตาม type_name เรียงตาม score จาก สูงไปต่ำ
        // มีการจำกัดที่ 36 หัว ต่อ 1 หน้า (4 บรรทัด บรรทัดละ 9 column)
        String sql = "SELECT * FROM plots WHERE type_name = '" + type_name + "' ORDER BY score DESC LIMIT 36 OFFSET " + ((page_number-1) * 36);

        try (
            Connection conn = DriverManager.getConnection(connUrl);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql)
        ){
            ItemStack[] topHeads = new ItemStack[45];
            int i = 0;
            // วนลูปแต่ละ plots ใส่ใน player head
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
                // methods สำหรับนำ data มาใส่ใน player head
                topHeads[i++] = GUI.getTopPlotHead(owner_name, i ,score, location, plot_id,regis_date);
            }
            // ส่ง heads ทั้งหมด ไปตาม methods นี้ เพื่อสร้าง GUI
            Inventory top = GUI.getTopPlotGUI(topHeads, sender, type_name, page_number);
            // เปิด players's inventory
            sender.openInventory(top);
        } catch (SQLException e) {
            Logger.print(e.getMessage());
        }
    }

    // แสดง Type ทั้งหมด แบบ text
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

    // ตรวจสอบว่า register plot เกิน limit ต่อคนต่อประเภท หรือยัง
    private Boolean isExceedLimitRegis(String type_name, Player player){
        // รับค่า limit มาจาก Config.yml
        Integer limit = PlotVoting.plugin.getConfig().getInt("limit_regis_plot");
        try(
                Connection conn = DriverManager.getConnection(connUrl);
                Statement stmt = conn.createStatement();
        ){
            ResultSet res = stmt.executeQuery("SELECT COUNT(*) AS count FROM plots WHERE owner_name = '"+player.getName()+"' AND type_name = '" +type_name+"'");
            if(res.getInt("count") >= limit) {
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            Logger.print(e.getMessage());
            return true;
        }
    }

    // สำหรับ register plot กับประเภทนั้นๆ
    public void registerPlot(String type_name, Player sender){
        //ตรวจสอบว่า type มีอยู่จริง
        if(!isTypeExists(type_name)){
            sender.sendMessage(ChatColor.RED + "Type invalid");
            return;
        }
        //ตรวจสอบว่าสามารถ register plot ได้อยู่
        if(isExceedLimitRegis(type_name, sender)){
            // ดึงค่า limit จาก file config.yml
            Integer limit = PlotVoting.plugin.getConfig().getInt("limit_regis_plot");
            sender.sendMessage(ChatColor.RED + "ไม่สามารถลงเพิ่มได้แล้ว //ท่านสามารถลงสมัครได้เพียง " + limit + " ที่ ต่อ 1 ประเภท เท่านั้น!!");
            return;
        }

        // เริ่มเพิ่ม data ลงใน table plots
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
            Libs.playSoundLevelUp(sender);
            Libs.spawnFireWork(sender);
            sender.sendMessage(ChatColor.GOLD + "ทำการลงทะเบียนพิกัด ประเภท '" + type_name +"' สำเร็จแล้ว");
            sender.sendMessage(ChatColor.GREEN + "ท่านสามารถ ตรวจสอบสถานะ plot ของท่านได้ ด้วยคำสั่ง /pv myplot");

        } catch (SQLException e) {
            sender.sendMessage(e.getMessage());
            Logger.print(e.getMessage());
        }
    }

    // ตรวจสอบว่า plot มีอยู่หรือไม่
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

    // ตรวจสอบว่า มีประเภท นี้อยู่จริงหรือไม่
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

    // ลบ plot (ใช้ได้เฉพาะ เจ้าของ plot เท่านั้น)
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

    // วาร์ปไปที่ plot นั้นๆ
    public void plotTp(Integer plot_id, Player sender) {
        if (!this.isPlotExist(plot_id)) {
            sender.sendMessage("Plot id invalid.");
            return;
        }
        if(!Libs.isWorldAllowed(sender.getLocation().getWorld().getName())){
            sender.sendMessage("การวาร์ปจำเป็นต้องอยู่ในโลกสร้างสรรค์ หรือโลกสร้างบ้านก่อน");
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

    // โหวดคะแนนให้ plot นั้น
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

    // เพิ่มประเภท
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

    // แสดง plot ทั้งหมด ของ player ที่ใช้คำสั่ง
    public void getAllMyPlots(Player sender, Integer page_number){
        try (Connection conn = DriverManager.getConnection(connUrl);
            Statement stmt = conn.createStatement();
        ){
            ResultSet rs = stmt.executeQuery("SELECT * FROM plots WHERE owner_name = '"+sender.getName() + "' ORDER BY score DESC LIMIT 36 OFFSET " + ((page_number-1) * 36));

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
                topHeads[i++] = GUI.getMyPlotHead(owner_name, i ,score, location, plot_id,regis_date);
            }
            Inventory top = GUI.getTopPlotGUI(topHeads, sender, "เฉพาะของฉัน", page_number);
            sender.openInventory(top);

        }catch(Exception e){

        }
    }

    // สร้างตารางเริ่มต้น กรณีที่ตารางยังไม่ถูกสร้างขึ้น
    public void createInitTables(){
        /*
         Tables Structure - types
          - type_name ชื่อประเภท
          - start_date วันที่สร้างประเภท
          - closed สถานะ ปิดรับหรือยัง (ยังไม่ได้ใช้)
         */
        String types = "CREATE TABLE IF NOT EXISTS types (\n"
                + "	type_name text NOT NULL PRIMARY KEY,\n"
                + "	start_date date NOT NULL,\n"
                + "	closed boolean NOT NULL\n"
                + ");";
        /*
         Tables Structure - plots
          - id คีย์หลัก
          - x_pos , y_pos , z_pos ,world พิกัดที่ใช้คำสั่ง
          - score แต้มโหวด
          - regis_date วันลงทะเบียน plot
          - owner_name เจ้าของ plot (คนใช้คำสั่ง)
          - type_name ชื่อประเภท
         */
        String plots = "CREATE TABLE IF NOT EXISTS plots (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
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
        /*
         Tables Structure - votes
          - id คีย์หลัก
          - vote_date วันที่โหวด
          - player_name คนโหวด
          - plot_id ไอดี plot
         */
        String votes = "CREATE TABLE IF NOT EXISTS votes (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
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
