package me.gosdev.chatpointsttv.Actions;

import java.util.Optional;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class SpawnAction extends BaseAction {
    private String entity;
    private Integer amount;
    private String name;
    private GenericPlayer player;
    private boolean shouldGlow;

    public SpawnAction(String entity, String chatter, Optional<Integer> amount, GenericPlayer target, Boolean shouldGlow) {
        this.entity = entity;
        this.name = chatter;
        this.amount = amount.orElse(1);
        this.player = target;
        this.shouldGlow = shouldGlow;
    }

    @Override 
    public void run() {
        if (!ChatPointsTTV.getLoader().getEntities().contains(entity)) {
           notifyFailure("");
        }

        for (int i = 0; i < amount; i++) {
            for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                if (player != null) { // Is targeting a specific player?
                    if (!player.isOnline()) {
                        ChatPointsTTV.log.warn("Couldn't find player " + player.getName() + ".");
                        return;
                    }
                } else if (!p.hasPermission(permissions.TARGET)) continue;

                ChatPointsTTV.getLoader().spawnEntity(p, entity, shouldGlow, name);
            }     
        }
    }
}