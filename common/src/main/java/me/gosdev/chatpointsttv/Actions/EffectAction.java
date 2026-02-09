package me.gosdev.chatpointsttv.Actions;
import java.util.List;
import java.util.Random;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Events.EventInformation;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;

public class EffectAction implements BaseAction {
    public static final String ACTION_NAME = "EFFECT";

    @Override
    public String getActionName() {
        return ACTION_NAME;
    }

    private final String effect;
    private final String target;
    private int duration;
    private int strength;

    public EffectAction(String effectName, Integer strength, Integer duration, String target) {
        if (effectName.equalsIgnoreCase("random")) {
            List<String> effects = ChatPointsTTV.getLoader().getPotionEffects();
            this.effect = effects.get(new Random().nextInt(effects.size()));
        } else if (effectName.equalsIgnoreCase("clear")) {
            this.effect = null;
        } else {
            this.effect = effectName;
            this.strength = strength;
            this.duration = duration != null ? duration : Integer.MAX_VALUE;
        }
        this.target = target;
    }

    @Override
    public void run(EventInformation ei) {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Targeting a specific player
                if (!p.equals(ChatPointsTTV.getLoader().getPlayer(target))) continue;
            } else if (!p.hasPermission(ChatPointsTTV.permissions.TARGET)) continue;

            p.giveEffect(effect, duration, strength);

            /*if (effect == null) {
                Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> {
                    for (PotionEffect e : p.getActivePotionEffects()) {
                        p.removePotionEffect(e.getType());
                    }
                });
            } else {
                Bukkit.getScheduler().runTask(SpigotPlugin.getPlugin(), () -> p.addPotionEffect(effect, true));
            }*/
        }
    }    
}
