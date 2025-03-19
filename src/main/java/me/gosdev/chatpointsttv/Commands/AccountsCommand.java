package me.gosdev.chatpointsttv.Commands;

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
        TextComponent msg = new TextComponent("Connected Accounts:\n");

        for (Channel channel : ChatPointsTTV.getPlugin().getTwitch().getListenedChannels().values()) {
            BaseComponent deleteButton = new ComponentBuilder(ChatColor.RED + "[x]").create()[0];
            deleteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to unlink this account").create()));
            deleteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch unlink " + channel.getChannelUsername()));
            msg.addExtra(deleteButton);
            msg.addExtra(new TextComponent(" " + channel.getChannelUsername() + "\n"));
        }

        p.spigot().sendMessage(msg);
    }
}
