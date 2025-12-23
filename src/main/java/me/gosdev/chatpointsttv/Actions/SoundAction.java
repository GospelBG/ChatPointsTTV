package me.gosdev.chatpointsttv.Actions;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.ChatPointsTTV.permissions;

public class SoundAction extends BaseAction {
    Player target;
    Sound sound;

    public SoundAction(Player target, Sound sound) {
        this.target = target;
        this.sound = sound;
    }

    @Override
    public void run() {
        for (Player p : ChatPointsTTV.getPlugin().getServer().getOnlinePlayers()) {
            if (target != null) { // Is targeting a player?
                if (!p.equals(target)) {
                    continue;
                }
            } else if (!p.hasPermission(permissions.TARGET.permission_id)) continue;
        
            p.playSound(p.getLocation(), sound, 10, 1);
        }

    }
}
