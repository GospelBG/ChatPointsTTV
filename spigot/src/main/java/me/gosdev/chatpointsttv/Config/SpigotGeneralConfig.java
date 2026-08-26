package me.gosdev.chatpointsttv.Config;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Generic.GenericGeneralConfig;

public class SpigotGeneralConfig extends SpigotConfigFile implements GenericGeneralConfig {
    public SpigotGeneralConfig(String fileName) {
        super(fileName);
    }

    @Override
    public boolean getHideLoginCodes() {
        return getBoolean("HIDE_LOGIN_CODES", false);
    }

    @Override
    public void setHideLoginCodes(boolean hideLoginCodes) {
        set("HIDE_LOGIN_CODES", hideLoginCodes);
    }

    @Override public boolean getLogEvents() { return getBoolean("LOG_EVENTS", false); }
    @Override public void setLogEvents(boolean logEvents) { set("LOG_EVENTS", logEvents); }

    @Override public boolean getMobGlow() { return getBoolean("MOB_GLOW", false); }
    @Override public void setMobGlow(boolean mobGlow) { set("MOB_GLOW", mobGlow); }

    @Override public AlertMode getIngameAlerts() { return AlertMode.valueOf(getString("INGAME_ALERTS", "NONE").toUpperCase()); }
    @Override public void setIngameAlerts(AlertMode alertMode) { set("INGAME_ALERTS", alertMode.name()); }

    @Override public boolean getDisplayNameOnMob() { return getBoolean("DISPLAY_NAME_ON_MOB", true); }
    @Override public void setDisplayNameOnMob(boolean displayNameOnMob) { set("DISPLAY_NAME_ON_MOB", displayNameOnMob); }

    @Override public boolean getEnableTwitch() { return getBoolean("ENABLE_TWITCH", true); }
    @Override public void setEnableTwitch(boolean enableTwitch) { set("ENABLE_TWITCH", enableTwitch); }

    @Override public boolean getEnableTikTok() { return getBoolean("ENABLE_TIKTOK", true); }
    @Override public void setEnableTikTok(boolean enableTikTok) { set("ENABLE_TIKTOK", enableTikTok); }

    @Override public boolean getIgnoreOfflineStreamers() { return getBoolean("IGNORE_OFFLINE_STREAMERS", false); }
    @Override public void setIgnoreOfflineStreamers(boolean ignoreOfflineStreamers) { set("IGNORE_OFFLINE_STREAMERS", ignoreOfflineStreamers); }

    @Override public boolean getShowChat() { return getBoolean("SHOW_CHAT", true); }
    @Override public void setShowChat(boolean showChat) { set("SHOW_CHAT", showChat); }
}
