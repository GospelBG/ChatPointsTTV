package me.gosdev.chatpointsttv.Actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.gosdev.chatpointsttv.ChatPointsTTV;

public class InvShuffleAction extends BaseAction {
    Player target;
    public InvShuffleAction(Player target) {
        this.target = target;
    }

    @Override
    public void run() {
        if (target == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission(ChatPointsTTV.permissions.TARGET.permission_id)) continue;
                shuffle(p);
            }
        } else {
            shuffle(target);
        }
    }

    private void shuffle(Player p) {
        ArrayList<ItemStack> inv = new ArrayList<>(Arrays.asList(p.getInventory().getStorageContents()));
        inv.add(p.getInventory().getItemInOffHand());

        Collections.shuffle(inv);

        for (int i = 0; i < inv.size() - 1; i++) { // Don't include last item (offhand)
            p.getInventory().setItem(i, inv.get(i));
        }
        p.getInventory().setItemInOffHand(inv.get(inv.size() - 1));
    }
}
