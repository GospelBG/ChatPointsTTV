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
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
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
import com.github.twitch4j.helix.domain.User;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.alert_mode;
import me.gosdev.chatpointsttv.Events;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.Scopes;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class TwitchClient {
    public Thread linkThread;
    public Boolean ignoreOfflineStreamers = false;
    public static Boolean accountConnected = false;
    public OAuth2Credential oauth;
    public HashMap<String, OAuth2Credential> credentialManager;

    private boolean started;
    private User user;
    private List<String> chatBlacklist;
    private static ITwitchClient client;
    private static HashMap<String, Channel> channels;
    private static TwitchEventHandler eventHandler;
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

    public Boolean overrideShouldMobsGlow;
    public Boolean overrideNameSpawnedMobs;
    public alert_mode overrideAlertMode;
    public ChatColor override_msgActionColor;
    public ChatColor override_msgUserColor;
    public Boolean override_msgRewardBold;
    
    private final static String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    public final static List<Object> scopes = new ArrayList<>(Arrays.asList(
        Scopes.CHANNEL_READ_REDEMPTIONS,
        Scopes.CHANNEL_READ_SUBSCRIPTIONS,
        Scopes.USER_READ_MODERATED_CHANNELS,
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
            plugin.saveResource("twitch.yml", false);
        }
        twitchConfig = YamlConfiguration.loadConfiguration(twitchConfigFile);

        accountsFile = new File(plugin.getDataFolder(), "accounts.yml");
        accountsConfig = YamlConfiguration.loadConfiguration(accountsFile);
        accounts = accountsConfig.getConfigurationSection("twitch");
        identityProvider = new TwitchIdentityProvider(getClientID(), null, null);
        credentialManager = new HashMap<>();
        exec = ThreadUtils.getDefaultScheduledThreadPoolExecutor("twitch4j", Runtime.getRuntime().availableProcessors());

        chatBlacklist = twitchConfig.getStringList("CHAT_BLACKLIST");
        ignoreOfflineStreamers = plugin.getConfig().getBoolean("IGNORE_OFFLINE_STREAMERS", false);

        // Configuration overrides
        overrideShouldMobsGlow = (Boolean) twitchConfig.get("MOB_GLOW", null);
        overrideNameSpawnedMobs = (Boolean)twitchConfig.get("DISPLAY_NAME_ON_MOB", null);
        Events.setAlertMode(alert_mode.valueOf(twitchConfig.getString("INGAME_ALERTS", ChatPointsTTV.alertMode.toString()).toUpperCase()));
        override_msgRewardBold = (Boolean) twitchConfig.get("REWARD_NAME_BOLD", null);
        try {
            override_msgActionColor = ChatColor.valueOf(twitchConfig.getString("COLORS.ACTION_COLOR", null).toUpperCase());
        } catch (NullPointerException e) {
            override_msgActionColor = null;
        }
        try {
            override_msgUserColor = ChatColor.valueOf(twitchConfig.getString("COLORS.USER_COLOR", null).toUpperCase());
        } catch (NullPointerException e) {
            override_msgUserColor = null;
        }
        
        if (accounts != null) {
            for (String userid : accounts.getKeys(false)) {
                ConfigurationSection account = accounts.getConfigurationSection(userid);
                OAuth2Credential credential = new OAuth2Credential(TwitchIdentityProvider.PROVIDER_NAME, account.getString("access_token"), account.getString("refresh_token"), userid, null, null, null);
                // Try to refresh token
                try {
                    credential = refreshCredentials(credential);
                } catch (RuntimeException e) {
                    ChatPointsTTV.log.warning("Credentials for User ID: " + userid + " have expired. You will need to link your account again.");
                    saveCredential(userid, null);
                    continue;
                }
                link(Bukkit.getConsoleSender(), credential);
            }    
        }

        started = true;
    }

    public void link(CommandSender p, OAuth2Credential credential) {
        saveCredential(credential.getUserId(), credential);
        credentialManager.put(credential.getUserId(), credential);

        if (linkThread != null) {
            try {
                linkThread.join();
            } catch (InterruptedException e) {}
        }
        
        for (Channel channel : channels.values()) {
            if (credential.getUserId().equals(channel.getChannelId())) {
                p.sendMessage(ChatPointsTTV.msgPrefix + "You cannot link an account twice!");
                return;
            }
        }

        tokenRefreshTasks.put(credential.getUserId(), Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Thread() {
            @Override
            public void run() {
                refreshCredentials(credential);
            }
        }, 1200, credential.getExpiresIn() / 2 * 20));

        if (accountConnected) {
            subscribeToEvents(credential);
        } else {
            start(p, credential);
        }

        p.sendMessage(ChatPointsTTV.msgPrefix + "Logged in successfully!");
    }

    private void start(CommandSender p, OAuth2Credential credential) {
        linkThread = new Thread(() -> {
            // Build TwitchClient
            client = TwitchClientBuilder.builder()
                .withDefaultAuthToken(credential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnableEventSocket(true)
                .withDefaultEventHandler(SimpleEventHandler.class)
                .withScheduledThreadPoolExecutor(exec)
                .build();
            
            oauth = credential;
            user = client.getHelix().getUsers(credential.getAccessToken(), null, null).execute().getUsers().get(0);

            Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Logged in as: "+ user.getDisplayName());
            
            eventHandler = new TwitchEventHandler();

            eventSocket = client.getEventSocket();
            eventManager = client.getEventManager();

            int subs = 0;

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
            if (Rewards.getRewards(twitchConfig, Rewards.rewardType.CHANNEL_POINTS) != null) {
                subs++;
                eventManager.onEvent(CustomRewardRedemptionAddEvent.class, (CustomRewardRedemptionAddEvent e) -> {
                    eventHandler.onChannelPointsRedemption(e);
                });
            }
            if (Rewards.getRewards(twitchConfig, Rewards.rewardType.FOLLOW) != null) {
                subs++;
                eventManager.onEvent(ChannelFollowEvent.class, (ChannelFollowEvent e) -> {
                    eventHandler.onFollow(e);
                });
            }
            if (Rewards.getRewards(twitchConfig, Rewards.rewardType.CHEER) != null) {
                subs++;
                eventManager.onEvent(ChannelChatMessageEvent.class, (ChannelChatMessageEvent e) -> {
                    eventHandler.onCheer(e);
                }); 
            }
            if (Rewards.getRewards(twitchConfig, Rewards.rewardType.SUB) != null || Rewards.getRewards(twitchConfig, Rewards.rewardType.GIFT) != null) {
                subs++;
                eventManager.onEvent(ChannelChatNotificationEvent.class, (ChannelChatNotificationEvent e) -> {
                        if (e.getNoticeType() == NoticeType.SUB || e.getNoticeType() == NoticeType.RESUB) eventHandler.onSub(e);
                        else if (e.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT) eventHandler.onSubGift(e);
                });
            }
            if (Rewards.getRewards(twitchConfig, Rewards.rewardType.RAID) != null) {
                subs++;
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

            CountDownLatch latch = new CountDownLatch(getListenedChannels().size() * subs); //TODO: this is outdated

            eventManager.onEvent(EventSocketSubscriptionSuccessEvent.class, e -> latch.countDown());
            eventManager.onEvent(EventSocketSubscriptionFailureEvent.class, e -> latch.countDown());

            // Join the twitch chat of this channel(s) and enable stream/follow events
            subscribeToEvents(credential);

            try {
                client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventHandler);
                latch.await();
            } catch (InterruptedException e) {
                ChatPointsTTV.log.warning("Failed to bind events.");
                return;
            }
            accountConnected = true;
        });

        linkThread.start();
        linkThread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
            linkThread.interrupt();
            e.printStackTrace();
            p.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + "Account linking failed!");
            stop(Bukkit.getConsoleSender());
        });

        try {
            linkThread.join();
        } catch (InterruptedException e) {}
    }
    
    private void subscribeToEvents(OAuth2Credential credential) {
        String channel_id = credential.getUserId();
        Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Listening to " + credential.getUserName() + "'s events...");

        ArrayList<EventSubSubscription> subs = new ArrayList<>();

        if (Rewards.getRewards(twitchConfig, Rewards.rewardType.CHANNEL_POINTS) != null) {
            subs.add(SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD.prepareSubscription(b -> b.broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(twitchConfig, Rewards.rewardType.FOLLOW) != null) {
            subs.add(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(channel_id).broadcasterUserId(channel_id).build(), null));
        } 

        if (Rewards.getRewards(twitchConfig, Rewards.rewardType.CHEER) != null) {
            subs.add(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(twitchConfig, Rewards.rewardType.SUB) != null || Rewards.getRewards(twitchConfig, Rewards.rewardType.GIFT) != null) {
            subs.add(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(twitchConfig, Rewards.rewardType.RAID) != null) {
            subs.add(SubscriptionTypes.CHANNEL_RAID.prepareSubscription(b -> b.toBroadcasterUserId(channel_id).build(), null));
        }

        for (EventSubSubscription eventSubSubscription : subs) {
            eventSocket.register(credential, eventSubSubscription);
        }

        Channel channel = new Channel(credential.getUserName(), credential.getUserId(), TwitchUtils.isLive(credential.getAccessToken(), credential.getUserName()));
        channel.setSubscriptions(subs);
        channels.put(credential.getUserName(), channel);
        
        client.getChat().joinChannel(credential.getUserName());
    }

    public OAuth2Credential refreshCredentials(OAuth2Credential oldCredential) {
        Optional<OAuth2Credential> refreshed = identityProvider.refreshCredential(oldCredential);

        if (refreshed.isPresent()) {
            saveCredential(oldCredential.getUserId(), refreshed.get());
            return identityProvider.getAdditionalCredentialInformation(refreshed.get()).get();
        }
        throw new RuntimeException("Failed to refresh credentials.");
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
            ChatPointsTTV.log.severe("There was an issue saving account session credentials.");
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

        if (credentialManager.size() == 0) {
            accountConnected = false;
        }

        ChatPointsTTV.log.info("Done!");
    }

    public void stop(CommandSender p) {
        if (!accountConnected) {
            p.sendMessage(ChatPointsTTV.msgPrefix + new TextComponent(ChatColor.RED + "There is no connected account."));
            return;
        }
        try {
            if (!linkThread.isInterrupted()) linkThread.join(); // Wait until linking is finished
            client.getEventSocket().close();
            client.close();
            credentialManager.clear();
            accountConnected = false;
        } catch (Exception e) {
            ChatPointsTTV.log.warning("Error while disabling ChatPointsTTV.");
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
        accountConnected = false;

        started = false;
    }
}
