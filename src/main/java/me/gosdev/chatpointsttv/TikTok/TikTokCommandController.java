package me.gosdev.chatpointsttv.TikTok;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TikTokCommandController implements TabExecutor {

    private final BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV help" + ChatColor.RESET + " ----------\n" + 
    ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("tiktok").getUsage() + ChatColor.RESET + "\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok accounts: " + ChatColor.RESET + "Manage linked accounts.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok link <username>: " + ChatColor.RESET + "Use this command to connect to a TikTok LIVE.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok unlink [username]: " + ChatColor.RESET + "Disconnects from the provided user's LIVE. If a username is not provided all accounts will be disconencted.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok status: " + ChatColor.RESET + "Displays information about the plugin and the TikTok client.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok start: " + ChatColor.RESET + "Starts the TikTok client and logs in to any saved accounts.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok stop: " + ChatColor.RESET + "Stops the TikTok client. All incoming events will be ignored.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files.\n" + 
    ChatColor.LIGHT_PURPLE + "/tiktok test <type> <...>: " + ChatColor.RESET + "Mocks an event.\n" +
    ChatColor.LIGHT_PURPLE + "/tiktok help: " + ChatColor.RESET + "Displays this help message.").create()[0];

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        switch(args[0]) {
            case "start":
                if (TikTokClient.isEnabled) {
                    sender.sendMessage(ChatColor.RED + "TikTok client is already started.");
                    return true;
                }

                sender.sendMessage("Enabling TikTok module...");

                TikTokClient.enable(sender);
                return true;

            case "stop":
                if (!TikTokClient.isEnabled) {
                    sender.sendMessage(ChatColor.RED + "TikTok client is already stopped.");
                    return true;
                }
                
                sender.sendMessage("Disabling TikTok module...");

                TikTokClient.stop(sender);
                return true;

            case "reload":
                sender.sendMessage("Reloading ChatPointsTTV...");
                TikTokClient.stop(sender);
                TikTokClient.enable(sender);
                return true;

            case "link":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok link <username>");
                    return true;
                }
                TikTokClient.link(sender, args[1], true);
                return true;

            case "unlink":
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /tiktok unlink <username>");
                    return true;
                }
                if (args.length == 2) {
                   for (String acc : TikTokClient.getClients().keySet()) {
                        TikTokClient.unlink(acc, true);
                   }
                } else {
                    TikTokClient.unlink(args[1], true);
                }
                
                sender.sendMessage(ChatColor.GREEN + "TikTok account " + args[1] + " unlinked successfully!");
                return true;
            
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command: /tiktok " + args[0]);
                help(sender);
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        return null;
    }

    public void help(CommandSender p) {
        p.spigot().sendMessage(helpMsg);
    }
}
