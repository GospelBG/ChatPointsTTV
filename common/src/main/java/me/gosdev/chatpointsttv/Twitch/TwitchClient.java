package me.gosdev.chatpointsttv.Twitch;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.auth.providers.TwitchIdentityProvider;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
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
import com.github.twitch4j.helix.domain.CustomReward;
import com.github.twitch4j.helix.domain.InboundFollow;
import com.github.twitch4j.helix.domain.InboundFollowers;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Chat.ChatComponent;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ConfigFile;
import me.gosdev.chatpointsttv.Events.Action;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.FollowerLog;
import me.gosdev.chatpointsttv.Utils.Scopes;

public class TwitchClient {
    public Boolean ignoreOfflineStreamers = false;
    public Thread stopThread;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean accountConnected = new AtomicBoolean(false);
    private final AtomicBoolean linkInProgress = new AtomicBoolean(false);
    public AtomicBoolean reloading = new AtomicBoolean(true);
    private List<String> chatBlacklist;
    private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();
    private TwitchEvents eventHandler;
    private IEventSubSocket eventSocket;
    private EventManager eventManager;
    private ITwitchClient client;
    private ConfigFile twitchConfig;
    public final TwitchIdentityProvider identityProvider = new TwitchIdentityProvider(CLIENT_ID, null, null);
    private ScheduledThreadPoolExecutor exec;
    private final HashMap<String, ScheduledFuture<?>> tokenRefreshTasks = new HashMap<>();
    private final ScheduledExecutorService refreshExecutor;
    private final ExecutorService twitchExecutor;

    public Boolean shouldMobsGlow;
    public Boolean nameSpawnedMobs;
    public AlertMode alertMode;
    
    public final static String CLIENT_ID = "1peexftcqommf5tf5pt74g7b3gyki3";
    public final static List<Object> scopes = new ArrayList<>(Arrays.asList(
        Scopes.CHANNEL_READ_REDEMPTIONS,
        Scopes.CHANNEL_MANAGE_REDEMPTIONS,
        Scopes.MODERATOR_READ_FOLLOWERS,
        Scopes.USER_READ_CHAT,
        Scopes.CHAT_READ
    ));

    public ITwitchClient getClient() {
        return client;
    }

    public ConfigFile getConfig() {
        return twitchConfig;
    }

    public ConcurrentHashMap<String, Channel> getListenedChannels() {
        return channels;
    }

    public boolean isStarted() {
        return started.get();
    }

    public Boolean isAccountConnected() {
        return accountConnected.get();
    }

    public ExecutorService getExecutor() {
        return twitchExecutor;
    }

    public TwitchClient(GenericSender p, ConfigFile config) {
        started.set(false);
        reloading.set(true);

        twitchConfig = config;
        twitchExecutor = Executors.newSingleThreadExecutor();
        refreshExecutor = Executors.newSingleThreadScheduledExecutor();

        twitchExecutor.submit(() -> {
            config.reload();
            for (TwitchEventType type : TwitchEventType.values()) {
                me.gosdev.chatpointsttv.Events.EventManager.parseActions(type, config);
            }

            exec = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
            exec.setRemoveOnCancelPolicy(true);

            chatBlacklist = twitchConfig.getStringList("CHAT_BLACKLIST");
            ignoreOfflineStreamers = ChatPointsTTV.getConfig().getBoolean("IGNORE_OFFLINE_STREAMERS", false);

            // Configuration overrides
            shouldMobsGlow = twitchConfig.getBoolean("MOB_GLOW", ChatPointsTTV.shouldMobsGlow);
            nameSpawnedMobs = twitchConfig.getBoolean("DISPLAY_NAME_ON_MOB", ChatPointsTTV.nameSpawnedMobs);
            alertMode = AlertMode.valueOf(twitchConfig.getString("INGAME_ALERTS", ChatPointsTTV.alertMode.toString()).toUpperCase());

            setupTwitch4JLogs();
            
            if (twitchConfig.getBoolean("FOLLOW_SPAM_PROTECTION", true)) {
                FollowerLog.start();
            }

            List<String> storedUserIds = ChatPointsTTV.getAccounts().getStoredUsers(Platforms.TWITCH);
            if (!storedUserIds.isEmpty()) {
                for (String userId : storedUserIds) {
                    // Try to refresh token
                    try {
                        link(p, refreshCredentials(userId));
                    } catch (RuntimeException e) {
                        ChatPointsTTV.log.warn("Credentials for User ID: " + userId + " have expired. You will need to link your account again.");
                        ChatPointsTTV.getAccounts().removeAccount(Platforms.TWITCH, userId);
                    } 
                }
            }
            
            p.sendMessage(ChatPointsTTV.PREFIX + "Twitch Module has started successfully!");   
            started.set(true);
            reloading.set(false);
        });
    }

