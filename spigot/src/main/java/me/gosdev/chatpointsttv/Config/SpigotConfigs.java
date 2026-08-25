package me.gosdev.chatpointsttv.Config;

import me.gosdev.chatpointsttv.Generic.ConfigFile;
import me.gosdev.chatpointsttv.Generic.GenericConfig;

import me.gosdev.chatpointsttv.Generic.GenericGeneralConfig;
import me.gosdev.chatpointsttv.Generic.GenericTwitchConfig;
import me.gosdev.chatpointsttv.Generic.GenericTikTokConfig;

public class SpigotConfigs implements GenericConfig {
    private final SpigotGeneralConfig generalConfig;
    private final SpigotTwitchConfig twitchConfig;
    private final SpigotTikTokConfig tiktokConfig;
    private final SpigotConfigFile localesConfig;
    private final SpigotConfigFile followerLog;
    private final SpigotConfigFile accounts;

    public SpigotConfigs() {
        this.generalConfig = new SpigotGeneralConfig("config.yml");
        this.twitchConfig = new SpigotTwitchConfig("twitch.yml");
        this.tiktokConfig = new SpigotTikTokConfig("tiktok.yml");
        this.localesConfig = new SpigotConfigFile("locales.yml");
        this.followerLog = new SpigotConfigFile("followers");
        this.accounts = new SpigotConfigFile("accounts");
    }

    @Override
    public GenericGeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    @Override
    public GenericTwitchConfig getTwitchConfig() {
        return twitchConfig;
    }

    @Override
    public GenericTikTokConfig getTikTokConfig() {
        return tiktokConfig;
    }

    @Override
    public ConfigFile getTwitchEventsConfig() {
        return twitchConfig;
    }

    @Override
    public ConfigFile getTikTokEventsConfig() {
        return tiktokConfig;
    }

    @Override
    public ConfigFile getLocalesConfig() {
        return localesConfig;
    }

    @Override
    public ConfigFile getAccounts() {
        return accounts;
    }

    @Override
    public ConfigFile getFollowerLog() {
        return followerLog;
    }

    @Override
    public void reload() {
        generalConfig.reload();
        twitchConfig.reload();
        tiktokConfig.reload();
        localesConfig.reload();
        accounts.reload();
        followerLog.reload();
    }
}
