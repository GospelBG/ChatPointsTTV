package me.gosdev.chatpointsttv.EventActions;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class DeleteItemsAction extends Action {
    public static enum Type {
        ALL,
        RANDOM,
        HAND
    }
    
    private final Type type;
    private final Player target;

    public DeleteItemsAction(Type type, Player target) {
        this.type = type;
        this.target = target;
    }

    @Override
    public void run() {
         for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            switch (type) {
                case ALL:
                    for (ItemStack item : p.getInventory().getContents()) {
                        Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> p.getInventory().remove(item));
                    }                    
                    break;

                case HAND:
                    Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> p.getInventory().removeItem(p.getInventory().getItemInMainHand()));
                    Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () ->p.getInventory().removeItem(p.getInventory().getItemInOffHand()));
                    break;

                case RANDOM:
                    ArrayList<Integer> populatedSlots = new ArrayList<>();
                    for (int i = 0; i < p.getInventory().getSize(); i++) {
                        if (p.getInventory().getContents()[i] != null) {
                            populatedSlots.add(i);
                        }
                    }

                    if (populatedSlots.isEmpty()) return; // Empty inventory

                    int deletedItem = populatedSlots.get(new Random().nextInt(populatedSlots.size()));
                    p.getInventory().setItem(deletedItem, null);
                    break;
            }
        }
    }
}
