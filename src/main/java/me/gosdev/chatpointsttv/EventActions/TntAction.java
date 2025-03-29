package me.gosdev.chatpointsttv.EventActions;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class TntAction extends Action {
    private int amount;
    private Optional<Integer> explosionTime;
    private Player target;
    private int taskId;

    public TntAction(int amount, Optional<Integer> explosionTime, Optional<Player> target) {
        this.amount = amount;

        if (explosionTime.isPresent()) {
            this.explosionTime = explosionTime;
        }
        if (target.isPresent()) {
            this.target = target.get();
        }
    }

    @Override
    public void run() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(ChatPointsTTV.getPlugin(), spawnTnt, 0, 4);
    }

    private final Runnable spawnTnt = (() -> {
        if (amount <= 0) {
            Bukkit.getScheduler().cancelTask(taskId);
            return;
        }
        amount--;
        if (target == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission(permissions.TARGET.permission_id)) continue;
                this.target = p;
                TNTPrimed tnt = (TNTPrimed) this.target.getWorld().spawnEntity(this.target.getLocation(), EntityType.PRIMED_TNT);

                if (explosionTime.isPresent()) {
                    tnt.setFuseTicks(explosionTime.get());
                }
            }
        } else {
            TNTPrimed tnt = (TNTPrimed) target.getWorld().spawnEntity(target.getLocation(), EntityType.PRIMED_TNT);

            if (explosionTime.isPresent()) {
                tnt.setFuseTicks(explosionTime.get());
            }
        }
        
    });
}
