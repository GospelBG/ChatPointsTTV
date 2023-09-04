package com.gospelbg.chatpointsttv;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.configuration.ConfigurationSection;

import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.github.twitch4j.auth.TwitchAuth;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.helix.domain.UserList;

import com.gospelbg.chatpointsttv.Events;

public class Main extends JavaPlugin {
    private ITwitchClient client;
    public static Main plugin;
    public Logger log = getLogger();
    public FileConfiguration config;
    private Map<String, Object> rewards;
    private List<String> titleBlacklist = new ArrayList<String>();

    @Override
    public void onEnable() {
        plugin = this;
        // Get the latest config after saving the default if missing
        this.saveDefaultConfig();
        config = getConfig();
        rewards = config.getConfigurationSection("REWARDS").getValues(false);

        config.getList("TITLE_BLACKLIST").forEach(i -> {
            titleBlacklist.add(i.toString());
            log.info(i.toString());
        });
        
        //titleBlacklist = config.getList("TITLE_BLACK_LIST").toString();

        // Build TwitchClient
        client = TwitchClientBuilder.builder()
            .withClientId(config.getString("CLIENT_ID"))
            .withClientSecret(config.getString("SECRET"))
            .withEnableHelix(true)
            .withEnableChat(true)
            .withEnablePubSub(true)
            //.withDefaultAuthToken(credential)
            //.withChatCommandsViaHelix(false)
            .build();

        // Join the twitch chats of this channel and enable stream/follow events
        String channel = config.getString("CHANNEL_USERNAME");
        String user_id = getUserId(channel);
        if (!user_id.isEmpty()) {
            client.getChat().joinChannel(user_id);
            client.getClientHelper().enableStreamEventListener(user_id);
            client.getClientHelper().enableFollowEventListener(user_id);
        }

        // Register event listeners
        //client.getEventManager().getEventHandler(Events.class).registerListener(new TwitchEventHandler(this));
        client.getPubSub().listenForChannelPointsRedemptionEvents(null, user_id);
        client.getEventManager().onEvent(RewardRedeemedEvent.class, event -> {
            String rewardTitle = event.getRedemption().getReward().getTitle();

            if (!titleBlacklist.contains(rewardTitle)) {
                plugin.getServer().getOnlinePlayers().forEach (p -> {
                    p.sendTitle(event.getRedemption().getUser().getDisplayName(), "ha canjeado " + rewardTitle, 10, 70, 20);
                });
            }
            //Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.spawnMob(EntityType.CREEPER);}});

            rewards.forEach((k, v) -> {
                log.info(rewardTitle + " --> " + k);
                if (k.toString().equals(rewardTitle)) {
                    log.info("MATCH!");
                    if (v.toString().startsWith("SPAWN")) {
                        log.info("Spawning...");
                        List<String> action = Arrays.asList(v.toString().split(" "));
                        Bukkit.getScheduler().runTask(this, new Runnable() {public void run() {Events.spawnMob(EntityType.fromName(action.get(1)), Integer.valueOf(action.get(2)));}});
                    } else if (v.toString().startsWith("RUN")) {
                        log.info("Running...");
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
                        log.info(cmd);

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