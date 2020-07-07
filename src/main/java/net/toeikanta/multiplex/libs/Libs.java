package net.toeikanta.multiplex.libs;

import net.toeikanta.multiplex.PlotVoting;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class Libs {
    // ตรวจสอบ โลก ว่าอยู่ใน Whitelist หรือไม่
    public static boolean isWorldAllowed(String world_name){
       return  PlotVoting.plugin.getConfig().getStringList("world_whitelist").contains(world_name);
    }
    // จุดพรุ
    public static void spawnFireWork(Player sender){
        Firework fireWork = (Firework) sender.getLocation().getWorld().spawnEntity(sender.getLocation(), EntityType.FIREWORK);
        FireworkMeta meta = (FireworkMeta) fireWork.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(Color.PURPLE).withColor(Color.GREEN).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
        meta.setPower(1);
        fireWork.setFireworkMeta(meta);
    }
    // เล่นเสียง level up
    public static void playSoundLevelUp(Player sender){
        sender.playSound(sender.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,100f,1f);
    }
}
