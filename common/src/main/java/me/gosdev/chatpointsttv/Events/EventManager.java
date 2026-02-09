package me.gosdev.chatpointsttv.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.gosdev.chatpointsttv.Actions.BaseAction;
import me.gosdev.chatpointsttv.Actions.CustomMsgAction;
import me.gosdev.chatpointsttv.Actions.DeleteItemsAction;
import me.gosdev.chatpointsttv.Actions.EffectAction;
import me.gosdev.chatpointsttv.Actions.FreezeAction;
import me.gosdev.chatpointsttv.Actions.GiveAction;
import me.gosdev.chatpointsttv.Actions.InvShuffleAction;
import me.gosdev.chatpointsttv.Actions.RunCmdAction;
import me.gosdev.chatpointsttv.Actions.SoundAction;
import me.gosdev.chatpointsttv.Actions.SpawnAction;
import me.gosdev.chatpointsttv.Actions.TntAction;
import me.gosdev.chatpointsttv.Actions.WaitAction;
import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ConfigFile;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.TikTok.TikTokEventType;
import me.gosdev.chatpointsttv.Twitch.Channel;
import me.gosdev.chatpointsttv.Twitch.TwitchEventType;
import me.gosdev.chatpointsttv.Utils.LocalizationUtils;

public class EventManager {
    public static final String EVERYONE = "*";
    public static HashMap<EventType, ArrayList<Action>> actions = new HashMap<>();

