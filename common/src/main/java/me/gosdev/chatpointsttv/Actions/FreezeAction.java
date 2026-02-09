package me.gosdev.chatpointsttv.Actions;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class FreezeAction implements BaseAction {
    public static final String ACTION_NAME = "FREEZE";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    //HashMap<GenericPlayer, Location> frozenPlayers = new HashMap<>();
    String target;
    Integer time;

    public FreezeAction(String target, Integer time) {
        this.target = target;
        this.time = time;
    }

    @Override
    public void run(EventInformation ei) {
        
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;
            p.freeze(time);
        }

        
        /*Bukkit.getPluginManager().registerEvents(this, SpigotPlugin.getPlugin());
        for (Player p : SpigotPlugin.getPlugin().getServer().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;
            
            Bukkit.getScheduler().runTaskAsynchronously(SpigotPlugin.getPlugin(), () -> {
                Boolean allowFlightState = p.getAllowFlight();

                freeze(p);
                try {
                    Thread.sleep(time * 1000);
                } catch (InterruptedException e) {}
                unfreeze(p, allowFlightState);
            });
        }*/
    }

    /*private void freeze(Player target) {
        frozenPlayers.put(target, target.getLocation());

        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
            target.setAllowFlight(true);
            target.teleport(target.getLocation().add(0,0.001,0));
            target.setFlying(true);
            target.setFlySpeed(0);
        });
    }

    private void unfreeze(Player target, Boolean canFly) {
        frozenPlayers.remove(target);
        if (frozenPlayers.isEmpty()) HandlerList.unregisterAll(this);

        Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
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
    }*/
}
