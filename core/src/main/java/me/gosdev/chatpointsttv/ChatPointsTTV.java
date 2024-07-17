package me.gosdev.chatpointsttv;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import me.gosdev.chatpointsttv.Rewards.Rewards;
import me.gosdev.chatpointsttv.Rewards.Reward;
import me.gosdev.chatpointsttv.Rewards.Rewards.rewardType;
import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Twitch.auth.ImplicitGrantFlow;
import me.gosdev.chatpointsttv.Utils.Utils;

public class ChatPointsTTV extends JavaPlugin {
    private static ChatPointsTTV plugin;
    private CommandController cmdController;

    private static Map<String, ChatColor> colors = new HashMap<String, org.bukkit.ChatColor>();
    private static Map<String, String> titleStrings = new HashMap<String, String>();
    
    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;
    public List<String> chatBlacklist;
    public static boolean configOk = true;
    public static Boolean twitchCustomCredentials = false;
    public Boolean rewardBold;

    public Logger log = getLogger();
    public FileConfiguration config;

    public static TwitchClient Twitch = new TwitchClient();
    public static TikTokClient Tiktok = new TikTokClient();  

    public static enum permissions {
        BROADCAST("chatpointsttv.broadcast"),
        MANAGE("chatpointsttv.manage"),
        TARGET("chatpointsttv.target");

        public final String permission_id;

        private permissions(String label) {
            this.permission_id = label;
        }
    }

    public static enum platforms {
        TWITCH,
        TIKTOK
    }

    public static ChatPointsTTV getPlugin() {
        return plugin;
    }

    public static Map<String, org.bukkit.ChatColor> getChatColors() {
        return colors;
    }
    public static Map<String, String> getRedemptionStrings() {
        return titleStrings;
    }
    

    private static Utils utils;

    public static Utils getUtils() {
        if (utils != null) return  utils;
        final Pattern pattern = Pattern.compile("1\\.\\d\\d?");
        final Matcher matcher = pattern.matcher(Bukkit.getVersion());
        matcher.find();
        int version = Integer.parseInt(matcher.group().split("\\.")[1]);
        try {
            if (version >= 12) { 
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_12_R1").getDeclaredConstructor().newInstance();
            } else {
                utils = (Utils) Class.forName(ChatPointsTTV.class.getPackage().getName() + ".Utils.Utils_1_9_R1").getDeclaredConstructor().newInstance();
            }
            return utils;    
        } catch (Exception e) {
            plugin.log.warning(e.toString());
            return null;
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        PluginManager pm = Bukkit.getServer().getPluginManager();

        utils = getUtils();

        try {
            // Get the latest config after saving the default if missing
            this.saveDefaultConfig();
            config = getConfig();

            if (config.getString("TWITCH_CHANNEL_USERNAME") == null | config.getString("TWITCH_CHANNEL_USERNAME").startsWith("MemorySection[path=")) { // Invalid string (probably left default "{YOUR CHANNEL}")
                throw new Exception("Cannot read channel. Config file may be not set up or invalid.");
            } else {
                configOk = true;
            }
        } catch (Exception e) {
            configOk = false;
            log.warning(e.toString());
        }


        config.getConfigurationSection("COLORS").getKeys(false).forEach(i -> {
            colors.put(i, org.bukkit.ChatColor.valueOf(config.getConfigurationSection("COLORS").getString(i)));
        });

        config.getConfigurationSection("STRINGS").getKeys(true).forEach(i -> {
            titleStrings.put(i, config.getConfigurationSection("STRINGS").getString(i));
        });

        rewardBold = config.getBoolean("REWARD_NAME_BOLD");

        shouldMobsGlow = config.getBoolean("MOB_GLOW");
        nameSpawnedMobs = config.getBoolean("DISPLAY_NAME_ON_MOB");
        chatBlacklist = config.getStringList("CHAT_BLACKLIST");

        cmdController = new CommandController();
        this.getCommand("twitch").setExecutor(cmdController);
        this.getCommand("twitch").setTabCompleter(cmdController);

        utils.sendMessage(Bukkit.getConsoleSender(), "ChatPointsTTV enabled!");
        for (Player p: plugin.getServer().getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.sendMessage("ChatPointsTTV reloaded!");
            }
        }
        VersionCheck.check();


        if (config.getString("TWITCH_CLIENT_ID") != null || config.getString("CUSTOM_CLIENT_SECRET") != null) twitchCustomCredentials = true;
        if(twitchCustomCredentials && config.getBoolean("TWITCH_AUTO_LINK", false) == true) {
            Twitch.link(Bukkit.getConsoleSender(), plugin.config.getString("TWITCH_ACCESS_TOKEN"));
        }

        pm.registerEvents(new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent player) {
                if (!Twitch.isAccountConnected() && /*!Tiktok.isAccountConexted() */player.getPlayer().hasPermission(permissions.MANAGE.permission_id)) {
                    String msg = "Welcome! Remember to log in with your Twitch account for ChatPointsTTV to be able to connect and listen.\n";
                    BaseComponent btn = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "[Click here to login]").create()[0];

                    btn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to run command").create()));
                    btn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch link"));

                    utils.sendMessage(player.getPlayer(), new BaseComponent[] {new ComponentBuilder(msg).create()[0], btn});
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {      
        ImplicitGrantFlow.server.stop();
    
        config = null;

        Rewards.rewards = new HashMap<rewardType,ArrayList<Reward>>();
        rewardBold = null;
    }
}