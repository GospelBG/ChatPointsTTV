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

import me.gosdev.chatpointsttv.Generic.GenericTikTokConfig;
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
import me.gosdev.chatpointsttv.Utils.Translatable;

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
    private GenericTikTokConfig tiktokConfig;
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
    public GenericTikTokConfig getConfig() {
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

            chatBlacklist = tiktokConfig.getChatBlacklist();
            eventHandler = new TikTokEvents();

            // Configuration overrides
            shouldMobsGlow = tiktokConfig.getMobGlow(ChatPointsTTV.getInstance().shouldMobsGlow);
            nameSpawnedMobs = tiktokConfig.getDisplayNameOnMob(ChatPointsTTV.getInstance().nameSpawnedMobs);
            alertMode = tiktokConfig.getIngameAlerts(ChatPointsTTV.getInstance().alertMode);


            started = true;
            reloading.set(false);

            for (String username : ChatPointsTTV.getAccountsManager().getAccounts(Platforms.TIKTOK)) {
                if (username.isBlank()) continue;
                link(p, username, false);
            }

            p.sendMessage(ChatPointsTTV.msgPrefix + Translatable.getString("generic.message.success.start", "TikTok"));
        });
    }


    public void link(GenericSender p, String handle, Boolean save) {
        if (!started) return;
        if (reloading.get()) {
            p.sendMessage(ChatColor.RED + Translatable.getString("generic.message.still_starting", "TikTok"));
            return;
        }
        tiktokExecutor.submit(() -> {
            // Sanitise username
            String username = (handle.startsWith("@") ? handle.substring(1) : handle).toLowerCase();

            if (clients.containsKey(username)) {
                p.sendMessage(ChatPointsTTV.msgPrefix + Translatable.getString("tiktok.message.already_linked"));
                return;
            }

            p.sendMessage(ChatPointsTTV.msgPrefix + Translatable.getString("tiktok.message.linking", username));

            LiveClientBuilder builder = TikTokLive.newClient(username);
            if (CPTTV_EventHandler.getActions(ChatPointsTTV.getInstance().config.getTikTokEventsConfig(), TikTokEventType.LIKE) != null) {
                builder.onLike((liveClient, event) -> {
                    eventHandler.onLike(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (CPTTV_EventHandler.getActions(ChatPointsTTV.getInstance().config.getTikTokEventsConfig(), TikTokEventType.GIFT) != null) {
                builder.onGiftCombo((liveClient, event) -> {
                    if (event.getComboState().equals(GiftComboStateType.Finished)) eventHandler.onGift(event, clients.get(username).getRoomInfo().getHostName()); // Only handle Finished Combos
                });
            }
            if (CPTTV_EventHandler.getActions(ChatPointsTTV.getInstance().config.getTikTokEventsConfig(), TikTokEventType.FOLLOW) != null) {
                builder.onFollow((liveClient, event) -> {
                    eventHandler.onFollow(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (CPTTV_EventHandler.getActions(ChatPointsTTV.getInstance().config.getTikTokEventsConfig(), TikTokEventType.SHARE) != null) {
                builder.onShare((liveClient, event) -> {
                    eventHandler.onShare(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (ChatPointsTTV.getInstance().config.getGeneralConfig().getShowChat()) {
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

                if (tiktokConfig.hasEulerstreamApiKey()) {
                    settings.setApiKey(tiktokConfig.getEulerstreamApiKey());
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

                    p.sendMessage(ChatPointsTTV.msgPrefix + Translatable.getString("tiktok.message.success.link", c.getRoomInfo().getHostName()));
                    break;

                } catch (Exception ex) {
                    if (ex instanceof  TikTokLiveOfflineHostException) {
                        p.sendMessage(ChatColor.RED + Translatable.getString("tiktok.message.failure.offline", username));
                        return;
                    } else if (ex instanceof TikTokLiveUnknownHostException) {
                        p.sendMessage(ChatColor.RED + Translatable.getString("tiktok.messsage.failure.not_found", username));
                        return;
                    }
                    if (i == maxRetries) {
                        if (ex instanceof TikTokSignServerException) {
                            p.sendMessage(ChatColor.RED + Translatable.getString("tiktok.message.failure.generic", username) + " " + (tiktokConfig.hasEulerstreamApiKey() ? Translatable.getString("tiktok.message.failure.server.api_key") : Translatable.getString("tiktok.message.failure.server.try_again")));
                        } else if (ex instanceof TikTokLiveRequestException && ex.getCause() instanceof HttpTimeoutException) {
                            p.sendMessage(ChatColor.RED + Translatable.getString("tiktok.message.failure.timeout", username));
                        } else {
                            p.sendMessage(ChatColor.RED + Translatable.getString("tiktok.message.failure.generic", username) + " " + Translatable.getString("tiktok.message.failure.generic.check_console", username));
                            ex.printStackTrace();
                            return;
                        }
                    } else {
                        ChatPointsTTV.log.warn(Translatable.getString("tiktok.message.failure.generic", username) + " " +  Translatable.getString("tiktok.message.failure.generic.retrying"));
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
            p.sendMessage(ChatPointsTTV.msgPrefix + Translatable.getString("generic.message.success.stop", "TikTok"));
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
