package me.gosdev.chatpointsttv;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import me.gosdev.chatpointsttv.Generic.*;
import me.gosdev.chatpointsttv.Utils.ChatColor;

import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Utils.FollowerLog;


public class ChatPointsTTV {
    private static ChatPointsTTV instance;

    private final AtomicBoolean isReloading = new AtomicBoolean(false);
    private AccountsManager accounts;
    private TwitchClient twitch;
    private TikTokClient tiktok;
    private boolean firstRun = false;

    public HashMap<String, String> locales;
    public Boolean shouldMobsGlow;
    public Boolean nameSpawnedMobs;
    public AlertMode alertMode;
    public Boolean logEvents;

    private GenericSender consoleSender;
    public static GenericLogger log;
    private GenericLoader loader;
    public GenericConfig config;
    public String version;

    public static final String msgPrefix = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "[ChatPointsTTV] " + ChatColor.WHITE + "" + ChatColor.RESET;

    public enum permissions {
        BROADCAST("chatpointsttv.broadcast"),
        MANAGE("chatpointsttv.manage"),
        TARGET("chatpointsttv.target");

        public final String permission_id;

        private permissions(String label) {
            this.permission_id = label;
        }
    }

    public ChatPointsTTV(GenericLoader loader, GenericConfig config, AccountsManager accountsManager) {
        instance = this;
        this.loader = loader;
        this.config = config;
        ChatPointsTTV.log = loader.getLogger();
        this.accounts = accountsManager;
        
        this.version = loader.getVersion();
        this.consoleSender = loader.consoleSender();

        if (firstRun) {
            this.consoleSender.sendMessage(msgPrefix + "Configuration files have just been created. You need to set up ChatPointsTTV before using it.\nCheck out the quick start guide at https://gosdev.me/chatpointsttv/install");
        }
    }

    public static ChatPointsTTV getInstance() {
        return instance;
    }

    public static GenericLoader getLoader() {
        return instance != null ? instance.loader : null;
    }

    public static GenericSender getConsole() {
        return instance != null ? instance.consoleSender : null;
    }

    public static AccountsManager getAccountsManager() {
        return instance.accounts;
    }

    public Boolean isReloading() {
        return isReloading.get();
    }

    public static TwitchClient getTwitch() {
        return instance != null ? instance.twitch : null;
    }

    public static TikTokClient getTikTok() {
        return instance != null ? instance.tiktok : null;
    }

    public static ConfigFile getPluginConfig() {
        return instance != null && instance.config != null ? instance.config.getGeneralConfig() : null;
    }

    public void enableTwitch(GenericSender p) {
        if (this.twitch == null || !this.twitch.isStarted()) this.twitch = new TwitchClient(p);
    }
    
    public void enableTikTok(GenericSender p) {
        if (this.tiktok == null || !this.tiktok.isStarted()) this.tiktok = new TikTokClient(p);
    }

    public void onEnable() {
        this.logEvents = this.config.getGeneralConfig().getBoolean("LOG_EVENTS", false);
        this.shouldMobsGlow = this.config.getGeneralConfig().getBoolean("MOB_GLOW", false);
        this.alertMode = AlertMode.valueOf(this.config.getGeneralConfig().getString("INGAME_ALERTS", "NONE").toUpperCase());
        this.nameSpawnedMobs = this.config.getGeneralConfig().getBoolean("DISPLAY_NAME_ON_MOB", true);

        if (this.config.getGeneralConfig().getBoolean("ENABLE_TWITCH", true)) enableTwitch(this.consoleSender);
        if (this.config.getGeneralConfig().getBoolean("ENABLE_TIKTOK", true)) enableTikTok(this.consoleSender);

        VersionCheck.check();
    }

    public void onDisable() {
        if (this.twitch != null) this.twitch.stop(this.consoleSender);
        if (this.tiktok != null) this.tiktok.stop(this.consoleSender);
        FollowerLog.stop();

        try {
            if (this.twitch != null && this.twitch.stopThread != null) this.twitch.stopThread.join();
            if (this.tiktok != null && this.tiktok.stopThread != null) this.tiktok.stopThread.join();
        } catch (InterruptedException ex) {
        }
    }

    public void reload(GenericSender p) {
        if (!isReloading.compareAndSet(false, true)) {
            p.sendMessage(ChatColor.RED + "ChatPointsTTV is already reloading!");
            return;
        }

        if (!p.equals(consoleSender)) p.sendMessage(msgPrefix + "Reloading ChatPointsTTV...");
        log.info("Reloading ChatPointsTTV...");

        onDisable();
        config.reload();
        try {
            onEnable();
        } catch (Exception e) {
            p.sendMessage(ChatColor.RED + "There was an error reloading ChatPointsTTV. Please check the server console.");
            e.printStackTrace();
        }
        
        isReloading.set(false);
    }

}
