package me.gosdev.chatpointsttv.Commands;

import org.bukkit.command.CommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Utils.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class StatusCommand {
    public static void status(CommandSender p, ChatPointsTTV plugin) {
        TwitchClient twitch = plugin.getTwitch();
        String strChannels = "";

        for (Channel channel : twitch.getListenedChannels().values()) {
            ChatColor color = channel.isLive() ? ChatColor.DARK_RED : ChatColor.GRAY;
            strChannels += color + channel.getChannelUsername() + ChatColor.RESET + ", ";
        }

        strChannels = !twitch.getListenedChannels().isEmpty() && twitch.getListenedChannels() != null ? strChannels.substring(0, strChannels.length() - 2) : "None"; // Get a comma-separated list of channels. If empty or null, display "None"

        BaseComponent msg = new ComponentBuilder(
            "---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD  + "ChatPointsTTV status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" +plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" + 
            "\n"
        ).create()[0];

        BaseComponent status = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "Connection status: " + (twitch.isAccountConnected() ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "DISCONNECTED")).create()[0];
        status.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to toggle connection").create()));
        status.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, twitch.isAccountConnected() ? "/twitch unlink" : "/twitch link"));

        msg.addExtra(status);

        p.spigot().sendMessage(msg);
    }

}
