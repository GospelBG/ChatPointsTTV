package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class TntAction implements BaseAction {
    public static final String ACTION_NAME = "TNT";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    private final Integer amount;
    private Integer explosionTime;
    private final String target;

    public TntAction(Integer amount, Integer explosionTime, String target) {
        this.amount = amount;

        if (explosionTime != null) {
            this.explosionTime = explosionTime;
        }
        this.target = target;
    }

    @Override
    public void run(EventInformation ei) {
        for (Integer i = 0; i < amount; i++) {
            if (target == null) {
                for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                    if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;
                    p.spawnTnt(explosionTime);
                }
            } else {
                ChatPointsTTV.getLoader().getPlayer(target).spawnTnt(explosionTime);
            }
        }
    }
}
