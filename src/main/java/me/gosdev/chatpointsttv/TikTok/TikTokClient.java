package me.gosdev.chatpointsttv.TikTok;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;
import me.gosdev.chatpointsttv.Events.CPTTV_EventHandler;
import me.gosdev.chatpointsttv.Events.EventType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TikTokClient {
    public static Boolean accountConnected = false;
    public static Boolean isEnabled = false;
    public static List<String> listenedProfiles;

    private static TikTokEvents eventHandler;
    private static Thread linkThread;

    private static final ChatPointsTTV plugin = ChatPointsTTV.getPlugin();
    private static final Integer maxRetries = 3;
    private static FileConfiguration tiktokConfig;

    private static HashMap<String, LiveClient> clients = new HashMap<>();
    private static List<String> chatBlacklist = new ArrayList<>();

    public static HashMap<String, LiveClient> getClients() {
        return clients;
    }
    public Boolean isAccountConected() {
        return accountConnected;
    }
    public static FileConfiguration getConfig() {
        return tiktokConfig;
    }
    public static TikTokEvents getEventHandler() {
        return eventHandler;
    }

    public static void link(CommandSender p, String handle, Boolean save) {
        Bukkit.getScheduler().runTaskAsynchronously(ChatPointsTTV.getPlugin(), () -> {
            if (linkThread != null) {
                try {
                    linkThread.join();
                } catch (InterruptedException e) {}
            }
            
            linkThread = new Thread(() -> {
                // Sanitise username
                String username = (handle.startsWith("@") ? handle.substring(1) : handle).toLowerCase();

                if (clients.containsKey(username)) {
                    p.sendMessage(ChatPointsTTV.msgPrefix + "You cannot link the same LIVE twice!");
                    return;
                }

                p.sendMessage(ChatPointsTTV.msgPrefix + "Linking...");

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
                if (plugin.config.getBoolean("SHOW_CHAT")) {
                    builder.onComment((liveClient, event) -> {
                        if (!chatBlacklist.contains(event.getUser().getName())) {
                            BaseComponent[] components = new BaseComponent[] {
                                new ComponentBuilder(ChatColor.DARK_PURPLE + event.getUser().getProfileName() + ": ").create()[0],
                                new ComponentBuilder(event.getText()).create()[0]
                            };
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (player.hasPermission(permissions.BROADCAST.permission_id)) {
                                    player.spigot().sendMessage(components);
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
                        accountConnected = true;
                        clients.put(username, c);

                        if (save) {
                            tiktokConfig.set("LISTENED_PROFILES", clients.keySet().toArray());
                            try {
                                tiktokConfig.save(new File(plugin.getDataFolder(), "tiktok.yml"));
                            } catch (IOException e) {
                                Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + "Failed to save TikTok configuration file!");
                            }
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
                                p.sendMessage(ChatColor.RED + "There was an error while connecting to @" + username + "'s LIVE. Please try again.");
                            } else if (ex instanceof TikTokLiveRequestException && ex.getCause() instanceof HttpTimeoutException) {
                                p.sendMessage(ChatColor.RED + "Connection timed out while connecting to @" + username + "'s LIVE. Please try again.");
                            } else {
                                p.sendMessage(ChatColor.RED + "There was an error while connecting to @" + username + "'s LIVE. Check the server console for details.");
                                ex.printStackTrace();
                                return;
                            }
                        } else {
                            ChatPointsTTV.log.warning("There was an error while connecting to @" + username + "'s LIVE. Retrying in a few seconds...");
                            try {Thread.sleep(3000); } catch (InterruptedException e) {}
                        }  
                    }
                }

            });
            
            linkThread.start();
        });
    }

    public static void stop(CommandSender p) {
        for (String username : clients.keySet()) {
            unlink(username, false);
        }

        accountConnected = false;
        clients = null;
        chatBlacklist = null;
        tiktokConfig = null;

        for (EventType type : TikTokEventType.values()) {
            CPTTV_EventHandler.actions.remove(type);
        }

        isEnabled = false;
        p.sendMessage(ChatColor.GREEN + "TikTok disconnected successfully!");
    }

    public static void unlink(String username, Boolean save) {
        for (String clientHost : clients.keySet()) {
            if (clientHost.equalsIgnoreCase(username)) {
                clients.get(username).disconnect();
                clients.remove(clientHost);

                if (save) {
                    tiktokConfig.set("LISTENED_PROFILES", clients.keySet().toArray());
                    try {
                        tiktokConfig.save(new File(plugin.getDataFolder(), "tiktok.yml"));
                    } catch (IOException e) {
                        Bukkit.getConsoleSender().sendMessage(ChatPointsTTV.msgPrefix + ChatColor.RED + "Failed to save TikTok configuration file!");
                    }
                }
                break;
            }
        }
    }

    public static void enable(CommandSender p) {
        clients = new HashMap<>();
        chatBlacklist = new ArrayList<>();

        File tiktokConfigFile = new File(plugin.getDataFolder(), "tiktok.yml");
        if (!tiktokConfigFile.exists()) {
            plugin.saveResource(tiktokConfigFile.getName(), false);
        }

        tiktokConfig = YamlConfiguration.loadConfiguration(tiktokConfigFile);
        if (!tiktokConfig.getStringList("CHAT_BLACKLIST").isEmpty()) {
            chatBlacklist.addAll(tiktokConfig.getStringList("CHAT_BLACKLIST"));
        }

        eventHandler = new TikTokEvents();

        listenedProfiles = tiktokConfig.getStringList("LISTENED_PROFILES");
        for (String username : listenedProfiles) {
            if (username.isBlank()) continue;
            link(p, username, false);
        }

        isEnabled = true;
        p.sendMessage("TikTok module enabled successfully!");
    }
    
}
