package net.toeikanta.multiplex;

import net.toeikanta.multiplex.commands.PV_Command;
import net.toeikanta.multiplex.events.ClickEvent;
import net.toeikanta.multiplex.libs.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class PlotVoting extends JavaPlugin {
    public DatabaseHandler db;
    public static PlotVoting plugin;

    @Override
    public void onEnable() {
        // Copy the config.yml in the plugin configuration folder if it doesn't exists.
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        // register default's command
        getCommand("plotvote").setExecutor(new PV_Command(this));
        getCommand("pv").setExecutor(new PV_Command(this));
        getCommand("pvote").setExecutor(new PV_Command(this));
        // register admin's command
        getCommand("pva").setExecutor(new PV_Command(this));
        // register ClickEvents listener
        getServer().getPluginManager().registerEvents(new ClickEvent(this), this);
        Logger.print("enabled " + getConfig().getString("name") + " plugin!");
        // สร้าง หรือ เริ่มเชื่อมต่อ Database
        this.db = new DatabaseHandler(this);
        plugin = this;
    }

    @Override
    public void onDisable() {
        Logger.print("disabled PlotVoting plugin!");
    }

}
