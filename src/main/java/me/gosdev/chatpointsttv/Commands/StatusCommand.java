package me.gosdev.chatpointsttv.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Twitch.Channel;
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
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" + plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" + 
            "\n"
        ).create()[0];

        String currentState;
        if (ChatPointsTTV.getTwitch().isStarted()) {
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                currentState = ChatColor.GREEN + "" + ChatColor.BOLD + "CONNECTED";
            } else {
                currentState = ChatColor.YELLOW + "" + ChatColor.BOLD + "UNLINKED";
            }
        } else {
            currentState = ChatColor.RED + "" + ChatColor.BOLD + "STOPPED";
        }

        BaseComponent status = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "Connection status: " + currentState).create()[0];
        msg.addExtra(status);

        if (!p.equals(Bukkit.getConsoleSender())) {
            if (ChatPointsTTV.getTwitch().isStarted()) {
                TextComponent accountsBtn = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[✔]" + ChatColor.RESET + ChatColor.YELLOW + " Manage accounts" );
                accountsBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to add/remove accounts").create()));
                accountsBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch accounts"));
    
                TextComponent stopBtn = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.RED + " Stop Twitch Client");
                stopBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to stop all Twitch events.").create()));
                stopBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch stop"));
    
                msg.addExtra("\n\n");
                msg.addExtra(accountsBtn);
                msg.addExtra(ChatColor.GRAY + "  -  ");
                msg.addExtra(stopBtn);
    
            } else {
                TextComponent startBtn = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "[⏻]" + ChatColor.RESET + ChatColor.GREEN + " Start Twitch Client");
                startBtn.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to start the Twitch client.").create()));
                startBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/twitch start"));

                msg.addExtra("\n\n");
                msg.addExtra(startBtn);
            }
        }

        p.spigot().sendMessage(msg);
    }

}
