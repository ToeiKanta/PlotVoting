package net.toeikanta.multiplex;

import net.toeikanta.multiplex.commands.PV_Command;
import net.toeikanta.multiplex.events.ClickEvent;
import net.toeikanta.multiplex.libs.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlotVoting extends JavaPlugin {
    public DatabaseHandler db;

    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exists.
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        // register command
        getCommand("plotvote").setExecutor(new PV_Command(this));
        getCommand("pv").setExecutor(new PV_Command(this));
        getCommand("pvote").setExecutor(new PV_Command(this));
        getCommand("pva").setExecutor(new PV_Command(this));

        getServer().getPluginManager().registerEvents(new ClickEvent(this), this);
        Logger.print("enabled " + getConfig().getString("name") + " plugin!");
        this.db = new DatabaseHandler(this);
    }

    @Override
    public void onDisable() {
        Logger.print("disabled PlotVoting plugin!");
    }

}
