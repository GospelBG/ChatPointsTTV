package me.gosdev.chatpointsttv.EventActions;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class SpawnAction extends Action {
    private EntityType entity;
    private Integer amount;
    private String name;
    private Player player;

    private Boolean useCustomName = ChatPointsTTV.getTwitch().overrideNameSpawnedMobs != null ? ChatPointsTTV.getTwitch().overrideNameSpawnedMobs : ChatPointsTTV.nameSpawnedMobs;

    public SpawnAction(EntityType entity, String chatter, Optional<Integer> amount, Optional<Player> target) {
        this.entity = entity;
        this.name = chatter;
        this.amount = amount.orElse(1);
        this.player = target.orElse(null);
    }

    @Override 
    public void run() {
        for (int i = 0; i < amount; i++) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (player != null) { // Is targeting a specific player?
                    if (player.isOnline()) {
                        ChatPointsTTV.log.warning("Couldn't find player " + player.getDisplayName() + ".");
                        return;
                    }
                } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;
        
                Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> { // Entities should only be spawned synchronously
                    Entity e = p.getWorld().spawnEntity(p.getLocation(), entity);
                    e.setGlowing(ChatPointsTTV.getTwitch().overrideShouldMobsGlow != null ? ChatPointsTTV.getTwitch().overrideShouldMobsGlow : ChatPointsTTV.shouldMobsGlow);
                    if (useCustomName) {
                        e.setCustomName(name);
                        e.setCustomNameVisible(true);
                    }
                });
            }     
        }
    }
}