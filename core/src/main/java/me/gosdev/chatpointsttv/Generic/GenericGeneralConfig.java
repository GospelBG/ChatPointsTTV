package me.gosdev.chatpointsttv.Generic;

import me.gosdev.chatpointsttv.AlertMode;

public interface GenericGeneralConfig {
    boolean getHideLoginCodes();
    void setHideLoginCodes(boolean hideLoginCodes);

    boolean getLogEvents();
    void setLogEvents(boolean logEvents);
    
    boolean getMobGlow();
    void setMobGlow(boolean mobGlow);
    
    AlertMode getIngameAlerts();
    void setIngameAlerts(AlertMode alertMode);
    
    boolean getDisplayNameOnMob();
    void setDisplayNameOnMob(boolean displayNameOnMob);
    
    boolean getEnableTwitch();
    void setEnableTwitch(boolean enableTwitch);
    
    boolean getEnableTikTok();
    void setEnableTikTok(boolean enableTikTok);
    
    boolean getIgnoreOfflineStreamers();
    void setIgnoreOfflineStreamers(boolean ignoreOfflineStreamers);
    
    boolean getShowChat();
    void setShowChat(boolean showChat);
}
