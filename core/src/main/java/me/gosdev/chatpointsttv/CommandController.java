package me.gosdev.chatpointsttv;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

public class CommandController implements TabExecutor {
    private BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("twitch").getUsage() + ChatColor.RESET + "\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch link: " + ChatColor.RESET + "Use this command to link your Twitch account and enable the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch unlink: " + ChatColor.RESET + "Use this command to unlink your account and disable the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch status: " + ChatColor.RESET + "Displays information about the plugin and the Twitch connection.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files. You will need to link again your Twitch account.\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch help: " + ChatColor.RESET + "Displays this help message.").create()[0];
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ChatPointsTTV plugin = ChatPointsTTV.getPlugin();

        if(cmd.getName().equalsIgnoreCase("twitch")) {
            if (args.length == 0) {
                help(platforms.TWITCH, sender);
                return true;
            
            } else {
                switch (args[0]) {
                    case "link":
                        if (ChatPointsTTV.configOk) {
                            link(plugin, sender, args.length == 2 ? args[1] : "default");
                        } else {
                            sender.sendMessage("Invalid configuration. Please check your config file.");
                            break;
                        }
                        
                        return true;
    
                    case "reload":
                        reload(plugin);
                        return true;
    
                    case "help":
                        help(platforms.TWITCH, sender);
                        return true;
    
                    case "unlink":
                        ChatPointsTTV.Twitch.unlink(sender);
                        return true;
                    case "status":
                        status(sender, plugin);
                        return true;
    
                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown command: /twitch " + args[0]);
                        help(platforms.TWITCH, sender);
                        return true;
                }
            }
    
            }
        }

        // If the sender (or console) uses our command correct, we can return true
        if (!ChatPointsTTV.configOk) ChatPointsTTV.getUtils().sendLogToPlayers(ChatColor.RED + "Config file is invalid or has been left at default. Please edit the config.yml file and reload the plugin.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 1) {
            if (!ChatPointsTTV.getPlugin().isAccountConnected()) list.add("link");
            else list.add("unlink");
            list.add("reload");
            list.add("status");
            list.add("help");

            return list;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("link")) {
            list.add("key");
            list.add("browser");

            return list;
        }

        return null;        
    }

    private void link(ChatPointsTTV plugin, CommandSender p, String method) {

        if (method.equalsIgnoreCase("browser"))  ChatPointsTTV.twitchCustomCredentials = false;
        else if (method.equalsIgnoreCase("key")) ChatPointsTTV.twitchCustomCredentials = true;
        else if (method.equals("default")) {
            ChatPointsTTV.twitchCustomCredentials = (plugin.config.getString("TWITCH_CLIENT_ID") != null || plugin.config.getString("CUSTOM_CLIENT_SECRET") != null);
        } else {
            ChatPointsTTV.getUtils().sendMessage(p, new ComponentBuilder(ChatColor.RED + "Unknown command: /twitch link " + method).create()[0]);
            help(platforms.TWITCH, p);
            return;
        }
        if (ChatPointsTTV.twitchCustomCredentials) {
            // Try to log in using the provided client secret. Otherwise, proceed as normal using Implicit Grant Flow
            ChatPointsTTV.Twitch.link(p, plugin.config.getString("TWITCH_ACCESS_TOKEN"));
        } else {
            CompletableFuture<String> future = ImplicitGrantFlow.getAccessToken(p);
            future.thenAccept(token -> {
                ChatPointsTTV.Twitch.link(p, token);
            });
        }
    }

    private void reload(ChatPointsTTV plugin) {
        if (ImplicitGrantFlow.server != null) ImplicitGrantFlow.server.stop(); // Stop HTTP server if it is actve

        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();
    }

    private void help(platforms platform, CommandSender p) {
        if (platform == platforms.TWITCH) ChatPointsTTV.getUtils().sendMessage(p, twitchHelpMsg);
    }

    private void status(CommandSender p, ChatPointsTTV plugin) {
        String msg = (
            "---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD  + "ChatPointsTTV status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" +plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Connected account: " + ChatColor.RESET + ChatPointsTTV.Twitch.getConnectedUsername() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channel: " + ChatColor.RESET + ChatPointsTTV.Twitch.getListenedChannel() + "\n" + 
            "\n" +
            ChatColor.LIGHT_PURPLE + "Connection status: " + (ChatPointsTTV.Twitch.isAccountConnected() ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "DISCONNECTED")
        );

        ComponentBuilder formatted = new ComponentBuilder(msg);
        ChatPointsTTV.getUtils().sendMessage(p, formatted.create()[0]);
    }
}
