package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class SpawnAction implements BaseAction {
    public static final String ACTION_NAME = "SPAWN";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    private final String entity;
    private final String target;
    private final Integer amount;

    public SpawnAction(String entity, String target, Integer amount) {
        this.entity = entity;
        this.target = target;
        this.amount = amount;
    }

    @Override 
    public void run(EventInformation ei) {
        Boolean shouldGlow;
        Boolean nameSpawnedMobs;

        switch (ei.getPlatform()) {
            case TWITCH:
                shouldGlow = ChatPointsTTV.getTwitch().shouldMobsGlow;
                nameSpawnedMobs = ChatPointsTTV.getTwitch().nameSpawnedMobs;
                break;
            case TIKTOK:
                shouldGlow = ChatPointsTTV.getTikTok().shouldMobsGlow;
                nameSpawnedMobs = ChatPointsTTV.getTikTok().nameSpawnedMobs;
                break;
            default:
                shouldGlow = ChatPointsTTV.shouldMobsGlow;
                nameSpawnedMobs = ChatPointsTTV.nameSpawnedMobs;
                break;
        }
        for (int i = 0; i < amount; i++) {
            for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                if (target != null) { // Is targeting a specific player?
                    if (!ChatPointsTTV.getLoader().getPlayer(target).isOnline()) {
                        ChatPointsTTV.log.warn("Couldn't find player " + target + ".");
                        return;
                    }
                } else if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;
                
                p.spawnEntity(entity, nameSpawnedMobs ? ei.getChatter() : null, shouldGlow);
            }     
        }
    }
}