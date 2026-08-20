package me.gosdev.chatpointsttv.Actions;

import java.util.Optional;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class GiveAction extends BaseAction {
    private String item;
    private int amount;
    private GenericPlayer target;

    public GiveAction(String item, Optional<Integer> amount, Optional<GenericPlayer> target) {
        this.item = item;
        this.amount = amount.orElse(1);
        this.target = target.orElse(null);
    }

    @Override
    public void run() {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET)) continue;

            p.giveItem(item, amount);
        }
    }
}