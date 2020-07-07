package net.toeikanta.multiplex.libs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class GUI {
    public static ItemStack getTopPlotHead(String playerName,Integer order, Integer score, Location location, Integer plot_id) {
        ItemStack item = PlayerLibs.getPlayerHead(playerName);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GREEN + "ID " + ChatColor.RED + plot_id);
        lore.add(ChatColor.GREEN + "โลก :: " + ChatColor.RED + location.getWorld().getName());
        lore.add(ChatColor.GREEN + "พิกัด :: " + ChatColor.RED + " (" + MathLibs.parseDouble(location.getX()) + "," + MathLibs.parseDouble(location.getY()) + "," + MathLibs.parseDouble(location.getZ()) + ")");
        lore.add(ChatColor.GOLD + "คะแนนที่ได้ " + score.toString() + " คะแนน");
        lore.add(ChatColor.WHITE + "-----------------------");
        lore.add(ChatColor.YELLOW + "คลิก เพื่อวาร์ปไปพื้นที่");
        skull.setLore(lore);
        skull.setDisplayName(ChatColor.YELLOW + "อันดับ " + order + " โดย " + ChatColor.GOLD + playerName + " id:" + plot_id);
        item.setItemMeta(skull);
        return item;
    }

    public static Inventory getTopPlotGUI(ItemStack[] items, Player sender, String type_name){
        Inventory gui = Bukkit.createInventory(sender,45, ChatColor.BLACK + "จัดอันดับ " + type_name);
        gui.setContents(items);
        return gui;
    }
}
