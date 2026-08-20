package me.gosdev.chatpointsttv.Spigot;

import java.util.ArrayList;
import java.util.List;

import me.gosdev.chatpointsttv.ChatPointsTTVSpigot;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.gosdev.chatpointsttv.Generic.GenericLoader;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Generic.GenericSender;

public class SpigotLoader implements GenericLoader {
    SpigotLog logger = new SpigotLog();

    @Override
    public String getVersion() {
        return ChatPointsTTVSpigot.getPlugin().getDescription().getVersion();
    }

    @Override
    public List<GenericPlayer> getOnlinePlayers() {
        List<GenericPlayer> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            list.add(new SpigotPlayer(p));
        }

        return list;
    }

    @Override
    public SpigotLog getLogger() {
        return logger;
    }

    @Override
    public GenericSender consoleSender() {
        return new SpigotSender(Bukkit.getConsoleSender());
    }

    @Override
    public GenericPlayer getPlayer(String username) {
        return new SpigotPlayer(Bukkit.getPlayer(username));
    }

    @Override
    public List<String> getSounds() {
        List<String> sounds = new ArrayList<>();

        for (Sound s : Sound.values()) {
            sounds.add(s.name());
        }

        return sounds;
    }

    @Override
    public List<String> getEntities() {
        List<String> entities = new ArrayList<>();

        for (EntityType e : EntityType.values()) {
            entities.add(e.name());
        }

        return entities;
    }

    @Override
    public List<String> getPotionEffects() {
        List<String> effects = new ArrayList<>(); 
        for (PotionEffectType e : PotionEffectType.values()) {
            effects.add(e.getName());
        }
        
        return effects;
    }

    @Override
    public List<String> getItems() {
        List<String> items = new ArrayList<>();
        for (org.bukkit.Material m : org.bukkit.Material.values()) {
            items.add(m.name());
        }
        return items;
    }

    @Override
    public void runTask(Runnable task) {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), task);
    }

    @Override
    public int scheduleSyncRepeatingTask(Runnable task, int delay, int period) {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(ChatPointsTTVSpigot.getPlugin(), task, delay, period);
    }

    @Override
    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override
    public void spawnEntity(GenericPlayer target, String entity, boolean shouldGlow, String name) {
        Bukkit.getScheduler().runTask(ChatPointsTTVSpigot.getPlugin(), () -> {
            Player p = Bukkit.getPlayer(target.getUUID());
            if (p != null) {
                Entity e = p.getWorld().spawnEntity(p.getLocation(), EntityType.valueOf(entity));
                e.setGlowing(shouldGlow);
                if (name != null) {
                    e.setCustomName(name);
                    e.setCustomNameVisible(true);
                }
            }
        });
    }
}
