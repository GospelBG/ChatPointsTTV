package me.gosdev.chatpointsttv;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.eventsub.domain.chat.NoticeType;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.socket.events.EventSocketSubscriptionFailureEvent;
import com.github.twitch4j.eventsub.socket.events.EventSocketSubscriptionSuccessEvent;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.pubsub.events.RaidGoEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.TwitchAuth.ImplicitGrantFlow;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.Scopes;
import me.gosdev.chatpointsttv.Utils.TwitchUtils;
import me.gosdev.chatpointsttv.Utils.Utils;

public class ChatPointsTTV extends JavaPlugin {
    private static ITwitchClient client;
    private static ArrayList<Channel> channels;
    private User user;
    private static TwitchEventHandler eventHandler;
    private static IEventSubSocket eventSocket;
    private static EventManager eventManager;
    private static ChatPointsTTV plugin;
    private CommandController cmdController;

    private static Map<String, ChatColor> colors = new HashMap<String, org.bukkit.ChatColor>();
    private static Map<String, String> titleStrings = new HashMap<String, String>();
    public static Boolean customCredentials = false;
    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;
    public static alert_mode alertMode;
    private List<String> chatBlacklist;
    public static boolean configOk = true;
    public Thread linkThread;

    private String user_id;

    public Logger log = getLogger();
    public FileConfiguration config;
    public Metrics metrics;
    private Boolean accountConnected = false;

    private final static String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    public final String scopes = Scopes.join(
        Scopes.CHANNEL_READ_REDEMPTIONS,
        Scopes.CHANNEL_READ_SUBSCRIPTIONS,
        Scopes.USER_READ_MODERATED_CHANNELS,
        Scopes.MODERATOR_READ_FOLLOWERS,
        Scopes.BITS_READ,
        Scopes.CHANNEL_READ_SUBSCRIPTIONS,
        Scopes.USER_READ_CHAT,
        Scopes.CHAT_READ,
        Scopes.USER_BOT,
        Scopes.CHANNEL_BOT
        ).replace(":", "%3A"); // Format colon character for browser

    public static OAuth2Credential oauth;

    public static enum permissions {
        BROADCAST("chatpointsttv.broadcast"),
        MANAGE("chatpointsttv.manage"),
        TARGET("chatpointsttv.target");

        public final String permission_id;

        private permissions(String label) {
            this.permission_id = label;
        }
    }

    public static enum alert_mode {
        NONE,
        CHAT,
        TITLE,
        ALL
    }

    public static ChatPointsTTV getPlugin() {
        return plugin;
    }

    public static ITwitchClient getTwitchClient() {
        return client;
    }

