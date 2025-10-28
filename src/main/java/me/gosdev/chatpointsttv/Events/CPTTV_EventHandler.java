package me.gosdev.chatpointsttv.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.gosdev.chatpointsttv.Actions.BaseAction;
import me.gosdev.chatpointsttv.Actions.DeleteItemsAction;
import me.gosdev.chatpointsttv.Actions.EffectAction;
import me.gosdev.chatpointsttv.Actions.GiveAction;
import me.gosdev.chatpointsttv.Actions.InvShuffleAction;
import me.gosdev.chatpointsttv.Actions.RunCmdAction;
import me.gosdev.chatpointsttv.Actions.SpawnAction;
import me.gosdev.chatpointsttv.Actions.TntAction;
import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.TikTok.TikTokEventType;
import me.gosdev.chatpointsttv.Twitch.Channel;
import me.gosdev.chatpointsttv.Twitch.TwitchEventType;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;

public class CPTTV_EventHandler {
    public static final String EVERYONE = "*";
    public static Map<EventType, ArrayList<Event>> actions = new HashMap<>();

    public static String getEventMessage(Platforms platform, EventType type, String chatter, String channel, Optional<String> event) {
        String str = ChatPointsTTV.strings.get("str_" + platform.toString().toLowerCase() + "_"+type.toString().toLowerCase());
        str = LocalizationUtils.replacePlaceholders(str, chatter, channel, event.orElse(null), platform);

        return str;
    }

