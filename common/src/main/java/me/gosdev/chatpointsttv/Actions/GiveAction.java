package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class GiveAction implements  BaseAction {
    private String item;
    private int amount;
    private String target;

    public static final String ACTION_NAME = "GIVE";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    public GiveAction(String item, Integer amount, String target) {
        this.item = item;
        this.amount = amount;
        this.target = target;
    }

    @Override
    public void run(EventInformation ei) {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(ChatPointsTTV.getLoader().getPlayer(target))) {
                    continue;
                }
            } else if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;

            p.giveItem(item, amount);
        }
    }
}