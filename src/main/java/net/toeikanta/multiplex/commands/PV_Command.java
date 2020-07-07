package net.toeikanta.multiplex.commands;

import net.toeikanta.multiplex.PlotVoting;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PV_Command implements CommandExecutor {

    Plugin plugin = PlotVoting.getProvidingPlugin(PlotVoting.class);

    PlotVoting plotVoting;

    String pvote = "pv";
    String addType = "add";
    String listType = "list";
    String registerPlot = "addplot";
    String topPlot = "top";
    String votePlot = "vote";
    String plotTP = "tp";
    String removePlot = "remove";
    static String creative_world_name = "plot_world";

    public PV_Command(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission("pvote.admin")){
                if (label.equalsIgnoreCase(pvote)) {
                    try {
                        if (args[0].equalsIgnoreCase(addType)) {
                            plotVoting.db.addType(args[1],player);
                        }else if (args[0].equalsIgnoreCase(listType)) {
                            plotVoting.db.selectAllType(player);
                        }else if(args[0].equalsIgnoreCase(registerPlot)){
                            if(player.getLocation().getWorld().getName().equals(creative_world_name)){
                                plotVoting.db.registerPlot(args[1],player);
                            }else{
                                player.sendMessage(ChatColor.RED + "คำสั่งนี้ใช้ได้เฉพาะโลกสร้างสรรค์เท่านั้น (creative_world)");
                            }
                        }else if(args[0].equalsIgnoreCase(topPlot)){
                            plotVoting.db.getTopPlotByType(args[1],player);
                        }else if(args[0].equalsIgnoreCase(votePlot)){
                            plotVoting.db.votePlot(Integer.parseInt(args[1]),player);
                        }else if(args[0].equalsIgnoreCase(plotTP)){
                            if(player.getLocation().getWorld().getName().equals(creative_world_name)){
                                plotVoting.db.plotTp(Integer.parseInt(args[1]),player);
                            }else{
                                player.sendMessage(ChatColor.RED + "คำสั่งนี้ใช้ได้เฉพาะโลกสร้างสรรค์เท่านั้น (creative_world)");
                            }
                        }else if(args[0].equalsIgnoreCase(removePlot)){
                            plotVoting.db.removePlot(Integer.parseInt(args[1]),player);
                        }
                    }catch (Exception e){
                        sender.sendMessage(ChatColor.GREEN+"=========== Use this command =========== ");
                        sender.sendMessage(ChatColor.GREEN+"/pvote " + addType + " <type_name>  "+ ChatColor.YELLOW +"#add new type");
                        sender.sendMessage(ChatColor.GREEN+"/pvote " + listType + ChatColor.YELLOW +" #show all types");
                        sender.sendMessage(ChatColor.GREEN+"/pvote " + registerPlot + " <type_name> "+ ChatColor.YELLOW +" #add plot to type");
                        sender.sendMessage(ChatColor.GREEN+"/pvote " + topPlot + " <type_name> "+ ChatColor.YELLOW +"#show top plot by type");
                        sender.sendMessage(ChatColor.GREEN+"/pvote " + votePlot + " <plot_id> "+ ChatColor.YELLOW +"#vote plot");
                        sender.sendMessage(ChatColor.GREEN+"/pvote " + plotTP + " <plot_id> "+ ChatColor.YELLOW +"#teleport to plot");
                        sender.sendMessage(ChatColor.GREEN+"/pvote " + removePlot + " <plot_id> "+ ChatColor.YELLOW +"#remove your plot");
                        sender.sendMessage(ChatColor.GREEN+"======================================== ");
                        return false;
                    }
                }
            }else{
                sender.sendMessage("You don't have permission!");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only Player can use this command.");
            return false;
        }
        return false;
    }
}
