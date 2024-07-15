package me.gosdev.chatpointsttv;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class Events {
    static ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    static Logger log = plugin.log;

    public static void displayTitle(String user, String action, String rewardName, ChatColor titleColor, ChatColor userColor, Boolean isBold) {
        plugin.getServer().getOnlinePlayers().forEach (p -> {
            if (p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                ChatPointsTTV.getUtils().displayTitle(p.getPlayer(), user, action, rewardName, isBold, userColor, titleColor);
            }
        });
    }

    public static void runAction(String action, String args, String user) throws Exception {
        List<String> cmd  = Arrays.asList(args.split(" "));
        switch(action.toUpperCase()) {
            case "SPAWN":
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        if (p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                            for (int i = 0; i < Integer.valueOf(cmd.get(1)); i++) {
                                Entity e = p.getWorld().spawnEntity(p.getLocation(), EntityType.valueOf(cmd.get(0).toUpperCase()));

                                e.setGlowing(ChatPointsTTV.shouldMobsGlow);
                                if (ChatPointsTTV.nameSpawnedMobs) {
                                    e.setCustomName(user);
                                    e.setCustomNameVisible(true);
                                } 
                            }
                        }
                    }
                });
                break;

            case "RUN":
                String text = "";
                String runAs = cmd.get(0);
                
                for (int i = 0; i < cmd.size(); i++) {
                    if (i <= 0) continue;
                    
                    text += " " + cmd.get(i);
                }
                text = text.trim();

                final String command = text.replace("/", "");

                if (runAs.equalsIgnoreCase("CONSOLE")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
                        }
                    }.runTask(plugin);
                } else  {
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        if (p.hasPermission(ChatPointsTTV.permissions.TARGET.permission_id)) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    Bukkit.dispatchCommand(p, command);
                                }
                            }.runTask(plugin);
                            return;    
                        }
                    }
                    throw new Exception("Couldn't find player " + runAs);
                }
                break;

            case "GIVE":
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                        try {
                            ItemStack item = new ItemStack(Material.valueOf(cmd.get(0).toUpperCase()), Integer.parseInt(cmd.get(1)));
                            p.getInventory().addItem(item);
                        } catch (IllegalArgumentException e) {
                            log.warning("Couldn't fetch item " + cmd.get(0));
                        }
                        
                    }
                }
        }

    }
}
