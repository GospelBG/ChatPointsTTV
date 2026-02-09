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
import me.gosdev.chatpointsttv.ConfigFile;
import me.gosdev.chatpointsttv.Events.EventManager;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.Utils.ChatColor;

public class TikTokClient {
    public AtomicBoolean reloading = new AtomicBoolean(true);
    public List<String> listenedProfiles;
    public Thread stopThread;

    private volatile Boolean started = false;
    private volatile Boolean accountConnected = false;
    
    //private final ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
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

    public TikTokClient(GenericSender p, ConfigFile config) {
        tiktokConfig = config;
        //Bukkit.getScheduler().runTaskAsynchronously(ChatPointsTTV.getPlugin(), () -> {
        new Thread(() -> {
            config.reload();
            reloading.set(true);
            clearClients();
            chatBlacklist = new ArrayList<>();
            listenedProfiles = new ArrayList<>();

            for (TikTokEventType type : TikTokEventType.values()) {
                EventManager.parseActions(type, config);
            }

            chatBlacklist = tiktokConfig.getStringList("CHAT_BLACKLIST");
            eventHandler = new TikTokEvents();            

            // Configuration overrides
            shouldMobsGlow = tiktokConfig.getBoolean("MOB_GLOW", ChatPointsTTV.shouldMobsGlow);
            nameSpawnedMobs = tiktokConfig.getBoolean("DISPLAY_NAME_ON_MOB", ChatPointsTTV.nameSpawnedMobs);
            alertMode = AlertMode.valueOf(tiktokConfig.getString("INGAME_ALERTS", ChatPointsTTV.alertMode.toString()).toUpperCase());


            started = true;
            reloading.set(false);

            for (String username : ChatPointsTTV.getAccounts().getStoredUsers(Platforms.TIKTOK)) {
                if (username.isBlank()) continue;
                link(p, username, false);
            }

            p.sendMessage(ChatPointsTTV.PREFIX + "TikTok Module has started successfully!");
        }).start();
    }


    public void link(GenericSender p, String handle, Boolean save) {
        if (!started) return;
        if (reloading.get()) {
            p.sendMessage(ChatColor.RED + "Please wait until the TikTok Module has finished starting.");
            return;
        }
        //tiktokExecutor.submit(() -> {
            // Sanitise username
            String username = (handle.startsWith("@") ? handle.substring(1) : handle).toLowerCase();

            if (clients.containsKey(username)) {
                p.sendMessage(ChatPointsTTV.PREFIX + "You cannot link the same LIVE twice!");
                return;
            }

            p.sendMessage(ChatPointsTTV.PREFIX + "Linking to @" + username + "'s LIVE");

            LiveClientBuilder builder = TikTokLive.newClient(username);
            if (EventManager.actionsFound(TikTokEventType.LIKE)) {
                builder.onLike((liveClient, event) -> {
                    eventHandler.onLike(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (EventManager.actionsFound(TikTokEventType.GIFT)) {
                builder.onGiftCombo((liveClient, event) -> {
                    if (event.getComboState().equals(GiftComboStateType.Finished)) eventHandler.onGift(event, clients.get(username).getRoomInfo().getHostName()); // Only handle Finished Combos
                });
            }
            if (EventManager.actionsFound(TikTokEventType.FOLLOW)) {
                builder.onFollow((liveClient, event) -> {
                    eventHandler.onFollow(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (EventManager.actionsFound(TikTokEventType.SHARE)) {
                builder.onShare((liveClient, event) -> {
                    eventHandler.onShare(event, clients.get(username).getRoomInfo().getHostName());
                });
            }
            if (ChatPointsTTV.getConfig().getBoolean("SHOW_CHAT", true)) {
                builder.onComment((liveClient, event) -> {
                    if (!chatBlacklist.contains(event.getUser().getName())) {
                        /*BaseComponent[] components = new BaseComponent[] {
                            new ComponentBuilder(ChatColor.DARK_PURPLE + event.getUser().getProfileName() + ": ").create()[0],
                            new ComponentBuilder(event.getText()).create()[0]*/
                        };
                        /*for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.hasPermission(permissions.BROADCAST.permission_id)) {
                                player.spigot().sendMessage(components);
                            }
                        }
                    }*/
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
                        ChatPointsTTV.getAccounts().saveCredential(username, Optional.empty());
                    }

                    ChatPointsTTV.log.info(ChatPointsTTV.PREFIX + "Linked succesfully to @" + c.getRoomInfo().getHostName() + "'s LIVE!");
                    break;

                } catch (Exception ex) {
                    if (ex instanceof  TikTokLiveOfflineHostException) {
                        ChatPointsTTV.log.info(ChatColor.RED + "Cannot connect to @" + username + " because they are currently offline!"); //p.sendMessage
                        return;
                    } else if (ex instanceof TikTokLiveUnknownHostException) {
                        ChatPointsTTV.log.info(ChatColor.RED + "Couldn't find TikTok user: @" + username); //p.sendMessage
                        return;
                    }
                    if (i == maxRetries) {
                        if (ex instanceof TikTokSignServerException) {
                            ChatPointsTTV.log.info(ChatColor.RED + "There was an error while connecting to @" + username + "'s LIVE." /*+ (tiktokConfig.isString("EULERSTREAM_API_KEY") ? " Please check your API key." : " Please try again.")*/); //p.sendMessage
                        } else if (ex instanceof TikTokLiveRequestException && ex.getCause() instanceof HttpTimeoutException) {
                            ChatPointsTTV.log.info(ChatColor.RED + "Connection timed out while connecting to @" + username + "'s LIVE. Please try again."); //p.sendMessage
                        } else {
                            ChatPointsTTV.log.info(ChatColor.RED + "There was an error while connecting to @" + username + "'s LIVE. Check the server console for details."); //p.sendMessage
                            ex.printStackTrace();
                            return;
                        }
                    } else {
                        ChatPointsTTV.log.warn("There was an error while connecting to @" + username + "'s LIVE. Retrying in a few seconds..."); //p.sendMessage
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {}
                    }  
                }
            }
        //});
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
            p.sendMessage(ChatPointsTTV.PREFIX + "TikTok Module has been successfully stopped!");
        });

        stopThread.start();
    }

    public void unlink(String username, Boolean save) {
        for (String clientHost : clients.keySet()) {
            if (clientHost.equalsIgnoreCase(username)) {
                clients.get(username).disconnect();
                clients.remove(clientHost);

                if (save) {
                    ChatPointsTTV.getAccounts().saveCredential(clientHost, Optional.empty());
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
