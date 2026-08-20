package me.gosdev.chatpointsttv.Actions;

import java.util.Optional;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class TntAction extends BaseAction {
    private final int DEFAULT_FUSE_TICKS = 80;

    private int amount;
    private Optional<Integer> explosionTime;
    private GenericPlayer target;
    private int taskId;

    public TntAction(int amount, Optional<Integer> explosionTime, Optional<GenericPlayer> target) {
        this.amount = amount;
        this.explosionTime = explosionTime;

        if (target.isPresent()) {
            this.target = target.get();
        }
    }

    @Override
    public void run() {
        taskId = ChatPointsTTV.getLoader().scheduleSyncRepeatingTask(spawnTnt, 0, 4);
    }

    private final Runnable spawnTnt = (() -> {
        if (amount <= 0) {
            ChatPointsTTV.getLoader().cancelTask(taskId);
            return;
        }
        amount--;
        if (target == null) {
            for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
                if (!p.hasPermission(permissions.TARGET)) continue;
                p.spawnTnt(explosionTime.orElse(DEFAULT_FUSE_TICKS));
            }
        } else {
            target.spawnTnt(explosionTime.orElse(DEFAULT_FUSE_TICKS));
        }
    });
}
