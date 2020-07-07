package net.toeikanta.multiplex.events;

import net.toeikanta.multiplex.PlotVoting;
import net.toeikanta.multiplex.libs.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        if(e.getView().getTitle().contains(ChatColor.BLACK + "จัดอันดับ ")){
            Player player = (Player) e.getWhoClicked();
            //add warp on click
            try{
                if(e.getCurrentItem() != null && e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)){
                    if(e.getCurrentItem().getItemMeta().getDisplayName().contains("อันดับ")){
                        String id = e.getCurrentItem().getItemMeta().getDisplayName().split("id:")[1].trim();
                        plotVoting.db.plotTp(Integer.parseInt(id), player);
                        e.setCancelled(true);
                    }
                }
            }catch(Exception ex){
                Logger.print(ex.getMessage());
            }
        }

    }
}
