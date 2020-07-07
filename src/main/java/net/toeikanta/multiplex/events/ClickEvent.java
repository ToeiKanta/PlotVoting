package net.toeikanta.multiplex.events;

import net.toeikanta.multiplex.PlotVoting;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ClickEvent implements Listener {
    PlotVoting plotVoting;

    public ClickEvent(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
    }

    @EventHandler
    public void onClickTopGUI(InventoryClickEvent e){
        Player player = (Player) e.getWhoClicked();
        if(e.getView().getTitle().contains(ChatColor.BLACK + "จัดอันดับ ")){
            //add warp on click
            String id = e.getCurrentItem().getItemMeta().getDisplayName().split("id:")[1].trim();
            plotVoting.db.plotTp(Integer.parseInt(id), player);
            e.setCancelled(true);
        }

    }
}
