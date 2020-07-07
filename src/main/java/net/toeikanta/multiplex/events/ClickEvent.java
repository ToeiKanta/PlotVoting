package net.toeikanta.multiplex.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ClickEvent implements Listener {
    @EventHandler
    public void onClickTopGUI(InventoryClickEvent e){
        if(e.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD + "อันดับสูงสุด")){
            e.setCancelled(true);
        }

    }
}
