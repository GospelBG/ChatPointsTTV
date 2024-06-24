package me.gosdev.chatpointsttv;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;

import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.Scopes;
import me.gosdev.chatpointsttv.Utils.TwitchUtils;
import me.gosdev.chatpointsttv.Utils.Utils;

public class ChatPointsTTV extends JavaPlugin {
    private static ITwitchClient client;
    private static TwitchEventHandler eventHandler;
    private static IEventSubSocket eventSocket;
    private static EventManager eventManager;
    private static ChatPointsTTV plugin;
    private CommandController cmdController;

    private static Map<String, ChatColor> colors = new HashMap<String, org.bukkit.ChatColor>();
    private static Map<String, String> titleStrings = new HashMap<String, String>();
    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;

    public static boolean configOk = true;

    public Logger log = getLogger();
    public FileConfiguration config;
    private Boolean accountConnected = false;

    private final String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    private final String scopes = Scopes.join(
        Scopes.CHANNEL_READ_REDEMPTIONS,
        Scopes.CHANNEL_MANAGE_REDEMPTIONS,
        Scopes.USER_READ_MODERATED_CHANNELS,
        Scopes.MODERATOR_READ_FOLLOWERS,
        Scopes.BITS_READ,
        Scopes.CHANNEL_READ_SUBSCRIPTIONS,
        Scopes.USER_READ_CHAT,
        Scopes.CHAT_READ,
        Scopes.CHAT_EDIT
        ).replace(":", "%3A"); // Format colon character for browser
    private final String AuthURL = "https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + ClientID + "&redirect_uri=http://localhost:3000&scope="+scopes;

    private OAuth2Credential oauth;

    public static enum permissions {
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
        UserList resultList = getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(username)).execute();
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
    public Boolean isAccountConnected() {
        return accountConnected;
    }
    public static ITwitchClient getClient() {
        return client;
    }

    public static Map<String, org.bukkit.ChatColor> getChatColors() {
        return colors;
    }
    public static Map<String, String> getRedemptionStrings() {
        return titleStrings;
    }

    public static Utils utils;

