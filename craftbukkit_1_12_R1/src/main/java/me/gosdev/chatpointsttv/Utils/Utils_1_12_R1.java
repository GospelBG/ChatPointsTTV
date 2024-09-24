package me.gosdev.chatpointsttv.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Utils_1_12_R1 implements Utils {
    @Override
    public void displayTitle(Player p, String title, String action, String sub, Boolean bold, ChatColor titleColor, ChatColor subColor) {
        ChatColor format = bold ? ChatColor.BOLD : ChatColor.RESET;
        p.sendTitle(titleColor + title, action + subColor + " " + format + sub, 10, 70, 20);
    }
    
    @Override
    public void sendMessage(CommandSender p, BaseComponent[] message) {
        p.spigot().sendMessage(message);
    }

    @Override
    public void sendMessage(CommandSender p, BaseComponent message) {
        p.spigot().sendMessage(message);
    }
    @Override
    public void sendMessage(CommandSender p, String message) {
        BaseComponent component = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD +"[ChatPointsTTV] " + ChatColor.RESET + message).create()[0];
        p.spigot().sendMessage(component);
    }
    @Override
    public void sendLogToPlayers(String msg) {
        BaseComponent component = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD +"[ChatPointsTTV] " + ChatColor.RESET + msg).create()[0];
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(ChatPointsTTV.permissions.MANAGE.permission_id)) {
                p.spigot().sendMessage(component);
            }
        }
    }
}