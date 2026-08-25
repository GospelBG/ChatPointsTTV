package me.gosdev.chatpointsttv.Config;

import java.util.List;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Generic.GenericTwitchConfig;

public class SpigotTwitchConfig extends SpigotConfigFile implements GenericTwitchConfig {
    public SpigotTwitchConfig(String fileName) {
        super(fileName);
    }

    @Override public List<String> getChatBlacklist() {
        return getStringList("CHAT_BLACKLIST");
    }

    @Override public void setChatBlacklist(List<String> chatBlacklist) {
        set("CHAT_BLACKLIST", chatBlacklist);
    }

    @Override public boolean getMobGlow(boolean def) {
        return getBoolean("MOB_GLOW", def);
    }
    @Override public void setMobGlow(boolean mobGlow) {
        set("MOB_GLOW", mobGlow);
    }

    @Override public boolean getDisplayNameOnMob(boolean def) {
        return getBoolean("DISPLAY_NAME_ON_MOB", def);
    }

    @Override public void setDisplayNameOnMob(boolean displayNameOnMob) {
        set("DISPLAY_NAME_ON_MOB", displayNameOnMob);
    }

    @Override public AlertMode getIngameAlerts(AlertMode def) {
        return AlertMode.valueOf(getString("INGAME_ALERTS", def.name()));
    }

    @Override public void setIngameAlerts(AlertMode ingameAlerts) {
        set("INGAME_ALERTS", ingameAlerts.name());
    }

    @Override public boolean getFollowSpamProtection() {
        return getBoolean("FOLLOW_SPAM_PROTECTION", true);
    }

    @Override public void setFollowSpamProtection(boolean followSpamProtection) {
        set("FOLLOW_SPAM_PROTECTION", followSpamProtection);
    }

    @Override public boolean getManageChannelPointRewards() {
        return getBoolean("MANAGE_CHANNEL_POINT_REWARDS", true);
    }

    @Override public void setManageChannelPointRewards(boolean manageChannelPointRewards) {
        set("MANAGE_CHANNEL_POINT_REWARDS", manageChannelPointRewards);
    }
}
