package com.gospelbg.chatpointsttv;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import com.github.twitch4j.helix.domain.UserList;

public class Main extends JavaPlugin {
    private ITwitchClient client;
    public static Main plugin;
    public Logger log = getLogger();
    public FileConfiguration config;
    private Map<String, Object> rewards;
    private List<String> titleBlacklist = new ArrayList<String>();
    private Map<String, ChatColor> colors = new HashMap<String, ChatColor>();

    private final String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    private final String AuthURL = "https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + ClientID + "&redirect_uri=http://localhost:3000&scope=channel%3Aread%3Aredemptions+channel%3Amanage%3Aredemptions";

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

        config.getList("TITLE_BLACKLIST").forEach(i -> {
            if (i == null) return;
            titleBlacklist.add(i.toString());
        });

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
        client.getPubSub().listenForChannelPointsRedemptionEvents(oauth, user_id);
        client.getEventManager().onEvent(RewardRedeemedEvent.class, event -> {
            String rewardTitle = event.getRedemption().getReward().getTitle();

            if (!titleBlacklist.contains(rewardTitle)) {
                ChatColor isBold = config.getBoolean("REWARD_NAME_BOLD") ? ChatColor.BOLD : ChatColor.RESET;

                plugin.getServer().getOnlinePlayers().forEach (p -> {
                    p.sendTitle(colors.get("USER_COLOR") + event.getRedemption().getUser().getDisplayName(), config.getString("HAS_REDEEMED_STRING") + " " + isBold + colors.get("REWARD_NAME_COLOR") + rewardTitle, 10, 70, 20);
                });
            }
            rewards.forEach((k, v) -> {
                if (k.toString().equals(rewardTitle)) {
                    log.info("Claimed Reward" + rewardTitle + "!");
                    if (v.toString().startsWith("SPAWN")) {
                        log.info("Spawning...");
                        List<String> action = Arrays.asList(v.toString().split(" "));
                        Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.spawnMob(EntityType.fromName(action.get(1)), Integer.valueOf(action.get(2)));}});
                    } else if (v.toString().startsWith("RUN")) {
                        List<String> action = Arrays.asList(v.toString().split(" "));
                        String text = "";
                        for (int i = 0; i < action.size(); i++) {
                            if (i == 0 | i == 1) continue;

                            if (action.get(i).equals("{TEXT}")) {
                                text += " " + event.getRedemption().getUserInput();
                                continue;
                            }

                            text += " " + action.get(i);
                        }
                        text = text.trim();

                        final String cmd = text.replace("/", "");
                        log.info("Running command: \""+ cmd + "\"...");

                        if (action.get(1).equals("CONSOLE")) {
                            Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.runCommand(Bukkit.getServer().getConsoleSender(), cmd);}});
                        } else {
                            Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.runCommand(Bukkit.getPlayer(action.get(1)), cmd);}});
                        }
                    }
                }
            });
        });
    }

    @Override
    public void onDisable() {
        if (client != null) {
            client.getChat().leaveChannel(config.getString("CHANNEL_USERNAME"));
            client.getEventManager().close();
            client.close();
            client = null;
        }
    }

    public static Main getPlugin() {
        return plugin;
    }

    public String getUserId(String username) {
        UserList resultList = getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(new String[]{username})).execute();
        return resultList.getUsers().get(0).getId();
    }

    public ITwitchClient getTwitchClient() {
        return this.client;
    }
}