    public static void onEvent(Platforms platform, EventType type, Event reward, String chatter, String channel, Optional<String> event) {
        new Thread (()-> {
            String errorStr = "There was an error running a " + type + " action: ";
            if (ChatPointsTTV.logEvents) Bukkit.getConsoleSender().sendMessage(getEventMessage(platform, type, chatter, channel, event));
            if (platform.equals(Platforms.TWITCH) && ChatPointsTTV.getTwitch().ignoreOfflineStreamers) {
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
                String title;
                String subtitle;
                if (reward.getCustomMsg() != null) {
                    String[] splitted = reward.getCustomMsg().replace("{USER}", chatter).replace("{AMOUNT}", event.orElse("")).split("\\\\n", 2);
                    title = splitted[0];
                    if (splitted.length == 2) {
                        subtitle = splitted[1];
                    } else {
                        subtitle = "";
                    }
                } else {
                    title = LocalizationUtils.replacePlaceholders(ChatPointsTTV.strings.get("title"), chatter, channel, event.orElse(null), platform);
                    subtitle = LocalizationUtils.replacePlaceholders(ChatPointsTTV.strings.get("sub_" + platform.toString().toLowerCase() + "_" + type.toString().toLowerCase()), chatter, channel, event.orElse(null), platform);
                }    
        
                switch (alertMode) {
                    case CHAT:
                        broadcastMessage(title + " " + subtitle);
                        break;
        
                    case TITLE:
                        showTitle(title, subtitle);
                        break;
        
                    case ALL:
                        broadcastMessage(title + " " + subtitle);
                        showTitle(title, subtitle);
                        break;
        
                    default:
                        ChatPointsTTV.log.warning("Invalid mode: " + ChatPointsTTV.alertMode);
                        break;
                }    
            }
        
            for (String cmd : reward.getCommands()) { // Event actions
                cmd = cmd.replace("{USER}", chatter);
                if (type.equals(TwitchEventType.CHEER) || type.equals(TwitchEventType.GIFT) || type.equals(TwitchEventType.RAID) || type.equals(TwitchEventType.SUB)) {
                    cmd = cmd.replace("{AMOUNT}", event.get());
                }
                
                String[] parts = cmd.split(" ");
    
                if (parts.length <= 1) {
                    ChatPointsTTV.log.warning(errorStr + "Invalid command \"" + parts[0] + "\"");
                    continue;
                }
                try {
                    BaseAction action;
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

                            if (text == null || text.isBlank()) {
                                ChatPointsTTV.log.warning(errorStr + "Trying to run a blank command.");
                                continue;
                            }

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
                                    ChatPointsTTV.log.warning(errorStr + "Couldn't find player " + parts[4] + ".");
                                }
                            }

                            action = new EffectAction(effect, strength, duration, target);
                            break;

                        case "DELETE":
                            if (!EnumUtils.isValidEnum(DeleteItemsAction.Type.class, parts[1].toUpperCase())) {
                                ChatPointsTTV.log.warning(errorStr + "Invalid option: " + parts[1]);
                                continue;
                            }

                            if (parts.length > 2) {
                                target = Bukkit.getPlayer(parts[2]);
                                if (target == null || !target.isOnline()) {
                                    ChatPointsTTV.log.warning(errorStr + "Couldn't find player " + parts[2] + ".");
                                }
                            }

                            action = new DeleteItemsAction(DeleteItemsAction.Type.valueOf(parts[1].toUpperCase()), target);
                            break;

                        case "SHUFFLE":
                            if (parts[1].equalsIgnoreCase("ALL")) {
                                target = Bukkit.getPlayer(parts[1]);
                            }
                            
                            action = new InvShuffleAction(target);
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

    public static ArrayList<Event> getActions(FileConfiguration config, EventType type) {
        if (actions.get(type) != null) return actions.get(type); // Give stored dictionary if it was already fetched

        String key = type.toString().toUpperCase() + "_REWARDS";
        ArrayList<Event> action_list = new ArrayList<>();

        if (!config.contains(key)) return null; // No configured rewards for this type

        if (type.equals(TwitchEventType.FOLLOW) || type.equals(TikTokEventType.FOLLOW) || type.equals(TikTokEventType.SHARE)) {
            if (config.isConfigurationSection(key)) { // Streamer-specific?
                Set<String> keys = (config.getConfigurationSection(key)).getKeys(false);
                for (String channel : keys) {
                    action_list.add(new Event(type, channel.equals("default") ? EVERYONE : channel, null, config.getConfigurationSection(key).getStringList(channel)));
                }
            } else if (config.isList(key)) {
                action_list.add(new Event(type, EVERYONE, null, config.getStringList(key)));
            } else {
                ChatPointsTTV.log.severe("ChatPointsTTV: Follow actions must be entered as a list (or a configuration section, if targeting specific streamers). Read the docs for more information.");
                return null;
            }
        } else {
            if (config.isConfigurationSection(key)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                Set<String> keys = section.getKeys(false);
                    for (String subkey : keys) {
                        if (!section.isConfigurationSection(subkey) && !section.isList(subkey)) {
                            ChatPointsTTV.log.severe("ChatPointsTTV: Invalid configuration for " + type.toString().toLowerCase() + " (" + subkey + ") actions. Read the docs for more information.");
                            continue;
                        }
                        if (type.equals(TwitchEventType.CHEER) || type.equals(TwitchEventType.GIFT) || type.equals(TwitchEventType.RAID)) {
                            try {
                                Integer.valueOf(subkey);
                            } catch (NumberFormatException e) {
                                ChatPointsTTV.log.severe("ChatPointsTTV: \"" + subkey +  "\" must be a number.");
                                continue;
                            }
                        }

                        ConfigurationSection channelSection = section.getConfigurationSection(subkey);
                        if (channelSection == null) {
                            // No channel specified
                            action_list.add(new Event(type, EVERYONE, subkey, section.getStringList(subkey)));
                        } else {
                            // Streamer specific event
                            Set<String> channelKeys = channelSection.getKeys(false);
                            for (String channel : channelKeys) {
                                action_list.add(new Event(type, channel.equals("default") ? EVERYONE : channel, subkey, channelSection.getStringList(channel)));
                            }
                        }
                    }
            } else {
                ChatPointsTTV.log.severe("ChatPointsTTV: Invalid configuration for " + type.toString().toLowerCase() + " actions. Read the docs for more information.");
                return null;
            }
        }

        action_list.sort(new EventComparator());
        actions.put(type, action_list);

        return actions.get(type);
    }
}
