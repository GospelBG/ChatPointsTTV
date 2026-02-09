package me.gosdev.chatpointsttv.Generic;

import java.util.List;

public interface GenericLoader {
    String getVersion();
    
    List<GenericPlayer> getOnlinePlayers();

    GenericSender consoleSender();

    GenericPlayer getPlayer(String username);

    List<String> getPotionEffects();
}