    public static String getClientID() {
        if (customCredentials) {
            return plugin.config.getString("CUSTOM_CLIENT_ID");
        } else {
            return ClientID;
        }
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
    public String getConnectedUsername() {
        return accountConnected ? user.getLogin() : "Not Linked";
    }
    public List<Channel> getListenedChannels() {
        boolean needRefresh = false;
        if (channels != null) {
            for (int i = 0; i < channels.size(); i++) {
                if (channels.get(i).getChannelId() == null) needRefresh = true;
            }
        }

        if (channels == null || channels.isEmpty()) needRefresh = true;

        if (needRefresh) {
            channels = new ArrayList<>();
            List<String> usernames = new ArrayList<>();
            if (plugin.config.getStringList("CHANNEL_USERNAME") != null)  {
                usernames = plugin.config.getStringList("CHANNEL_USERNAME");
            } else {
                usernames.add(plugin.config.getString("CHANNEL_USERNAME"));
            }

            if (isAccountConnected()) {
                for (String name : usernames) {
                    StreamList request = client.getHelix().getStreams(oauth.getAccessToken(), null, null, null, null, null, null, Arrays.asList(name)).execute();
                    String id = client.getHelix().getUsers(null, null, Arrays.asList(name)).execute().getUsers().get(0).getId();
        
                    channels.add(new Channel(name, id, request.getStreams().size() > 0));
                }
            } else {
                for (String name : usernames) {
                    channels.add(new Channel(name, null, false)); // If unable to check live status
                }
            }
            return channels;
        }
        return ChatPointsTTV.channels;
    }

    private static Utils utils;

    public static Utils getUtils() {
        if (utils != null) return  utils;
        final Pattern pattern = Pattern.compile("1\\.\\d\\d?");
        final Matcher matcher = pattern.matcher(Bukkit.getVersion());
        matcher.find();
        int version = Integer.parseInt(matcher.group().split("\\.")[1]);
        try {
            if (version >= 12) { 
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_12_R1").getDeclaredConstructor().newInstance();
            } else {
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_9_R1").getDeclaredConstructor().newInstance();
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
        metrics = new Metrics(this, 22873);

        utils = getUtils();

        try {
            // Get the latest config after saving the default if missing
            this.saveDefaultConfig();
            config = getConfig();

            if (config.getString("CHANNEL_USERNAME") == null | config.getString("CHANNEL_USERNAME").startsWith("MemorySection[path=")) { // Invalid string (probably left default "{YOUR CHANNEL}")
                throw new Exception("Cannot read channel. Config file may be not set up or invalid.");
            } else {
                configOk = true;
            }


        if (config.getString("CUSTOM_CLIENT_ID") != null || config.getString("CUSTOM_CLIENT_SECRET") != null) customCredentials = true;

        config.getConfigurationSection("COLORS").getKeys(false).forEach(i -> {
            colors.put(i, org.bukkit.ChatColor.valueOf(config.getConfigurationSection("COLORS").getString(i)));
        });

        config.getConfigurationSection("STRINGS").getKeys(true).forEach(i -> {
            titleStrings.put(i, config.getConfigurationSection("STRINGS").getString(i));
        });

        TwitchEventHandler.rewardBold = config.getBoolean("REWARD_NAME_BOLD");

        shouldMobsGlow = config.getBoolean("MOB_GLOW", false);
        alertMode = alert_mode.valueOf(config.getString("INGAME_ALERTS").toUpperCase());
        nameSpawnedMobs = config.getBoolean("DISPLAY_NAME_ON_MOB", true);
        chatBlacklist = config.getStringList("CHAT_BLACKLIST");
    } catch (Exception e) {
        configOk = false;
        log.warning("An error occurred while reading config.yml");
    }

        cmdController = new CommandController();
        this.getCommand("twitch").setExecutor(cmdController);
        this.getCommand("twitch").setTabCompleter(cmdController);

        utils.sendMessage(Bukkit.getConsoleSender(), "ChatPointsTTV enabled!");
        for (Player p: plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                utils.sendMessage(p, new TextComponent("ChatPointsTTV reloaded!"));
            }
        }
        VersionCheck.check();

        if(customCredentials && config.getBoolean("AUTO_LINK_CUSTOM", false) == true) {
            metrics.addCustomChart(new SimplePie("authentication_method", () -> {
                return "Twitch Auto-Link (Key)";
            }));
            
            linkToTwitch(Bukkit.getConsoleSender(), plugin.config.getString("CUSTOM_ACCESS_TOKEN"));
        }

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (!accountConnected && player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) {
                    String msg = "Welcome! Remember to log in with your Twitch account for ChatPointsTTV to be able to connect and listen.\n";
                    BaseComponent btn = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "[Click here to login]").create()[0];

                    btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to run command").create()));
                    btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch link"));

                    utils.sendMessage(player.getPlayer(), new BaseComponent[] {new ComponentBuilder(msg).create()[0], btn});
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        if (client != null) unlink(Bukkit.getConsoleSender());
        
        if (ImplicitGrantFlow.server.isRunning()) {
            ImplicitGrantFlow.server.stop();
        }
    
        // Erase variables
        client = null;
        eventHandler = null;
        eventSocket = null;
        eventManager = null;
        config = null;
        accountConnected = false;
        channels = null;
        oauth = null;
        plugin = null;

        Rewards.rewards = new HashMap<rewardType,ArrayList<Reward>>();
        TwitchEventHandler.rewardBold = null;

        HandlerList.unregisterAll(this);
    }

    public void linkToTwitch(CommandSender p, String token) {
        linkThread = new Thread(() -> {
            utils.sendMessage(p, "Logging in...");

            if(getClientID() == null || getClientID().isEmpty()) {
                throw new NullPointerException("Invalid Client ID");
            }
            if (token == null || token.isEmpty()) {
                throw new NullPointerException("Invalid Access Token");
            }


            oauth = new OAuth2Credential(getClientID(), token);

            // Build TwitchClient
            client = TwitchClientBuilder.builder()
                .withDefaultAuthToken(oauth)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnablePubSub(true)
                .withEnableEventSocket(true)
                .withDefaultEventHandler(SimpleEventHandler.class)
                .build();        
            
            user = client.getHelix().getUsers(token, null, null).execute().getUsers().get(0);

            utils.sendMessage(Bukkit.getConsoleSender(), "Logged in as: "+ user.getDisplayName());

            eventHandler = new TwitchEventHandler();

            // Linked account UserID
            user_id = new TwitchIdentityProvider(null, null, null).getAdditionalCredentialInformation(oauth).map(OAuth2Credential::getUserId).orElse(null);

            eventSocket = client.getEventSocket();
            eventManager = client.getEventManager();

            int channels = getListenedChannels().size();
            int subs = 0;

            eventManager.onEvent(ChannelGoLiveEvent.class, new Consumer<ChannelGoLiveEvent>() {
                @Override
                public void accept(ChannelGoLiveEvent e) {
                    for (Channel channel : getListenedChannels()) {
                        if (channel.getChannelUsername().equalsIgnoreCase(e.getChannel().getName())) channel.updateStatus(true);
                    }
                }
            });

            eventManager.onEvent(ChannelGoOfflineEvent.class, new Consumer<ChannelGoOfflineEvent>() {
                @Override
                public void accept(ChannelGoOfflineEvent e) {
                    for (Channel channel : getListenedChannels()) {
                        if (channel.getChannelUsername().equalsIgnoreCase(e.getChannel().getName())) channel.updateStatus(false);
                    }
                }
            });            
            if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
                eventManager.onEvent(RewardRedeemedEvent.class, new Consumer<RewardRedeemedEvent>() {
                    @Override
                    public void accept(RewardRedeemedEvent e) {
                        eventHandler.onChannelPointsRedemption(e);
                    }
                });
            }
            if (Rewards.getRewards(Rewards.rewardType.FOLLOW) != null) {
                subs++;
                eventManager.onEvent(ChannelFollowEvent.class, new Consumer<ChannelFollowEvent>() {
                    @Override
                    public void accept(ChannelFollowEvent e) {
                        try { // May get NullPointerException if event is triggered while still subscribing
                            eventHandler.onFollow(e);
                        } catch (NullPointerException ex) {}
                    }
                });
            }
            if (Rewards.getRewards(Rewards.rewardType.CHEER) != null) {
                subs++;
                eventManager.onEvent(ChannelChatMessageEvent.class, new Consumer<ChannelChatMessageEvent>() {
                    @Override
                    public void accept(ChannelChatMessageEvent e) {
                        try { // May get NullPointerException if event is triggered while still subscribing
                            eventHandler.onCheer(e);
                        } catch (NullPointerException ex) {}// May get NullPointerException if event is triggered while still subscribing
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }); 
            }
            if (Rewards.getRewards(Rewards.rewardType.SUB) != null || Rewards.getRewards(Rewards.rewardType.GIFT) != null) {
                subs++;
                eventManager.onEvent(ChannelChatNotificationEvent.class, new Consumer<ChannelChatNotificationEvent>(){
                    @Override
                    public void accept(ChannelChatNotificationEvent e) {
                        try { // May get NullPointerException if event is triggered while still subscribing
                            if (e.getNoticeType() == NoticeType.SUB || e.getNoticeType() == NoticeType.RESUB) eventHandler.onSub(e);
                            else if (e.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT) eventHandler.onSubGift(e);
                        } catch (NullPointerException ex) {}// May get NullPointerException if event is triggered while still subscribing
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            if (Rewards.getRewards(Rewards.rewardType.RAID) != null) {
                eventManager.onEvent(ChannelRaidEvent.class, new Consumer<ChannelRaidEvent>() { // Don't count for CountdownLatch because this is PubSub
                    @Override
                    public void accept(ChannelRaidEvent e) {
                        try { // May get NullPointerException if event is triggered while still subscribing
                            eventHandler.onRaid(e);
                        } catch (NullPointerException ex) {}// May get NullPointerException if event is triggered while still subscribing
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }); 
            }
            if (config.getBoolean("SHOW_CHAT")) {
                eventManager.onEvent(ChannelMessageEvent.class, event -> {
                    if (!chatBlacklist.contains(event.getUser().getName())) {
                        net.md_5.bungee.api.ChatColor mcColor;
                        try {
                            mcColor = ColorUtils.getClosestChatColor(new Color(ColorUtils.hexToRgb(event.getMessageEvent().getUserChatColor().get())));
                        } catch (Exception e) {
                            mcColor = net.md_5.bungee.api.ChatColor.RED; 
                        }
                        BaseComponent[] components = new BaseComponent[] {
                            new ComponentBuilder(mcColor + event.getMessageEvent().getUserDisplayName().get() + ": ").create()[0],
                            new ComponentBuilder(event.getMessage()).create()[0]
                        };
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.hasPermission(permissions.BROADCAST.permission_id)) {
                                utils.sendMessage(player, components);
                            }
                        }
                    }
                });
            }

            CountDownLatch latch = new CountDownLatch(subs * channels);

            eventManager.onEvent(EventSocketSubscriptionSuccessEvent.class, e -> latch.countDown());
            eventManager.onEvent(EventSocketSubscriptionFailureEvent.class, e -> latch.countDown());

            // Join the twitch chat of this channel(s) and enable stream/follow events
            if (config.getList("CHANNEL_USERNAME") == null) { // If field is not a list (single channel)            
                subscribeToEvents(p, latch, config.getString("CHANNEL_USERNAME"));
            } else {
                for (String channel : config.getStringList("CHANNEL_USERNAME")) {
                    subscribeToEvents(p, latch, channel);
                }
            }

            try {
                client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventHandler);
                latch.await();
            } catch (InterruptedException e) {
                log.warning("Failed to bind events.");
                return;
            }

            utils.sendMessage(p, "Twitch client has started successfully!");
            accountConnected = true;
        });
        linkThread.start();
        linkThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                linkThread.interrupt();
                e.printStackTrace();
                utils.sendMessage(p, ChatColor.RED + "Account linking failed!");
                accountConnected = true;
                unlink(Bukkit.getConsoleSender());
            }
        });
    }
    
    public void subscribeToEvents(CommandSender p, CountDownLatch latch, String channel) {
        String channel_id = TwitchUtils.getUserId(channel);

        client.getClientHelper().enableStreamEventListener(channel);
        
        if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
            client.getPubSub().listenForChannelPointsRedemptionEvents(null, channel_id);
        }

        if (Rewards.getRewards(Rewards.rewardType.FOLLOW) != null) {
            if (TwitchUtils.getModeratedChannelIDs(oauth.getAccessToken(), user_id).contains(channel_id) || user_id.equals(channel_id)) { // If account is the streamer or a mod (need to have mod permissions on the channel)
                eventSocket.register(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(user_id).broadcasterUserId(channel_id).build(), null));
            } else {
                log.warning(channel + ": Follow events cannot be listened to on unauthorised channels.");
                latch.countDown();
            }
        } 

        if (Rewards.getRewards(Rewards.rewardType.CHEER) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.SUB) != null || Rewards.getRewards(Rewards.rewardType.GIFT) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.RAID) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_RAID.prepareSubscription(b -> b.toBroadcasterUserId(channel_id).build(), null));
        }
        utils.sendMessage(Bukkit.getConsoleSender(), "Listening to " + channel + "'s events...");
        client.getChat().joinChannel(channel);
    }

    public void unlink(CommandSender p) {
        if (!accountConnected) {
            utils.sendMessage(p, new TextComponent(ChatColor.RED + "There is no connected account."));
            return;
        }
        try {
            if (!linkThread.isInterrupted()) linkThread.join(); // Wait until linking is finished
            client.getEventSocket().close();
            client.getPubSub().close();
            client.close();
            accountConnected = false;
        } catch (Exception e) {
            log.warning("Error while disabling ChatPointsTTV.");
            e.printStackTrace();
            return;
        }

        utils.sendMessage(p, ChatColor.GREEN + "Account disconnected!");
    }
}
