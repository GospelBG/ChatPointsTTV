package me.gosdev.chatpointsttv.Twitch;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.CPTTV_EventHandler;
import me.gosdev.chatpointsttv.Events.Event;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.FollowerLog;
import me.gosdev.chatpointsttv.Utils.Scopes;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TwitchClient {
    public Boolean ignoreOfflineStreamers = false;
    public ConcurrentHashMap<String, OAuth2Credential> credentialManager;

    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean accountConnected = new AtomicBoolean(false);
    private AtomicBoolean linkInProgress = new AtomicBoolean(false);
    public AtomicBoolean reloading = new AtomicBoolean(true);
    private List<String> chatBlacklist;
    private ConcurrentHashMap<String, Channel> channels;
    private TwitchEvents eventHandler;
    private IEventSubSocket eventSocket;
    private EventManager eventManager;
    private ITwitchClient client;
    private FileConfiguration twitchConfig;
    private File accountsFile;
    private FileConfiguration accountsConfig;
    private ConfigurationSection accounts;
    private TwitchIdentityProvider identityProvider;
    private ScheduledThreadPoolExecutor exec;
    private HashMap<String, BukkitTask> tokenRefreshTasks;
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
    public FileConfiguration getConfig() {
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

    public TwitchClient(CommandSender p) {
        started.set(false);
        twitchExecutor = Executors.newSingleThreadExecutor();

        twitchExecutor.submit(() -> {
            CPTTV_EventHandler.clearActions(Platforms.TWITCH); // Make sure actions will be parsed again
            channels = new ConcurrentHashMap<>();
            tokenRefreshTasks = new HashMap<>();

            File twitchConfigFile = new File(ChatPointsTTV.getPlugin().getDataFolder(), "twitch.yml");
            if (!twitchConfigFile.exists()) {
                ChatPointsTTV.getPlugin().saveResource(twitchConfigFile.getName(), false);
            }
            twitchConfig = YamlConfiguration.loadConfiguration(twitchConfigFile);

            accountsFile = new File(ChatPointsTTV.getPlugin().getDataFolder(), "accounts");
            accountsConfig = YamlConfiguration.loadConfiguration(accountsFile);
            if (!accountsConfig.contains("twitch")) {
                accountsConfig.createSection("twitch");
            }
            accounts = accountsConfig.getConfigurationSection("twitch");

            identityProvider = new TwitchIdentityProvider(CLIENT_ID, null, null);
            credentialManager = new ConcurrentHashMap<>();
            exec = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
            exec.setRemoveOnCancelPolicy(true);

            chatBlacklist = twitchConfig.getStringList("CHAT_BLACKLIST");
            ignoreOfflineStreamers = ChatPointsTTV.getPlugin().getConfig().getBoolean("IGNORE_OFFLINE_STREAMERS", false);

            // Configuration overrides
            shouldMobsGlow = twitchConfig.getBoolean("MOB_GLOW", ChatPointsTTV.shouldMobsGlow);
            nameSpawnedMobs = twitchConfig.getBoolean("DISPLAY_NAME_ON_MOB", ChatPointsTTV.nameSpawnedMobs);
            alertMode = AlertMode.valueOf(twitchConfig.getString("INGAME_ALERTS", ChatPointsTTV.alertMode.toString()).toUpperCase());

            
            if (twitchConfig.getBoolean("FOLLOW_SPAM_PROTECTION", true)) {
                FollowerLog.start();
            }

            if (accounts != null) {
                for (String userid : accounts.getKeys(false)) {
                    // Try to refresh token
                    try {
                        link(p, refreshCredentials(userid));
                    } catch (RuntimeException e) {
                        ChatPointsTTV.log.warning("Credentials for User ID: " + userid + " have expired. You will need to link your account again.");
                        ChatPointsTTV.getAccountsManager().removeAccount(Platforms.TWITCH, userid);
                    }      
                }
            }
            
            p.sendMessage(ChatPointsTTV.msgPrefix + "Twitch client has started successfully!");   
            started.set(true);
            reloading.set(false);
        });
    }

    public void link(CommandSender p, OAuth2Credential credential) {
        linkInProgress.set(true);
        try {
            saveCredential(credential.getUserId(), credential);
            credentialManager.put(credential.getUserId(), credential);

            for (Channel channel : channels.values()) {
                if (credential.getUserId().equals(channel.getChannelId())) {
                    p.sendMessage(ChatPointsTTV.msgPrefix + "You cannot link an account twice!");
                    return;
                }
            }

            p.sendMessage(ChatPointsTTV.msgPrefix + "Logging in as: " + credential.getUserName());
    
            tokenRefreshTasks.put(credential.getUserId(), Bukkit.getScheduler().runTaskTimerAsynchronously(ChatPointsTTV.getPlugin(), new Thread() {
                @Override
                public void run() {
                    refreshCredentials(credential.getUserId());
                }
            }, credential.getExpiresIn() / 2 * 20, credential.getExpiresIn() / 2 * 20));
    
            if (accountConnected.get()) {
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
                    List<InboundFollow> follows = request.getFollows();

                    if (follows == null || follows.isEmpty()) break;
                    for (InboundFollow follower : follows) {
                        followerIDs.add(follower.getUserId());
                    }
                    if (cursor == null) break;
                }
                FollowerLog.populateList(Platforms.TWITCH, credential.getUserId(), followerIDs);
            }
        } catch (Exception e) {
            p.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + "Twitch account linking failed.");
            e.printStackTrace();
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
        if (ChatPointsTTV.getPlugin().getConfig().getBoolean("SHOW_CHAT")) {
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
        
        accountConnected.set(true);
    }
    
    private void subscribeToEvents(OAuth2Credential credential) {
        String channel_id = credential.getUserId();
        Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Listening to " + credential.getUserName() + "'s events...");

        ArrayList<EventSubSubscription> subs = new ArrayList<>();

        if (CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.CHANNEL_POINTS) != null) {
            toggleChannelPointRewards(credential, true);
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
                ChatPointsTTV.log.warning("Twitch account " + account.getUserName() + " cannot create new Channel Point Rewards because they have reached the maximum number of rewards.");
            } else if (e.getCause().getMessage().contains("errorStatus=403")) {
                ChatPointsTTV.log.warning("Twitch account " + account.getUserName() + " has no affiliate privileges. Therefore they cannot create Channel Point Rewards.");
            } else {
                ChatPointsTTV.log.severe("There was an error while creating a new Channel Point Reward for Twitch account " + account.getUserName() + ".");
                e.printStackTrace();
            }
            return false;
        }
    }

    public void toggleChannelPointRewards(OAuth2Credential account, Boolean state) {
        if (!twitchConfig.getBoolean("MANAGE_CHANNEL_POINT_REWARDS", true)) return;
        ArrayList<String> configRewardNames = new ArrayList<>();

        for (Event e : CPTTV_EventHandler.getActions(twitchConfig, TwitchEventType.CHANNEL_POINTS)) {
            if (e.getTargetChannel().equals(account.getUserName().toLowerCase()) || e.getTargetChannel().equals(CPTTV_EventHandler.EVERYONE)) configRewardNames.add(e.getEvent().toLowerCase());
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
                ChatPointsTTV.log.severe("There was an error while updating Channel Point Rewards for Twitch account " + account.getUserName() + ".");
                e.printStackTrace();
            }
        }

    }

    private OAuth2Credential refreshCredentials(String userId) {
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

            return fullCredential;
        } else {
            throw new RuntimeException("Failed to refresh credentials.");
        }
    }

    private void saveCredential(String userId, OAuth2Credential credential) {
        HashMap<String, String> account = new HashMap<>();
        account.put("access_token", credential.getAccessToken());
        account.put("refresh_token", credential.getRefreshToken());
        
        ChatPointsTTV.getAccountsManager().saveAccount(Platforms.TWITCH, userId, Optional.of(account));
    }

    public void unlink(CommandSender p, Optional<String> channelField) {
        twitchExecutor.submit(() -> {
            linkInProgress.set(true);
            try {
                if (!started.get()) {
                    p.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
                    return;
                }
                if (!accountConnected.get()) {
                    p.sendMessage(ChatColor.RED + "There are no accounts linked!");
                    return;
                }
                if (channelField.isPresent()) {
                    try {
                        removeAccount(channelField.get());
                        p.sendMessage(ChatPointsTTV.msgPrefix + "Account unlinked!");
                    } catch (NullPointerException e) {
                        p.sendMessage(e.getMessage() + " " + channelField.get());
                    }
                } else {
                    try {
                        ArrayList<Channel> channelsSnapshot = new ArrayList<>(channels.values());

                        for (Channel channel : channelsSnapshot) {
                            removeAccount(channel.getChannelUsername());
                        }
                        p.sendMessage(ChatPointsTTV.msgPrefix + "All accounts were unlinked successfully!");
                    } catch (NullPointerException e) {
                        p.sendMessage(e.getMessage() + " " + channelField.get());
                    }
                }
            } catch (Exception e) {
                p.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + "Failed to unlink an account.");
                e.printStackTrace();
            }
            linkInProgress.set(false);
        });
    }

    private void removeAccount(String username) {
        Channel channel = channels.get(username);
        if (channel == null) throw new NullPointerException("Cannot find channel");
        toggleChannelPointRewards(credentialManager.get(channel.getChannelId()), false);

        for (EventSubSubscription sub : channel.getSubs()) {
            eventSocket.unregister(sub);
        }

        channels.remove(username);
        client.getChat().leaveChannel(username);

        ChatPointsTTV.getAccountsManager().removeAccount(Platforms.TWITCH, channel.getChannelId());
        identityProvider.revokeCredential(credentialManager.get(channel.getChannelId()));
        tokenRefreshTasks.get(channel.getChannelId()).cancel();
        credentialManager.remove(channel.getChannelId());

        if (credentialManager.isEmpty()) {
            accountConnected.set(false);
        }
    }

    public void stop(CommandSender p) {
        if (twitchExecutor.isShutdown()) return;

        twitchExecutor.submit(() -> {
            if (!isStarted()) {
                p.sendMessage(ChatColor.RED + "Twitch Module is already stopped.");
                return;
            }

            try {
                if (client != null) {
                    for (Channel channel : channels.values()) {
                        toggleChannelPointRewards(credentialManager.get(channel.getChannelId()), false);
                    }
                    if (eventSocket != null) eventSocket.close();
                    client.close();
                }
            } catch (Exception e) {
                ChatPointsTTV.log.warning("There was an error while disabling the Twitch client.");
                e.printStackTrace();
                return;
            }

            if (tokenRefreshTasks != null) {
                for (BukkitTask task : tokenRefreshTasks.values()) {
                    task.cancel();
                }
            }

            client = null;
            eventHandler = null;
            eventSocket = null;
            eventManager = null;
            channels.clear();
            credentialManager.clear();

            accountConnected.set(false);
        });

        twitchExecutor.shutdown();
        try {
            if(!linkInProgress.get() && !twitchExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                twitchExecutor.shutdownNow();
                ChatPointsTTV.log.warning("Twitch Module is taking too long to stop. Forcing shutdown...");
            }
        } catch (InterruptedException e) {
            twitchExecutor.shutdownNow();
        }

        started.set(false);
        p.sendMessage(ChatPointsTTV.msgPrefix + "Twitch client has been successfully stopped!");
    }
}
