package net.toeikanta.multiplex;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlotVoting extends JavaPlugin {
    public DatabaseHandler db;

    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exists.
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        // register command
        this.getCommand("pv").setExecutor(new Command(this));
        Logger.print("enabled " + getConfig().getString("name") + " plugin!");
        this.db = new DatabaseHandler(this);
    }

    @Override
    public void onDisable() {
        Logger.print("disabled PlotVoting plugin!");
    }

}
