package me.gosdev.chatpointsttv.EventActions;

import java.util.ArrayList;
import java.util.Arrays;
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
                    ArrayList<ItemStack> validItems = new ArrayList<>();

                    // Add main inventory items
                    for (ItemStack item : p.getInventory().getContents()) {
                        if (item != null) {
                            validItems.add(item);
                        }
                    }

                    // Add armor items
                    for (ItemStack armorItem : p.getInventory().getArmorContents()) {
                        if (armorItem != null) {
                            validItems.add(armorItem);
                        }
                    }

                    // Add offhand item
                    ItemStack offHandItem = p.getInventory().getItemInOffHand();
                    if (offHandItem != null) {
                        validItems.add(offHandItem);
                    }

                    if (!validItems.isEmpty()) {
                        Random random = new Random();
                        ItemStack randomItem = validItems.get(random.nextInt(validItems.size()));
                        Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> {
                            if (randomItem.equals(p.getInventory().getItemInOffHand())) {
                                p.getInventory().setItemInOffHand(null);
                            } else {
                                ItemStack[] newArmor = new ItemStack[4];
                                for (int i = 0; i >= 4; i++) {
                                    if (randomItem.equals(p.getInventory().getArmorContents()[i])) { // Always true (TODO)
                                        ChatPointsTTV.log.info(randomItem.toString() + " = " + p.getInventory().getArmorContents()[i].toString());
                                        newArmor[i] = null;
                                    } else {
                                        newArmor[i] = p.getInventory().getArmorContents()[i];
                                    }
                                }

                                if (!Arrays.equals(p.getInventory().getArmorContents(), newArmor)) { // Always true (TODO)
                                    p.getInventory().setArmorContents(newArmor);
                                    return;
                                }
                                
                                ItemStack[] armorContents = p.getInventory().getArmorContents();
                                for (int i = 0; i < armorContents.length; i++) {
                                    if (randomItem.equals(armorContents[i])) {
                                        armorContents[i] = null;
                                        break;
                                    }
                                }
                                p.getInventory().setArmorContents(armorContents);
                            }
                        });
                    }
                    break;
            }
        }
    }
}
