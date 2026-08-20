package me.gosdev.chatpointsttv.Actions;
import java.util.Random;

import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class EffectAction extends BaseAction {

    private final String effect;
    private final int duration;
    private final Integer strength;
    private final GenericPlayer target;

    public EffectAction(String effectName, Integer strength, Integer duration, GenericPlayer target) {
        if (duration == null) this.duration = Integer.MAX_VALUE;
        else this.duration = duration;
        this.strength = strength;


        if (effectName.equalsIgnoreCase("random")) {
            this.effect = ChatPointsTTV.getLoader().getPotionEffects().get(new Random().nextInt(ChatPointsTTV.getLoader().getPotionEffects().size()));
        } else if (effectName.equalsIgnoreCase("clear")) {
            this.effect = null;
        } else {
            this.effect = effectName;
        }
        this.target = target;
    }

    @Override
    public void run() {
        for (GenericPlayer p : ChatPointsTTV.getLoader().getOnlinePlayers()) {
            if (target != null) { // Targeting a specific player
                if (!p.equals(target)) continue;
            } else if (!p.hasPermission(permissions.TARGET)) continue;

            if (effect == null) {
                p.clearEffects();
            } else {
                p.giveEffect(effect, duration, strength);
            }
        }
    }    
}
