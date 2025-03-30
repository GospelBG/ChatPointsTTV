package me.gosdev.chatpointsttv;

import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.gosdev.chatpointsttv.EventActions.Action;
import me.gosdev.chatpointsttv.EventActions.EffectAction;
import me.gosdev.chatpointsttv.EventActions.GiveAction;
import me.gosdev.chatpointsttv.EventActions.RunCmdAction;
import me.gosdev.chatpointsttv.EventActions.SpawnAction;
import me.gosdev.chatpointsttv.EventActions.TntAction;
import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;

public class Events {
    public static String getEventMessage(Platforms platform, rewardType type, String chatter, String channel, Optional<String> event) {
        String str = ChatPointsTTV.strings.get("str_" + platform.toString().toLowerCase() + "_"+type.name().toLowerCase());

        str = LocalizationUtils.replacePlaceholders(str, chatter, channel, event.orElse(null), platform);

        return str;
    }

    public static void onEvent(Platforms platform, rewardType type, Reward reward, String chatter, String channel, Optional<String> event) {
        new Thread (()-> {
            String eventMsg = getEventMessage(platform, type, chatter, channel, event);
            String errorStr = "There was an error running a " + type + " action: ";
            if (ChatPointsTTV.logEvents) Bukkit.getConsoleSender().sendMessage(eventMsg);
            if (ChatPointsTTV.getTwitch().ignoreOfflineStreamers) {
                for (Channel ch : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    if (ch.getChannelUsername().equals(channel) && !ch.isLive()) return; // Return if channel matches and it's offline.
                }
            }

            Boolean shouldGlow;
            Boolean nameSpawnedMobs;
            AlertMode alertMode;

            if (platform.equals(Platforms.TWITCH)) {
                shouldGlow = ChatPointsTTV.getTwitch().shouldMobsGlow;
                nameSpawnedMobs = ChatPointsTTV.getTwitch().nameSpawnedMobs;
                alertMode = ChatPointsTTV.getTwitch().alertMode;
            } else {
                shouldGlow = ChatPointsTTV.shouldMobsGlow;
                nameSpawnedMobs = ChatPointsTTV.nameSpawnedMobs;
                alertMode = ChatPointsTTV.alertMode;
            }

            if (!alertMode.equals(AlertMode.NONE)) { // In-game alert
                String chatMessage = eventMsg;
    
                String title = LocalizationUtils.replacePlaceholders(ChatPointsTTV.strings.get("title"), chatter, channel, event.orElse(null), platform);
                String subtitle = LocalizationUtils.replacePlaceholders(ChatPointsTTV.strings.get("sub_twitch_" + type.toString().toLowerCase()), chatter, channel, event.orElse(null), platform);
        
                switch (alertMode) {
                    case CHAT:
                        broadcastMessage(chatMessage);
                        break;
        
                    case TITLE:
                        showTitle(title, subtitle);
                        break;
        
                    case ALL:
                        broadcastMessage(chatMessage);
                        showTitle(title, subtitle);
                        break;
        
                    default:
                        ChatPointsTTV.log.warning("Invalid mode: " + ChatPointsTTV.alertMode);
                        break;
                }    
            }
    
            for (String cmd : reward.getCommands()) { // Event actions
                cmd = cmd.replace("{USER}", chatter);
                if (type.equals(rewardType.CHEER) || type.equals(rewardType.GIFT) || type.equals(rewardType.RAID)) {
                    cmd = cmd.replace("{AMOUNT}", event.get());
                }
                
                String[] parts = cmd.split(" ");
    
                if (parts.length <= 1) {
                    ChatPointsTTV.log.warning(errorStr + "Invalid command \"" + parts[0] + "\"");
                    continue;
                }
                try {
                    Action action;
                    Integer amount = null;
                    Player target = null;
                    switch (parts[0].toUpperCase()) {
                        case "SPAWN":
                            if (!EnumUtils.isValidEnum(EntityType.class, parts[1].toUpperCase())) {
                                ChatPointsTTV.log.warning(errorStr + "Entity " + parts[1].toUpperCase() + " does not exist.");
                                continue;
                            }
                            if (parts.length > 2) {
                                amount = Integer.valueOf(parts[2]);
                            }
                            if (parts.length > 3) {
                                target = Bukkit.getPlayer(parts[3]);
                            }
                            action = new SpawnAction(EntityType.valueOf(parts[1]), nameSpawnedMobs ? chatter : null, Optional.ofNullable(amount), target, shouldGlow);
                            break;
                        case "RUN":
                            String text = "";
                            for (int i = 2; i < parts.length; i++) {
                                text += " " + parts[i];
                            }
                            text = text.trim();
                            action = new RunCmdAction(parts[1], text);
                            break;
                        case "GIVE":
                            if (!EnumUtils.isValidEnum(Material.class, parts[1].toUpperCase())) {
                                ChatPointsTTV.log.warning(errorStr + "Item " + parts[1] + " does not exist.");
                                continue;
                            }
                            if (parts.length > 2) {
                                amount = Integer.valueOf(parts[2]);
                            }
                            if (parts.length > 3) {
                                target = Bukkit.getPlayer(parts[3]);
                                if (target == null || !target.isOnline()) {
                                    ChatPointsTTV.log.warning(errorStr + "Couldn't find player " + parts[3] + ".");
                                }
                                continue;
                            }
                            action = new GiveAction(Material.valueOf(parts[1].toUpperCase()), Optional.ofNullable(amount), Optional.ofNullable(target));
                            break;

                        case "EFFECT":
                            PotionEffectType effect = PotionEffectType.getByName(parts[1]);
                            Integer strength = Integer.valueOf(parts[2]);
                            Integer duration = Integer.valueOf(parts[3]);
                            if (effect == null) {
                                ChatPointsTTV.log.warning(errorStr + "Potion effect " + parts[1] + " does not exist.");
                                continue;
                            }
                            if (parts.length > 4) {
                                target = Bukkit.getPlayer(parts[4]);
                                if (target == null || !target.isOnline()) {
                                    ChatPointsTTV.log.warning(errorStr + "Couldn't find player " + parts[3] + ".");
                                }
                            }

                            action = new EffectAction(effect, strength, duration, target);
                            break;

                        case "TNT":
                            Integer fuseTime = null;
                            target = null;
                            if (parts.length > 2) {
                                fuseTime = Integer.valueOf(parts[2]);
                            }
                            if (parts.length > 3) {
                                target = Bukkit.getPlayer(parts[3]);
                                if (target == null || !target.isOnline()) {
                                    ChatPointsTTV.log.warning(errorStr + "Couldn't find player " + parts[3] + ".");
                                    continue;
                                }
                            }
                            action = new TntAction(Integer.parseInt(parts[1]), Optional.ofNullable(fuseTime), Optional.ofNullable(target));
                            break;
                        case "WAIT":
                            try {
                                Thread.sleep((long) (Float.parseFloat(parts[1])*1000));
                            } catch (InterruptedException e) {}
                            continue;
                        default:
                            ChatPointsTTV.log.warning(errorStr + "Invalid action \"" + parts[0] + "\"");
                            return;
                    }
                    action.run();
                } catch (NumberFormatException e) {
                    ChatPointsTTV.log.warning(errorStr + "Invalid amount \"" + e.getMessage().substring(19, e.getMessage().length() - 1)+"\"");
                }
            }
    }).start();
    }


    public static void showTitle(String title, String subtitle) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
            p.sendTitle(title, subtitle, 10, 70, 20);
        }
    }

    public static void broadcastMessage(String msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) continue;
            p.sendMessage(msg);
        }
    }
}
