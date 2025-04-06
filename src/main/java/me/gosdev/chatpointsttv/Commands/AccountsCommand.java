package me.gosdev.chatpointsttv.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Utils.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;


public class AccountsCommand {
    public static void displayAccounts(CommandSender p) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            p.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
            return;
        }
        TextComponent msg = new TextComponent("\n---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "Connected Accounts" + ChatColor.RESET + " ----------\n\n");
        if (p.equals(Bukkit.getConsoleSender())) {
            for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                msg.addExtra(ChatColor.GRAY + "  -  " + channel.getChannelUsername() + "\n");
            }
            msg.addExtra(ChatColor.ITALIC + "\nTo unlink an account, use /twitch unlink <channel>\nTo add an account, use /twitch link");
        } else {
            TextComponent addBtn = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "\n[+]" + ChatColor.RESET + ChatColor.GREEN + " Add account");
            addBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to link another account").create()));
            addBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch link"));
    
            TextComponent stopBtn = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[❌]" + ChatColor.RESET + ChatColor.RED + " Remove all accounts");
            stopBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink all accounts").create()));
            stopBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch unlink"));
            
            if (ChatPointsTTV.getTwitch().getListenedChannels().isEmpty()) {
                msg.addExtra(ChatColor.GRAY + "  There are no connected accounts :(\n");
                msg.addExtra(addBtn);
            } else {
                for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    BaseComponent deleteButton = new ComponentBuilder(ChatColor.RED + "  [❌]").create()[0];
                    deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink this account").create()));
                    deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch unlink " + channel.getChannelUsername()));
                    msg.addExtra(deleteButton);
                    msg.addExtra(new TextComponent("  " + channel.getChannelUsername() + "\n"));
                }
        
                msg.addExtra(addBtn);
                msg.addExtra(ChatColor.GRAY + "  -  ");
                msg.addExtra(stopBtn);
            }    
        }
        p.spigot().sendMessage(msg);
    }
}
