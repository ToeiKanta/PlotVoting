package net.toeikanta.multiplex.libs;

import net.toeikanta.multiplex.PlotVoting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GUI {
    // ส่งกลับหัว Player เฉพาะคำสั่ง Top
    public static ItemStack getTopPlotHead(String playerName, Integer order, Integer score, Location location, Integer plot_id, Date regis_date) {
        ItemStack item = PlayerLibs.getPlayerHead(playerName);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        //add lore
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "ID " + ChatColor.RED + plot_id);
        lore.add(ChatColor.GREEN + "โลก :: " + ChatColor.RED + location.getWorld().getName());
        // calculate time in day
        Long day = (new Date(System.currentTimeMillis()).getTime() - regis_date.getTime()) / (1000*60*60*24);
        if(day == 0){
            lore.add(ChatColor.GREEN + "ลงสมัครเมื่อ :: " + ChatColor.RED + "วันนี้") ;
        }else
            lore.add(ChatColor.GREEN + "ลงสมัครเมื่อ :: " + ChatColor.RED + day.toString() + ChatColor.GREEN + " วันก่อน") ;
        lore.add(ChatColor.GREEN + "พิกัด :: " + ChatColor.RED + " (" + MathLibs.parseDouble(location.getX()) + "," + MathLibs.parseDouble(location.getY()) + "," + MathLibs.parseDouble(location.getZ()) + ")");
        lore.add(ChatColor.GOLD + "คะแนนที่ได้ " + score.toString() + " คะแนน");
        lore.add(ChatColor.WHITE + "-----------------------");
        lore.add(ChatColor.AQUA + "คลิกซ้าย เพื่อวาร์ปไปพื้นที่");
        skull.setLore(lore);
        // ตั้งชื่อ head
        skull.setDisplayName(ChatColor.YELLOW + "อันดับ " + order + " โดย " + ChatColor.GOLD + playerName + " id:" + plot_id); //ระวัง!! มีการใช้ id: เป็น indicator split(":""
        item.setItemMeta(skull);
        return item;
    }

    // ส่งกลับหัว Player เฉพาะคำสั่ง MyPlot
    public static ItemStack getMyPlotHead(String playerName, Integer order, Integer score, Location location, Integer plot_id, Date regis_date) {
        // นำ topPlot head มาเพิ่ม lore ให้ลบ plot ได้
        ItemStack item = getTopPlotHead( playerName,  order,  score,  location,  plot_id,  regis_date);
        ItemMeta skull = item.getItemMeta();
        List<String> lore = skull.getLore();
        lore.add(ChatColor.RED + "คลิกขวา เพื่อลบพิกัด"); //แก้ไขข้อความได้ ไม่มีการใช้งานชื่อ
        skull.setLore(lore);
        item.setItemMeta(skull);
        return item;
    }

    // ส่งกลับ GUI Inventory ใช้ได้ทั้ง Top และ MyPlot
    public static Inventory getTopPlotGUI(ItemStack[] items, Player sender, String type_name, Integer page_number){
        Inventory gui = Bukkit.createInventory(sender,45, ChatColor.BLACK + "จัดอันดับ " + type_name);
        //ไอเท็ม บอกเลขหน้า ตามจำนวน stack
        ItemStack item = new ItemStack(Material.PAINTING, page_number); //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง
        ItemMeta wool = item.getItemMeta();
        wool.setDisplayName("หน้า : " + page_number); // ระวัง การเปลี่ยนชื่อ มีการ split ":" และเอาเลขด้านหลัง
        item.setItemMeta(wool);
        items[40] = item; //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง

        //ไอเท็ม หน้าถัดไป
        if(items[35] != null){
            item = new ItemStack(Material.LIME_WOOL, 1); //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง
            wool = item.getItemMeta();
            wool.setDisplayName(type_name + ":"+ChatColor.GREEN+"หน้าถัดไป"); // ระวัง การเปลี่ยนชื่อ
            item.setItemMeta(wool);
            items[41] = item; //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง
        }

        //ไอเท็ม ก่อนหน้า
        if(page_number > 1){
            item = new ItemStack(Material.LIME_WOOL, 1); //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง
            wool = item.getItemMeta();
            wool.setDisplayName(type_name + ":"+ChatColor.GREEN+"หน้าก่อนหน้า"); // ระวัง การเปลี่ยนชื่อ
            item.setItemMeta(wool);
            items[39] = item; //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง
        }

        //เพิ่ม ปุ่ม กลับ
        if(PlotVoting.plugin.getConfig().getBoolean("back_btn")){
            item = new ItemStack(Material.IRON_CHESTPLATE, 1); //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง
            wool = item.getItemMeta();
            wool.setDisplayName("กลับ"); // ระวัง การเปลี่ยนชื่อ
            item.setItemMeta(wool);
            items[36] = item; //เปลี่ยนไอเท็มได้ แต่อย่าเปลี่ยนตำแหน่ง
        }

        // ปิดไม่ให้ เล่นกับ item ใน inventory ได้
        gui.setContents(items);
        return gui;
    }
}
