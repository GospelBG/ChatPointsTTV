package me.gosdev.chatpointsttv.Commands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.TikTok.TikTokButtonComponents;
import me.gosdev.chatpointsttv.Twitch.Channel;
import me.gosdev.chatpointsttv.Twitch.TwitchButtonComponents;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;


public class AccountsCommand {
    public static void displayAccounts(CommandSender p, Platforms platform) {
        TextComponent msg = new TextComponent("\n---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "Connected Accounts" + ChatColor.RESET + " ----------\n\n");
        TextComponent footer = null;

        ArrayList<String> channels = new ArrayList<>();

        switch (platform) {
            case TWITCH:
                if (!ChatPointsTTV.getTwitch().isStarted()) {
                    p.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
                    return;
                }
    
                for (Channel i : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    channels.add(i.getChannelUsername());
                }

                if (p.equals(Bukkit.getConsoleSender())) {
                    footer = new TextComponent(ChatColor.ITALIC + "\nTo unlink an account, use /twitch unlink <channel>\nTo add an account, use /twitch link");
                } else {
                    footer = TwitchButtonComponents.accountLink();
                    if (!channels.isEmpty()) {
                        footer.addExtra(ChatColor.GRAY + "  -  ");
                        footer.addExtra(TwitchButtonComponents.accountUnlink());
                    }
                }
                break;

            case TIKTOK:
                if (!ChatPointsTTV.getTikTok().isEnabled) {
                    p.sendMessage(ChatColor.RED + "You must start the TikTok Client first!");
                    return;
                }

                channels.addAll(ChatPointsTTV.getTikTok().getClients().keySet());

                if (p.equals(Bukkit.getConsoleSender())) {
                    footer = new TextComponent(ChatColor.ITALIC + "\nTo unlink an account, use /tiktok unlink <username>\nTo add an account, use /tiktok link <username>");
                } else {
                    footer = TikTokButtonComponents.accountLink();
                    if (!channels.isEmpty()) {
                        footer.addExtra(ChatColor.GRAY + "  -  ");
                        footer.addExtra(TikTokButtonComponents.accountUnlink());
                    }
                }
                break;
        }
        
        if (p.equals(Bukkit.getConsoleSender())) {
            for (String channel : channels) {
                msg.addExtra(ChatColor.GRAY + "  -  " + channel + "\n");
            }
        } else {    
            for (String channel : channels) {
                BaseComponent deleteButton = new ComponentBuilder(ChatColor.RED + "  [❌]").create()[0];
                deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink this account").create()));
                deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + platform.getName().toLowerCase() + " unlink " + channel));
                msg.addExtra(deleteButton);
                msg.addExtra(new TextComponent("  " + channel + "\n"));
            }
        }

        if (channels.isEmpty()) {
            msg.addExtra(ChatColor.GRAY + "  There are no connected accounts :(\n");
        }

        msg.addExtra(footer);
        msg.addExtra("\n");
        p.spigot().sendMessage(msg);
    }
}
