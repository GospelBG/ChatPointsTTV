package me.gosdev.chatpointsttv;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpigotListeners implements Listener {
    public static HashMap<Player, Location> frozenPlayers = new HashMap<>();
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (frozenPlayers.containsKey(event.getPlayer())) {
            event.getPlayer().teleport(frozenPlayers.get(event.getPlayer()));
        }
    }
}
