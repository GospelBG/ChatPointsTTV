package me.gosdev.chatpointsttv.Actions;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class FreezeAction extends BaseAction implements Listener {
    HashMap<Player, Location> frozenPlayers = new HashMap<>();
    Player target;
    Integer time;

    public FreezeAction(Player target, Integer time) {
        this.target = target;
        this.time = time;
    }

    @Override
    public void run() {
        Bukkit.getPluginManager().registerEvents(this, ChatPointsTTV.getPlugin());
        for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;
            
            Bukkit.getScheduler().runTaskAsynchronously(ChatPointsTTV.getPlugin(), () -> {
                Boolean allowFlightState = p.getAllowFlight();

                freeze(p);
                try {
                    Thread.sleep(time * 1000);
                } catch (InterruptedException e) {}
                unfreeze(p, allowFlightState);
            });
        }
    }

    private void freeze(Player target) {
        frozenPlayers.put(target, target.getLocation());

        Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> {
            target.setAllowFlight(true);
            target.teleport(target.getLocation().add(0,0.001,0));
            target.setFlying(true);
            target.setFlySpeed(0);
        });
    }

    private void unfreeze(Player target, Boolean canFly) {
        frozenPlayers.remove(target);
        if (frozenPlayers.isEmpty()) HandlerList.unregisterAll(this);

        Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> {
            target.setAllowFlight(canFly);
            target.setFlying(false);
            target.setFlySpeed(0.1f);
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.containsKey(event.getPlayer())) {
            event.getPlayer().teleport(frozenPlayers.get(event.getPlayer()));
        }
    }
}
