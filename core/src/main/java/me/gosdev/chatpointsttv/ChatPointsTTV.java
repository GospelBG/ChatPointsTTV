package me.gosdev.chatpointsttv;

import java.util.Arrays;
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
import com.github.twitch4j.eventsub.domain.RedemptionStatus;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import com.github.twitch4j.helix.domain.UserList;
import me.gosdev.chatpointsttv.TwitchAuth.Scopes;
import me.gosdev.chatpointsttv.Utils.Utils;

public class ChatPointsTTV extends JavaPlugin {
    private static ITwitchClient client;
    private static TwitchEventHandler eventHandler;
    private static IEventSubSocket eventSocket;
    private static EventManager eventManager;
    private static ChatPointsTTV plugin;
    private CommandController cmdController;

    private static Map<String, ChatColor> colors = new HashMap<String, ChatColor>();
    private static Map<String, String> titleStrings = new HashMap<String, String>();

    public static boolean configOk = true;

    public Logger log = getLogger();
    public FileConfiguration config;
    private Boolean accountConnected = false;

    private final String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    private final String scopes = Scopes.join(
        Scopes.CHANNEL_READ_REDEMPTIONS,
        Scopes.CHANNEL_MANAGE_REDEMPTIONS,
        Scopes.BITS_READ,
        Scopes.CHANNEL_READ_SUBSCRIPTIONS,
        Scopes.USER_READ_CHAT,
        Scopes.CHAT_READ,
        Scopes.CHAT_EDIT
        ).replace(":", "%3A"); // Format colon character for browser
    private final String AuthURL = "https://id.twitch.tv/oauth2/authorize?response_type=token&client_id=" + ClientID + "&redirect_uri=http://localhost:3000&scope="+scopes;

    private OAuth2Credential oauth;
    public enum reward_type {
        CHANNEL_POINTS,
        CHEER,
        SUB,
        SUB_GIFT
    };

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

    public static Map<String, ChatColor> getChatColors() {
        return colors;
    }
    public static Map<String, String> getRedemptionStrings() {
        return titleStrings;
    }
    public static Map<String, Object> getRewards(reward_type type) {
        switch(type) {
            case CHANNEL_POINTS:
                return plugin.getConfig().getConfigurationSection("REWARDS").getValues(false);
            case CHEER:
                return plugin.getConfig().getConfigurationSection("CHEER_REWARDS").getValues(false);
            case SUB:
                return plugin.getConfig().getConfigurationSection("SUB_REWARDS").getValues(false);
            case SUB_GIFT:
                return plugin.getConfig().getConfigurationSection("GIFT_REWARDS").getValues(false);
                
            default:
                plugin.log.warning("Cannot find any reward of type " + type);
                return null;
        }
    }

    private static Utils utils;

    public static Utils getUtils() {
        if (utils != null) return utils;

        int version = Integer.parseInt(Bukkit.getServer().getClass().getName().split("\\.")[3].split("_")[1]);
        try {
            if (version <= 8) { 
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_8_R1").getDeclaredConstructor().newInstance();
            } else {
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_8_R1").getDeclaredConstructor().newInstance();
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

        // Get the latest config after saving the default if missing
        this.saveDefaultConfig();
        config = getConfig();

        config.getConfigurationSection("COLORS").getKeys(false).forEach(i -> {
            colors.put(i, ChatColor.valueOf(config.getConfigurationSection("COLORS").getString(i)));
        });

        config.getConfigurationSection("STRINGS").getKeys(true).forEach(i -> {
            titleStrings.put(i, config.getConfigurationSection("STRINGS").getString(i));
        });

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (!accountConnected && player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) {
                    String msg = "Welcome! Remember to log in with your Twitch account for ChatPointsTTV to be able to connect and listen.";
                    ComponentBuilder builder = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "[Click here to login]");
                    BaseComponent btn = builder.create()[0];

                    btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to run command").create()));
                    btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch link"));

                    player.getPlayer().spigot().sendMessage(new ComponentBuilder(msg).create()[0]);
                    player.getPlayer().spigot().sendMessage(btn);
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
    }

    public void linkToTwitch(String token) {
        getUtils().sendLogToPlayers("Logging in...");
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

        log.info("Logged in as: "+ client.getHelix().getUsers(token, null, null).execute().getUsers().get(0).getDisplayName());
        accountConnected = true;

        // Join the twitch chat of this channel and enable stream/follow events
        String channel = config.getString("CHANNEL_USERNAME");
        String channel_id = getUserId(channel);
        String user_id = new TwitchIdentityProvider(null, null, null).getAdditionalCredentialInformation(oauth).map(OAuth2Credential::getUserId).orElse(null);
        log.info("Listening to " + channel + "'s events...");
        getUtils().sendLogToPlayers("Listening to: " + channel);
        client.getChat().joinChannel(channel);

        getUtils().sendLogToPlayers("Logged in as: " + client.getHelix().getUsers(token, null, null).execute().getUsers().get(0).getDisplayName());

        eventSocket = client.getEventSocket();
        eventManager = client.getEventManager();
        if (!config.getConfigurationSection("REWARDS").getKeys(true).isEmpty()) {
            client.getPubSub().listenForChannelPointsRedemptionEvents(oauth, channel_id);
            log.info("Listening for channel point rewards...");
        }
        if (!config.getConfigurationSection("CHEER_REWARDS").getKeys(true).isEmpty()) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
            eventManager.onEvent(ChannelChatMessageEvent.class, new Consumer<ChannelChatMessageEvent>(){
                @Override
                public void accept(ChannelChatMessageEvent e) {
                    eventHandler.onCheer(e);
                }
            }); 
            log.info("Listening for Cheers...");
        }

        if (!config.getConfigurationSection("SUB_REWARDS").getKeys(true).isEmpty() || !config.getConfigurationSection("GIFT_REWARDS").getKeys(true).isEmpty()) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
            eventManager.onEvent(ChannelChatNotificationEvent.class, new Consumer<ChannelChatNotificationEvent>(){
                @Override
                public void accept(ChannelChatNotificationEvent e) {
                    eventHandler.onEvent(e);
                }
            });
            log.info("Listening for subscriptions and gifts...");
        }
        eventHandler = new TwitchEventHandler();
        client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventHandler);
        log.info("Done!");
    }

    public void updateRedemption(String reward, String redemption, RedemptionStatus status) {
        client.getHelix().updateRedemptionStatus(oauth.getAccessToken(), client.getChat().getChannels().iterator().next(), reward, Arrays.asList(reward), status);
    }
}