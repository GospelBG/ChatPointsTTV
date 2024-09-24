package me.gosdev.chatpointsttv.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public interface Utils {
    public void displayTitle(Player p, String title, String action, String sub, Boolean bold, ChatColor titleColor, ChatColor subColor);

    public void sendMessage(CommandSender p, BaseComponent[] message);
    public void sendMessage(CommandSender p, BaseComponent message);
    public void sendMessage(CommandSender p, String message);

    public void sendLogToPlayers(String msg);
}
