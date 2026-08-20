package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class FreezeAction extends BaseAction {
    GenericPlayer target;
    Integer time;

    public FreezeAction(GenericPlayer target, Integer time) {
        this.target = target;
        this.time = time;
    }

    @Override
    public void run() {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET)) continue;
            
            p.freeze(time);
        }
    }
}
