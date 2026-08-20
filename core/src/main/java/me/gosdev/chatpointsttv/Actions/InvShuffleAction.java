package me.gosdev.chatpointsttv.Actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.ChatPointsTTV;

public class InvShuffleAction extends BaseAction {
    GenericPlayer target;
    public InvShuffleAction(GenericPlayer target) {
        this.target = target;
    }

    @Override
    public void run() {
        if (target == null) {
            for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;
                shuffle(p);
            }
        } else {
            shuffle(target);
        }
    }

    private void shuffle(GenericPlayer p) {
        java.util.Random rnd = new java.util.Random();
        int slots = p.getInvSlots();
        for (int i = slots - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            p.exchangeSlots(i, index);
        }
    }
}
