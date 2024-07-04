package me.gosdev.chatpointsttv.Utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.twitch4j.common.enums.SubscriptionPlan;

import net.md_5.bungee.api.chat.BaseComponent;

public interface Utils {
    public String PlanToString(SubscriptionPlan plan);

    public String PlanToConfig(SubscriptionPlan plan);

    public void displayTitle(Player p, String title, String action, String sub, Boolean bold, ChatColor titleColor, ChatColor subColor);

    public void sendMessage(CommandSender p, BaseComponent[] message);
    public void sendMessage(CommandSender p, BaseComponent message);
    public void sendMessage(CommandSender p, String message);

    public void sendLogToPlayers(String msg);
}
