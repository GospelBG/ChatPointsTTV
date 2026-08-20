package me.gosdev.chatpointsttv.TikTok;

import java.net.http.HttpTimeoutException;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import me.gosdev.chatpointsttv.Generic.ConfigFile;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Utils.ChatColor;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.data.settings.HttpClientSettings;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveUnknownHostException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokSignServerException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;
import me.gosdev.chatpointsttv.Events.CPTTV_EventHandler;
import me.gosdev.chatpointsttv.Platforms;

public class TikTokClient {
    public AtomicBoolean reloading = new AtomicBoolean(true);
    public List<String> listenedProfiles;
    public Thread stopThread;

    private volatile Boolean started = false;
    private volatile Boolean accountConnected = false;
    
    private final ChatPointsTTV plugin = ChatPointsTTV.getInstance();
    private final Integer maxRetries = 3;
    private final ExecutorService tiktokExecutor = Executors.newSingleThreadExecutor();
    private final ConcurrentHashMap<String, LiveClient> clients = new ConcurrentHashMap<>();

    private TikTokEvents eventHandler;
    private ConfigFile tiktokConfig;
    private List<String> chatBlacklist;

    public Boolean shouldMobsGlow;
    public Boolean nameSpawnedMobs;
    public AlertMode alertMode;

    public HashMap<String, LiveClient> getClients() {
        return new HashMap<>(clients);
    }
    public Boolean isAccountConnected() {
        return accountConnected;
    }
    public Boolean isStarted() {
        return started;
    }
    public Boolean isReloading() {
        return reloading.get();
    }
    public ConfigFile getConfig() {
        return tiktokConfig;
    }
    public TikTokEvents getEventHandler() {
        return eventHandler;
    }

    public TikTokClient(GenericSender p) {
        tiktokConfig = ChatPointsTTV.getInstance().config.getTikTokConfig();

        tiktokExecutor.submit(() -> {
            reloading.set(true);
            clearClients();
            chatBlacklist = new ArrayList<>();
            listenedProfiles = new ArrayList<>();

            CPTTV_EventHandler.clearActions(Platforms.TIKTOK); // Make sure actions will be parsed again

            chatBlacklist = tiktokConfig.getStringList("CHAT_BLACKLIST");
            eventHandler = new TikTokEvents();

            // Configuration overrides
            shouldMobsGlow = tiktokConfig.getBoolean("MOB_GLOW", ChatPointsTTV.getInstance().shouldMobsGlow);
            nameSpawnedMobs = tiktokConfig.getBoolean("DISPLAY_NAME_ON_MOB", ChatPointsTTV.getInstance().nameSpawnedMobs);
            alertMode = AlertMode.valueOf(tiktokConfig.getString("INGAME_ALERTS", ChatPointsTTV.getInstance().alertMode.toString()).toUpperCase());


            started = true;
            reloading.set(false);

            for (String username : ChatPointsTTV.getAccountsManager().getAccounts(Platforms.TIKTOK)) {
                if (username.isBlank()) continue;
                link(p, username, false);
            }

            p.sendMessage(ChatPointsTTV.msgPrefix + "TikTok Module has started successfully!");
        });
    }


