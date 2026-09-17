package me.gosdev.chatpointsttv.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import me.gosdev.chatpointsttv.Generic.ConfigFile;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Utils.ChatColor;

import me.gosdev.chatpointsttv.Actions.BaseAction;
import me.gosdev.chatpointsttv.Actions.DeleteItemsAction;
import me.gosdev.chatpointsttv.Actions.EffectAction;
import me.gosdev.chatpointsttv.Actions.FreezeAction;
import me.gosdev.chatpointsttv.Actions.GiveAction;
import me.gosdev.chatpointsttv.Actions.InvShuffleAction;
import me.gosdev.chatpointsttv.Actions.RunCmdAction;
import me.gosdev.chatpointsttv.Actions.SoundAction;
import me.gosdev.chatpointsttv.Actions.SpawnAction;
import me.gosdev.chatpointsttv.Actions.TntAction;
import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.TikTok.TikTokEventType;
import me.gosdev.chatpointsttv.Twitch.Channel;
import me.gosdev.chatpointsttv.Twitch.TwitchEventType;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;
import me.gosdev.chatpointsttv.Utils.Translatable;

public class CPTTV_EventHandler {
    public static final String EVERYONE = "*";
    private static final Map<EventType, ArrayList<Event>> actions = new HashMap<>();

    public static String getEventMessage(Platforms platform, EventType type, String chatter, String channel, Optional<String> event, Optional<Integer> amount) {
        return Translatable.getString("twitch.event.channelpoints.title", chatter, event.get(), channel);

        /*String key = "str_" + platform.getName().toLowerCase() + "_"+type.toString().toLowerCase();
        if (!ChatPointsTTV.getInstance().locales.containsKey(key)) {
            throw new NullPointerException("Missing Message for " + platform.getName() + " " + type.toString() + " events");
        }
        String str = ChatPointsTTV.getInstance().locales.get(key);
        str = LocalizationUtils.replacePlaceholders(str, chatter, channel, event.orElse(null), amount.orElse(null), platform);

        return str;*/
    }

