package net.toeikanta.multiplex.commands;

import net.toeikanta.multiplex.PlotVoting;
import net.toeikanta.multiplex.libs.Libs;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PV_Command implements CommandExecutor {

    Plugin plugin = PlotVoting.getProvidingPlugin(PlotVoting.class);

    PlotVoting plotVoting;

    String addType = "tadd";
    String removeType = "tremove";
    String listType = "tlist";
    String registerPlot = "addplot";
    String topPlot = "top";
    String votePlot = "vote";
    String plotTP = "tp";
    String removePlot = "remove";
    String myPlot = "myplot";

    public PV_Command(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equalsIgnoreCase("pv") || label.equalsIgnoreCase("plotvote") || label.equalsIgnoreCase("pvote")) {
                try {
                    String currentWorld = player.getLocation().getWorld().getName();
                    if(args[0].equalsIgnoreCase(registerPlot)){
                        if(Libs.isWorldAllowed(currentWorld)){
                            plotVoting.db.registerPlot(args[1],player);
                        }else{
                            player.sendMessage(ChatColor.RED + "โลกนี้ไม่ได้รับอนุญาตให้ใช้คำสั่งนี้ได้");
                        }
                    }else if(args[0].equalsIgnoreCase(topPlot)){
                        plotVoting.db.getTopPlotByType(args[1],player, 1);
                    }else if(args[0].equalsIgnoreCase(plotTP)){
                        if(Libs.isWorldAllowed(currentWorld)){
                            plotVoting.db.plotTp(Integer.parseInt(args[1]),player);
                        }else{
                            player.sendMessage(ChatColor.RED + "การวาร์ปจำเป็นต้องอยู่ในโลกสร้างสรรค์ หรือโลกสร้างบ้านก่อน");
                        }
                    }else if(args[0].equalsIgnoreCase(votePlot)){
                        plotVoting.db.votePlot(Integer.parseInt(args[1]),player);
                    }else if(args[0].equalsIgnoreCase(removePlot)){
                        plotVoting.db.removePlot(Integer.parseInt(args[1]),player);
                    }else if(args[0].equalsIgnoreCase(myPlot)){
                        plotVoting.db.getAllMyPlots(player,1);
                    }else{
                        showDescription(player);
                    }
                }catch(Exception e){
                    showDescription(player);
                    return false;
                }
            }

            if(label.equalsIgnoreCase("pva")){
                try {
                    if (args[0].equalsIgnoreCase(addType) ) {
                        plotVoting.db.addType(args[1],player);
                    }else if (args[0].equalsIgnoreCase(listType)) {
                        plotVoting.db.selectAllType(player);
                    }else if (args[0].equalsIgnoreCase(removeType)) {
//                            plotVoting.db.selectAllType(player);
                    }else{
                        showDescriptionAdmin(player);
                    }
                }catch (Exception e){
                    showDescriptionAdmin(player);
                    return false;
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only Player can use this command.");
            return false;
        }
        return false;
    }

    void showDescription(Player sender){
        sender.sendMessage(ChatColor.GRAY+"======== PlotVoting by MC-Multiplex ======== ");
        sender.sendMessage(ChatColor.GREEN+"/pva " + ChatColor.YELLOW +" #for admin commands");
        sender.sendMessage(ChatColor.GRAY+"- member commands (pvote.default)");
        sender.sendMessage(ChatColor.GREEN+"/pv " + registerPlot + " <type_name> "+ ChatColor.YELLOW +" #add plot to type");
        sender.sendMessage(ChatColor.GREEN+"/pv " + topPlot + " <type_name> "+ ChatColor.YELLOW +"#show top plot by type");
        sender.sendMessage(ChatColor.GREEN+"/pv " + votePlot + " <plot_id> "+ ChatColor.YELLOW +"#vote plot");
        sender.sendMessage(ChatColor.GREEN+"/pv " + plotTP + " <plot_id> "+ ChatColor.YELLOW +"#teleport to plot");
        sender.sendMessage(ChatColor.GREEN+"/pv " + removePlot + " <plot_id> "+ ChatColor.YELLOW +"#remove your plot");
        sender.sendMessage(ChatColor.GRAY+"============================== ");
    }

    void showDescriptionAdmin(Player sender){
        sender.sendMessage(ChatColor.GRAY+"======== PlotVoting by MC-Multiplex ======== ");
        sender.sendMessage(ChatColor.GRAY+"- admin commands (pvote.admin)");
        sender.sendMessage(ChatColor.GREEN+"/pva " + addType + " <type_name>  "+ ChatColor.YELLOW +"#add new type");
        sender.sendMessage(ChatColor.GREEN+"/pva " + removeType + " <type_name>  "+ ChatColor.YELLOW +"#remove type");
        sender.sendMessage(ChatColor.GREEN+"/pva " + listType + ChatColor.YELLOW +" #show all types");
        sender.sendMessage(ChatColor.GRAY+"============================== ");
    }
}
