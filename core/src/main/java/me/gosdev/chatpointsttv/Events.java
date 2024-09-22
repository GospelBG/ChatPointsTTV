package me.gosdev.chatpointsttv;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.gosdev.chatpointsttv.ChatPointsTTV.alert_mode;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;
import me.gosdev.chatpointsttv.Utils.SpawnRunnable;
import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.entity.EntityType;

public class Events {
    static ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    static Logger log = plugin.log;

    static Utils utils = ChatPointsTTV.getUtils();

    public static void setAlertMode(alert_mode alertMode) {
        ChatPointsTTV.alertMode = alertMode;
    }

    public static void showIngameAlert(String user, String action, String rewardName, ChatColor titleColor, ChatColor userColor, Boolean isBold) {
        ComponentBuilder builder = new ComponentBuilder(user).color(userColor).bold(isBold);
        builder.append(" " + action).color(titleColor);
        builder.append(" " + rewardName).color(userColor);

        switch (ChatPointsTTV.alertMode) {
            case CHAT:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
                    utils.sendMessage(p, builder.create());
                }
                break;
            case TITLE:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
                    utils.displayTitle(p.getPlayer(), user, action, rewardName, isBold, userColor, titleColor);
                };
                break;

            case ALL:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
                    utils.sendMessage(p, builder.create());
                    utils.displayTitle(p.getPlayer(), user, action, rewardName, isBold, userColor, titleColor);
                }
                break;
                
            default:
                return;                
        }
    }

    public static void runAction(String action, String args, String user) throws Exception {
        List<String> cmd = Arrays.asList(args.split(" "));
        switch(action.toUpperCase()) {
            case "SPAWN":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(permissions.TARGET.permission_id)) continue;
                    
                    SpawnRunnable entityRunnable = new SpawnRunnable();
                    entityRunnable.entity = EntityType.valueOf(cmd.get(0));
                    try {
                        entityRunnable.amount = Integer.parseInt(cmd.get(1));
                    } catch (NumberFormatException  | ArrayIndexOutOfBoundsException e) {
                        entityRunnable.amount = 1;
                    }
                    entityRunnable.entityName = user;
                    entityRunnable.p = p;
    
                    entityRunnable.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, entityRunnable, 0, 0);
                }
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
                            int amount;
                            try {
                                amount = Integer.parseInt(cmd.get(1));
                            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                                amount = 1;
                            }
                            ItemStack item = new ItemStack(Material.valueOf(cmd.get(0).toUpperCase()), amount);
                            p.getInventory().addItem(item);
                        } catch (IllegalArgumentException e) {
                            log.warning("Couldn't fetch item " + cmd.get(0));
                        }
                        
                    }
                }
                break;
            
            case "TNT":
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

                    SpawnRunnable tntRunnable = new SpawnRunnable();
                    tntRunnable.entity = EntityType.PRIMED_TNT;
                    tntRunnable.amount = Integer.valueOf(cmd.get(0));
                    try {
                        tntRunnable.explosionTime = Integer.valueOf(cmd.get(1));
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {}
                    
                    tntRunnable.p = p;
                    tntRunnable.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, tntRunnable, 0, 2);
                }
                break;

            default:
                log.warning("No such action: " + action.toUpperCase());
                return;
        }
    }
}