package me.gosdev.chatpointsttv.Config;

import me.gosdev.chatpointsttv.Generic.ConfigFile;
import me.gosdev.chatpointsttv.Generic.GenericConfig;

public class SpigotConfig implements GenericConfig {
    private final SpigotConfigFile generalConfig;
    private final SpigotConfigFile twitchConfig;
    private final SpigotConfigFile tiktokConfig;
    private final SpigotConfigFile localesConfig;
    private final SpigotConfigFile followerLog;
    private final SpigotConfigFile accounts;

    public SpigotConfig() {
        this.generalConfig = new SpigotConfigFile("config.yml");
        this.twitchConfig = new SpigotConfigFile("twitch.yml");
        this.tiktokConfig = new SpigotConfigFile("tiktok.yml");
        this.localesConfig = new SpigotConfigFile("locales.yml");
        this.followerLog = new SpigotConfigFile("followers");
        this.accounts = new SpigotConfigFile("accounts");
    }

    @Override
    public ConfigFile getGeneralConfig() {
        return generalConfig;
    }

    @Override
    public ConfigFile getTwitchConfig() {
        return twitchConfig;
    }

    @Override
    public ConfigFile getTikTokConfig() {
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