    public void link(GenericSender p, OAuth2Credential credential) {
        linkInProgress.set(true);
        try {
            ChatPointsTTV.getAccounts().saveCredential(credential.getUserId(), Optional.of(credential));

            for (Channel channel : channels.values()) {
                if (credential.getUserId().equals(channel.getChannelId())) {
                    p.sendMessage(ChatPointsTTV.PREFIX + "You cannot link an account twice!");
                    return;
                }
            }

            p.sendMessage(ChatPointsTTV.PREFIX + "Logging in as: " + credential.getUserName());
            tokenRefreshTasks.put(credential.getUserId(), refreshExecutor.scheduleAtFixedRate(() -> {
                refreshCredentials(credential.getUserId());
            }, credential.getExpiresIn() / 2 * 20, credential.getExpiresIn() / 2 * 20, TimeUnit.SECONDS));


            if (accountConnected.get()) {
                subscribeToEvents(credential);
            } else {
                start(credential);
            }

            if (twitchConfig.getBoolean("FOLLOW_SPAM_PROTECTION", true)) {
                List<String> followerIDs = new ArrayList<>();
                String cursor = null;
                while (true) { 
                    InboundFollowers request = client.getHelix().getChannelFollowers(credential.getAccessToken(), credential.getUserId(), null, null, cursor).execute();
                    cursor = request.getPagination().getCursor();
                    List<InboundFollow> follows = request.getFollows();

                    if (follows == null || follows.isEmpty()) break;
                    for (InboundFollow follower : follows) {
                        followerIDs.add(follower.getUserId());
                    }
                    if (cursor == null) break;
                }
                FollowerLog.populateList(Platforms.TWITCH, credential.getUserId(), followerIDs);
            }

            p.sendMessage(ChatPointsTTV.PREFIX + "Logged in successfully!");
        } catch (Exception e) {
            p.sendMessage(ChatPointsTTV.PREFIX + ChatColor.RED + "Twitch account linking failed.");
            e.printStackTrace();
            unlink(p, null);
        }
        linkInProgress.set(false);
    }

