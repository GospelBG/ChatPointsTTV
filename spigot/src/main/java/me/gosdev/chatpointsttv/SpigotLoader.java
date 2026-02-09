package me.gosdev.chatpointsttv;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import me.gosdev.chatpointsttv.Chat.SpigotSender;
import me.gosdev.chatpointsttv.Generic.GenericLoader;
import me.gosdev.chatpointsttv.Generic.GenericPlayer;
import me.gosdev.chatpointsttv.Generic.GenericSender;

public class SpigotLoader implements GenericLoader {
    @Override
    public String getVersion() {
        return SpigotPlugin.getPlugin().getDescription().getVersion();
    }

    @Override
    public List<GenericPlayer> getOnlinePlayers() {
        List<GenericPlayer> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            list.add(new SpigotPlayer(p));
        }

        return list;
    }

    @Override
    public GenericSender consoleSender() {
        return new SpigotSender(Bukkit.getConsoleSender());
    }

    @Override
    public GenericPlayer getPlayer(String username) {
        return new SpigotPlayer(Bukkit.getPlayer(username));
    }

    @Override
    public List<String> getPotionEffects() {
        List<String> effects = new ArrayList<>(); 
        for (PotionEffectType e : PotionEffectType.values()) {
            effects.add(e.getName());
        }
        
        return effects;
    }    
}