    public void link(GenericSender p, String handle, Boolean save) {
        if (!started) return;
        if (reloading.get()) {
            p.sendMessage(ChatColor.RED + "Please wait until the TikTok Module has finished starting.");
            return;
        }
        tiktokExecutor.submit(() -> {
            // Sanitise username
            String username = (handle.startsWith("@") ? handle.substring(1) : handle).toLowerCase();

            if (clients.containsKey(username)) {
                p.sendMessage(ChatPointsTTV.msgPrefix + "You cannot link the same LIVE twice!");
                return;
            }

            p.sendMessage(ChatPointsTTV.msgPrefix + "Linking to @" + username + "'s LIVE");

            LiveClientBuilder builder = TikTokLive.newClient(username);
            if (CPTTV_EventHandler.getActions(tiktokConfig, TikTokEventType.LIKE) != null) {
                builder.onLike((liveClient, event) -> {
                    eventHandler.onLike(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (CPTTV_EventHandler.getActions(tiktokConfig, TikTokEventType.GIFT) != null) {
                builder.onGiftCombo((liveClient, event) -> {
                    if (event.getComboState().equals(GiftComboStateType.Finished)) eventHandler.onGift(event, clients.get(username).getRoomInfo().getHostName()); // Only handle Finished Combos
                });
            }
            if (CPTTV_EventHandler.getActions(tiktokConfig, TikTokEventType.FOLLOW) != null) {
                builder.onFollow((liveClient, event) -> {
                    eventHandler.onFollow(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (CPTTV_EventHandler.getActions(tiktokConfig, TikTokEventType.SHARE) != null) {
                builder.onShare((liveClient, event) -> {
                    eventHandler.onShare(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (tiktokConfig.getBoolean("SHOW_CHAT", true)) {
                builder.onComment((liveClient, event) -> {
                    if (!chatBlacklist.contains(event.getUser().getName())) {
                        String message = ChatColor.DARK_PURPLE + event.getUser().getProfileName() + ": " + ChatColor.RESET + event.getText();
                        for (GenericPlayer player : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                            if (player.hasPermission(permissions.BROADCAST)) {
                                player.sendMessage(message);
                            }
                        }
                    }
                });
            }

            builder.configure((LiveClientSettings settings) -> {
                HttpClientSettings httpSettings = settings.getHttpSettings();
                httpSettings.setTimeout(Duration.of(30L, SECONDS));

                if (tiktokConfig.isString("EULERSTREAM_API_KEY")) {
                    settings.setApiKey(tiktokConfig.getString("EULERSTREAM_API_KEY"));
                }
                settings.setHttpSettings(httpSettings);
            });

            for (int i = 1; i <= maxRetries; i++) {
                try {
                    LiveClient c = builder.buildAndConnect();
                    if (!started || clients.containsKey(username)) {
                        c.disconnect();
                        return;
                    }

                    clients.put(username, c);
                    listenedProfiles.add(username);
                    accountConnected = true;

                    if (save) {
                        ChatPointsTTV.getAccountsManager().saveAccount(Platforms.TIKTOK, username, Optional.empty());
                    }

                    p.sendMessage(ChatPointsTTV.msgPrefix + "Linked succesfully to @" + c.getRoomInfo().getHostName() + "'s LIVE!");
                    break;

                } catch (Exception ex) {
                    if (ex instanceof  TikTokLiveOfflineHostException) {
                        p.sendMessage(ChatColor.RED + "Cannot connect to @" + username + " because they are currently offline!");
                        return;
                    } else if (ex instanceof TikTokLiveUnknownHostException) {
                        p.sendMessage(ChatColor.RED + "Couldn't find TikTok user: @" + username);
                        return;
                    }
                    if (i == maxRetries) {
                        if (ex instanceof TikTokSignServerException) {
                            p.sendMessage(ChatColor.RED + "There was an error while connecting to @" + username + "'s LIVE." + (tiktokConfig.isString("EULERSTREAM_API_KEY") ? " Please check your API key." : " Please try again."));
                        } else if (ex instanceof TikTokLiveRequestException && ex.getCause() instanceof HttpTimeoutException) {
                            p.sendMessage(ChatColor.RED + "Connection timed out while connecting to @" + username + "'s LIVE. Please try again.");
                        } else {
                            p.sendMessage(ChatColor.RED + "There was an error while connecting to @" + username + "'s LIVE. Check the server console for details.");
                            ex.printStackTrace();
                            return;
                        }
                    } else {
                        ChatPointsTTV.log.warn("There was an error while connecting to @" + username + "'s LIVE. Retrying in a few seconds...");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {}
                    }  
                }
            }
        });
    }

    public void stop(GenericSender p) {
        if (!started || tiktokExecutor.isShutdown()) return;
        reloading.set(true);

        stopThread = new Thread(() -> {
            tiktokExecutor.shutdown();
            try {
                if(!tiktokExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    tiktokExecutor.shutdownNow();
                    ChatPointsTTV.log.warn("TikTok Module is taking too long to stop. Forcing shutdown...");
                }
            } catch (InterruptedException e) {
                tiktokExecutor.shutdownNow();
            }

            clearClients();

            chatBlacklist = new ArrayList<>();
            tiktokConfig = null;
            
            started = false;
            accountConnected = false;

            reloading.set(false);
            p.sendMessage(ChatPointsTTV.msgPrefix + "TikTok Module has been successfully stopped!");
        });

        stopThread.start();
    }

    public void unlink(String username, Boolean save) {
        for (String clientHost : clients.keySet()) {
            if (clientHost.equalsIgnoreCase(username)) {
                clients.get(username).disconnect();
                clients.remove(clientHost);

                if (save) {
                    ChatPointsTTV.getAccountsManager().removeAccount(Platforms.TIKTOK, clientHost);
                }
                break;
            }
        }
    }

    private void clearClients() {
        for (String c : clients.keySet()) {
            clients.get(c).disconnect();
            clients.remove(c);
        }
    }
}
