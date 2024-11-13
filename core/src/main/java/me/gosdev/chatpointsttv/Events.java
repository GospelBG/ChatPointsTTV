package me.gosdev.chatpointsttv;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.EnumUtils;
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

    public static void runAction(String action, String args, String user) {
        args.replace("\\{USER\\}", user);
        List<String> cmd = Arrays.asList(args.split(" "));
        switch(action.toUpperCase()) {
            case "SPAWN":
                try {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!EnumUtils.isValidEnum(EntityType.class, cmd.get(0))) throw new IllegalArgumentException(cmd.get(0)); // Check if entity exists
                        SpawnRunnable entityRunnable = new SpawnRunnable();
                        entityRunnable.entity = EntityType.valueOf(cmd.get(0));

                        if (cmd.size() >= 2) { // Is specifying an amount?
                            try {
                                entityRunnable.amount = Integer.parseInt(cmd.get(1));
                            } catch (Exception e) {
                                throw new NumberFormatException(cmd.get(1));
                            }
                        } else {
                            entityRunnable.amount = 1;
                        }

                        entityRunnable.entityName = user;

                        if (cmd.size() >= 3) { // Is targeting a player?
                            Player query = Bukkit.getPlayer(cmd.get(2));
                            if (query == null || !query.isOnline()) throw new NullPointerException(cmd.get(2));
                            if (!p.getName().equalsIgnoreCase(cmd.get(2))) {
                                continue;
                            }
                        } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

                        entityRunnable.p = p;
                        entityRunnable.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, entityRunnable, 0, 0);
                    }
                } catch (NullPointerException e) {
                    log.warning("Couldn't find player " + e.getMessage() + ".");
                } catch (NumberFormatException e) {
                    log.warning("Invalid amount of entities: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    log.warning("Entity " + e.getMessage() + " does not exist.");
                } catch (Exception e) {
                    e.printStackTrace(); // Unknown error. Print full stack trace
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
                    log.warning("Couldn't find player " + runAs);
                    return;
                }
                break;

            case "GIVE":
                try {
                    for (Player p : plugin.getServer().getOnlinePlayers()) {
                        if (cmd.size() >= 3) { // Is targeting a player?
                            Player query = Bukkit.getPlayer(cmd.get(2));
                            if (query == null || !query.isOnline()) throw new NullPointerException(cmd.get(2));
                            if (!p.getName().equalsIgnoreCase(cmd.get(2))) {
                                continue;
                            }
                        } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

                        int amount;
                        if (cmd.size() >= 2) { // Is specifying an amount?
                            try {
                                amount = Integer.parseInt(cmd.get(1));
                            } catch (Exception e) {
                                throw new NumberFormatException(cmd.get(1));
                            }
                        } else amount = 1;
                        
                        if (!EnumUtils.isValidEnum(Material.class, cmd.get(0))) throw new IllegalArgumentException(cmd.get(0)); // Check if entity exists
                        ItemStack item = new ItemStack(Material.valueOf(cmd.get(0).toUpperCase()), amount);
                        p.getInventory().addItem(item);
                    }
                } catch (NullPointerException e) {
                    log.warning("Couldn't find player " + e.getMessage() + ".");
                } catch (NumberFormatException e) {
                    log.warning("Invalid amount of items: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    log.warning("Item " + e.getMessage() + " does not exist.");
                } catch (Exception e) {
                    e.printStackTrace(); // Unknown error. Print full stack trace
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