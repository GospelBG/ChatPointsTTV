package me.gosdev.chatpointsttv.EventActions;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class GiveAction extends Action {
    private final Material item;
    private final int amount;
    private final Player target;

    public GiveAction(Material item, Optional<Integer> amount, Optional<Player> target) {
        this.item = item;
        this.amount = amount.orElse(1);
        this.target = target.orElse(null);
    }

    @Override
    public void run() {
        for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            ItemStack itemStack = new ItemStack(item, amount);
            p.getInventory().addItem(itemStack);
        }
    }
}