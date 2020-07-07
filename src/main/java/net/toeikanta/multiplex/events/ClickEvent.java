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

    //สำหรับการวาร์ป และ ลบ plot
    @EventHandler
    public void onClickTopGUI(InventoryClickEvent e){
        //กรณีที่เปิด GUI topPlot หรือ myPlot จะมีคำว่าจัดอันดับอยู่ส่วนบนเสมอ
        if(e.getView().getTitle().contains(ChatColor.BLACK + "จัดอันดับ ")){
            Player player = (Player) e.getWhoClicked();
            Integer page_number = e.getClickedInventory().getItem(40).getAmount();
            try{
                // กรณีคลิกที่หัวเท่านั้น
                if(
                    e.getCurrentItem() != null
                    && e.getCurrentItem().getType().equals(Material.PLAYER_HEAD)
                    && e.getCurrentItem().getItemMeta().getDisplayName().contains("อันดับ")
                ){
                    // หาไอดีจาก title
                    String plot_str = e.getCurrentItem().getItemMeta().getDisplayName().split("id:")[1].trim();
                    Integer plot_id = Integer.parseInt(plot_str);
                    // กรณีเป็น GUI ที่แสดง myplot จะคลิกขวาเพื่อลบได้
                    if(e.getView().getTitle().contains("เฉพาะของฉัน")) {
                        if(e.isRightClick()){
                            plotVoting.db.removePlot(plot_id,player);
                            plotVoting.db.getAllMyPlots(player,page_number);
                        }
                    }
                    // กรณีทั่วไป คลิปซ้ายลบได้เลย
                    if(e.isLeftClick()){
                        plotVoting.db.plotTp(plot_id, player);
                    }
                }

                // กรณีกดปุ่มหน้าถัดไป
                if(e.getCurrentItem() != null&& e.getCurrentItem().getItemMeta().getDisplayName().contains("หน้าถัดไป")){
                    String type_name = e.getCurrentItem().getItemMeta().getDisplayName().split(":")[0];
                    // กรณีทั่วไป คลิปซ้ายลบได้เลย
                    if(e.isLeftClick()){
                        if(type_name.equals("เฉพาะของฉัน")){
                            plotVoting.db.getAllMyPlots(player,page_number + 1);
                        }else
                            plotVoting.db.getTopPlotByType(type_name,player,page_number + 1);
                    }
                }

                // กรณีกดปุ่ม ก่อนหน้า
                if(e.getCurrentItem() != null && e.getCurrentItem().getItemMeta().getDisplayName().contains("หน้าก่อนหน้า")){
                    String type_name = e.getCurrentItem().getItemMeta().getDisplayName().split(":")[0];
                    // กรณีทั่วไป คลิปซ้ายลบได้เลย
                    if(e.isLeftClick() && page_number > 1){
                        if(type_name.equals("เฉพาะของฉัน")){
                            plotVoting.db.getAllMyPlots(player,page_number - 1);
                        }else
                            plotVoting.db.getTopPlotByType(type_name,player,page_number - 1);
                    }
                }
                // cancle event ไม่ให้ interact กับไอเท็มได้
                e.setCancelled(true);
            }catch(Exception ex){
                Logger.print(ex.getMessage());
            }
        }

    }
}