    private static Action getAction(EventInformation event) {
        for (Action reward : actions.get(event.getEventType())) {
            if (!reward.getTargetChannel().equals(EventManager.EVERYONE) && !reward.getTargetChannel().equals(event.getStreamer())) continue;

            if (event.getAmount() != null) { // Check threshold
                try {
                    if (event.getAmount() < Integer.valueOf(reward.getEvent())) {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    ChatPointsTTV.log.warn("Invalid amount: " + reward.getEvent());
                    continue;
                }
            } else if (event.getEvent() != null) { // Check if requirement matches
                if (!reward.getEvent().equals(event.getEvent())) {
                    continue;
                }
            }
            return reward;
        }
        return null;
    }

    private static void broadcastAlert(EventInformation event, Action action, AlertMode mode) {
        if (!mode.equals(AlertMode.NONE)) { // In-game alert
            String title;
            String subtitle;
            if (action.getCustomMsg() != null) { // Overriden message
                String[] splitted = LocalizationUtils.replacePlaceholders(action.getCustomMsg(), event).split("\\\\n", 2); //action.getCustomMsg().replace("{USER}", event.getChatter()).replace("{AMOUNT}", event.orElse("")).split("\\\\n", 2);
                title = splitted[0];
                if (splitted.length == 2) {
                    subtitle = splitted[1];
                } else {
                    subtitle = "";
                }
            } else {
                title = LocalizationUtils.replacePlaceholders(ChatPointsTTV.locales.get("title"), event);
                subtitle = LocalizationUtils.replacePlaceholders(ChatPointsTTV.locales.get("sub_" + event.getPlatform().toString().toLowerCase() + "_" + event.getEventType().toString().toLowerCase()), event);
            }    

            switch (mode) {
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
                    ChatPointsTTV.log.warn("Invalid mode: " + ChatPointsTTV.alertMode);
                    break;
            }    
        }
    }

    public static void onEvent(EventInformation event) {
        Action action = getAction(event);
        if (action == null) return; // No matching event or an error occured

        if (event.getPlatform().equals(Platforms.TWITCH) && ChatPointsTTV.getTwitch().ignoreOfflineStreamers) {
            for (Channel ch : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                if (ch.getChannelUsername().equals(event.getStreamer()) && !ch.isLive()) return; // Return if channel matches and it's offline.
            }
        }

        // Get Alert Mode
        AlertMode alertMode;

        switch (event.getPlatform()) {
            case TWITCH:
                alertMode = ChatPointsTTV.getTwitch().alertMode;
                break;
            case TIKTOK:
                alertMode = ChatPointsTTV.getTikTok().alertMode;
                break;
            default:
                alertMode = ChatPointsTTV.alertMode;
                break;
        }

        broadcastAlert(event, action, alertMode);

        new Thread(() -> {
            List<BaseAction> commands = createActions(action, event);
            for (BaseAction a : commands) {
                a.run(event);
            }
        }).start();
    }

    private static void showTitle(String title, String subtitle) {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST)) continue;
            p.sendTitle(title, subtitle);
        }
    }

    private static void broadcastMessage(String msg) {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (!p.hasPermission(ChatPointsTTV.permissions.BROADCAST)) continue;
            p.sendMessage(msg);
        }
    }

    public static Platforms getPlatformFromType(EventType type) {
        if (type instanceof TwitchEventType) {
            return Platforms.TWITCH;
        } else if (type instanceof TikTokEventType) {
            return Platforms.TIKTOK;
        } else {
            ChatPointsTTV.log.error("Unknown platform with event type " + type.toString());
            return null;
        }
    }

    public static List<Action> parseActions(EventType type, ConfigFile config) {
        ArrayList<Action> action_list = new ArrayList<>();

        String key = type.toString().toUpperCase() + "_EVENTS";
        if (!config.contains(key)) return null; // No configured rewards for this type

        if (type.equals(TwitchEventType.FOLLOW) || type.equals(TikTokEventType.FOLLOW) || type.equals(TikTokEventType.SHARE)) {
            if (config.isSection(key)) { // Streamer-specific?
                for (String channel : config.getSectionKeys(key)) {
                    action_list.add(new Action(type, channel, null, config.getStringList(key + "." + channel)));
                    ChatPointsTTV.log.info("Loaded " + type.toString().toLowerCase() + " actions for channel " + channel);
                }
            } else if (config.isList(key)) {
                action_list.add(new Action(type, EVERYONE, null, config.getStringList(key))); 
            } else {
                ChatPointsTTV.log.error("ChatPointsTTV: " + type.toString() + " actions must be entered as a list (or a configuration section, if targeting specific streamers). Read the docs for more information.");
                return null;
            }
        } else {
            if (config.isSection(key)) {
                for (String subkey : config.getSectionKeys(key)) {
                    if (type.equals(TwitchEventType.CHEER) || type.equals(TwitchEventType.GIFT) || type.equals(TwitchEventType.RAID)) {
                        try {
                            Integer.valueOf(subkey);
                        } catch (NumberFormatException e) {
                            ChatPointsTTV.log.error("ChatPointsTTV: \"" + subkey +  "\" must be a number.");
                            continue;
                        }
                    }

                    String configPath = key + "." + subkey;
                    if (config.isList(configPath)) { // No channel specified
                        action_list.add(new Action(type, EVERYONE, subkey, config.getStringList(configPath)));
                    } else if (config.isSection(configPath)) { // Streamer specific event
                        List<String> channelKeys = config.getSectionKeys(configPath);

                        for (String channel : channelKeys) {
                            action_list.add(new Action(type, channel, subkey, config.getStringList(configPath + "." + channel)));
                        }
                    } else {
                        ChatPointsTTV.log.error("ChatPointsTTV: Invalid configuration for " + type.toString().toLowerCase() + " (" + subkey + ") actions. Read the docs for more information.");
                    }
                }
            } else {
                ChatPointsTTV.log.error("ChatPointsTTV: Invalid configuration for " + type.toString().toLowerCase() + " actions. Read the docs for more information.");
                return null;
            }
        }
        action_list.sort(new EventComparator());
        actions.put(type, action_list);

        return action_list;
    }

    private static List<BaseAction> createActions(Action a, EventInformation ei) {
        ArrayList<BaseAction> action_list = new ArrayList<>();
        for (String action : a.getRawActions()) {
            action = LocalizationUtils.replacePlaceholders(action, ei);
            String[] parts = action.split(" ");
            switch (parts[0]) { //TODO: Try-catch error handling
                case DeleteItemsAction.ACTION_NAME:
                    String target = parts.length >= 3 ? parts[2] : null;
                    try {
                        action_list.add(new DeleteItemsAction(DeleteItemsAction.Type.valueOf(parts[1].toUpperCase()), target));
                    } catch (IllegalArgumentException e) {
                        ChatPointsTTV.log.error("Invalid option for DELETE action: " + parts[1]);
                    }
                    break;

                case EffectAction.ACTION_NAME:
                    if (parts[1].equalsIgnoreCase("CLEAR")) {
                        action_list.add(new EffectAction(parts[1], null, null, parts.length >= 3 ? parts[2] : null));
                    } else {
                        action_list.add(new EffectAction(parts[1], Integer.valueOf(parts[2]), parts.length >= 4 ? Integer.valueOf(parts[3]) : null, parts.length >= 5 ? parts[4] : null));
                    }
                    break;

                case FreezeAction.ACTION_NAME:
                    action_list.add(new FreezeAction(parts.length >= 3 ? parts[2] : null, Integer.valueOf(parts[1])));
                    break;

                case GiveAction.ACTION_NAME:
                    action_list.add(new GiveAction(parts[1], Integer.valueOf(parts.length >= 3 ? parts[2] : "1"), parts.length >= 4 ? parts[3] : null));
                    break;

                case InvShuffleAction.ACTION_NAME:
                    action_list.add(new InvShuffleAction(parts.length >= 2 ? parts[1] : null));
                    break;

                case RunCmdAction.ACTION_NAME:
                    action_list.add(new RunCmdAction(parts[1], action.split(" ", 3)[2]));
                    break;

                case SoundAction.ACTION_NAME:
                    action_list.add(new SoundAction(parts.length >= 3 ? parts[2] : null, parts[1]));
                    break;

                case SpawnAction.ACTION_NAME:
                    String spawnTarget = parts.length >= 4 ? parts[3] : null;
                    Integer amount = parts.length >= 3 ? Integer.valueOf(parts[2]) : 1;
                    action_list.add(new SpawnAction(parts[1], spawnTarget, amount));
                    break;

                case TntAction.ACTION_NAME:
                    Integer fuseTime = parts.length >= 3 ? Integer.valueOf(parts[2]) : null;
                    String tntTarget = parts.length >= 4 ? parts[3] : null;
                    action_list.add(new TntAction(Integer.valueOf(parts[1]), fuseTime, tntTarget));
                    break;

                case WaitAction.ACTION_NAME:
                    action_list.add(new WaitAction(Integer.valueOf(parts[1])));
                    break;

                case CustomMsgAction.ACTION_NAME:
                    action_list.add(new CustomMsgAction(action.replace(CustomMsgAction.ACTION_NAME + " ", "")));
                    break;

                default:
                    ChatPointsTTV.log.error("ChatPointsTTV: Unknown action \"" + parts[0] + "\" for " + a.getType().toString().toLowerCase() + " events.");
                    break;
            }
        }
        return action_list;
    }

    public static Boolean actionsFound(EventType type) {
        List<Action> typeActions = actions.get(type);
        return !(typeActions == null || typeActions.isEmpty());
    }
}
