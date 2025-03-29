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

import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Utils.LibraryLoader;
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

    private static final HashMap<rewardType, String> titleStrings = new HashMap<>();
    public static final HashMap<String, String> strings = new HashMap<>();
    public static ChatColor eventColor;
    public static ChatColor userColor;
    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;
    public static Boolean configOk = true;
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

    public static String getRedemptionString(rewardType type) {
        return titleStrings.get(type);
    }

    @Override
    public void onLoad() {
        LibraryLoader.LoadLibraries(this);
    }

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();

        metrics = new Metrics(this, 22873);
        
        // Get the latest config after saving the default if missing
        this.saveDefaultConfig();
        reloadConfig();
        config = getConfig();

        logEvents = config.getBoolean("LOG_EVENTS", false);
        userColor = ChatColor.valueOf(config.getConfigurationSection("COLORS").getString("USER_COLOR", ChatColor.WHITE.name()));
        eventColor = ChatColor.valueOf(config.getConfigurationSection("COLORS").getString("EVENT_COLOR", ChatColor.LIGHT_PURPLE.name()));
        shouldMobsGlow = config.getBoolean("MOB_GLOW", false);
        alertMode = AlertMode.valueOf(config.getString("INGAME_ALERTS", "NONE").toUpperCase());
        nameSpawnedMobs = config.getBoolean("DISPLAY_NAME_ON_MOB", true);

        File stringsFile = new File(plugin.getDataFolder(), "localization.yml");
        if (!stringsFile.exists()) {
            plugin.saveResource("localization.yml", false);
        }
        
        FileConfiguration stringsYaml = YamlConfiguration.loadConfiguration(stringsFile);
        for (String key : YamlConfiguration.loadConfiguration(plugin.getTextResource("localization.yml")).getKeys(true)) {
            strings.put(key, stringsYaml.getString(key));
        }

        cmdController = new CommandController();
        this.getCommand("twitch").setExecutor(cmdController);
        this.getCommand("twitch").setTabCompleter(cmdController);

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.spigot().sendMessage(new TextComponent("ChatPointsTTV reloaded!"));
            }
        }
        
        twitch = new TwitchClient();
        twitch.enable(); 

        try {
            for (rewardType type : rewardType.values()) {
                titleStrings.put(type, twitch.getConfig().getConfigurationSection("STRINGS").getString(type.toString() + "_STRING"));
            }
        } catch (NullPointerException e) {
            log.severe("Error loading strings from config. Please check your config file.");
            configOk = false;
        }

        VersionCheck.check();

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (twitch != null && !twitch.linkThread.isAlive() && !TwitchClient.accountConnected && player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) {
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
    
        // Erase variables
        config = null;
        configOk = true;
        plugin = null;
        twitch = null;

        Rewards.rewards = new HashMap<>();

        HandlerList.unregisterAll(this);
    }

}
