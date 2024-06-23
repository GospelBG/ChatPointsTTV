package me.gosdev.chatpointsttv.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.twitch4j.common.enums.SubscriptionPlan;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Utils_1_12_R1 implements Utils {
    @Override
     public String PlanToString(SubscriptionPlan plan) {
        switch (plan.toString()) {
            case "Prime":
                return "Tier 1 (Prime)";
            case "1000":
                return "Tier 1";
            case "2000":
                return "Tier 2";
            case "3000":
                return "Tier 3";
            default:
                return null;
        }
    }

    @Override
    public String PlanToConfig(SubscriptionPlan plan) {
        switch (plan.toString()) {
            case "Prime":
                return "TWITCH_PRIME";
            case "1000":
                return "TIER1";
            case "2000":
                return "TIER2";
            case "3000":
                return "TIER3";
            default:
                return null;
        }
    }

    @Override
    public void displayTitle(Player p, String title, String sub) {
        p.sendTitle(title, sub);

    }
    
    @Override
    public void sendMessage(CommandSender p, BaseComponent[] message) {
        p.getServer().spigot().broadcast(message);
    }

    @Override
    public void sendLogToPlayers(String msg) {
        BaseComponent component = new ComponentBuilder("[ChatPointsTTV] " + msg).create()[0];
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.spigot().sendMessage(component);
            }
        }
    }
}
