package me.gosdev.chatpointsttv.Actions;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class EffectAction extends BaseAction {

    private final PotionEffect effect;
    private final Player target;

    public EffectAction(String effectName, Integer strength, Integer duration, Player target) {
        if (duration == null) duration = Integer.MAX_VALUE;

        if (effectName.equalsIgnoreCase("random")) {
            this.effect = PotionEffectType.values()[new Random().nextInt(PotionEffectType.values().length)].createEffect(duration * 20, strength);
        } else if (effectName.equalsIgnoreCase("clear")) {
            this.effect = null;
        } else {
            this.effect = PotionEffectType.getByName(effectName).createEffect(duration * 20, strength);
        }
        this.target = target;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (target != null) { // Targeting a specific player
                if (!p.equals(target)) continue;
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            if (effect == null) {
                Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> {
                    for (PotionEffect e : p.getActivePotionEffects()) {
                        p.removePotionEffect(e.getType());
                    }
                });
            } else {
                Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> p.addPotionEffect(effect, true));
            }
        }
    }    
}
