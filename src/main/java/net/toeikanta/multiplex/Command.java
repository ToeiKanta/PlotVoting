package net.toeikanta.multiplex;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {

    PlotVoting plotVoting;

    String pvote = "pvote";
    String addGroup = "addgroup";

    Command(PlotVoting plotVoting){
        this.plotVoting = plotVoting;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission("pvote.admin")){
                if (label.equalsIgnoreCase(pvote)) {
                    if (args[0].equalsIgnoreCase(addGroup)) {
                            sender.sendMessage(ChatColor.YELLOW + "test command worked. " + args[1]);
                    }else {
                        sender.sendMessage("You must provide cmd!");
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
