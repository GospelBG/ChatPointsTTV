package me.gosdev.chatpointsttv.Commands;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.github.philippheuer.credentialmanager.domain.DeviceAuthorization;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.Channel;
import me.gosdev.chatpointsttv.Twitch.DeviceCodeGrantFlow;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class LinkCommand {
    public static void link(ChatPointsTTV plugin, CommandSender p) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            p.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
            return;
        }
        p.sendMessage(ChatColor.GRAY + "Please wait...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            DeviceAuthorization auth = DeviceCodeGrantFlow.link(p, ChatPointsTTV.getTwitch());
            TextComponent comp = new TextComponent("\n  ------------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "Account Linking" + ChatColor.RESET + " -------------\n\n");
            if (p.equals(Bukkit.getConsoleSender())) {
                comp.addExtra(new TextComponent(ChatColor.LIGHT_PURPLE + "Go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "https://twitch.tv/activate" + ChatColor.LIGHT_PURPLE + " and enter the code: " + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode()));
            } else {
                TextComponent button = new TextComponent("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + ChatColor.UNDERLINE + "[Click here]");
                button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, auth.getVerificationUri()));
                button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent("Click to open in browser")).create()));
    
                comp.addExtra(button);

                comp.addExtra(ChatColor.LIGHT_PURPLE + " or go to " + ChatColor.DARK_PURPLE + ChatColor.ITALIC + "https://twitch.tv/activate" + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " and enter this code:\n\n" + ChatColor.GRAY + "   ➡ " + ChatColor.DARK_PURPLE + ChatColor.BOLD + auth.getUserCode() + "\n");
            }
            p.spigot().sendMessage(comp);
        });
    }

    public static void unlink(CommandSender p, Optional<String> channelField) {
        if (!ChatPointsTTV.getTwitch().isStarted()) {
            p.sendMessage(ChatColor.RED + "You must start the Twitch Client first!");
            return;
        }
        if (!ChatPointsTTV.getTwitch().isAccountConnected()) {
            p.sendMessage(ChatColor.RED + "There are no accounts linked!");
            return;
        }
        if (channelField.isPresent()) {
            try {
                ChatPointsTTV.getTwitch().unlinkAccount(channelField.get());
                p.sendMessage(ChatPointsTTV.msgPrefix + "Account unlinked!");
            } catch (NullPointerException e) {
                p.sendMessage(e.getMessage() + " " + channelField.get());
            }
        } else {
            try {
                for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    ChatPointsTTV.getTwitch().unlinkAccount(channel.getChannelUsername());
                }
                p.sendMessage(ChatPointsTTV.msgPrefix + "All accounts were unlinked successfully!");
            } catch (NullPointerException e) {
                p.sendMessage(e.getMessage() + " " + channelField.get());
            }
        }
    }
}
