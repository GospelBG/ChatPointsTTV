package me.gosdev.chatpointsttv.Twitch;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.github.philippheuer.credentialmanager.CredentialManager;
import com.github.philippheuer.credentialmanager.CredentialManagerBuilder;
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
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.Scopes;
import me.gosdev.chatpointsttv.Utils.TwitchUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class TwitchClient {
    public Thread linkThread;
    public Boolean ignoreOfflineStreamers = false;
    public static Boolean accountConnected = false;
    public OAuth2Credential oauth;
    public CredentialManager credentialManager;
    
    private User user;
    private List<String> chatBlacklist;
    private static ITwitchClient client;
    private static HashMap<String, Channel> channels;
    private static TwitchEventHandler eventHandler;
    private static IEventSubSocket eventSocket;
    private static EventManager eventManager;
    private final ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    private final Logger log = plugin.log;
    private final FileConfiguration config = plugin.getConfig();
    private FileConfiguration accounts;
    private File accountsFile;
    private TwitchIdentityProvider identityProvider;
    private ScheduledThreadPoolExecutor exec;
    private ArrayList<BukkitTask> tokenRefreshTasks;

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
    
    public Boolean isAccountConnected() {
        return accountConnected;
    }
    public ITwitchClient getClient() {
        return client;
    }

    public HashMap<String, Channel> getListenedChannels() {
        return channels;
    }

    public void enable() throws ConfigurationException {
        channels = new HashMap<>();
        tokenRefreshTasks = new ArrayList<>();
            
        chatBlacklist = config.getStringList("CHAT_BLACKLIST");
        ignoreOfflineStreamers = plugin.config.getBoolean("IGNORE_OFFLINE_STREAMERS", false);

        accountsFile = new File(plugin.getDataFolder(), "twitch.yml");
        accounts = YamlConfiguration.loadConfiguration(accountsFile);
        identityProvider = new TwitchIdentityProvider(getClientID(), null, null);
        credentialManager = CredentialManagerBuilder.builder().build();
        credentialManager.registerIdentityProvider(identityProvider);
        exec = ThreadUtils.getDefaultScheduledThreadPoolExecutor("twitch4j", Runtime.getRuntime().availableProcessors());
        
        for (String userid : accounts.getKeys(false)) {
            ConfigurationSection account = accounts.getConfigurationSection(userid);
            OAuth2Credential credential = new OAuth2Credential(TwitchIdentityProvider.PROVIDER_NAME, account.getString("access_token"), account.getString("refresh_token"), userid, null, null, null);
            if (!identityProvider.isCredentialValid(credential).orElse(false)) {
                // Try to refresh token
                credential = identityProvider.refreshCredential(credential).orElse(null);
                if (credential == null) { // Cannot refresh token (refresh expired or invalid)
                    plugin.log.warning("Credentials for User ID: " + userid + " are expired. You will need to link your account again.");
                    accounts.set(userid, null); // Remove invalid credentials from file
                    continue;
                }
            }

            credential = identityProvider.getAdditionalCredentialInformation(credential).get();
            link(Bukkit.getConsoleSender(), credential);
        }
    }

    public void link(CommandSender p, OAuth2Credential credential) {
        if (linkThread != null) {
            try {
                linkThread.join();
            } catch (InterruptedException e) {}
        }
        credential.updateCredential(identityProvider.getAdditionalCredentialInformation(credential).get());
        for (Channel channel : channels.values()) {
            if (credential.getUserId().equals(channel.getChannelId())) {
                saveCredential(credential); // Due to the credential refreshing, we need to save it again
                p.sendMessage(ChatPointsTTV.msgPrefix + "You cannot link an account twice!");
                return;
            }
        }

        if (accountConnected) {
            subscribeToEvents(p, credential);
        } else {
            start(p, credential);
        }
        
        credentialManager.addCredential(TwitchIdentityProvider.PROVIDER_NAME, credential);
        saveCredential(credential);
        
        tokenRefreshTasks.add(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Thread() {
            @Override
            public void run() {
                identityProvider.refreshCredential(credential).ifPresent(newCred -> {
                    credentialManager.getOAuth2CredentialByUserId(credential.getUserId()).ifPresent(cred -> {
                        cred.updateCredential(newCred);
                        saveCredential(newCred);
                    });
                });
            }
        }, credential.getExpiresIn() / 2, Double.valueOf(credential.getExpiresIn() / 1.25 * 20).longValue()));
    }

    private void start(CommandSender p, OAuth2Credential credential) {
        linkThread = new Thread(() -> {
            p.sendMessage(ChatPointsTTV.msgPrefix + "Logging in...");

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
            if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
                subs++;
                eventManager.onEvent(CustomRewardRedemptionAddEvent.class, (CustomRewardRedemptionAddEvent e) -> {
                    eventHandler.onChannelPointsRedemption(e);
                });
            }
            if (Rewards.getRewards(Rewards.rewardType.FOLLOW) != null) {
                subs++;
                eventManager.onEvent(ChannelFollowEvent.class, (ChannelFollowEvent e) -> {
                    eventHandler.onFollow(e);
                });
            }
            if (Rewards.getRewards(Rewards.rewardType.CHEER) != null) {
                subs++;
                eventManager.onEvent(ChannelChatMessageEvent.class, (ChannelChatMessageEvent e) -> {
                    eventHandler.onCheer(e);
                }); 
            }
            if (Rewards.getRewards(Rewards.rewardType.SUB) != null || Rewards.getRewards(Rewards.rewardType.GIFT) != null) {
                subs++;
                eventManager.onEvent(ChannelChatNotificationEvent.class, (ChannelChatNotificationEvent e) -> {
                        if (e.getNoticeType() == NoticeType.SUB || e.getNoticeType() == NoticeType.RESUB) eventHandler.onSub(e);
                        else if (e.getNoticeType() == NoticeType.COMMUNITY_SUB_GIFT) eventHandler.onSubGift(e);
                });
            }
            if (Rewards.getRewards(Rewards.rewardType.RAID) != null) {
                subs++;
                eventManager.onEvent(ChannelRaidEvent.class, (ChannelRaidEvent e) -> {
                        eventHandler.onRaid(e);
                }); 
            }
            if (config.getBoolean("SHOW_CHAT")) {
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

            CountDownLatch latch = new CountDownLatch(getListenedChannels().size() * subs);

            eventManager.onEvent(EventSocketSubscriptionSuccessEvent.class, e -> latch.countDown());
            eventManager.onEvent(EventSocketSubscriptionFailureEvent.class, e -> latch.countDown());

            // Join the twitch chat of this channel(s) and enable stream/follow events
            subscribeToEvents(p, credential);

            try {
                client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventHandler);
                latch.await();
            } catch (InterruptedException e) {
                log.warning("Failed to bind events.");
                return;
            }

            p.sendMessage(ChatPointsTTV.msgPrefix + "Twitch client has started successfully!");
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
    
    private void subscribeToEvents(CommandSender p, OAuth2Credential channel) {
        String channel_id = channel.getUserId();
        Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Listening to " + channel.getUserName() + "'s events...");

        channels.put(channel.getUserName(), new Channel(channel.getUserName(), channel.getUserId(), TwitchUtils.isLive(channel.getAccessToken(), channel.getUserName())));
        
        if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
            eventSocket.register(channel, SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD.prepareSubscription(b -> b.broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.FOLLOW) != null) {
            eventSocket.register(channel, SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(channel_id).broadcasterUserId(channel_id).build(), null));
        } 

        if (Rewards.getRewards(Rewards.rewardType.CHEER) != null) {
            eventSocket.register(channel, SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.SUB) != null || Rewards.getRewards(Rewards.rewardType.GIFT) != null) {
            eventSocket.register(channel, SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.RAID) != null) {
            eventSocket.register(channel, SubscriptionTypes.CHANNEL_RAID.prepareSubscription(b -> b.toBroadcasterUserId(channel_id).build(), null));
        }
        client.getChat().joinChannel(channel.getUserName());
    }

    public void saveCredential(OAuth2Credential credential) {
        ConfigurationSection account = accounts.createSection(credential.getUserId());
        account.set("access_token", credential.getAccessToken());
        account.set("refresh_token", credential.getRefreshToken());

        try {
            accounts.save(accountsFile);
        } catch (IOException e) {
            plugin.log.severe("There was an issue saving account session credentials.");
        }
    }

    public void unlinkAccount(String userId) {
        for (EventSubSubscription sub : eventSocket.getSubscriptions()) {
            plugin.log.info(sub.toString());
        }

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
            accountConnected = false;
        } catch (Exception e) {
            log.warning("Error while disabling ChatPointsTTV.");
            e.printStackTrace();
            return;
        }
        
        for (BukkitTask task : tokenRefreshTasks) {
            task.cancel();
        }

        client = null;
        eventHandler = null;
        eventSocket = null;
        eventManager = null;
        accountConnected = false;

        p.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.GREEN + "Account disconnected!");
    }
}
