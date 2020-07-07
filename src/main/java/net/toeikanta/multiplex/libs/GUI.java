package net.toeikanta.multiplex.libs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GUI {
    public static ItemStack getTopPlotHead(String playerName, Integer order, Integer score, Location location, Integer plot_id, Date regis_date) {
        ItemStack item = PlayerLibs.getPlayerHead(playerName);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GREEN + "ID " + ChatColor.RED + plot_id);
        lore.add(ChatColor.GREEN + "โลก :: " + ChatColor.RED + location.getWorld().getName());
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
        skull.setDisplayName(ChatColor.YELLOW + "อันดับ " + order + " โดย " + ChatColor.GOLD + playerName + " id:" + plot_id);
        item.setItemMeta(skull);
        return item;
    }

    public static ItemStack getMyPlotHead(String playerName, Integer order, Integer score, Location location, Integer plot_id, Date regis_date) {
        ItemStack item = getTopPlotHead( playerName,  order,  score,  location,  plot_id,  regis_date);
        ItemMeta skull = item.getItemMeta();
        List<String> lore = skull.getLore();
        lore.add(ChatColor.RED + "คลิกขวา เพื่อลบพิกัด");
        skull.setLore(lore);
        item.setItemMeta(skull);
        return item;
    }

    public static Inventory getTopPlotGUI(ItemStack[] items, Player sender, String type_name){
        Inventory gui = Bukkit.createInventory(sender,45, ChatColor.BLACK + "จัดอันดับ " + type_name);
        gui.setContents(items);
        return gui;
    }
}
