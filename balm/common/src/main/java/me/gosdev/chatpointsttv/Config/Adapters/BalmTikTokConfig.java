package me.gosdev.chatpointsttv.Config.Adapters;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Config.TikTokConfig;
import me.gosdev.chatpointsttv.Generic.GenericTikTokConfig;
import net.blay09.mods.balm.Balm;

import java.util.List;

public class BalmTikTokConfig implements GenericTikTokConfig {
    private final TikTokConfig config =  Balm.config().getActiveConfig(TikTokConfig.class);

    @Override
    public String getEulerstreamApiKey() {
        return config.eulerstreamApiKey;
    }

    @Override
    public void setEulerstreamApiKey(String eulerstreamApiKey) {
        Balm.config().updateLocalConfig(TikTokConfig.class, config -> {
            config.eulerstreamApiKey = eulerstreamApiKey;
        });
    }

    @Override
    public boolean hasEulerstreamApiKey() {
        return config.eulerstreamApiKey != null && !config.eulerstreamApiKey.isEmpty();
    }

    @Override
    public boolean getFollowSpamProtection() {
        return config.followSpamProtection;
    }

    @Override
    public void setFollowSpamProtection(boolean followSpamProtection) {
        Balm.config().updateLocalConfig(TikTokConfig.class, config -> {
            config.followSpamProtection = followSpamProtection;
        });
    }

    @Override
    public List<String> getChatBlacklist() {
        return config.chatBlacklist;
    }

    @Override
    public void setChatBlacklist(List<String> chatBlacklist) {
        Balm.config().updateLocalConfig(TikTokConfig.class, config -> {
            config.chatBlacklist = chatBlacklist;
        });
    }

    @Override
    public boolean getMobGlow(boolean def) {
        return config.overridenConfig.mobGlow;
    }

    @Override
    public void setMobGlow(boolean mobGlow) {
        Balm.config().updateLocalConfig(TikTokConfig.class, config -> {
            config.overridenConfig.mobGlow = mobGlow;
        });
    }

    @Override
    public boolean getDisplayNameOnMob(boolean def) {
        return config.overridenConfig.displayNameOnMob;
    }

    @Override
    public void setDisplayNameOnMob(boolean displayNameOnMob) {
        Balm.config().updateLocalConfig(TikTokConfig.class, config -> {
            config.overridenConfig.displayNameOnMob = displayNameOnMob;
        });
    }

    @Override
    public AlertMode getIngameAlerts(AlertMode def) {
        return AlertMode.valueOf(config.overridenConfig.ingameAlerts);
    }

    @Override
    public void setIngameAlerts(AlertMode ingameAlerts) {
        Balm.config().updateLocalConfig(TikTokConfig.class, config -> {
            config.overridenConfig.ingameAlerts = ingameAlerts.name();
        });
    }
}