    private void start(OAuth2Credential credential) {
        // Build TwitchClient
        client = TwitchClientBuilder.builder()
            .withDefaultAuthToken(credential)
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

        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.CHANNEL_POINTS)) {
            eventManager.onEvent(CustomRewardRedemptionAddEvent.class, (CustomRewardRedemptionAddEvent e) -> {
                eventHandler.onChannelPointsRedemption(e);
            });
        }
        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.FOLLOW)) {
            eventManager.onEvent(ChannelFollowEvent.class, (ChannelFollowEvent e) -> {
                eventHandler.onFollow(e);
            });
        }
        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.CHEER)) {
            eventManager.onEvent(ChannelChatMessageEvent.class, (ChannelChatMessageEvent e) -> {
                eventHandler.onCheer(e);
            }); 
        }
        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.SUB) || me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.GIFT)) {
            eventManager.onEvent(ChannelChatNotificationEvent.class, (ChannelChatNotificationEvent e) -> {
                    if (e.getNoticeType() == NoticeType.SUB || e.getNoticeType() == NoticeType.RESUB) eventHandler.onSub(e);
                    else if (e.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT) eventHandler.onSubGift(e);
            });
        }
        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.RAID)) {
            eventManager.onEvent(ChannelRaidEvent.class, (ChannelRaidEvent e) -> {
                    eventHandler.onRaid(e);
            }); 
        }
        if (ChatPointsTTV.getConfig().getBoolean("SHOW_CHAT", true)) {
            eventManager.onEvent(ChannelMessageEvent.class, event -> {
                if (ignoreOfflineStreamers && !getListenedChannels().get(event.getChannel().getName().toLowerCase()).isLive()) return;
                if (!chatBlacklist.contains(event.getUser().getName())) {
                    ChatColor mcColor;
                    try {
                        mcColor = ColorUtils.getClosestChatColor(new Color(ColorUtils.hexToRgb(event.getMessageEvent().getUserChatColor().get())));
                    } catch (Exception e) {
                        mcColor = ChatColor.RED; // Use red as fallback
                    }

                    ChatComponent chatMsg = new ChatComponent(mcColor + event.getMessageEvent().getUserDisplayName().get() + ": ")
                        .addExtra(event.getMessage());
                    for (GenericPlayer player : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                        if (player.hasPermission(ChatPointsTTV.permissions.BROADCAST)) {
                            player.sendMessage(chatMsg);
                        }
                    }
                }
            });
        }

        // Join the twitch chat of this channel(s) and enable stream/follow events
        subscribeToEvents(credential);
        
        accountConnected.set(true);
    }
    
    private void subscribeToEvents(OAuth2Credential credential) {
        String channel_id = credential.getUserId();
        ArrayList<EventSubSubscription> subs = new ArrayList<>();

        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.CHANNEL_POINTS)) {
            toggleChannelPointRewards(credential, true);
            subs.add(SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD.prepareSubscription(b -> b.broadcasterUserId(channel_id).build(), null));
        }

        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.FOLLOW)) {
            subs.add(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.CHEER)) {
            subs.add(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.SUB) || me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.GIFT)) {
            subs.add(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (me.gosdev.chatpointsttv.Events.EventManager.actionsFound(TwitchEventType.RAID)) {
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
            ChatPointsTTV.log.warn("Failed to subscribe to events.");
        }
    }

    public Boolean createChannelPointRewards(OAuth2Credential account) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder uID = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            int index = (int)(chars.length() * Math.random());
            uID.append(chars.charAt(index));
        }

        try {
            client.getHelix().createCustomReward(
                account.getAccessToken(),
                account.getUserId(),
                CustomReward.builder()
                    .title("ChatPointsTTV Reward (" + uID.toString() + ")")
                    .prompt("This reward was created by ChatPointsTTV and its redemptions will be managed automatically.")
                    .isEnabled(false)
                    .cost(1)
                    .build())
                .execute();
            return true;
        } catch (HystrixRuntimeException e) {
            if (e.getCause().getMessage().contains("errorStatus=400")) { // Max rewards reached
                ChatPointsTTV.log.warn("Twitch account " + account.getUserName() + " cannot create new Channel Point Rewards because they have reached the maximum number of rewards.");
            } else if (e.getCause().getMessage().contains("errorStatus=403")) {
                ChatPointsTTV.log.warn("Twitch account " + account.getUserName() + " has no affiliate privileges. Therefore they cannot create Channel Point Rewards.");
            } else {
                ChatPointsTTV.log.error("There was an error while creating a new Channel Point Reward for Twitch account " + account.getUserName() + ".");
                e.printStackTrace();
            }
            return false;
        }
    }

    public void toggleChannelPointRewards(OAuth2Credential account, Boolean state) {
        if (!twitchConfig.getBoolean("MANAGE_CHANNEL_POINT_REWARDS", true)) return;
        List<Action> actions = me.gosdev.chatpointsttv.Events.EventManager.actions.get(TwitchEventType.CHANNEL_POINTS);
        ArrayList<String> configRewardNames = new ArrayList<>();

        if (actions != null) {
            for (Action e : actions) {
                if (e.getTargetChannel().equals(account.getUserName().toLowerCase()) || e.getTargetChannel().equals(me.gosdev.chatpointsttv.Events.EventManager.EVERYONE)) configRewardNames.add(e.getEvent().toLowerCase());
            }
        }

        try {
            for (CustomReward r : client.getHelix().getCustomRewards(account.getAccessToken(), account.getUserId(), null, true).execute().getRewards()) {
                if (configRewardNames.contains(r.getTitle().toLowerCase())) {
                    client.getHelix().updateCustomReward(account.getAccessToken(), account.getUserId(), r.getId(), r.withIsEnabled(state)).execute();
                }
            }
        } catch (HystrixRuntimeException e) {
            if (e.getCause().getMessage().contains("errorStatus=403")) {} // No affiliate privileges. Fail silently
            else {
                ChatPointsTTV.log.error("There was an error while updating Channel Point Rewards for Twitch account " + account.getUserName() + ".");
                e.printStackTrace();
            }
        }

    }

    private OAuth2Credential refreshCredentials(String userId) {
        OAuth2Credential oldCredential = ChatPointsTTV.getAccounts().getTwitchOAuth(userId);
        if (oldCredential == null) {
            return null;
        }

        Optional<OAuth2Credential> refreshed = identityProvider.refreshCredential(oldCredential);
        if (refreshed.isPresent()) {
            OAuth2Credential fullCredential = identityProvider.getAdditionalCredentialInformation(refreshed.get()).get();
            ChatPointsTTV.getAccounts().saveCredential(userId, Optional.of(fullCredential));
            return fullCredential;
        } else {
            throw new RuntimeException("Failed to refresh credentials.");
        }
    }

    public void unlink(GenericSender p, Optional<String> channelField) {
        twitchExecutor.submit(() -> {
            linkInProgress.set(true);
            try {
                if (!started.get()) {
                    p.sendMessage(ChatColor.RED + "You must start the Twitch Module first!");
                    return;
                }
                if (!accountConnected.get()) {
                    p.sendMessage(ChatColor.RED + "There are no accounts linked!");
                    return;
                }
                if (channelField.isPresent()) {
                    try {
                        removeAccount(channelField.get());
                        p.sendMessage(ChatPointsTTV.PREFIX + "Account unlinked!");
                    } catch (NullPointerException e) {
                        p.sendMessage(e.getMessage() + " " + channelField.get());
                    }
                } else {
                    try {
                        ArrayList<Channel> channelsSnapshot = new ArrayList<>(channels.values());

                        for (Channel channel : channelsSnapshot) {
                            removeAccount(channel.getChannelUsername());
                        }
                        p.sendMessage(ChatPointsTTV.PREFIX + "All accounts were unlinked successfully!");
                    } catch (NullPointerException e) {
                        p.sendMessage(e.getMessage() + " " + channelField.get());
                    }
                }
            } catch (Exception e) {
                p.sendMessage(ChatPointsTTV.PREFIX + ChatColor.RED + "Failed to unlink an account.");
                e.printStackTrace();
            }
            linkInProgress.set(false);
        });
    }

    private void removeAccount(String username) {
        Channel channel = channels.get(username);
        if (channel == null) throw new NullPointerException("Cannot find channel");
        OAuth2Credential credential = ChatPointsTTV.getAccounts().getTwitchOAuth(channel.getChannelId());

        toggleChannelPointRewards(credential, false); 

        for (EventSubSubscription sub : channel.getSubs()) {
            eventSocket.unregister(sub);
        }

        channels.remove(username);
        client.getChat().leaveChannel(username);

        identityProvider.revokeCredential(credential);
        tokenRefreshTasks.get(channel.getChannelId()).cancel(false);
        ChatPointsTTV.getAccounts().removeAccount(Platforms.TWITCH, channel.getChannelId());
    }

    public void stop(GenericSender p) {
        stopThread = new Thread(() -> {
            if (!isStarted()) {
                p.sendMessage(ChatColor.RED + "Twitch Module is already stopped.");
                return;
            }

            twitchExecutor.shutdown();
            refreshExecutor.shutdown();

            try {
                if (client != null) {
                    for (Channel channel : channels.values()) {
                        toggleChannelPointRewards(ChatPointsTTV.getAccounts().getTwitchOAuth(channel.getChannelId()), false);
                    }
                    if (eventSocket != null) eventSocket.close();
                    client.close();
                }
            } catch (Exception e) {
                p.sendMessage("There was an error while disabling the Twitch Module.");
                e.printStackTrace();
                return;
            }

            if (tokenRefreshTasks != null) {
                for (ScheduledFuture<?> task : tokenRefreshTasks.values()) {
                    task.cancel(false);
                }
            }

            client = null;
            eventHandler = null;
            eventSocket = null;
            eventManager = null;
            channels.clear();

            try {
                if(!linkInProgress.get() && !twitchExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    twitchExecutor.shutdownNow();
                    ChatPointsTTV.log.warn("Twitch Module is taking too long to stop. Forcing shutdown...");
                }
            } catch (InterruptedException e) {
                twitchExecutor.shutdownNow();
            }

            started.set(false);

            accountConnected.set(false);
            p.sendMessage(ChatPointsTTV.PREFIX + "Twitch Module has been successfully stopped!");

        });
        stopThread.start();
    }
    private void setupTwitch4JLogs() {
        String[] loggers = {
            "io.github.xanthic",
            "com.netflix.config",
            "me.gosdev.chatpointsttv.libraries.twitch4j"
        };

        try {
            // Get Log4J2 with reflection
            Class<?> configuratorClass = Class.forName("org.apache.logging.log4j.core.config.Configurator");
            Class<?> levelClass = Class.forName("org.apache.logging.log4j.Level");
            
            Object level = levelClass.getField("ERROR").get(null);
            java.lang.reflect.Method setLevelMethod = configuratorClass.getMethod("setLevel", String.class, levelClass);

            for (String loggerName : loggers) {
                setLevelMethod.invoke(null, loggerName, level);
            }
        } catch (Exception e) {
            // In case of failure, try with java.util.logging
            for (String loggerName : loggers) {
                java.util.logging.Logger.getLogger(loggerName).setLevel(java.util.logging.Level.SEVERE);
            }
        }
    }
}
