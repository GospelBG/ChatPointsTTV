package com.gospelbg.chatpointsttv;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;

public class Events {
    public static void spawnMob(EntityType entity, int amount) {
        for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                for (int i = 0; i < amount; i++) {
                    p.getWorld().spawnEntity(p.getLocation(), entity);
                }
            }
        }
    }

    public static void runCommand(String runAs, String cmd) {
        if (runAs.equalsIgnoreCase("CONSOLE")) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
        } else if (runAs.equalsIgnoreCase("TARGET")) {
            for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
                Bukkit.dispatchCommand(p, cmd);
            }
        } else {
            //TODO: ERROR MSG
        }
    }
}
