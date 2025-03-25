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

public class StatusCommand {
    public static void status(CommandSender p, ChatPointsTTV plugin) {
        String strChannels = "";

        for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
            ChatColor color = channel.isLive() ? ChatColor.DARK_RED : ChatColor.GRAY;
            strChannels += color + channel.getChannelUsername() + ChatColor.RESET + ", ";
        }

        strChannels = !ChatPointsTTV.getTwitch().getListenedChannels().isEmpty() && ChatPointsTTV.getTwitch().getListenedChannels() != null ? strChannels.substring(0, strChannels.length() - 2) : "None"; // Get a comma-separated list of channels. If empty or null, display "None"

        BaseComponent msg = new ComponentBuilder(
            "---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "ChatPointsTTV status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" +plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" + 
            "\n"
        ).create()[0];

        BaseComponent status = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "Connection status: " + (ChatPointsTTV.getTwitch().isAccountConnected() ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "DISCONNECTED")).create()[0];
        status.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to toggle connection").create()));
        status.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ChatPointsTTV.getTwitch().isAccountConnected() ? "/twitch stop" : "/twitch start"));

        msg.addExtra(status);

        if (ChatPointsTTV.getTwitch().isAccountConnected()) {
            TextComponent accountsBtn = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[✔]" + ChatColor.RESET + ChatColor.YELLOW + " Manage accounts" );
            accountsBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to add/display accounts").create()));
            accountsBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch accounts"));

            TextComponent stopBtn = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.RED + " Stop Twitch Client");
            stopBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to stop all Twitch events.").create()));
            stopBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch stop"));

            msg.addExtra("\n\n");
            msg.addExtra(accountsBtn);
            msg.addExtra(ChatColor.GRAY + "  -  ");
            msg.addExtra(stopBtn);
        }

        p.spigot().sendMessage(msg);
    }

}
