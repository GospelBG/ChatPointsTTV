package com.gospelbg.chatpointsttv;

import java.lang.Integer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.pubsub.events.ChannelBitsEvent;
import com.github.twitch4j.pubsub.events.ChannelSubGiftEvent;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import com.github.twitch4j.helix.domain.UserList;

public class ChatPointsTTV extends JavaPlugin {
    private static ITwitchClient client;
    private static ChatPointsTTV plugin;
    public Logger log = getLogger();
    public FileConfiguration config;
    private Map<String, Object> rewards;
    private Map<String, ChatColor> colors = new HashMap<String, ChatColor>();

    private final String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    private final String AuthURL = "https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + ClientID + "&redirect_uri=http://localhost:3000&scope=channel%3Aread%3Aredemptions+channel%3Amanage%3Aredemptions+bits%3Aread+channel%3Aread%3Asubscriptions";

    public enum reward_type {
        CHANNEL_POINTS,
        CHEER,
        SUB,
        SUB_GIFT
    };

    public enum permissions {
        BROADCAST("chatpointsttv.broadcast"),
        MANAGE("chatpointsttv.manage"),
        TARGET("chatpointsttv.target");

        public final String permission_id;

        private permissions(String label) {
            this.permission_id = label;
        }
    }

    public static ChatPointsTTV getPlugin() {
        return plugin;
    }

