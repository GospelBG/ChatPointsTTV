package me.gosdev.chatpointsttv.Twitch;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
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
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

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
    public Boolean customCredentialsFound = false;
    public Boolean ignoreOfflineStreamers = false;
    public static Boolean accountConnected = false;
    public OAuth2Credential oauth;
    
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

    public String getConnectedUsername() {
        return accountConnected ? user.getLogin() : "Not Linked";
    }

    public HashMap<String, Channel> getListenedChannels() {
        return channels;
    }

    public void enableTwitch() throws ConfigurationException {
        String channel_allowedChars = "^[a-zA-Z0-9_]*$";
        Object cfg_channel = config.get("CHANNEL_USERNAME");
        channels = new HashMap<>();

        if (cfg_channel instanceof String) {
            if (((String) cfg_channel).isEmpty()) {
                throw new ConfigurationException("Channel field is blank.");
            } else if (!((String) cfg_channel).matches(channel_allowedChars)) {
                throw new ConfigurationException("Invalid channel name: " + cfg_channel.toString());
            } else {
                channels.put((String) cfg_channel, null);
            }
        } else if (cfg_channel instanceof List) {
            if (((List<String>) cfg_channel).isEmpty()) {
                throw new ConfigurationException("Channel list is blank.");
            } else {
                for (String channel : (List<String>) cfg_channel) {
                    if (!channel.matches(channel_allowedChars)) {
                        throw new ConfigurationException("Invalid channel name: " + channel);
                    } else if (channel.isEmpty()) {
                        throw new ConfigurationException("A channel field is blank.");
                    } else {
                        channels.put(channel, null);
                    }
                }
            }
        } else {
            throw new ConfigurationException("Cannot read channel. Config file may be not set up or invalid.");
        }

        if (config.getString("CUSTOM_CLIENT_ID") != null || config.getString("CUSTOM_CLIENT_SECRET") != null) customCredentialsFound = true;
            
        chatBlacklist = config.getStringList("CHAT_BLACKLIST");
        ignoreOfflineStreamers = plugin.config.getBoolean("IGNORE_OFFLINE_STREAMERS", false);
    }

    public void link(CommandSender p, OAuth2Credential credential) {
        if (accountConnected) {
            subscribeToEvents(p, credential);
        } else {
            start(p, credential);
        }
        
    }

    public void start(CommandSender p, OAuth2Credential credential) {
        linkThread = new Thread(() -> {
            p.sendMessage(ChatPointsTTV.msgPrefix + "Logging in...");

            // Build TwitchClient
            client = TwitchClientBuilder.builder()
                .withDefaultAuthToken(credential)
                .withEnableChat(true)
                .withEnableHelix(true)
                .withEnablePubSub(true)
                .withEnableEventSocket(true)
                .withDefaultEventHandler(SimpleEventHandler.class)
                .build();
            
            oauth = credential;
            user = client.getHelix().getUsers(credential.getAccessToken(), null, null).execute().getUsers().get(0);

            Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Logged in as: "+ user.getDisplayName());

            for (String username : channels.keySet()) { // Populate "channels" hashmap values
                channels.put(username, new Channel(username, credential.getUserId(), TwitchUtils.isLive(credential.getAccessToken(), username)));
            }

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
                eventManager.onEvent(RewardRedeemedEvent.class, (RewardRedeemedEvent e) -> {
                    eventHandler.onChannelPointsRedemption(e);
                });

                eventManager.onEvent(CustomRewardRedemptionAddEvent.class, (CustomRewardRedemptionAddEvent e) -> {
                    log.info("NEW REWARD REDEMPTION: " + e.getReward().getTitle());
                    log.info("By: " + e.getUserName());
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
            accountConnected = true;
            unlink(Bukkit.getConsoleSender());
        });
    }
    
    public void subscribeToEvents(CommandSender p, OAuth2Credential channel) {
        String channel_id = channel.getUserId();
        Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + "Listening to " + channel.getUserName() + "'s events...");
        
        if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_POINTS_CUSTOM_REWARD_REDEMPTION_ADD.prepareSubscription(b -> b.broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.FOLLOW) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(channel_id).broadcasterUserId(channel_id).build(), null));
        } 

        if (Rewards.getRewards(Rewards.rewardType.CHEER) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.SUB) != null || Rewards.getRewards(Rewards.rewardType.GIFT) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.userId(channel_id).broadcasterUserId(channel_id).build(), null));
        }

        if (Rewards.getRewards(Rewards.rewardType.RAID) != null) {
            eventSocket.register(SubscriptionTypes.CHANNEL_RAID.prepareSubscription(b -> b.toBroadcasterUserId(channel_id).build(), null));
        }
        client.getChat().joinChannel(channel.getUserName());
    }

    public void unlink(CommandSender p) {
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

        client = null;
        eventHandler = null;
        eventSocket = null;
        eventManager = null;
        accountConnected = false;

        p.sendMessage(ChatPointsTTV.msgPrefix + ChatColor.GREEN + "Account disconnected!");
    }
}
