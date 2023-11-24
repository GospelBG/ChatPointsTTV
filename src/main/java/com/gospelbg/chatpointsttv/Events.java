package com.gospelbg.chatpointsttv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;

import com.gospelbg.chatpointsttv.Main;

public class Events {
    public static void spawnMob(EntityType entity, int amount) {
        for (Player p : Main.getPlugin().getServer().getOnlinePlayers()) {
            for (int i = 0; i < amount; i++) {
                p.getWorld().spawnEntity(p.getLocation(), entity);
            }
        }
    }

    public static void runCommand(CommandSender runAs, String cmd) {
        Bukkit.dispatchCommand(runAs, cmd);
    }
}
