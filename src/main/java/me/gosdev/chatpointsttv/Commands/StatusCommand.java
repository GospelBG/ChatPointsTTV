package me.gosdev.chatpointsttv.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import me.gosdev.chatpointsttv.Platforms;
import me.gosdev.chatpointsttv.TikTok.TikTokButtonComponents;
import me.gosdev.chatpointsttv.TikTok.TikTokClient;
import me.gosdev.chatpointsttv.Twitch.Channel;
import me.gosdev.chatpointsttv.Twitch.TwitchButtonComponents;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class StatusCommand {
    public static void status(CommandSender p, ChatPointsTTV plugin, Platforms platform) {
        String strChannels = "";
        
        switch (platform) {
            case TWITCH:
                if (ChatPointsTTV.getTwitch().getListenedChannels() == null || ChatPointsTTV.getTwitch().getListenedChannels().isEmpty()) {
                    strChannels = "None";
                    break;
                }

                for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    ChatColor color = channel.isLive() ? ChatColor.DARK_RED : ChatColor.GRAY;
                    strChannels += color + channel.getChannelUsername() + ChatColor.RESET + ", ";
                }
                strChannels = strChannels.subSequence(0, strChannels.length() - 2).toString(); // Remove last comma
                break;

            case TIKTOK:
                if (TikTokClient.getClients() == null || TikTokClient.getClients().isEmpty()) {
                    strChannels = "None";
                    break;
                }
                for (String profile : TikTokClient.getClients().keySet()) {
                    strChannels += profile + ", ";
                }
                strChannels = strChannels.subSequence(0, strChannels.length() - 2).toString(); // Remove last comma
                break;
        }

        BaseComponent msg = new ComponentBuilder(
            "---------- " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD  + "ChatPointsTTV " + platform.getName() + " status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" + plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" + 
            "\n"
        ).create()[0];

        String currentState = "";
        switch (platform) {
            case TWITCH:
                if (ChatPointsTTV.getTwitch().isStarted()) {
                    if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                        currentState = ChatColor.GREEN + "" + ChatColor.BOLD + "CONNECTED";
                    } else {
                        currentState = ChatColor.YELLOW + "" + ChatColor.BOLD + "UNLINKED";
                    }
                } else {
                    currentState = ChatColor.RED + "" + ChatColor.BOLD + "STOPPED";
                }
                break;
            
            case TIKTOK:
                if(TikTokClient.isEnabled) {
                    if (TikTokClient.accountConnected) {
                        currentState = ChatColor.GREEN + "" + ChatColor.BOLD + "CONNECTED";
                    } else {
                        currentState = ChatColor.YELLOW + "" + ChatColor.BOLD + "UNLINKED";
                    }
                } else {
                    currentState = ChatColor.RED + "" + ChatColor.BOLD + "STOPPED";
                }
                break;
        }
        

        BaseComponent status = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "Connection status: " + currentState).create()[0];
        msg.addExtra(status);

        
        if (!p.equals(Bukkit.getConsoleSender())) {
            msg.addExtra("\n\n");

            switch(platform) {
                case TWITCH:
                    if (ChatPointsTTV.getTwitch().isStarted()) {
                        msg.addExtra(TwitchButtonComponents.manageAccounts());
                        msg.addExtra(ChatColor.GRAY + "  -  ");
                        msg.addExtra(TwitchButtonComponents.clientStop());
                    } else {
                        msg.addExtra(TwitchButtonComponents.clientStart());
                    }
                    break;

                case TIKTOK:
                    if(TikTokClient.isEnabled) {
                        msg.addExtra(TikTokButtonComponents.manageAccounts());
                        msg.addExtra(ChatColor.GRAY + "  -  ");
                        msg.addExtra(TikTokButtonComponents.clientStop());
                    } else {
                        msg.addExtra(TikTokButtonComponents.clientStart());
                    }
                    break;
            }
            
        }

        p.spigot().sendMessage(msg);
    }

}
