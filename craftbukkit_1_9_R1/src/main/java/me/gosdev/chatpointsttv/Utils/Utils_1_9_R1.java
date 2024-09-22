package me.gosdev.chatpointsttv.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.twitch4j.common.enums.SubscriptionPlan;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Utils_1_9_R1 implements Utils {
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

    @SuppressWarnings("deprecation")
    @Override
    public void displayTitle(Player p, String title, String action, String sub, Boolean bold, ChatColor titleColor, ChatColor subColor) {
        if (bold) {
            p.sendTitle(titleColor + title, action + " " + subColor + ChatColor.BOLD + sub);
        } else {
            p.sendTitle(titleColor + title, action + " " + subColor + sub);
        }
    }
    
    @Override
    public void sendMessage(CommandSender p, BaseComponent[] message) {
        p.getServer().spigot().broadcast(message);
    }

    @Override
    public void sendMessage(CommandSender p, BaseComponent message) {
        p.getServer().spigot().broadcast(message);
    }
    @Override
    public void sendMessage(CommandSender p, String message) {
        BaseComponent component = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD +"[ChatPointsTTV] " + ChatColor.RESET + message).create()[0];
        p.getServer().spigot().broadcast(component);
    }

    @Override
    public void sendLogToPlayers(String msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.sendRawMessage(ChatColor.LIGHT_PURPLE + "" +ChatColor.BOLD + "[ChatPointsTTV] " + ChatColor.RESET + msg);
                
            }
        }
    }
}