    private static Utils getUtils() {
        if (utils != null) return utils;

        int version = Integer.parseInt(Bukkit.getServer().getClass().getName().split("\\.")[3].split("_")[1]);
        try {
            if (version <= 8) { 
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_8_R1").getDeclaredConstructor().newInstance();
            } else {
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_12_R1").getDeclaredConstructor().newInstance();
            }
            return utils;    
        } catch (Exception e) {
            plugin.log.warning(e.toString());
            return null;
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();

        utils = getUtils();

        // Get the latest config after saving the default if missing
        this.saveDefaultConfig();
        config = getConfig();

        config.getConfigurationSection("COLORS").getKeys(false).forEach(i -> {
            colors.put(i, org.bukkit.ChatColor.valueOf(config.getConfigurationSection("COLORS").getString(i)));
        });

        config.getConfigurationSection("STRINGS").getKeys(true).forEach(i -> {
            titleStrings.put(i, config.getConfigurationSection("STRINGS").getString(i));
        });

        shouldMobsGlow = config.getBoolean("MOB_GLOW");
        nameSpawnedMobs = config.getBoolean("DISPLAY_NAME_ON_MOB");

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (!accountConnected && player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) {
                    String msg = "Welcome! Remember to log in with your Twitch account for ChatPointsTTV to be able to connect and listen.\n";
                    ComponentBuilder builder = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "[Click here to login]");
                    BaseComponent btn = builder.create()[0];

                    btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to run command").create()));
                    btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch link"));

                    utils.sendMessage(player.getPlayer(), new BaseComponent[] {new ComponentBuilder(msg).create()[0], btn});
                }
            }
        }, this);

        cmdController = new CommandController();
        this.getCommand("twitch").setExecutor(cmdController);
        this.getCommand("twitch").setTabCompleter(cmdController);

        log.info("ChatPointsTTV enabled!");
        for (Player p: plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.sendMessage("ChatPointsTTV reloaded!");
            }
        }
        VersionCheck.check();

        if (config.getString("CHANNEL_USERNAME") == null | config.getString("CHANNEL_USERNAME").startsWith("MemorySection[path=")) { // Invalid string (probably left default "{YOUR CHANNEL}")
            log.warning("Cannot read channel. Config file may be not set up or invalid.");
            configOk = false;
        } else {
            configOk = true;
        }
    }

    @Override
    public void onDisable() {
        if (client != null) {
            client.close();
        }
        
        CommandController.server.stop();
    
        // Erase variables
        client = null;
        eventHandler = null;
        eventSocket = null;
        eventManager = null;
        config = null;
        accountConnected = false;
        oauth = null;
        plugin = null;

        Rewards.rewards = Collections.emptyMap();
    }

    public void linkToTwitch(String token) {
        utils.sendLogToPlayers("Logging in...");
        oauth = new OAuth2Credential(ClientID, token);

        // Build TwitchClient
        client = TwitchClientBuilder.builder()
            .withDefaultAuthToken(oauth)
            .withEnableChat(true)
            .withChatAccount(oauth)
            .withEnableHelix(true)
            .withEnablePubSub(true)
            .withEnableEventSocket(true)
            .withDefaultEventHandler(SimpleEventHandler.class)
            .build();

        User user = client.getHelix().getUsers(token, null, null).execute().getUsers().get(0);

        log.info("Logged in as: "+ user.getDisplayName());
        accountConnected = true;

        // Join the twitch chat of this channel and enable stream/follow events
        String channel = config.getString("CHANNEL_USERNAME");
        String channel_id = getUserId(channel);
        String user_id = new TwitchIdentityProvider(null, null, null).getAdditionalCredentialInformation(oauth).map(OAuth2Credential::getUserId).orElse(null);
        log.info("Listening to " + channel + "'s events...");
        utils.sendLogToPlayers("Listening to: " + channel);
        client.getChat().joinChannel(channel);

        utils.sendLogToPlayers("Logged in as: " + user.getDisplayName());

        eventSocket = client.getEventSocket();
        eventManager = client.getEventManager();
        if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
            client.getPubSub().listenForChannelPointsRedemptionEvents(oauth, channel_id);
            log.info("Listening for channel point rewards...");
        }
        if (Rewards.getRewards(Rewards.rewardType.FOLLOW) != null) {
            if (TwitchUtils.getModeratedChannelIDs(oauth.getAccessToken(), user_id).contains(channel_id) || user_id.equals(channel_id)) { // If account is the streamer or a mod (need to have mod permissions on the channel)
                eventSocket.register(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(user_id).broadcasterUserId(channel_id).build(), null));
                eventManager.onEvent(FollowEvent.class, new Consumer<FollowEvent>() {
                    @Override
                    public void accept(FollowEvent e) {
                        eventHandler.onFollow(e);
                    }
                });
                log.info("Listening for follows...");            
            } else {
                log.warning("Follow events cannot be listened to on unauthorised channels.");
            }
        }
        if (Rewards.getRewards(Rewards.rewardType.CHEER) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
            eventManager.onEvent(ChannelChatMessageEvent.class, new Consumer<ChannelChatMessageEvent>() {
                @Override
                public void accept(ChannelChatMessageEvent e) {
                    eventHandler.onCheer(e);
                }
            }); 
            log.info("Listening for Cheers...");
        }

        if (Rewards.getRewards(Rewards.rewardType.SUB) != null || Rewards.getRewards(Rewards.rewardType.GIFT) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
            eventManager.onEvent(ChannelChatNotificationEvent.class, new Consumer<ChannelChatNotificationEvent>(){
                @Override
                public void accept(ChannelChatNotificationEvent e) {
                    eventHandler.onEvent(e);
                }
            });
            log.info("Listening for subscriptions and gifts...");
        }

        if (config.getBoolean("SHOW_CHAT")) {
            eventManager.onEvent(ChannelMessageEvent.class, event -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    net.md_5.bungee.api.ChatColor mcColor;
                    try {
                        mcColor = ColorUtils.getClosestChatColor(new Color(ColorUtils.hexToRgb(event.getMessageEvent().getUserChatColor().get())));
                    } catch (Exception e) {
                        log.warning(e.toString());
                        mcColor = net.md_5.bungee.api.ChatColor.RED; 
                    }
                    BaseComponent[] components = new BaseComponent[] {
                        new ComponentBuilder(mcColor + event.getMessageEvent().getUserDisplayName().get() + ": ").create()[0],
                        new ComponentBuilder(event.getMessage()).create()[0]
                    };
                    log.info(components[1].toPlainText());
                    utils.sendMessage(p, components);
                }
            });
        }
        eventHandler = new TwitchEventHandler();
        client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventHandler);
        log.info("Done!");
    }

    public void updateRedemption(String reward, String redemption, RedemptionStatus status) {
        client.getHelix().updateRedemptionStatus(oauth.getAccessToken(), client.getChat().getChannels().iterator().next(), reward, Arrays.asList(reward), status);
    }
}