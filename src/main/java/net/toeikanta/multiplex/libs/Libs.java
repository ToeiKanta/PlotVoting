package net.toeikanta.multiplex.libs;

import net.toeikanta.multiplex.PlotVoting;

public class Libs {
    public static boolean isWorldAllowed(String world_name){
       return  PlotVoting.plugin.getConfig().getStringList("world_whitelist").contains(world_name);
    }
}
