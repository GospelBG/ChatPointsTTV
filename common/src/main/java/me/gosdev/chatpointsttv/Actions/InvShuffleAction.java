package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class InvShuffleAction implements BaseAction {
    public static final String ACTION_NAME = "SHUFFLE";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    String target;
    public InvShuffleAction(String target) {
        this.target = target;
    }

    @Override
    public void run(EventInformation ei) {
        if (target == null) {
            for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;
                p.shuffleInventory();
            }
        } else {
            ChatPointsTTV.getLoader().getPlayer(target).shuffleInventory();
        }
    }
}
