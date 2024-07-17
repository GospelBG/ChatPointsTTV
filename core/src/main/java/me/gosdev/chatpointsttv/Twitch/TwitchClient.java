package me.gosdev.chatpointsttv.Twitch;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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
import com.github.twitch4j.eventsub.events.ChannelChatMessageEvent;
import com.github.twitch4j.eventsub.events.ChannelChatNotificationEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.socket.IEventSubSocket;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import com.github.twitch4j.helix.domain.ModeratedChannel;
import com.github.twitch4j.helix.domain.ModeratedChannelList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.netflix.hystrix.exception.HystrixRuntimeException;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Utils.ColorUtils;
import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TwitchClient {

    private final static String ClientID = "1peexftcqommf5tf5pt74g7b3gyki3";

    private static TwitchEventHandler eventHandler;
    private static IEventSubSocket eventSocket;
    private static EventManager eventManager;

    private String user_id;
    private String channel_id;

    private Boolean accountConnected = false;

    private static ITwitchClient client;

    private User user;
    private OAuth2Credential oauth;

    private Thread linkThread;

    private static ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    private static Utils utils = ChatPointsTTV.getUtils();

    public final String scopes = Scopes.join(
        Scopes.CHANNEL_READ_REDEMPTIONS,
        Scopes.USER_READ_MODERATED_CHANNELS,
        Scopes.MODERATOR_READ_FOLLOWERS,
        Scopes.BITS_READ,
        Scopes.CHANNEL_READ_SUBSCRIPTIONS,
        Scopes.USER_READ_CHAT,
        Scopes.CHAT_READ
        ).replace(":", "%3A"); // Format colon character for browser


    public String getUserId(String username) {
        UserList resultList = client.getHelix().getUsers(null, null, Arrays.asList(username)).execute();
        return resultList.getUsers().get(0).getId();
    }

    public static String getClientID() {
        if (ChatPointsTTV.twitchCustomCredentials) {
            return plugin.config.getString("TWITCH_CLIENT_ID");
        } else {
            return ClientID;
        }
    }
    public static ITwitchClient getClient() {
        return client;
    }
    public Boolean isAccountConnected() {
        return accountConnected;
    }

    public String getConnectedUsername() {
        return accountConnected ? user.getLogin() : "Not Linked";
    }
    public String getListenedChannel() {
        //if (config.getString("TWITCH_CHANNEL_USERNAME") == null | config.getString("TWITCH_CHANNEL_USERNAME").startsWith("MemorySection[path=")) return null; // Invalid string (probably left default "{YOUR CHANNEL}")) return null;
        //return config.getString("TWITCH_CHANNEL_USERNAME");

        return client.getChat().getChannels().iterator().next(); // UNTESTED 
    }

    public static List<String> getModeratedChannelIDs(String auth, String userId) throws HystrixRuntimeException {
        String cursor = null;
        List<String> modsOutput = new ArrayList<>();

        do {
            ModeratedChannelList moderatorList = client.getHelix().getModeratedChannels(
                    auth,
                    userId,
                    100,
                    cursor
            ).execute();
            cursor = moderatorList.getPagination().getCursor();
            for (ModeratedChannel channel : moderatorList.getChannels()) {
                modsOutput.add(channel.getBroadcasterId());
            }
        } while (cursor != null);
        return modsOutput;
    }

    public void link(CommandSender p, String token) {
        FileConfiguration config = plugin.getConfig();
        linkThread = new Thread(() -> {
            ChatPointsTTV.getUtils().sendMessage(p, "Logging in...");

            if(getClientID() == null || getClientID().isEmpty()) {
                throw new NullPointerException("Invalid Client ID");
            }
            if (token == null || token.isEmpty()) {
                throw new NullPointerException("Invalid Access Token");
            }

            try {
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
            } catch (Exception e) {
                throw new RuntimeException("Twitch API Login failed. Provided credentials may be invalid.");
            }
            
            utils.sendMessage(p, "Logged in as: "+ user.getDisplayName());
    
            // Join the twitch chat of this channel and enable stream/follow events
            String channel = config.getString("TWITCH_CHANNEL_USERNAME");
            channel_id = getUserId(channel);
            user_id = new TwitchIdentityProvider(null, null, null).getAdditionalCredentialInformation(oauth).map(OAuth2Credential::getUserId).orElse(null);
            utils.sendMessage(p, "Listening to " + channel + "'s events...");
            client.getChat().joinChannel(channel);

            p.sendMessage("Logged in as: " + user.getDisplayName());
    
            eventSocket = client.getEventSocket();
            eventManager = client.getEventManager();
            if (Rewards.getRewards(Rewards.rewardType.TWITCH_CHANNEL_POINTS) != null) {
                client.getPubSub().listenForChannelPointsRedemptionEvents(null, channel_id);
                eventManager.onEvent(RewardRedeemedEvent.class, new Consumer<RewardRedeemedEvent>() {
                    @Override
                    public void accept(RewardRedeemedEvent e) {
                        eventHandler.onChannelPointsRedemption(e);
                    }
                });
                utils.sendMessage(p, "Listening for channel point rewards...");
            }
            if (Rewards.getRewards(Rewards.rewardType.TWITCH_FOLLOW) != null) {
                if (TwitchClient.getModeratedChannelIDs(oauth.getAccessToken(), user_id).contains(channel_id) || user_id.equals(channel_id)) { // If account is the streamer or a mod (need to have mod permissions on the channel)
                    eventSocket.register(SubscriptionTypes.CHANNEL_FOLLOW_V2.prepareSubscription(b -> b.moderatorUserId(user_id).broadcasterUserId(channel_id).build(), null));
                    eventManager.onEvent(ChannelFollowEvent.class, new Consumer<ChannelFollowEvent>() {
                        @Override
                        public void accept(ChannelFollowEvent e) {
                            try { // May get NullPointerException if event is triggered while still subscribing
                                eventHandler.onFollow(e);
                            } catch (NullPointerException ex) {}
                        }
                    });
                    utils.sendMessage(p, "Listening for follows...");            
                } else {
                    plugin.log.warning("Follow events cannot be listened to on unauthorised channels.");
                }
            }
            if (Rewards.getRewards(Rewards.rewardType.TWITCH_CHEER) != null) {
                eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_MESSAGE.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
                eventManager.onEvent(ChannelChatMessageEvent.class, new Consumer<ChannelChatMessageEvent>() {
                    @Override
                    public void accept(ChannelChatMessageEvent e) {
                        try { // May get NullPointerException if event is triggered while still subscribing
                            eventHandler.onCheer(e);
                        } catch (NullPointerException ex) {}
                    }
                }); 
                utils.sendMessage(p, "Listening for Cheers...");
            }
    
            if (Rewards.getRewards(Rewards.rewardType.TWITCH_SUB) != null || Rewards.getRewards(Rewards.rewardType.TWITCH_GIFT) != null) {
                eventSocket.register(SubscriptionTypes.CHANNEL_CHAT_NOTIFICATION.prepareSubscription(b -> b.broadcasterUserId(channel_id).userId(user_id).build(), null));
                eventManager.onEvent(ChannelChatNotificationEvent.class, new Consumer<ChannelChatNotificationEvent>(){
                    @Override
                    public void accept(ChannelChatNotificationEvent e) {
                        try { // May get NullPointerException if event is triggered while still subscribing
                            eventHandler.onEvent(e);
                        } catch (NullPointerException ex) {}
                    }
                });
                utils.sendMessage(p, "Listening for subscriptions and gifts...");
            }
    
            if (config.getBoolean("SHOW_CHAT")) {
                eventManager.onEvent(ChannelMessageEvent.class, event -> {
                    if (!plugin.chatBlacklist.contains(event.getUser().getName())) {
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
                            utils.sendMessage(player, components);
                        }
                    }
                });
            }
            eventHandler = new TwitchEventHandler();
            client.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(eventHandler);
            utils.sendMessage(p, "Done!");
            accountConnected = true;
        });
        linkThread.start();
        linkThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                plugin.log.warning(e.toString());
                p.sendMessage(ChatColor.RED + "Account linking failed!");
                unlink(Bukkit.getConsoleSender());
                accountConnected = false;
            }
        });
    }
    public void unlink(CommandSender p) {
        try {
            linkThread.join(); // Wait until linking is finished
            client.getEventSocket().close();
            client.getPubSub().close();
            client.close();
            accountConnected = false;
        } catch (Exception e) {
            plugin.log.warning("Error while disabling ChatPointsTTV.");
            e.printStackTrace();
            return;
        }

        // Erase variables
        client = null;
        eventHandler = null;
        eventSocket = null;
        eventManager = null;
        accountConnected = false;
        oauth = null;
        plugin = null;

        p.sendMessage(ChatColor.GREEN + "Account disconnected!");
    }
}
