package me.gosdev.chatpointsttv.Generic;

public interface GenericConfig {
    ConfigFile getGeneralConfig();

    ConfigFile getTwitchConfig();

    ConfigFile getTikTokConfig();

    ConfigFile getLocalesConfig();

    ConfigFile getAccounts();

    ConfigFile getFollowerLog();

    void reload();
}
