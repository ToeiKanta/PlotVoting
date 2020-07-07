package net.toeikanta.multiplex;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class PlayerLibs {
    public static ItemStack getPlayerHead(String playerName) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(playerName);
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GOLD + "ทดสอบ Test");
        skull.setLore(lore);
        skull.setOwner(playerName);
        item.setItemMeta(skull);
        return item;
    }
}
