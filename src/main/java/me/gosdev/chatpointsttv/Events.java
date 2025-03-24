package me.gosdev.chatpointsttv;

import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.gosdev.chatpointsttv.ChatPointsTTV.alert_mode;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;
import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.SpawnRunnable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Events {
    static ChatPointsTTV plugin = ChatPointsTTV.getPlugin();

    public static void setAlertMode(alert_mode alertMode) {
        ChatPointsTTV.alertMode = alertMode;
    }

    public static String getEventString(rewardType type, String chatter, String channel, Optional<String> event) {
        String str = chatter + ChatPointsTTV.getRedemptionString(type);

        if (event.isPresent()) {
            str += event.get();
        }

        if (type != rewardType.FOLLOW && type != rewardType.RAID) {
            str += " to ";
        }

        str += channel;

        return str;
    }

    public static void onEvent(rewardType type, Reward reward, String chatter, String channel, Optional<String> event) {
        if (ChatPointsTTV.logEvents) Bukkit.getConsoleSender().sendMessage(getEventString(type, chatter, channel, event));
        if (plugin.getTwitch().ignoreOfflineStreamers) {
            for (Channel ch : plugin.getTwitch().getListenedChannels().values()) {
                if (ch.getChannelUsername().equals(channel) && !ch.isLive()) return; // Return if channel matches and it's offline.
            }
        }
        if (ChatPointsTTV.alertMode.equals(alert_mode.ALL) || ChatPointsTTV.alertMode.equals(alert_mode.TITLE)) {
            showIngameAlert(chatter, ChatPointsTTV.getRedemptionString(type), event.orElse(null));
        }

        for (String cmd : reward.getCommands()) {
            String[] parts = cmd.split(" ");

            if (parts.length <= 1) {
                plugin.log.warning("Invalid command: " + parts[0]);
                continue;
            }

            switch (parts[0].toUpperCase()) {
                case "SPAWN":
                    spawnAction(parts[1], Optional.ofNullable(Integer.valueOf(parts[2])), Optional.ofNullable(parts[3]), chatter);
                    break;
                case "RUN":
                    String text = "";
                    for (int i = 2; i < parts.length; i++) {
                        if (i <= 0) continue;
                        
                        text += " " + parts[i];
                    }
                    text = text.trim();
                    runAction(parts[1], text, channel);
                    break;
                case "GIVE":
                    giveAction(parts[1], Optional.ofNullable(Integer.valueOf(parts[2])), Optional.ofNullable(parts[3]));
                    break;
                case "TNT":
                    plugin.log.warning("Invalid action: " + parts[0]);
                    break;
                default:
                    plugin.log.warning("Invalid action: " + parts[0]);
                    break;
            }
        }
    }

    public static void showIngameAlert(String user, String action, String rewardName) {
        if (ChatPointsTTV.alertMode.equals(ChatPointsTTV.alert_mode.NONE)) return;
        ComponentBuilder builder = new ComponentBuilder(user).color(ChatPointsTTV.user_color).bold(ChatPointsTTV.rewardBold);
        builder.append(" " + action).color(ChatPointsTTV.action_color);
        if (rewardName != null) builder.append(" " + rewardName).color(ChatPointsTTV.user_color);

        switch (ChatPointsTTV.alertMode) {
            case CHAT:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
                    p.spigot().sendMessage(builder.create());
                }
                break;
            case TITLE:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
                    p.sendTitle(ChatPointsTTV.action_color + user, action + ChatPointsTTV.action_color + " " + (ChatPointsTTV.rewardBold ? ChatColor.BOLD : ChatColor.RESET) + rewardName, 10, 70, 20);
                };
                break;

            case ALL:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
                    p.spigot().sendMessage(builder.create());
                    p.sendTitle(ChatPointsTTV.action_color + user, action + ChatPointsTTV.action_color + " " + (ChatPointsTTV.rewardBold ? ChatColor.BOLD : ChatColor.RESET) + rewardName, 10, 70, 20);
                }
                break;

            default:
                plugin.log.warning("Invalid mode: " + action.toUpperCase());
                break;
        }
    }


    public static void spawnAction(String entity, Optional<Integer> amount, Optional<String> player, String chatter) {
        if (!EnumUtils.isValidEnum(EntityType.class, entity.toUpperCase())) {
            plugin.log.warning("Entity " + entity.toUpperCase() + " does not exist.");
            return;
        } 

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (player.isPresent()) { // Is targeting a player?
                Player query = Bukkit.getPlayer(player.get());
                if (query == null || !query.isOnline()) {
                    plugin.log.warning("Couldn't find player " + player.get() + ".");
                    return;
                } 
                if (!p.getName().equalsIgnoreCase(player.get())) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            SpawnRunnable entityRunnable = new SpawnRunnable();
            entityRunnable.entity = EntityType.valueOf(entity.toUpperCase());

            entityRunnable.amount = amount.orElse(1);
            if (ChatPointsTTV.nameSpawnedMobs) entityRunnable.entityName = chatter;
            
            entityRunnable.p = p;
            entityRunnable.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, entityRunnable, 0, 0);
        }
    }

    public static void runAction(String runAs, String cmd, String chatter) {
        String text = "";

        final String command = text.replace("/", "");

        if (runAs.equalsIgnoreCase("CONSOLE")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
                }
            }.runTask(plugin);
        } else if (runAs.equalsIgnoreCase("TARGET")) {
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
            plugin.log.warning("Couldn't find any target players!");
        } else {
            plugin.log.warning("Invalid parameter: " + runAs);
        }
    }

    public static void giveAction(String itemName, Optional<Integer> amount, Optional<String> player) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (player.isPresent()) { // Is targeting a player?
                Player query = Bukkit.getPlayer(player.get());
                if (query == null || !query.isOnline()) {
                    plugin.log.warning("Couldn't find player " + player.get() + ".");
                    return;
                }
                if (!p.getName().equalsIgnoreCase(player.get())) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            
            if (!EnumUtils.isValidEnum(Material.class, itemName)) {
                plugin.log.warning("Item " + itemName + " does not exist.");
                return;
            }
            ItemStack item = new ItemStack(Material.valueOf(itemName), amount.orElse(1));
            p.getInventory().addItem(item);
        }
    }

    public static void tntAction(int amount, Optional<Integer> explosionTime) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            SpawnRunnable tntRunnable = new SpawnRunnable();
            tntRunnable.entity = EntityType.PRIMED_TNT;
            tntRunnable.amount = amount;
            if (explosionTime.isPresent()) {
                tntRunnable.explosionTime = explosionTime.get();
            }
            tntRunnable.p = p;
            tntRunnable.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, tntRunnable, 0, 2);
        }
    }
}
