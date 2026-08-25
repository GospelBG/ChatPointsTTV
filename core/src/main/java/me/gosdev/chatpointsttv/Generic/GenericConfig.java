package me.gosdev.chatpointsttv.Generic;

public interface GenericConfig {
    GenericGeneralConfig getGeneralConfig();

    GenericTwitchConfig getTwitchConfig();

    GenericTikTokConfig getTikTokConfig();

    ConfigFile getTwitchEventsConfig();
    
    ConfigFile getTikTokEventsConfig();

    ConfigFile getLocalesConfig();

    ConfigFile getAccounts();

    ConfigFile getFollowerLog();

    void reload();
}
