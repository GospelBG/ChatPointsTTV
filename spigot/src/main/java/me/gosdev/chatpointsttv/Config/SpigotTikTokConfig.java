package me.gosdev.chatpointsttv.Config;

import java.util.List;

import me.gosdev.chatpointsttv.AlertMode;
import me.gosdev.chatpointsttv.Generic.GenericTikTokConfig;

public class SpigotTikTokConfig extends SpigotConfigFile implements GenericTikTokConfig {
    public SpigotTikTokConfig(String fileName) {
        super(fileName);
    }

    @Override public boolean getFollowSpamProtection() {
        return getBoolean("FOLLOW_SPAM_PROTECTION", true);
    }

    @Override public void setFollowSpamProtection(boolean followSpamProtection) {
        set("FOLLOW_SPAM_PROTECTION", followSpamProtection);
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

    @Override public boolean hasEulerstreamApiKey() {
        return isString("EULERSTREAM_API_KEY");
    }

    @Override public String getEulerstreamApiKey() {
        return getString("EULERSTREAM_API_KEY");
    }

    @Override public void setEulerstreamApiKey(String eulerstreamApiKey) {
        set("EULERSTREAM_API_KEY", eulerstreamApiKey);
    }
}
