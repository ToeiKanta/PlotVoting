package net.toeikanta.multiplex.libs;

import net.toeikanta.multiplex.PlotVoting;

public class Libs {
    // ตรวจสอบ โลก ว่าอยู่ใน Whitelist หรือไม่
    public static boolean isWorldAllowed(String world_name){
       return  PlotVoting.plugin.getConfig().getStringList("world_whitelist").contains(world_name);
    }
}
