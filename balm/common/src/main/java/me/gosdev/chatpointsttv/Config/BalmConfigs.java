package me.gosdev.chatpointsttv.Config;

import me.gosdev.chatpointsttv.Config.Adapters.BalmGeneralConfig;
import me.gosdev.chatpointsttv.Config.Adapters.BalmTikTokConfig;
import me.gosdev.chatpointsttv.Config.Adapters.BalmTwitchConfig;
import me.gosdev.chatpointsttv.Generic.GenericGeneralConfig;
import me.gosdev.chatpointsttv.Generic.GenericTwitchConfig;
import me.gosdev.chatpointsttv.Generic.GenericTikTokConfig;
import me.gosdev.chatpointsttv.Generic.ConfigFile;
import me.gosdev.chatpointsttv.Generic.GenericConfig;

public class BalmConfigs implements GenericConfig {
    private BalmGeneralConfig generalConfig = new BalmGeneralConfig();
    private BalmTwitchConfig twitchConfig = new BalmTwitchConfig();
    private BalmTikTokConfig tiktokConfig = new BalmTikTokConfig();

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

    private ConfigFile localesConfig = new BalmJsonConfigFile("locales.json");
    private ConfigFile accountsConfig = new BalmJsonConfigFile("accounts.json");
    private ConfigFile followerLogConfig = new BalmJsonConfigFile("followers.json");

    private ConfigFile twitchEventsConfig = new BalmJsonConfigFile("twitch_events.json");
    private ConfigFile tiktokEventsConfig = new BalmJsonConfigFile("tiktok_events.json");
    
    @Override
    public ConfigFile getTwitchEventsConfig() {
        return twitchEventsConfig;
    }

    @Override
    public ConfigFile getTikTokEventsConfig() {
        return tiktokEventsConfig;
    }

    @Override
    public ConfigFile getLocalesConfig() {
        return localesConfig;
    }

    @Override
    public ConfigFile getAccounts() {
        return accountsConfig;
    }

    @Override
    public ConfigFile getFollowerLog() {
        return followerLogConfig;
    }

    @Override
    public void reload() {

    }
}
