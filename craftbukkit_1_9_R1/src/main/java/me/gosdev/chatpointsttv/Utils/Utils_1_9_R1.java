package me.gosdev.chatpointsttv.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Utils_1_9_R1 implements Utils {    @SuppressWarnings("deprecation")
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
