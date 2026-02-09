package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class SoundAction implements BaseAction {
    public static final String ACTION_NAME = "SOUND";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    String target;
    String sound;

    public SoundAction(String target, String sound) {
        this.target = target;
        this.sound = sound;
    }

    @Override
    public void run(EventInformation ei) {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(ChatPointsTTV.getLoader().getPlayer(target))) {
                    continue;
                }
            } else if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;
        
            p.playSound(sound);
        }
    }
}
