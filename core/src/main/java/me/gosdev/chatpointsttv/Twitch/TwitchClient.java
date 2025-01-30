package me.gosdev.chatpointsttv.Twitch;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;

import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.Scopes;
import me.gosdev.chatpointsttv.Utils.TwitchUtils;
import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class TwitchClient {
    public Thread linkThread;
    public Boolean customCredentialsFound = false;
    public static Boolean usingCustomOauth = false;
    public static Boolean accountConnected = false;
    public static OAuth2Credential oauth;

    private User user;
    private String user_id;
    private List<String> chatBlacklist;

    private static ITwitchClient client;
    private static ArrayList<Channel> channels;
    private static TwitchEventHandler eventHandler;
    private static IEventSubSocket eventSocket;
    private static EventManager eventManager;
    
    private final Utils utils = ChatPointsTTV.getUtils();
    private final ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    private final Logger log = plugin.log;
    private final FileConfiguration config = plugin.getConfig();

    private final static String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";
    public final static String scopes = Scopes.join(
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

    public static String getClientID() {
        return usingCustomOauth ? ChatPointsTTV.getPlugin().getConfig().getString("CUSTOM_CLIENT_ID") : ClientID;
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

    public List<Channel> getListenedChannels() {
        if (channels == null && channels.isEmpty()) {
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
        
                    channels.add(new Channel(name, id, !request.getStreams().isEmpty()));
                }
            } else {
                for (String name : usernames) {
                    channels.add(new Channel(name, null, false)); // If unable to check live status
                }
            }
        }
        return channels;
    }

    public void enableTwitch() throws ConfigurationException {
        if (config.getString("CHANNEL_USERNAME") == null | config.getString("CHANNEL_USERNAME").startsWith("MemorySection[path=")) { // Invalid string (probably left default "{YOUR CHANNEL}")
            throw new ConfigurationException("Cannot read channel. Config file may be not set up or invalid.");
        }

        if (config.getString("CUSTOM_CLIENT_ID") != null || config.getString("CUSTOM_CLIENT_SECRET") != null) customCredentialsFound = true;
            
        chatBlacklist = config.getStringList("CHAT_BLACKLIST");

        if(customCredentialsFound && config.getBoolean("AUTO_LINK_CUSTOM", false) == true) {
            plugin.metrics.addCustomChart(new SimplePie("authentication_method", () -> {
                return "Twitch Auto-Link (Key)";
            }));

            linkToTwitch(Bukkit.getConsoleSender(), config.getString("CUSTOM_CLIENT_ID") , config.getString("CUSTOM_ACCESS_TOKEN"));
        }
    }

    public void linkToTwitch(CommandSender p, String clientID, String token) {
        linkThread = new Thread(() -> {
            if(clientID == null || clientID.isEmpty()) {
                throw new NullPointerException("Invalid Client ID");
            }
            if (token == null || token.isEmpty()) {
                throw new NullPointerException("Invalid Access Token");
            }

            utils.sendMessage(p, "Logging in...");

            oauth = new OAuth2Credential(clientID, token);

            // Build TwitchClient
            client = TwitchClientBuilder.builder()
                .withDefaultAuthToken(oauth)
                .withEnableChat(true)
                .withEnableHelix(true)
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

            int subs = 0;

            eventManager.onEvent(ChannelGoLiveEvent.class, (ChannelGoLiveEvent e) -> {
                for (Channel channel : getListenedChannels()) {
                    if (channel.getChannelUsername().equalsIgnoreCase(e.getChannel().getName())) channel.updateStatus(true);
                }
            });

            eventManager.onEvent(ChannelGoOfflineEvent.class, (ChannelGoOfflineEvent e) -> {
                for (Channel channel : getListenedChannels()) {
                    if (channel.getChannelUsername().equalsIgnoreCase(e.getChannel().getName())) channel.updateStatus(false);
                }
            });            
            if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
                eventManager.onEvent(RewardRedeemedEvent.class, (RewardRedeemedEvent e) -> {
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
                            if (player.hasPermission(ChatPointsTTV.permissions.BROADCAST.permission_id)) {
                                utils.sendMessage(player, components);
                            }
                        }
                    }
                });
            }

            CountDownLatch latch = new CountDownLatch(getListenedChannels().size() * subs);

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
        linkThread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
            linkThread.interrupt();
            e.printStackTrace();
            utils.sendMessage(p, ChatColor.RED + "Account linking failed!");
            accountConnected = true;
            unlink(Bukkit.getConsoleSender());
        });
    }
    
    public void subscribeToEvents(CommandSender p, CountDownLatch latch, String channel) {
        String channel_id = TwitchUtils.getUserId(channel);
        utils.sendMessage(Bukkit.getConsoleSender(), "Listening to " + channel + "'s events...");
        
        if (Rewards.getRewards(Rewards.rewardType.CHANNEL_POINTS) != null) {
            client.getPubSub().listenForChannelPointsRedemptionEvents(null, channel_id);
        }

        if (Rewards.getRewards(Rewards.rewardType.FOLLOW) != null) {
            if (TwitchUtils.getModeratedChannelIDs(oauth.getAccessToken(), user_id).contains(channel_id) || user_id.equals(channel_id)) { // If account is the streamer or a mod (need to have mod permissions on the channel)
                eventSocket.register(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(user_id).broadcasterUserId(channel_id).build(), null));
            } else {
                log.log(Level.WARNING, "{0}: Follow events cannot be listened to on unauthorised channels.", channel);
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
        channels = null;
        oauth = null;

        utils.sendMessage(p, ChatColor.GREEN + "Account disconnected!");
    }
}
