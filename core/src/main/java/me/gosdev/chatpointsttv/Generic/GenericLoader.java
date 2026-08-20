package me.gosdev.chatpointsttv.Generic;

import java.util.List;

public interface GenericLoader {
    String getVersion();

    GenericLogger getLogger();
    
    List<GenericPlayer> getOnlinePlayers();

    GenericSender consoleSender();

    GenericPlayer getPlayer(String username);

    List<String> getSounds();

    List<String> getEntities();

    List<String> getItems();

    List<String> getPotionEffects();

    void runTask(Runnable task);

    int scheduleSyncRepeatingTask(Runnable task, int delay, int period);

    void cancelTask(int taskId);

    void spawnEntity(GenericPlayer target, String entity, boolean shouldGlow, String name);
}
