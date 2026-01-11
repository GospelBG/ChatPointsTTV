package me.gosdev.chatpointsttv;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.TikTok.TikTokCommandController;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Twitch.TwitchCommandController;
import me.gosdev.chatpointsttv.Utils.AccountsManager;
import me.gosdev.chatpointsttv.Utils.FollowerLog;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatPointsTTV extends JavaPlugin {
    private final AtomicBoolean isReloading = new AtomicBoolean(false);
    private static AccountsManager accounts;
    private static ChatPointsTTV plugin;
    private static TwitchClient twitch;
    private static TikTokClient tiktok;
    private CommandController cmdController;
    private TwitchCommandController twitchCmdController;
    private TikTokCommandController tikTokCmdController;
    private boolean firstRun = false;

    public static HashMap<String, String> locales;
    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;
    public static AlertMode alertMode;
    public static Boolean logEvents;

    public static final Logger log = Bukkit.getLogger();
    public FileConfiguration config;
    public Metrics metrics;

    public static final String msgPrefix = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[ChatPointsTTV] " + ChatColor.WHITE + "" + ChatColor.RESET;

    public static enum permissions {
        BROADCAST("chatpointsttv.broadcast"),
        MANAGE("chatpointsttv.manage"),
        TARGET("chatpointsttv.target");

        public final String permission_id;

        private permissions(String label) {
            this.permission_id = label;
        }
    }

    public static ChatPointsTTV getPlugin() {
        return plugin;
    }

    public static AccountsManager getAccountsManager() {
        return accounts;
    }

    public Boolean isReloading() {
        return isReloading.get();
    }

    public static TwitchClient getTwitch() {
        return twitch;
    }

    public static TikTokClient getTikTok() {
        return tiktok;
    }

    public static FileConfiguration getPluginConfig() {
        return plugin.config;
    }

    public static void enableTwitch(CommandSender p) {
        if (twitch == null || !twitch.isStarted()) twitch = new TwitchClient(p);
    }
    
    public static void enableTikTok(CommandSender p) {
        if (tiktok == null || !tiktok.isStarted()) tiktok = new TikTokClient(p);
    }

    @Override
    public void onEnable() {
        plugin = this;
        accounts = new AccountsManager();
        PluginManager pm = Bukkit.getServer().getPluginManager();

        if (metrics == null) metrics = new Metrics(this, 22873);

        metrics.addCustomChart(new SingleLineChart("twitchModule", () -> {
            if (twitch != null && twitch.isAccountConnected()) {
                return 1;
            } else {
                return 0;
            }
        }));
        metrics.addCustomChart(new SingleLineChart("tiktokModule", () -> {
            if (tiktok != null && tiktok.isAccountConnected()) {
                return 1;
            } else {
                return 0;
            }
        }));
        
        if (!plugin.getDataFolder().exists()) firstRun = true;

        this.saveDefaultConfig();
        reloadConfig();
        config = getConfig();

        logEvents = config.getBoolean("LOG_EVENTS", false);
        shouldMobsGlow = config.getBoolean("MOB_GLOW", false);
        alertMode = AlertMode.valueOf(config.getString("INGAME_ALERTS", "NONE").toUpperCase());
        nameSpawnedMobs = config.getBoolean("DISPLAY_NAME_ON_MOB", true);

        File stringsFile = new File(plugin.getDataFolder(), "locales.yml");
        if (!stringsFile.exists()) {
            plugin.saveResource("locales.yml", false);
        }
        
        FileConfiguration stringsYaml = YamlConfiguration.loadConfiguration(stringsFile);
        FileConfiguration defaultStrings = YamlConfiguration.loadConfiguration(plugin.getTextResource("locales.yml"));
        locales = new HashMap<>();
        for (String key : defaultStrings.getKeys(true)) {
            if (stringsYaml.isString(key)) {
                locales.put(key, stringsYaml.getString(key));
            } else {
                locales.put(key, defaultStrings.getString(key));
            }
        }

        if (cmdController == null) {
            cmdController = new CommandController();
            this.getCommand("cpttv").setExecutor(cmdController);
            this.getCommand("cpttv").setTabCompleter(cmdController);
        }

        if (twitchCmdController == null) {
            twitchCmdController = new TwitchCommandController();
            this.getCommand("twitch").setExecutor(twitchCmdController);
            this.getCommand("twitch").setTabCompleter(twitchCmdController);
        }

        if (tikTokCmdController == null) {
            tikTokCmdController = new TikTokCommandController();
            this.getCommand("tiktok").setExecutor(tikTokCmdController);
            this.getCommand("tiktok").setTabCompleter(tikTokCmdController);
        }

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.spigot().sendMessage(new TextComponent("ChatPointsTTV reloaded!"));
            }
        }

        if (config.getBoolean("ENABLE_TWITCH", true)) enableTwitch(Bukkit.getConsoleSender());  
        if (config.getBoolean("ENABLE_TIKTOK", true)) enableTikTok(Bukkit.getConsoleSender()); 

        if (firstRun) {
            Bukkit.getConsoleSender().sendMessage(msgPrefix + "Configuration files have just been created. You need to set up ChatPointsTTV before using it.\nCheck out the quick start guide at https://gosdev.me/chatpointsttv/install");
        }

        VersionCheck.check();

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (!player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) return;
                if (firstRun) {
                    TextComponent welcomeMsg = new TextComponent("  ------------ " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "Welcome to ChatPointsTTV" + ChatColor.RESET + " ------------" + ChatColor.GRAY +
                        "\nThanks for installing ChatPointsTTV!\nYou " + ChatColor.BOLD + "need to set up the configuration files " + ChatColor.RESET + ChatColor.GRAY + "in order to use the plugin.\nYou can take a look at the quick start guide ");
                    
                    TextComponent link = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "here");
                    link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gosdev.me/chatpointsttv/install"));
                    link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));

                    welcomeMsg.addExtra(link);
                    welcomeMsg.addExtra(".\n  -------------------------------------------------\n");
                    player.getPlayer().spigot().sendMessage(welcomeMsg);
                }
                if (!VersionCheck.runningLatest) {
                    TextComponent updPrompt = new TextComponent(ChatColor.YELLOW + "ChatPointsTTV v" + VersionCheck.latestVersion + " has been released!\n" + ChatColor.YELLOW + "Click ");
                    
                    TextComponent updBtn = new TextComponent(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "here");
                    updBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create())); 
                    updBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VersionCheck.download_url));

                    updPrompt.addExtra(updBtn);
                    updPrompt.addExtra("" + ChatColor.RESET + ChatColor.YELLOW + " to download the latest version.\n");
    
                    player.getPlayer().spigot().sendMessage(updPrompt);
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        if (twitch != null) twitch.stop(Bukkit.getConsoleSender());
        if (tiktok != null) tiktok.stop(Bukkit.getConsoleSender());
        FollowerLog.stop();

        try {
            twitch.stopThread.join();
            tiktok.stopThread.join();
        } catch (InterruptedException ex) {
        }
        
        // Erase variables
        config = null;
        plugin = null;
        twitch = null;
        tiktok = null;

        HandlerList.unregisterAll(this);
    }

    public void reload(CommandSender p) {
        if (!isReloading.compareAndSet(false, true)) {
            p.sendMessage(ChatColor.RED + "ChatPointsTTV is already reloading!");
            return;
        }

        if (!p.equals(Bukkit.getConsoleSender())) p.sendMessage(ChatPointsTTV.msgPrefix + "Reloading ChatPointsTTV...");
        ChatPointsTTV.log.info("Reloading ChatPointsTTV...");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            onDisable();
            try {
                Bukkit.getScheduler().callSyncMethod(this, () -> { // Need to run on main thread, due to Bukkit API usage
                    onEnable();
                    return null;
                }).get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
                p.sendMessage(ChatColor.RED + "There was an error reloading ChatPointsTTV. Please check the server console.");
                e.printStackTrace();
            }
            
            isReloading.set(false);
        });
    }

}
