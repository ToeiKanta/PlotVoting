package net.toeikanta.multiplex;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Multiplex extends JavaPlugin {


    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exists.
//        this.saveDefaultConfig();
//        getCommand("pvote").setExecutor(new Commands(this));
        getLogger().info("onEnable has been invoked!");
        getLogger().info("onEnable has been invoked!");
        getLogger().info("onEnable has been invoked!");
        getLogger().info("onEnable has been invoked!");
        getLogger().info("onEnable has been invoked!");
        getLogger().info("onEnable has been invoked!");
        this.getCommand("pvote").setExecutor(this);
        // TODO : Do something if your plugin needs it (registering commands / listeners)
    }

    @Override
    public void onDisable() {
        // TODO : Do something if your plugin needs it (saving custom configs, clearing cache, closing connections...)
    }

    String pvote = "pvote";
    String addGroup = "addgroup";
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (label.equalsIgnoreCase(pvote)) {
                if (args[0].equalsIgnoreCase(addGroup)) {
                    Player player = (Player) sender;
                    if (player.hasPermission("pvote.addgroup")) {
                        sender.sendMessage(ChatColor.YELLOW + "test command worked. " + args[1]);
                    } else {
                        sender.sendMessage("You don't have permission!");
                        return false;
                    }
                }else {
                    sender.sendMessage("You must provide cmd!");
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only Player can use this command.");
        }
        return false;
    }
}
