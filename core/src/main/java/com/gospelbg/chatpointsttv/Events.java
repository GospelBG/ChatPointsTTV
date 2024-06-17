package com.gospelbg.chatpointsttv;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


import org.bukkit.entity.EntityType;

public class Events {
    static ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    static Logger log = plugin.log;

    public static void displayTitle(String user, String action, String rewardName, ChatColor titleColor, ChatColor userColor, ChatColor isBold, String extra) {
        plugin.getServer().getOnlinePlayers().forEach (p -> {
            if (p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                ChatPointsTTV.getUtils().displayTitle(p, userColor + user, action + " " + isBold + titleColor + rewardName + "\n" + extra);
            }
        });
    }

    public static void runAction(String action_str) throws Exception {
        if (action_str.startsWith("SPAWN")) {
            
            List<String> action = Arrays.asList(action_str.split(" "));
            Bukkit.getScheduler().runTask(plugin, () -> {
                log.info("Spawning...");
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                        for (int i = 0; i < Integer.valueOf(action.get(2)); i++) {
                            p.getWorld().spawnEntity(p.getLocation(), EntityType.valueOf(action.get(1).toUpperCase()));
                        }
                    }
                }
                    });

        } else if (action_str.startsWith("RUN")) {
            List<String> action = Arrays.asList(action_str.split(" "));
            String text = "";
            String runAs = action.get(1);
            
            for (int i = 0; i < action.size(); i++) {
                if (i <= 1) continue;
                
                text += " " + action.get(i);
            }
            text = text.trim();

            final String cmd = text.replace("/", "");
            log.info("Running command: \""+ cmd + "\"...");

            if (runAs.equalsIgnoreCase("CONSOLE")) {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
            } else  {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.hasPermission(ChatPointsTTV.permissions.TARGET.permission_id)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Bukkit.dispatchCommand(p, cmd);
                            }
                        }.runTask(plugin);
                        return;    
                    }
                }
            throw new Exception("Couldn't find player " + runAs);
            }
        }   
    }
}
