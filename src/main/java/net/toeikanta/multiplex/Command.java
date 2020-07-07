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

    Command(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission("pvote.admin")){
                if (label.equalsIgnoreCase(pvote)) {
                    if(args.length == 0){
                        sender.sendMessage("=========== Use this command =========== ");
                        sender.sendMessage("/pvote " + addType + " <type_name>  #add new type");
                        sender.sendMessage("/pvote " + listType + " #show all types");
                        sender.sendMessage("======================================== ");
                        return false;
                    }
                    if (args[0].equalsIgnoreCase(addType)) {
                        plotVoting.db.addType(args[1],player);
                    }else if (args[0].equalsIgnoreCase(listType)) {
                        plotVoting.db.selectAllType(player);
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
