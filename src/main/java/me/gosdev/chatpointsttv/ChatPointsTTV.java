package me.gosdev.chatpointsttv;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Twitch.TwitchEventHandler;
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

    private static final Map<String, ChatColor> colors = new HashMap<>();
    private static final Map<String, String> titleStrings = new HashMap<>();
    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;
    public static boolean configOk = true;
    public static alert_mode alertMode;

    public final Logger log = getLogger();
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

    public static enum alert_mode {
        NONE,
        CHAT,
        TITLE,
        ALL
    }

    public static ChatPointsTTV getPlugin() {
        return plugin;
    }

    public TwitchClient getTwitch() {
        return twitch;
    }

    public static FileConfiguration getPluginConfig() {
        return plugin.config;
    }


    public static Map<String, ChatColor> getChatColors() {
        return colors;
    }
    public static Map<String, String> getRedemptionStrings() {
        return titleStrings;
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

        config.getConfigurationSection("COLORS").getKeys(false).forEach(i -> {
            colors.put(i, ChatColor.valueOf(config.getConfigurationSection("COLORS").getString(i)));
        });

        config.getConfigurationSection("STRINGS").getKeys(true).forEach(i -> {
            titleStrings.put(i, config.getConfigurationSection("STRINGS").getString(i));
        });

        TwitchEventHandler.rewardBold = config.getBoolean("REWARD_NAME_BOLD");

        shouldMobsGlow = config.getBoolean("MOB_GLOW", false);
        alertMode = alert_mode.valueOf(config.getString("INGAME_ALERTS").toUpperCase());
        nameSpawnedMobs = config.getBoolean("DISPLAY_NAME_ON_MOB", true);

        twitch = new TwitchClient();
        twitch.enable(); 

        cmdController = new CommandController();
        this.getCommand("twitch").setExecutor(cmdController);
        this.getCommand("twitch").setTabCompleter(cmdController);

        Bukkit.getConsoleSender().sendMessage("ChatPointsTTV enabled!");
        for (Player p: plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.spigot().sendMessage(new TextComponent("ChatPointsTTV reloaded!"));
            }
        }
        VersionCheck.check();

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (twitch != null && !TwitchClient.accountConnected && player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) {
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
        TwitchEventHandler.rewardBold = null;

        HandlerList.unregisterAll(this);
    }

}
