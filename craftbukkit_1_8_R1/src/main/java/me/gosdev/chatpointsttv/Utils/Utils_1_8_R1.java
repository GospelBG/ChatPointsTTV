package me.gosdev.chatpointsttv.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.twitch4j.common.enums.SubscriptionPlan;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.chat.BaseComponent;

public class Utils_1_8_R1 implements Utils {
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
    public void displayTitle(Player p, String title, String action, String sub, Boolean bold, ChatColor titleColor, ChatColor subColor) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title "+p.getDisplayName()+" subtitle [{\"text\":\""+action+" \",\"color\":\"white\"},{\"text\":\"" + sub + "\",\"color\":\"" + subColor.toString().toLowerCase() + "\",\"bold\":\"" + bold.toString() + "\"}]");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title "+p.getDisplayName()+" title [{\"text\":\""+title+"\",\"color\":\"" + titleColor.toString().toLowerCase() + "\"}]");
            }
        }.runTask(ChatPointsTTV.getPlugin());
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
    public void sendLogToPlayers(String msg) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.sendRawMessage("[ChatPointsTTV] " + msg);
            }
        }
    }
}
