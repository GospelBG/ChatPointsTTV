package me.gosdev.chatpointsttv.Twitch;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.util.ThreadUtils;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.domain.chat.NoticeType;
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.CustomRewardRedemptionAddEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.socket.events.EventSocketSubscriptionFailureEvent;
import com.github.twitch4j.eventsub.socket.events.EventSocketSubscriptionSuccessEvent;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.domain.InboundFollow;
import com.github.twitch4j.helix.domain.InboundFollowers;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.CPTTV_EventHandler;
import me.gosdev.chatpointsttv.Events.EventType;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.FollowerLog;
import me.gosdev.chatpointsttv.Utils.Scopes;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TwitchClient {
    public Thread linkThread;
    public Boolean ignoreOfflineStreamers = false;
    public static Boolean accountConnected = false;
    public OAuth2Credential oauth;
    public HashMap<String, OAuth2Credential> credentialManager;

    private boolean started;
    private List<String> chatBlacklist;
    private static ITwitchClient client;
    private static HashMap<String, Channel> channels;
    private static TwitchEvents eventHandler;
    private static IEventSubSocket eventSocket;
    private static EventManager eventManager;
    private final ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    private static FileConfiguration twitchConfig;
    private File accountsFile;
    private FileConfiguration accountsConfig;
    private ConfigurationSection accounts;
    private TwitchIdentityProvider identityProvider;
    private ScheduledThreadPoolExecutor exec;
    private HashMap<String, BukkitTask> tokenRefreshTasks;

    public Boolean shouldMobsGlow;
    public Boolean nameSpawnedMobs;
    public AlertMode alertMode;
    public ChatColor eventColor;
    public ChatColor userColor;
    
    private final static String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    public final static List<Object> scopes = new ArrayList<>(Arrays.asList(
        Scopes.CHANNEL_READ_REDEMPTIONS,
        Scopes.CHANNEL_READ_SUBSCRIPTIONS,
        Scopes.MODERATOR_READ_FOLLOWERS,
        Scopes.BITS_READ,
        Scopes.USER_READ_CHAT,
        Scopes.CHAT_READ,
        Scopes.USER_BOT,
        Scopes.CHANNEL_BOT
    ));

    public static String getClientID() {
        return ClientID;
    }
    
    public ITwitchClient getClient() {
        return client;
    }

    public FileConfiguration getConfig() {
        return twitchConfig;
    }

    public HashMap<String, Channel> getListenedChannels() {
        return channels;
    }

    public boolean isStarted() {
        return started;
    }

    public Boolean isAccountConnected() {
        return accountConnected;
    }

    public void enable() {
        channels = new HashMap<>();
        tokenRefreshTasks = new HashMap<>();
        File twitchConfigFile = new File(plugin.getDataFolder(), "twitch.yml");
        if (!twitchConfigFile.exists()) {
            plugin.saveResource(twitchConfigFile.getName(), false);
        }
        twitchConfig = YamlConfiguration.loadConfiguration(twitchConfigFile);

        accountsFile = new File(plugin.getDataFolder(), "accounts");
        accountsConfig = YamlConfiguration.loadConfiguration(accountsFile);
        if (!accountsConfig.contains("twitch")) {
            accountsConfig.createSection("twitch");
        }
        accounts = accountsConfig.getConfigurationSection("twitch");

        identityProvider = new TwitchIdentityProvider(getClientID(), null, null);
        credentialManager = new HashMap<>();
        exec = ThreadUtils.getDefaultScheduledThreadPoolExecutor("twitch4j", Runtime.getRuntime().availableProcessors());

        chatBlacklist = twitchConfig.getStringList("CHAT_BLACKLIST");
        ignoreOfflineStreamers = plugin.getConfig().getBoolean("IGNORE_OFFLINE_STREAMERS", false);

        // Configuration overrides
        shouldMobsGlow = twitchConfig.getBoolean("MOB_GLOW", ChatPointsTTV.shouldMobsGlow);
        nameSpawnedMobs = twitchConfig.getBoolean("DISPLAY_NAME_ON_MOB", ChatPointsTTV.nameSpawnedMobs);
        alertMode = AlertMode.valueOf(twitchConfig.getString("INGAME_ALERTS", ChatPointsTTV.alertMode.toString()).toUpperCase());

        try {
            eventColor = ChatColor.valueOf(twitchConfig.getString("COLORS.EVENT_COLOR", ChatPointsTTV.eventColor.name()).toUpperCase());
        } catch (NullPointerException e) {
            eventColor = null;
        }
        try {
            userColor = ChatColor.valueOf(twitchConfig.getString("COLORS.USER_COLOR", ChatPointsTTV.userColor.name()).toUpperCase());
        } catch (NullPointerException e) {
            userColor = null;
        }
        
        if (twitchConfig.getBoolean("FOLLOW_SPAM_PROTECTION", true)) {
            FollowerLog.start();
        }

        if (accounts != null) {
            for (String userid : accounts.getKeys(false)) {
                // Try to refresh token
                try {
                    link(Bukkit.getConsoleSender(), refreshCredentials(userid));
                } catch (RuntimeException e) {
                    ChatPointsTTV.log.warning("Credentials for User ID: " + userid + " have expired. You will need to link your account again.");
                    saveCredential(userid, null);
                }
            }
        }
        started = true;
    }

    public void link(CommandSender p, OAuth2Credential credential) {
        Bukkit.getScheduler().runTaskAsynchronously(ChatPointsTTV.getPlugin(), () -> {
            if (linkThread != null) {
                try {
                    linkThread.join();
                } catch (InterruptedException e) {}
            }
            
            linkThread = new Thread(() -> {
                saveCredential(credential.getUserId(), credential);
                credentialManager.put(credential.getUserId(), credential);

                for (Channel channel : channels.values()) {
                    if (credential.getUserId().equals(channel.getChannelId())) {
                        p.sendMessage(ChatPointsTTV.msgPrefix + "You cannot link an account twice!");
                        return;
                    }
                }

                p.sendMessage(ChatPointsTTV.msgPrefix + "Logging in as: " + credential.getUserName());
        
                tokenRefreshTasks.put(credential.getUserId(), Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Thread() {
                    @Override
                    public void run() {
                        refreshCredentials(credential.getUserId());
                    }
                }, credential.getExpiresIn() / 2 * 20, credential.getExpiresIn() / 2 * 20));
        
                if (accountConnected) {
                    subscribeToEvents(credential);
                } else {
                    start(credential);
                }

                p.sendMessage(ChatPointsTTV.msgPrefix + "Logged in successfully!");

                if (twitchConfig.getBoolean("FOLLOW_SPAM_PROTECTION", true)) {
                    List<String> followerIDs = new ArrayList<>();
                    String cursor = null;
                    while (true) { 
                        InboundFollowers request = client.getHelix().getChannelFollowers(credential.getAccessToken(), credential.getUserId(), null, null, cursor).execute();
                        cursor = request.getPagination().getCursor();
                        for (InboundFollow follower : request.getFollows()) {
                            followerIDs.add(follower.getUserId());
                        }
                        if (cursor == null) break;
                    }
                    FollowerLog.populateList(Platforms.TWITCH, credential.getUserId(), followerIDs);
                }
            });
            linkThread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
                linkThread.interrupt();
                e.printStackTrace();
                p.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + "Account linking failed!");
                stop(Bukkit.getConsoleSender());
            });

            linkThread.start();
        });
    }

    private void start(OAuth2Credential credential) {
        oauth = credential;
        // Build TwitchClient
        client = TwitchClientBuilder.builder()
            .withDefaultAuthToken(oauth)
            .withEnableChat(true)
            .withEnableHelix(true)
            .withEnableEventSocket(true)
            .withScheduledThreadPoolExecutor(exec)
            .build();        

        eventHandler = new TwitchEvents();

        eventSocket = client.getEventSocket();
        eventManager = client.getEventManager();

        eventManager.onEvent(ChannelGoLiveEvent.class, (ChannelGoLiveEvent e) -> {
            for (Channel channel : getListenedChannels().values()) {
                if (channel.getChannelUsername().equalsIgnoreCase(e.getChannel().getName())) channel.updateStatus(true);
            }
        });

        eventManager.onEvent(ChannelGoOfflineEvent.class, (ChannelGoOfflineEvent e) -> {
            for (Channel channel : getListenedChannels().values()) {
                if (channel.getChannelUsername().equalsIgnoreCase(e.getChannel().getName())) channel.updateStatus(false);
            }
        });            
        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.CHANNEL_POINTS) != null) {
            eventManager.onEvent(CustomRewardRedemptionAddEvent.class, (CustomRewardRedemptionAddEvent e) -> {
                eventHandler.onChannelPointsRedemption(e);
            });
        }
        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.FOLLOW) != null) {
            eventManager.onEvent(ChannelFollowEvent.class, (ChannelFollowEvent e) -> {
                eventHandler.onFollow(e);
            });
        }
        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.CHEER) != null) {
            eventManager.onEvent(ChannelChatMessageEvent.class, (ChannelChatMessageEvent e) -> {
                eventHandler.onCheer(e);
            }); 
        }
        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.SUB) != null || CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.GIFT) != null) {
            eventManager.onEvent(ChannelChatNotificationEvent.class, (ChannelChatNotificationEvent e) -> {
                    if (e.getNoticeType() == NoticeType.SUB || e.getNoticeType() == NoticeType.RESUB) eventHandler.onSub(e);
                    else if (e.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT) eventHandler.onSubGift(e);
            });
        }
        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.RAID) != null) {
            eventManager.onEvent(ChannelRaidEvent.class, (ChannelRaidEvent e) -> {
                    eventHandler.onRaid(e);
            }); 
        }
        if (plugin.getConfig().getBoolean("SHOW_CHAT")) {
            eventManager.onEvent(ChannelMessageEvent.class, event -> {
                if (ignoreOfflineStreamers && !getListenedChannels().get(event.getChannel().getName().toLowerCase()).isLive()) return;
                if (!chatBlacklist.contains(event.getUser().getName())) {
                    ChatColor mcColor;
                    try {
                        mcColor = ColorUtils.getClosestChatColor(new Color(ColorUtils.hexToRgb(event.getMessageEvent().getUserChatColor().get())));
                    } catch (Exception e) {
                        mcColor = ChatColor.RED; // Use red as fallback
                    }
                    BaseComponent[] components = new BaseComponent[] {
                        new ComponentBuilder(mcColor + event.getMessageEvent().getUserDisplayName().get() + ": ").create()[0],
                        new ComponentBuilder(event.getMessage()).create()[0]
                    };
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                            player.spigot().sendMessage(components);
                        }
                    }
                }
            });
        }

        // Join the twitch chat of this channel(s) and enable stream/follow events
        subscribeToEvents(credential);
        
        accountConnected = true;
    }
    
    private void subscribeToEvents(OAuth2Credential credential) {
        String channel_id = credential.getUserId();
        Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Listening to " + credential.getUserName() + "'s events...");

        ArrayList<EventSubSubscription> subs = new ArrayList<>();

        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.CHANNEL_POINTS) != null) {
            subs.add(SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD.prepareSubscription(b -> b.broadcasterUserId(channel_id).build(), null));
        }

        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.FOLLOW) != null) {
            subs.add(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(channel_id).broadcasterUserId(channel_id).build(), null));
        } 

        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.CHEER) != null) {
            subs.add(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.SUB) != null || CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.GIFT) != null) {
            subs.add(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.RAID) != null) {
            subs.add(SubscriptionTypes.CHANNEL_RAID.prepareSubscription(b -> b.toBroadcasterUserId(channel_id).build(), null));
        }

        CountDownLatch latch = new CountDownLatch(subs.size());

        eventManager.onEvent(EventSocketSubscriptionSuccessEvent.class, e -> latch.countDown());
        eventManager.onEvent(EventSocketSubscriptionFailureEvent.class, e -> latch.countDown());

        for (EventSubSubscription eventSubSubscription : subs) {
            eventSocket.register(credential, eventSubSubscription);
        }

        Channel channel = new Channel(credential.getUserName(), credential.getUserId(), TwitchUtils.isLive(credential.getAccessToken(), credential.getUserName()));
        channel.setSubscriptions(subs);
        channels.put(credential.getUserName(), channel);
        
        client.getChat().joinChannel(credential.getUserName());

        try {
            latch.await();
        } catch (InterruptedException e) {
            ChatPointsTTV.log.warning("Failed to subscribe to events.");
        }
    }

    public OAuth2Credential refreshCredentials(String userId) {
        OAuth2Credential oldCredential;
        if (!credentialManager.containsKey(userId)) {
            if (!accounts.contains(userId) || !accounts.contains(userId + ".access_token") || !accounts.contains(userId + ".refresh_token")) {
                throw new NullPointerException("Couldn't retrieve credentials for user: " + userId);
            }
            oldCredential = new OAuth2Credential(identityProvider.getProviderName(), accounts.getString(userId + ".access_token"), accounts.getString(userId + ".refresh_token"), userId, null, null, null);
        } else {
            oldCredential = credentialManager.get(userId);
        }

        Optional<OAuth2Credential> refreshed = identityProvider.refreshCredential(oldCredential);
        if (refreshed.isPresent()) {
            OAuth2Credential fullCredential = identityProvider.getAdditionalCredentialInformation(refreshed.get()).get();
            saveCredential(fullCredential.getUserId(), fullCredential);
            credentialManager.put(fullCredential.getUserId(), fullCredential);

            if (oauth != null && oauth.getUserId().equals(fullCredential.getUserId())) {
                oauth.updateCredential(fullCredential);
            }

            return fullCredential;
        } else {
            throw new RuntimeException("Failed to refresh credentials.");
        }
    }

    public void saveCredential(String userId, OAuth2Credential credential) {
        if (credential == null) {
            accounts.set(userId, null);
        } else {
            ConfigurationSection account = accounts.createSection(userId);
            account.set("access_token", credential.getAccessToken());
            account.set("refresh_token", credential.getRefreshToken());
        }

        try {
            accountsConfig.save(accountsFile);
        } catch (IOException e) {
            ChatPointsTTV.log.severe("ChatPointsTTV: There was an issue saving account session credentials.");
        }
    }

    public void unlinkAccount(String username) {
        Channel channel = channels.get(username);
        if (channel == null) throw new NullPointerException("Cannot find channel");

        for (EventSubSubscription sub : channel.getSubs()) {
            eventSocket.unregister(sub);
        }

        channels.remove(username);
        client.getChat().leaveChannel(username);

        saveCredential(channel.getChannelId(), null); // Remove stored credential
        identityProvider.revokeCredential(credentialManager.get(channel.getChannelId()));
        tokenRefreshTasks.get(channel.getChannelId()).cancel();
        credentialManager.remove(channel.getChannelId());

        if (credentialManager.isEmpty()) {
            accountConnected = false;
        }
    }

    public void stop(CommandSender p) {
        try {
            if (linkThread != null && !linkThread.isInterrupted()) linkThread.join(); // Wait until linking is finished
            if (client != null) {
                eventSocket.close();
                client.close();
            }
        } catch (Exception e) {
            ChatPointsTTV.log.warning("There was an error while disabling the Twitch client.");
            e.printStackTrace();
            return;
        }
        
        for (BukkitTask task : tokenRefreshTasks.values()) {
            task.cancel();
        }

        client = null;
        eventHandler = null;
        eventSocket = null;
        eventManager = null;
        channels.clear();
        credentialManager.clear();

        for (EventType type : TwitchEventType.values()) {
            CPTTV_EventHandler.actions.remove(type);
        }

        accountConnected = false;
        started = false;    

        p.sendMessage(ChatPointsTTV.msgPrefix + "Twitch client has been successfully stopped!");
    }
}
