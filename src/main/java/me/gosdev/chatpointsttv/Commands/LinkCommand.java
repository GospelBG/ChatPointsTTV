package me.gosdev.chatpointsttv.Commands;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.github.philippheuer.credentialmanager.domain.DeviceAuthorization;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.DeviceCodeGrantFlow;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class LinkCommand {
    public static void link(ChatPointsTTV plugin, CommandSender p) {
        TwitchClient twitch = plugin.getTwitch();

        DeviceAuthorization auth = DeviceCodeGrantFlow.link(p, twitch);
        TextComponent comp = new TextComponent(ChatPointsTTV.msgPrefix);
        if (p.equals(Bukkit.getConsoleSender())) {
            comp.addExtra(new TextComponent("Go to https://twitch.tv/activate and enter the code: " + ChatColor.DARK_PURPLE + auth.getUserCode()));
        } else {
            TextComponent button = new TextComponent(ChatColor.DARK_PURPLE + "" + ChatColor.UNDERLINE + "[Click here]");
            button.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, auth.getVerificationUri()));
            button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(new TextComponent("Click to open in browser")).create()));

            comp.addExtra(button);
            comp.addExtra(new TextComponent(ChatColor.LIGHT_PURPLE + " and enter the code: " + ChatColor.DARK_PURPLE + auth.getUserCode()));

        }

        p.spigot().sendMessage(comp);
    }

    public static void unlink(CommandSender p, Optional<String> channelField) {
        if (channelField.isPresent()) {
            try {
                ChatPointsTTV.getPlugin().getTwitch().unlinkAccount(channelField.get());
                p.sendMessage(ChatPointsTTV.msgPrefix + "Account unlinked!");
            } catch (NullPointerException e) {
                p.sendMessage(e.getMessage() + " " + channelField.get());
            }
            
        } else {
            ChatPointsTTV.getPlugin().getTwitch().stop(p);
        }
    }
}
