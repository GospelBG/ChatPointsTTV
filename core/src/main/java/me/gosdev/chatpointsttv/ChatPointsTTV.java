package me.gosdev.chatpointsttv;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import me.gosdev.chatpointsttv.Generic.*;
import me.gosdev.chatpointsttv.Utils.ChatColor;

import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Utils.FollowerLog;
import me.gosdev.chatpointsttv.Utils.Translatable;


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
            this.consoleSender.sendMessage(msgPrefix + Translatable.getString("generic.message.welcome.config_files", "\n", "https://gosdev.me/chatpointsttv/install"));
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

    public static GenericGeneralConfig getPluginConfig() {
        return instance != null && instance.config != null ? instance.config.getGeneralConfig() : null;
    }

    public void enableTwitch(GenericSender p) {
        if (this.twitch == null || !this.twitch.isStarted()) this.twitch = new TwitchClient(p);
    }
    
    public void enableTikTok(GenericSender p) {
        if (this.tiktok == null || !this.tiktok.isStarted()) this.tiktok = new TikTokClient(p);
    }

    public void onEnable() {
        this.config.reload();
        Translatable.loadTranslationsFile("en_us"); //TODO: Change hard-coded value

        this.logEvents = this.config.getGeneralConfig().getLogEvents();
        this.shouldMobsGlow = this.config.getGeneralConfig().getMobGlow();
        this.alertMode = this.config.getGeneralConfig().getIngameAlerts();
        this.nameSpawnedMobs = this.config.getGeneralConfig().getDisplayNameOnMob();

        if (this.config.getGeneralConfig().getEnableTwitch()) enableTwitch(this.consoleSender);
        if (this.config.getGeneralConfig().getEnableTikTok()) enableTikTok(this.consoleSender);

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
            p.sendMessage(ChatColor.RED + Translatable.getString("generic.message.already_reloading", "ChatPointsTTV"));
            return;
        }

        if (!p.equals(consoleSender)) p.sendMessage(msgPrefix + Translatable.getString("generic.message.reloading"));
        log.info("Reloading ChatPointsTTV...");

        onDisable();
        config.reload();
        try {
            onEnable();
        } catch (Exception e) {
            p.sendMessage(ChatColor.RED + Translatable.getString("generic.message.reload.error"));
            e.printStackTrace();
        }
        
        isReloading.set(false);
    }

}
