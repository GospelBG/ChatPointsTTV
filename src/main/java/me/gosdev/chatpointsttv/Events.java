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
    public static void setAlertMode(alert_mode alertMode) {
        ChatPointsTTV.alertMode = alertMode;
    }

    public static String getEventString(rewardType type, String chatter, String channel, Optional<String> event) {
        String str = chatter + " " + ChatPointsTTV.getRedemptionString(type) + " ";

        if (event.isPresent()) {
            str += event.get();
        }

        if (type != rewardType.FOLLOW && type != rewardType.RAID) {
            str += " to ";
        }

        if (str.contains("{CHANNEL}")) {
            str = str.replaceAll("\\{CHANNEL\\}", channel);
        } else {
            str += channel;
        }

        str = str.replaceAll("\\{AMOUNT\\}", event.get());

        return str;
    }

    public static void onEvent(rewardType type, Reward reward, String chatter, String channel, Optional<String> event) {
        if (ChatPointsTTV.logEvents) Bukkit.getConsoleSender().sendMessage(getEventString(type, chatter, channel, event));
        if (ChatPointsTTV.getTwitch().ignoreOfflineStreamers) {
            for (Channel ch : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                if (ch.getChannelUsername().equals(channel) && !ch.isLive()) return; // Return if channel matches and it's offline.
            }
        }
        if (ChatPointsTTV.alertMode.equals(alert_mode.ALL) || ChatPointsTTV.alertMode.equals(alert_mode.TITLE)) {
            showIngameAlert(chatter, ChatPointsTTV.getRedemptionString(type), event.orElse(null));
        }

        for (String cmd : reward.getCommands()) {
            cmd = cmd.replace("{USER}", chatter);
            if (type.equals(rewardType.CHEER) || type.equals(rewardType.GIFT) || type.equals(rewardType.RAID)) {
                cmd = cmd.replace("{AMOUNT}", event.get());
            }
            
            String[] parts = cmd.split(" ");

            if (parts.length <= 1) {
                ChatPointsTTV.log.warning("Invalid command: " + parts[0]);
                continue;
            }
            try {
                switch (parts[0].toUpperCase()) {
                    case "SPAWN":
                        spawnAction(parts[1], Optional.ofNullable(parts.length > 2 ? Integer.valueOf(parts[2]) : null), Optional.ofNullable(parts.length > 3 ? parts[3] : null), chatter);
                        break;
                    case "RUN":
                        String text = "";
                        for (int i = 2; i < parts.length; i++) {
                            ChatPointsTTV.log.info(parts[i]);                    
                            text += " " + parts[i];
                        }
                        text = text.trim();
                        runAction(parts[1], text, channel);
                        break;
                    case "GIVE":
                        giveAction(parts[1], Optional.ofNullable(parts.length > 2 ? Integer.valueOf(parts[2]) : null), Optional.ofNullable(parts.length > 3 ? parts[3] : null));
                        break;
                    case "TNT":
                        tntAction(Integer.parseInt(parts[1]), Optional.ofNullable(parts.length > 2 ? Integer.valueOf(parts[2]) : null));
                        break;
                    default:
                        ChatPointsTTV.log.warning("Invalid action: " + parts[0]);
                        break;
                }
            } catch (NumberFormatException e) {
                ChatPointsTTV.log.warning("Invalid amount: " + parts[2]);
            }
        }
    }

    public static void showIngameAlert(String user, String action, String rewardName) {
        Boolean bold = ChatPointsTTV.getTwitch().override_msgRewardBold != null ? ChatPointsTTV.getTwitch().override_msgRewardBold : ChatPointsTTV.rewardBold;
        ChatColor userColor = ChatPointsTTV.getTwitch().override_msgUserColor != null ? ChatPointsTTV.getTwitch().override_msgUserColor : ChatPointsTTV.user_color;
        ChatColor actionColor = ChatPointsTTV.getTwitch().override_msgActionColor != null ? ChatPointsTTV.getTwitch().override_msgActionColor : ChatPointsTTV.action_color;
        if (ChatPointsTTV.alertMode.equals(ChatPointsTTV.alert_mode.NONE)) return;
        ComponentBuilder builder = new ComponentBuilder(user).color(userColor).bold(bold);
        builder.append(" " + action).color(ChatPointsTTV.action_color);
        if (rewardName != null) builder.append(" " + rewardName).color(userColor);

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
                    p.sendTitle(userColor + user, action + actionColor + " " + (bold ? ChatColor.BOLD : "") + rewardName, 10, 70, 20);
                };
                break;

            case ALL:
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
                    p.spigot().sendMessage(builder.create());
                    p.sendTitle(userColor + user, action + actionColor + " " + (bold ? ChatColor.BOLD : "") + rewardName, 10, 70, 20);
                }
                break;

            default:
                ChatPointsTTV.log.warning("Invalid mode: " + action.toUpperCase());
                break;
        }
    }


    public static void spawnAction(String entity, Optional<Integer> amount, Optional<String> player, String chatter) {
        if (!EnumUtils.isValidEnum(EntityType.class, entity.toUpperCase())) {
            ChatPointsTTV.log.warning("Entity " + entity.toUpperCase() + " does not exist.");
            return;
        } 

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (player.isPresent()) { // Is targeting a player?
                Player query = Bukkit.getPlayer(player.get());
                if (query == null || !query.isOnline()) {
                    ChatPointsTTV.log.warning("Couldn't find player " + player.get() + ".");
                    return;
                } 
                if (!p.getName().equalsIgnoreCase(player.get())) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            SpawnRunnable entityRunnable = new SpawnRunnable();
            entityRunnable.entity = EntityType.valueOf(entity.toUpperCase());

            entityRunnable.amount = amount.orElse(1);
            if (ChatPointsTTV.getTwitch().overrideNameSpawnedMobs != null ? ChatPointsTTV.getTwitch().overrideNameSpawnedMobs : ChatPointsTTV.nameSpawnedMobs) {
                entityRunnable.entityName = chatter;
            }
            
            entityRunnable.p = p;
            entityRunnable.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(ChatPointsTTV.getPlugin(), entityRunnable, 0, 0);
        }
    }

    public static void runAction(String runAs, String cmd, String chatter) {
        final String command = cmd.replace("/", "");
        ChatPointsTTV.log.info("Running command: " + command);

        if (runAs.equalsIgnoreCase("CONSOLE")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
                }
            }.runTask(ChatPointsTTV.getPlugin());
        } else if (runAs.equalsIgnoreCase("TARGET")) {
            for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
                if (p.hasPermission(ChatPointsTTV.permissions.TARGET.permission_id)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.dispatchCommand(p, command);
                        }
                    }.runTask(ChatPointsTTV.getPlugin());
                    return;    
                }
            }
            ChatPointsTTV.log.warning("Couldn't find any target players!");
        } else {
            ChatPointsTTV.log.warning("Invalid parameter: " + runAs);
        }
    }

    public static void giveAction(String itemName, Optional<Integer> amount, Optional<String> player) {
        for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
            if (player.isPresent()) { // Is targeting a player?
                Player query = Bukkit.getPlayer(player.get());
                if (query == null || !query.isOnline()) {
                    ChatPointsTTV.log.warning("Couldn't find player " + player.get() + ".");
                    return;
                }
                if (!p.getName().equalsIgnoreCase(player.get())) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            
            if (!EnumUtils.isValidEnum(Material.class, itemName)) {
                ChatPointsTTV.log.warning("Item " + itemName + " does not exist.");
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
            tntRunnable.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(ChatPointsTTV.getPlugin(), tntRunnable, 0, 2);
        }
    }
}
