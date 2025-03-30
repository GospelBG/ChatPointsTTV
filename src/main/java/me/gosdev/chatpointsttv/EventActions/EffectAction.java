package me.gosdev.chatpointsttv.EventActions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class EffectAction extends Action {

    private final PotionEffect effect;
    private final Player target;

    public EffectAction(PotionEffectType effect, Integer strength, Integer duration, Player target) {

        this.effect = effect.createEffect(duration * 20, strength);
        this.target = target;
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (target != null) { // Targeting a specific player
                if (!p.equals(target)) continue;
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;

            Bukkit.getScheduler().runTask(ChatPointsTTV.getPlugin(), () -> p.addPotionEffect(effect, true));
        }
    }    
}
