package me.gosdev.chatpointsttv.Actions;

import java.util.ArrayList;
import java.util.Random;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class DeleteItemsAction extends BaseAction {
    public static enum Type {
        ALL,
        RANDOM,
        HAND
    }
    
    private final Type type;
    private final GenericPlayer target;

    public DeleteItemsAction(Type type, GenericPlayer target) {
        this.type = type;
        this.target = target;
    }

    @Override
    public void run() {
         for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET)) continue;
            ChatPointsTTV.getLoader().runTask(() -> {
                switch (type) {
                    case ALL:
                        for (int i = 0; i < p.getInvSlots(); i++) {
                            p.removeItem(i);
                        }
                        break;
    
                    case HAND:
                        if (p.getHandSlot() == null) return;
                        if (p.hasItem(p.getHandSlot())) {
                            p.removeItem(p.getHandSlot());
                        } else {
                            // Assuming slot 40 is offhand. Can be abstracted later if needed.
                            p.removeItem(40);
                        }
                        break;
    
                    case RANDOM:
                        ArrayList<Integer> populatedSlots = new ArrayList<>();
                        for (int i = 0; i < p.getInvSlots(); i++) {
                            if (p.hasItem(i)) {
                                populatedSlots.add(i);
                            }
                        }
    
                        if (populatedSlots.isEmpty()) return; // Empty inventory
    
                        int deletedItem = populatedSlots.get(new Random().nextInt(populatedSlots.size()));
                        p.removeItem(deletedItem);
                        break;
                }
    
            });
        }
    }
}
