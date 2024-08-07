package me.gosdev.chatpointsttv.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import me.gosdev.chatpointsttv.ChatPointsTTV;

public class SpawnRunnable implements Runnable {
    public EntityType entity;
    public int iterations = 0;
    public int amount;
    public String entityName;
    public Integer explosionTime = null;
    public Player p;
    public int id;

    @Override
    public void run() {
        iterations++;
        if (iterations >= amount) Bukkit.getScheduler().cancelTask(id);
        if (entity == EntityType.PRIMED_TNT) {
            TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(p.getLocation(), EntityType.PRIMED_TNT);
            if (explosionTime != null) tnt.setFuseTicks(explosionTime);
        } else {
            Entity e = p.getWorld().spawnEntity(p.getLocation(), entity);
            e.setGlowing(ChatPointsTTV.shouldMobsGlow);
            if (ChatPointsTTV.nameSpawnedMobs) {
                e.setCustomName(entityName);
                e.setCustomNameVisible(true);
            }
        }
    }
}
    