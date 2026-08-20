package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class SoundAction extends BaseAction {
    GenericPlayer target;
    String sound;

    public SoundAction(GenericPlayer target, String sound) {
        this.target = target;
        this.sound = sound;
    }

    @Override
    public void run() {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET)) continue;
        
            p.playSound(sound);
        }

    }
}