    public static void onEvent(Platforms platform, EventType type, Event reward, String chatter, String channel, Optional<String> event, Optional<Integer> amount) {
        new Thread (()-> {
            String errorStr = Translatable.getString("action.error.generic", type);
            if (ChatPointsTTV.getInstance().logEvents) ChatPointsTTV.getLoader().consoleSender().sendMessage(getEventMessage(platform, type, chatter, channel, event, amount));
            if (platform.equals(Platforms.TWITCH) && ChatPointsTTV.getTwitch().ignoreOfflineStreamers) {
                for (Channel ch : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    if (ch.getChannelUsername().equals(channel) && !ch.isLive()) return; // Return if channel matches and it's offline.
                }
            }

            Boolean shouldGlow;
            Boolean nameSpawnedMobs;
            AlertMode alertMode;

            switch (platform) {
                case TWITCH:
                    shouldGlow = ChatPointsTTV.getTwitch().shouldMobsGlow;
                    nameSpawnedMobs = ChatPointsTTV.getTwitch().nameSpawnedMobs;
                    alertMode = ChatPointsTTV.getTwitch().alertMode;
                    break;
                case TIKTOK:
                    shouldGlow = ChatPointsTTV.getTikTok().shouldMobsGlow;
                    nameSpawnedMobs = ChatPointsTTV.getTikTok().nameSpawnedMobs;
                    alertMode = ChatPointsTTV.getTikTok().alertMode;
                    break;
                default:
                    shouldGlow = ChatPointsTTV.getInstance().shouldMobsGlow;
                    nameSpawnedMobs = ChatPointsTTV.getInstance().nameSpawnedMobs;
                    alertMode = ChatPointsTTV.getInstance().alertMode;
                    break;
            }

            /*if (!alertMode.equals(AlertMode.NONE)) { // In-game alert
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
                    title = LocalizationUtils.replacePlaceholders(ChatPointsTTV.getInstance().locales.get("title"), chatter, channel, event.orElse(null), amount.orElse(null), platform);
                    subtitle = LocalizationUtils.replacePlaceholders(ChatPointsTTV.getInstance().locales.get("sub_" + platform.toString().toLowerCase() + "_" + type.toString().toLowerCase()), chatter, channel, event.orElse(null), amount.orElse(null), platform);
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
                        ChatPointsTTV.log.warn("Invalid mode: " + ChatPointsTTV.getInstance().alertMode);
                        break;
                }    
            }*/
        
            for (String cmd : reward.getCommands()) { // Event actions
                cmd = cmd.replace("{USER}", chatter);
                if (type.equals(TwitchEventType.CHEER) || type.equals(TwitchEventType.GIFT) || type.equals(TwitchEventType.RAID) ||
                    type.equals(TikTokEventType.GIFT) || type.equals(TikTokEventType.LIKE)) {
                    cmd = cmd.replace("{AMOUNT}", amount.get().toString());
                }
                
                String[] parts = cmd.split(" ");

                try {
                    BaseAction action;
                    Integer act_amount = null;
                    GenericPlayer target = null;

                    switch (parts[0].toUpperCase()) {
                        case "SPAWN":
                            if (!ChatPointsTTV.getLoader().getEntities().contains(parts[1].toUpperCase())) {
                                notifyFailure(parts[0], errorStr + Translatable.getString("action.error.entity_not_found", parts[1].toUpperCase()));
                                continue;
                            }
                            if (parts.length > 2) {
                                act_amount = Integer.valueOf(parts[2]);
                            }
                            if (parts.length > 3) {
                                target = ChatPointsTTV.getLoader().getPlayer(parts[3]);
                            }
                            action = new SpawnAction(parts[1].toUpperCase(), nameSpawnedMobs ? chatter : null, Optional.ofNullable(act_amount), target, shouldGlow);
                            break;
                        case "RUN":
                            String text = "";
                            for (int i = 2; i < parts.length; i++) {
                                text += " " + parts[i];
                            }
                            text = text.trim();

                            if (text == null || text.isBlank() || text.equals("/")) {
                                notifyFailure(parts[0], errorStr + Translatable.getString("action.error.empty_command"));
                                continue;
                            }

                            action = new RunCmdAction(parts[1], text);
                            break;
                        case "GIVE":
                            if (!ChatPointsTTV.getLoader().getItems().contains(parts[1].toUpperCase())) {
                                notifyFailure(parts[0], errorStr + Translatable.getString("action.error.item_not_found", parts[1].toUpperCase()));
                                continue;
                            }
                            if (parts.length > 2) {
                                act_amount = Integer.valueOf(parts[2]);
                            }
                            if (parts.length > 3) {
                                target = ChatPointsTTV.getLoader().getPlayer(parts[3]);
                                if (target == null || !target.isOnline()) {
                                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.player_not_found", parts[3]));
                                    continue;
                                }
                            }
                            action = new GiveAction(parts[1].toUpperCase(), Optional.ofNullable(act_amount), Optional.ofNullable(target));
                            break;

                        case "EFFECT":
                            String effect = parts[1];
                            Integer duration = null;
                            Integer strength = null;
                            if (!effect.equalsIgnoreCase("clear")) {
                                if (parts.length < 4) {
                                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.missing_arguments", "3"));
                                }
                                duration = parts.length >= 4 ? Integer.valueOf(parts[3]) : null;
                                strength = Integer.valueOf(parts[2]);
                            }
                            if (!ChatPointsTTV.getLoader().getPotionEffects().contains(effect.toUpperCase()) && !effect.equalsIgnoreCase("random") && !effect.equalsIgnoreCase("clear")) {
                                notifyFailure(parts[0], errorStr + Translatable.getString("action.error.effect_not_found", parts[1]));
                                continue;
                            }
                            if (parts.length > (effect.equalsIgnoreCase("clear") ? 2 : 4)) {
                                target = ChatPointsTTV.getLoader().getPlayer(parts[parts.length -1]);
                                if (target == null || !target.isOnline()) {
                                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.player_not_found", parts[parts.length -1]));
                                    continue;
                                }
                            }

                            action = new EffectAction(effect, strength, duration, target);
                            break;

                        case "DELETE":
                            try {
                                DeleteItemsAction.Type.valueOf(parts[1].toUpperCase());
                            } catch (IllegalArgumentException e) {
                                notifyFailure(parts[0], errorStr + Translatable.getString("action.error.invalid_option", parts[1].toUpperCase()));
                                continue;
                            }

                            if (parts.length > 2) {
                                target = ChatPointsTTV.getLoader().getPlayer(parts[2]);
                                if (target == null || !target.isOnline()) {
                                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.player_not_found", parts[2]));
                                    continue;
                                }
                            }

                            action = new DeleteItemsAction(DeleteItemsAction.Type.valueOf(parts[1].toUpperCase()), target);
                            break;

                        case "FREEZE":
                            Integer time = Integer.valueOf(parts[1]);

                            if (parts.length > 2) {
                                target = ChatPointsTTV.getLoader().getPlayer(parts[2]);
                                if (target == null || !target.isOnline()) {
                                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.player_not_found", parts[2]));
                                    continue;
                                }
                            }
                            action = new FreezeAction(target, time);
                            break;

                        case "SHUFFLE":
                            if (!parts[1].equalsIgnoreCase("ALL")) {
                                target = ChatPointsTTV.getLoader().getPlayer(parts[1]);
                                if (target == null || !target.isOnline()) {
                                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.player_not_found", parts[1]));
                                    continue;
                                }
                            }

                            action = new InvShuffleAction(null);
                            break;

                        case "SOUND":
                            String sound = parts[1];
                            if (parts.length >= 3) target = ChatPointsTTV.getLoader().getPlayer(parts[2]);

                            try {
                                action = new SoundAction(target, sound.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                notifyFailure(parts[0], errorStr + Translatable.getString("action.error.sound_not_found", parts[1]));
                                continue;
                            }
                            break;


                        case "TNT":
                            Integer fuseTime = null;
                            target = null;
                            if (parts.length > 2) {
                                fuseTime = Integer.valueOf(parts[2]);
                            }
                            if (parts.length > 3) {
                                target = ChatPointsTTV.getLoader().getPlayer(parts[3]);
                                if (target == null || !target.isOnline()) {
                                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.player_not_found", parts[3]));
                                    continue;
                                }
                            }
                            action = new TntAction(Integer.parseInt(parts[1]), Optional.ofNullable(fuseTime), Optional.ofNullable(target));
                            break;
                        case "WAIT":
                            try {
                                Thread.sleep((long) (Float.parseFloat(parts[1])*1000));
                            } catch (InterruptedException ignored) {}
                            continue;
                        default:
                            notifyFailure(parts[0], errorStr + Translatable.getString("action.error.invalid_action", parts[0]));
                            return;
                    }
                    action.run();
                } catch (NumberFormatException e) {
                    notifyFailure(parts[0], errorStr + Translatable.getString("action.error.invalid_amount", e.getMessage().substring(19, e.getMessage().length() - 1)));
                }
            }
        }).start();
    }


    public static void showTitle(String title, String subtitle) {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST)) continue;
            p.sendTitle(title, subtitle);
        }
    }

    public static void broadcastMessage(String msg) {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST)) continue;
            p.sendMessage(msg);
        }
    }

    public static ArrayList<Event> getActions(ConfigFile config, EventType type) {
        if (actions.get(type) != null) return actions.get(type); // Give stored dictionary if it was already fetched

        String key = type.toString().toUpperCase() + "_EVENTS";
        ArrayList<Event> action_list = new ArrayList<>();

        if (!config.contains(key)) return null; // No configured rewards for this type

        if (type.equals(TwitchEventType.FOLLOW) || type.equals(TikTokEventType.FOLLOW) || type.equals(TikTokEventType.SHARE)) {
            if (config.isSection(key)) { // Streamer-specific?
                for (String channel : config.getSectionKeys(key)) {
                    action_list.add(new Event(type, channel.equals("default") ? EVERYONE : channel, null, config.getStringList(key + "." + channel)));
                }
            } else if (config.isList(key)) {
                action_list.add(new Event(type, EVERYONE, null, config.getStringList(key)));
            } else {
                ChatPointsTTV.log.error("ChatPointsTTV: " + Translatable.getString("config.error.invalid_format", type.toString()));
                return null;
            }
        } else {
            if (config.isSection(key)) {
                for (String subkey : config.getSectionKeys(key)) {
                    String subPath = key + "." + subkey;
                    if (!config.isSection(subPath) && !config.isList(subPath)) {
                        ChatPointsTTV.log.error("ChatPointsTTV: Invalid configuration for " + type.toString().toLowerCase() + " (" + subkey + ") actions. Read the docs for more information.");
                        continue;
                    }
                    if (type.equals(TwitchEventType.CHEER) || type.equals(TwitchEventType.GIFT) || type.equals(TwitchEventType.RAID)) {
                        try {
                            Integer.valueOf(subkey);
                        } catch (NumberFormatException e) {
                            ChatPointsTTV.log.error("ChatPointsTTV: " + Translatable.getString("events.error.must_be_number", subkey));
                            continue;
                        }
                    }

                    if (!config.isSection(subPath)) {
                        // No channel specified
                        action_list.add(new Event(type, EVERYONE, subkey, config.getStringList(subPath)));
                    } else {
                        // Streamer specific event
                        for (String channel : config.getSectionKeys(subPath)) {
                            action_list.add(new Event(type, channel.equals("default") ? EVERYONE : channel, subkey, config.getStringList(subPath + "." + channel)));
                        }
                    }
                }
            } else {
                ChatPointsTTV.log.error("ChatPointsTTV: " + Translatable.getString("config.error.invalid_format", type.toString()));
                return null;
            }
        }

        action_list.sort(new EventComparator());
        actions.put(type, action_list);

        return actions.get(type);
    }

    public static void clearActions(Platforms plat) {
        for (EventType e : plat.getEventTypes()) {
            actions.put(e, null);
        }
    }

    private static void notifyFailure(String action, String msg) {
        ChatPointsTTV.log.warn(msg);
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE)) {
                p.sendMessage(ChatColor.RED + msg);
            }
        }
    }
}
