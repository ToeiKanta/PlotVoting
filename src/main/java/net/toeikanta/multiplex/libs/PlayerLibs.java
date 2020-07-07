package net.toeikanta.multiplex.libs;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerLibs {
    public static ItemStack getPlayerHead(String playerName) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skull = (SkullMeta) item.getItemMeta();
        skull.setDisplayName(playerName);
        skull.setOwner(playerName);
        item.setItemMeta(skull);
        return item;
    }
}
