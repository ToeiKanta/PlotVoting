package net.toeikanta.multiplex;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {

    PlotVoting plotVoting;

    String pvote = "pvote";
    String addType = "add";
    String listType = "list";
    String registerPlot = "addplot";
    String topPlot = "top";

    Command(PlotVoting plotVoting){
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
                            plotVoting.db.registerPlot(args[1],player);
                        }else if(args[0].equalsIgnoreCase(topPlot)){
                            plotVoting.db.getTopPlotByType(args[1],player);
                        }
                    }catch (Exception e){
                        sender.sendMessage("=========== Use this command =========== ");
                        sender.sendMessage("/pvote " + addType + " <type_name>  #add new type");
                        sender.sendMessage("/pvote " + listType + " #show all types");
                        sender.sendMessage("/pvote " + registerPlot + " <type_name> #add plot to type");
                        sender.sendMessage("/pvote " + topPlot + " <type_name> #show top plot by type");
                        sender.sendMessage("======================================== ");
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
