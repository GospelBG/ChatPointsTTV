package me.gosdev.chatpointsttv.Generic;

import me.gosdev.chatpointsttv.AlertMode;

import java.util.List;

public interface GenericTwitchConfig {
    List<String> getChatBlacklist();
    void setChatBlacklist(List<String> chatBlacklist);
    
    boolean getMobGlow(boolean def);
    void setMobGlow(boolean mobGlow);
    
    boolean getDisplayNameOnMob(boolean def);
    void setDisplayNameOnMob(boolean displayNameOnMob);
    
    AlertMode getIngameAlerts(AlertMode def);
    void setIngameAlerts(AlertMode ingameAlerts);
    
    boolean getFollowSpamProtection();
    void setFollowSpamProtection(boolean followSpamProtection);
    
    boolean getManageChannelPointRewards();
    void setManageChannelPointRewards(boolean manageChannelPointRewards);
}