    public String getUserId(String username) {
        UserList resultList = getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(new String[]{username})).execute();
        return resultList.getUsers().get(0).getId();
    }

    public static ITwitchClient getTwitchClient() {
        return client;
    }

    public String getClientID() {
        return ClientID;
    }

    public String getAuthURL() {
        return AuthURL;
    }

    @Override
    public void onEnable() {
        plugin = this;
        // Get the latest config after saving the default if missing
        this.saveDefaultConfig();
        config = getConfig();
        rewards = config.getConfigurationSection("REWARDS").getValues(false);

        config.getConfigurationSection("COLORS").getKeys(false).forEach(i -> {
            colors.put(i, ChatColor.valueOf(config.getConfigurationSection("COLORS").getString(i)));
        });

        this.getCommand("link").setExecutor(new CommandController());
    }

    public void linkToTwitch(String token) {
        OAuth2Credential oauth = new OAuth2Credential(ClientID, token);
        // Build TwitchClient
        client = TwitchClientBuilder.builder()
            .withDefaultAuthToken(oauth)
            .withEnableHelix(true)
            .withEnablePubSub(true)
            .build();

        log.info("Logged in as: "+ client.getHelix().getUsers(token, null, null).execute().getUsers().get(0).getDisplayName());

        // Join the twitch chat of this channel and enable stream/follow events
        String channel = config.getString("CHANNEL_USERNAME");
        String user_id = getUserId(channel);

        // Register event listeners
        if (config.getBoolean("ENABLE_CHEER_REWARDS")) {
            rewards.putAll(config.getConfigurationSection("CHEER_REWARDS").getValues(false));

            client.getPubSub().listenForCheerEvents(oauth, user_id);
            client.getEventManager().onEvent(ChannelBitsEvent.class, event -> {
                log.info("CHEER");
                reward_action(reward_type.CHEER, event.getData().getBitsUsed() + " bits", event.getData().getUserName(), event.getData().getChatMessage());
            });
        }
        if (config.getBoolean("ENABLE_SUB_REWARDS")) {
            rewards.putAll(config.getConfigurationSection("SUB_REWARDS").getValues(false));

            client.getPubSub().listenForSubscriptionEvents(oauth, user_id);
            client.getPubSub().listenForChannelSubGiftsEvents(oauth, user_id);

            client.getEventManager().onEvent(ChannelSubscribeEvent.class, event -> {
                log.info("SUB");
                String msg;
                try {
                    msg = event.getData().getSubMessage().toString();
                } catch(NoSuchElementException e) {
                    msg = "";
                }
                log.info(event.getData().getSubPlan().toString());
                reward_action(reward_type.SUB, event.getData().getSubPlan().toString(), event.getData().getUserName(), msg);
            });

            client.getEventManager().onEvent(ChannelSubGiftEvent.class, event -> {
                log.info("SUB_GIFT");
                reward_action(reward_type.SUB_GIFT, event.getData().getCount() + " subs", event.getData().getUserName());

            });
        }

        client.getPubSub().listenForChannelPointsRedemptionEvents(oauth, user_id);
        client.getEventManager().onEvent(RewardRedeemedEvent.class, event -> {
            log.info("REWARD");
            reward_action(reward_type.CHANNEL_POINTS, event.getRedemption().getReward().getTitle(), event.getRedemption().getUser().getDisplayName(), event.getRedemption().getUserInput());
        });
    }

    private void reward_action(reward_type type, String reward, String username, String... extra) {
        rewards.forEach((k, v) -> {
            int amount = 0;
            if (type == reward_type.SUB_GIFT) {
                amount = Integer.parseInt(reward);
                k = "GIFT_"+k;
            }
            log.info(k + " " + v);
            if (k.toString().equals(reward) | (type == reward_type.CHEER && Integer.parseInt(reward) >= Integer.parseInt(k)) | (type == reward_type.SUB_GIFT && amount >= Integer.parseInt(reward))) {
                log.info("THIS ONE");
                final String custom_string;
                final String color_key;

                switch (type) {
                    case CHANNEL_POINTS:
                        log.info("Claimed Reward " + reward + "!");
                        custom_string = config.getString("HAS_REDEEMED_STRING");
                        color_key = "REWARD_NAME_COLOR";
                        break;

                    case CHEER:
                        log.info(reward + " bits cheered!");
                        custom_string = config.getString("CHEERED_STRING");
                        color_key = "CHEER_COLOR";
                        break;

                    case SUB:
                        log.info(username + " subscribed with a tier " + reward + " sub!");
                        custom_string = config.getString("SUB_STRING");
                        color_key = "SUB_COLOR";
                        break;

                    case SUB_GIFT:
                        log.info(username + " gifed " + reward + " subs!");
                        custom_string = config.getString("GIFT_STRING");
                        color_key = "GIFT_COLOR";
                        break;

                    default:
                        log.warning("Failed to get reward type. Using generic string");
                        custom_string = "redeemed";
                        color_key = "USER_COLOR";
                        break;
                        
                }

                String rewardTitle = reward;

                ChatColor isBold = config.getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;

                plugin.getServer().getOnlinePlayers().forEach (p -> {
                    if (p.hasPermission(permissions.BROADCAST.permission_id)) {
                    }
                });
                
                if (v.toString().startsWith("SPAWN")) {
                    
                    List<String> action = Arrays.asList(v.toString().split(" "));
                    //Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.spawnMob(EntityType.valueOf(action.get(1)), Integer.valueOf(action.get(2)));}});
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        log.info("Spawning...");
                        Events.spawnMob(EntityType.valueOf(action.get(1).toUpperCase()), Integer.valueOf(action.get(2)));
                    });

                } else if (v.toString().startsWith("RUN")) {
                    List<String> action = Arrays.asList(v.toString().split(" "));
                    String text = "";
                    
                    for (int i = 0; i < action.size(); i++) {
                        if (i == 0 | i == 1) continue;

                        if (action.get(i).equals("{TEXT}")) {
                            text += " " + extra[0];
                            continue;
                        }

                        text += " " + action.get(i);
                    }
                    text = text.trim();

                    final String cmd = text.replace("/", "");
                    log.info("Running command: \""+ cmd + "\"...");
                    Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.runCommand(action.get(1), cmd);}});
                }
            }
        });
    }

    @Override
    public void onDisable() {
        if (client != null) {
            client.getEventManager().close();
            client.close();
            client = null;
        }
    }
}