package me.gosdev.chatpointsttv.Config.Adapters;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Config.GeneralConfig;
import me.gosdev.chatpointsttv.Generic.GenericGeneralConfig;
import net.blay09.mods.balm.Balm;

import java.io.IOException;
import java.util.List;

public class BalmGeneralConfig implements GenericGeneralConfig {
    GeneralConfig config = Balm.config().getActiveConfig(GeneralConfig.class);

    @Override
    public boolean getHideLoginCodes() {
        return config.hideLoginCodes;
    }

    @Override
    public void setHideLoginCodes(boolean hideLoginCodes) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.hideLoginCodes = hideLoginCodes;
        });
    }

    @Override
    public boolean getLogEvents() {
        return config.logEvents;
    }

    @Override
    public boolean getMobGlow() {
        return config.overridenConfig.mobGlow;
    }

    @Override
    public AlertMode getIngameAlerts() {
        return config.overridenConfig.ingameAlerts.toAlertMode();
    }

    @Override
    public boolean getDisplayNameOnMob() {
        return config.overridenConfig.displayNameOnMob;
    }

    @Override
    public boolean getEnableTwitch() {
        return config.enableTwitch;
    }

    @Override
    public boolean getEnableTikTok() {
        return config.enableTikTok;
    }

    @Override
    public boolean getIgnoreOfflineStreamers() {
        return config.ignoreOfflineStreamers;
    }

    @Override
    public boolean getShowChat() {
        return config.showChat;
    }

    @Override
    public void setLogEvents(boolean logEvents) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.logEvents = logEvents;
        });
    }

    @Override
    public void setMobGlow(boolean mobGlow) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.overridenConfig.mobGlow = mobGlow;
        });
    }

    @Override
    public void setIngameAlerts(AlertMode alertMode) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.overridenConfig.ingameAlerts = BalmAlertMode.fromAlertMode(alertMode);
        });
    }

    @Override
    public void setDisplayNameOnMob(boolean displayNameOnMob) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.overridenConfig.displayNameOnMob = displayNameOnMob;
        });
    }

    @Override
    public void setEnableTwitch(boolean enableTwitch) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.enableTwitch = enableTwitch;
        });
    }

    @Override
    public void setEnableTikTok(boolean enableTikTok) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.enableTikTok = enableTikTok;
        });
    }

    @Override
    public void setIgnoreOfflineStreamers(boolean ignoreOfflineStreamers) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.ignoreOfflineStreamers = ignoreOfflineStreamers;
        });
    }

    @Override
    public void setShowChat(boolean showChat) {
        Balm.config().updateLocalConfig(GeneralConfig.class, config -> {
            config.showChat = showChat;
        });
    }

}
