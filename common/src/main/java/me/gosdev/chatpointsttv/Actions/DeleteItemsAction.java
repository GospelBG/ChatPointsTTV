package me.gosdev.chatpointsttv.Actions;

import java.util.ArrayList;
import java.util.Random;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class DeleteItemsAction implements BaseAction {
    public static final String ACTION_NAME = "DELETE";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    public static enum Type {
        ALL,
        RANDOM,
        HAND
    }
    
    private final Type type;
    private final String target;

    public DeleteItemsAction(Type type, String target) {
        this.type = type;
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
            
            switch (type) {
                case ALL:
                    for (int i = 0; i < p.getInvSlots(); i++) {
                        p.removeItem(i);
                    }
                    break;

                case HAND:
                    if (p.hasItem(p.getHandSlot())) {
                        p.removeItem(p.getHandSlot());
                    } else {
                        p.removeItem(40); // Offhand
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
        }
    }
}
