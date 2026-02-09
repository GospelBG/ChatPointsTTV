package me.gosdev.chatpointsttv;

import java.util.HashMap;

import me.gosdev.chatpointsttv.Config.AccountsConfig;
import me.gosdev.chatpointsttv.Generic.GenericLoader;
import me.gosdev.chatpointsttv.Generic.GenericSender;
import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Utils.ChatColor;
import me.gosdev.chatpointsttv.Utils.FollowerLog;

public class ChatPointsTTV {
    public static final String PREFIX = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[ChatPointsTTV] " + ChatColor.WHITE + "" + ChatColor.RESET;
    private static GenericSender console;
    private static GenericLoader loader;

    private static TwitchClient twitch;
    private static TikTokClient tiktok;
    private static AccountsConfig accounts;

    public static Boolean shouldMobsGlow;
    public static Boolean nameSpawnedMobs;
    public static AlertMode alertMode;

    public static HashMap<String, String> locales;
    public static Boolean logEvents;

    private static ConfigFile config;
    private static ConfigFile twitch_config;
    private static ConfigFile tiktok_config;
    public static CPTTV_Log log;

    public static enum permissions {
        BROADCAST("chatpointsttv.broadcast"),
        MANAGE("chatpointsttv.manage"),
        TARGET("chatpointsttv.target");

        public final String permission_id;

        private permissions(String label) {
            this.permission_id = label;
        }
    }


    public static GenericSender getConsole() {
        return console;
    }

    public static TwitchClient getTwitch() {
        return twitch;
    }
    public static TikTokClient getTikTok() {
        return tiktok;
    }
    public static ConfigFile getConfig() {
        return config;
    }

    public static AccountsConfig getAccounts() {
        return accounts;
    }

    public static GenericLoader getLoader() {
        return loader;
    }

    public static void enableTwitch() {
        if (twitch == null || !twitch.isStarted()) twitch = new TwitchClient(console, twitch_config);
    }
    
    public static void enableTikTok() {
        if (tiktok == null || !tiktok.isStarted()) tiktok = new TikTokClient(console, tiktok_config);
    }

    public static void enable(GenericLoader genericLoader, ConfigFile accountsFile, ConfigFile cpttvCfg, ConfigFile twitchCfg, ConfigFile TikTokCfg) {
        loader = genericLoader;
        console = loader.consoleSender();
        accounts = new AccountsConfig(accountsFile);
        config = cpttvCfg;
        twitch_config = twitchCfg;
        tiktok_config = TikTokCfg;

        ChatPointsTTV.logEvents = config.getBoolean("LOG_EVENTS", false);
        ChatPointsTTV.shouldMobsGlow = config.getBoolean("MOB_GLOW", false);
        ChatPointsTTV.alertMode = AlertMode.valueOf(config.getString("INGAME_ALERTS", "NONE").toUpperCase());
        ChatPointsTTV.nameSpawnedMobs = config.getBoolean("DISPLAY_NAME_ON_MOB", true);


        if (config.getBoolean("ENABLE_TWITCH", true)) enableTwitch();  
        if (config.getBoolean("ENABLE_TIKTOK", true)) enableTikTok(); 

        if (!VersionCheck.check()) {
            log.info(ChatColor.YELLOW + "ChatPointsTTV " + VersionCheck.latestVersion + " has been released! Download the latest version at " + VersionCheck.download_url);
        }
    }

    public static void disable() {
        if (twitch != null) twitch.stop(console);
        if (tiktok != null) tiktok.stop(console);
        FollowerLog.stop();

        try {
            twitch.stopThread.join();
            tiktok.stopThread.join();
        } catch (InterruptedException ex) {
        }
    }
}