package me.gosdev.chatpointsttv;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.gosdev.chatpointsttv.Events.CPTTV_EventHandler;
import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.TikTok.TikTokCommandController;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Twitch.TwitchCommandController;
import me.gosdev.chatpointsttv.Utils.FollowerLog;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatPointsTTV extends JavaPlugin {
    private static ChatPointsTTV plugin;
    private static TwitchClient twitch;
    private CommandController cmdController;
    private TwitchCommandController twitchCmdController;
    private TikTokCommandController tikTokCmdController;
    private boolean firstRun = false;

    public static final HashMap<String, String> strings = new HashMap<>();
    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;
    public static AlertMode alertMode;
    public static Boolean logEvents;

    public static final Logger log = Bukkit.getLogger();
    public FileConfiguration config;
    public Metrics metrics;

    public final static String msgPrefix = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[ChatPointsTTV] " + ChatColor.WHITE + "" + ChatColor.RESET;

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

    public static TwitchClient getTwitch() {
        return twitch;
    }

    public static FileConfiguration getPluginConfig() {
        return plugin.config;
    }

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();

        metrics = new Metrics(this, 22873);
        
        // Get the latest config after saving the default if missing
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
        for (String key : defaultStrings.getKeys(true)) {
            if (stringsYaml.isString(key)) {
                strings.put(key, stringsYaml.getString(key));
            } else {
                strings.put(key, defaultStrings.getString(key));
            }
        }

        cmdController = new CommandController();
        this.getCommand("cpttv").setExecutor(cmdController);
        this.getCommand("cpttv").setTabCompleter(cmdController);

        twitchCmdController = new TwitchCommandController();
        this.getCommand("twitch").setExecutor(twitchCmdController);
        this.getCommand("twitch").setTabCompleter(twitchCmdController);

        tikTokCmdController = new TikTokCommandController();
        this.getCommand("tiktok").setExecutor(tikTokCmdController);
        this.getCommand("tiktok").setTabCompleter(tikTokCmdController);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.spigot().sendMessage(new TextComponent("ChatPointsTTV reloaded!"));
            }
        }
        
        twitch = new TwitchClient();
        if (config.getBoolean("ENABLE_TWITCH", true)) twitch.enable(Bukkit.getConsoleSender()); 
        if (config.getBoolean("ENABLE_TIKTOK", true)) TikTokClient.enable(Bukkit.getConsoleSender()); 

        if (firstRun) {
            Bukkit.getConsoleSender().sendMessage(msgPrefix + "Configuration files have just been created. You will need to set up ChatPointsTTV before using it.\nCheck out the quick start guide at https://gosdev.me/chatpointsttv/install");
        }

        VersionCheck.check();

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (!player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) return;
                if (firstRun) {
                    TextComponent welcomeMsg = new TextComponent("------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "ChatPointsTTV" + ChatColor.RESET + " -------------" + ChatColor.GRAY + "\nThanks for installing ChatPointsTTV!\nYou will need to set up the configuration files in order to use the plugin.\nYou can take a look at the quick start guide ");
                    
                    TextComponent link = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "here.");
                    link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gosdev.me/chatpointsttv/install"));
                    link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create()));

                    welcomeMsg.addExtra(link);
                    welcomeMsg.addExtra("\n-----------------------------------------");
                    player.getPlayer().spigot().sendMessage(welcomeMsg);
                }
                if (!VersionCheck.runningLatest) {
                    TextComponent updPrompt = new TextComponent(ChatColor.YELLOW + "ChatPointsTTV v" + VersionCheck.latestVersion + " has been released!\n" + ChatColor.YELLOW + "Click ");
                    
                    TextComponent updBtn = new TextComponent(ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "here");
                    updBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open in browser").create())); 
                    updBtn.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, VersionCheck.download_url));

                    updPrompt.addExtra(updBtn);
                    updPrompt.addExtra("" + ChatColor.RESET + ChatColor.YELLOW + " to download the latest version\n");
    
                    player.getPlayer().spigot().sendMessage(updPrompt);
                }

                if (!twitch.isStarted()) return;
                if ((twitch.linkThread == null || !twitch.linkThread.isAlive()) && !TwitchClient.accountConnected && !firstRun) {
                    String msg = ChatColor.LIGHT_PURPLE + "Welcome! Remember to link your Twitch account to enable ChatPointsTTV and start listening to events!\n";
                    BaseComponent btn = new ComponentBuilder(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "[Click here to login]").create()[0];

                    btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to run command").create()));
                    btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch link"));

                    player.getPlayer().spigot().sendMessage(new BaseComponent[] {new ComponentBuilder(msg).create()[0], btn});
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        if (twitch != null && twitch.isAccountConnected()) twitch.stop(Bukkit.getConsoleSender());
        if (TikTokClient.accountConnected) TikTokClient.stop(Bukkit.getConsoleSender());
        FollowerLog.stop();
        
        // Erase variables
        config = null;
        plugin = null;
        twitch = null;

        CPTTV_EventHandler.actions = new HashMap<>();

        HandlerList.unregisterAll(this);
    }

}
