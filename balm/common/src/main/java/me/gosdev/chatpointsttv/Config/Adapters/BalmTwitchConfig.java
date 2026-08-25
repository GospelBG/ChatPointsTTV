package me.gosdev.chatpointsttv.Config.Adapters;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Config.TwitchConfig;
import me.gosdev.chatpointsttv.Generic.GenericTwitchConfig;
import net.blay09.mods.balm.Balm;

import java.util.List;

public class BalmTwitchConfig implements GenericTwitchConfig {
    private final TwitchConfig config = Balm.config().getActiveConfig(TwitchConfig.class);

    @Override
    public boolean getManageChannelPointRewards() {
        return config.manageChannelPointRewards;
    }

    @Override
    public void setManageChannelPointRewards(boolean manageChannelPointRewards) {
        Balm.config().updateLocalConfig(TwitchConfig.class, config -> {
            config.manageChannelPointRewards = manageChannelPointRewards;
        });
    }

    @Override
    public boolean getFollowSpamProtection() {
        return config.followSpamProtection;
    }

    @Override
    public void setFollowSpamProtection(boolean followSpamProtection) {
        Balm.config().updateLocalConfig(TwitchConfig.class, config -> {
            config.followSpamProtection = followSpamProtection;
        });
    }

    @Override
    public List<String> getChatBlacklist() {
        return config.chatBlacklist;
    }

    @Override
    public void setChatBlacklist(List<String> chatBlacklist) {
        Balm.config().updateLocalConfig(TwitchConfig.class, config -> {
            config.chatBlacklist = chatBlacklist;
        });
    }

    @Override
    public boolean getMobGlow(boolean def) {
        return config.overridenConfig.mobGlow;
    }

    @Override
    public void setMobGlow(boolean mobGlow) {
        Balm.config().updateLocalConfig(TwitchConfig.class, config -> {
            config.overridenConfig.mobGlow = mobGlow;
        });
    }

    @Override
    public boolean getDisplayNameOnMob(boolean def) {
        return config.overridenConfig.displayNameOnMob;
    }

    @Override
    public void setDisplayNameOnMob(boolean displayNameOnMob) {
        Balm.config().updateLocalConfig(TwitchConfig.class, config -> {
            config.overridenConfig.displayNameOnMob = displayNameOnMob;
        });
    }

    @Override
    public AlertMode getIngameAlerts(AlertMode def) {
        return AlertMode.valueOf(config.overridenConfig.ingameAlerts);
    }

    @Override
    public void setIngameAlerts(AlertMode ingameAlerts) {
        Balm.config().updateLocalConfig(TwitchConfig.class, config -> {
            config.overridenConfig.ingameAlerts = ingameAlerts.name();
        });
    }
}
