package me.gosdev.chatpointsttv;

import me.gosdev.chatpointsttv.TwitchAuth.ImplicitGrantFlow;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;

public class CommandController implements TabExecutor {
    private BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("twitch").getUsage() + ChatColor.RESET + "\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch link: " + ChatColor.RESET + "Use this command to link your Twitch account and enable the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch unlink: " + ChatColor.RESET + "Use this command to unlink your account and disable the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files. You will need to link again your Twitch account.\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch help: " + ChatColor.RESET + "Displays this help message.").create()[0];
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ChatPointsTTV plugin = ChatPointsTTV.getPlugin();

        if (args.length == 0) {
            help(plugin, sender, cmd);
            return true;
        
        } else {
            switch (args[0]) {
                case "link":
                    if (plugin.isAccountConnected()) {
                        sender.sendMessage("There is an account connected already!\nUnlink your account before linking another one.");
                        break;
                    }
                    if (!ChatPointsTTV.configOk) {
                        sender.sendMessage("Invalid configuration. Please check your config file.");
                        break;
                    }
                    link(plugin, sender);
                    break;

                case "reload":
                    reload(plugin);
                    break;

                case "help":
                    help(plugin, sender, cmd);
                    return true;

                case "unlink":
                    plugin.unlink(sender);
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /twitch " + args[0]);
                    help(plugin, sender, cmd);
                    return true;
            }
        }

        // If the sender (or console) uses our command correct, we can return true
        if (!ChatPointsTTV.configOk) ChatPointsTTV.utils.sendLogToPlayers(ChatColor.RED + "Config file is invalid or has been left at default. Please edit the config.yml file and reload the plugin.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (args.length == 1) {
            list.add("link");
            list.add("unlink");
            list.add("reload");
            list.add("help");

            return list;
        }
        return null;        
    }

    private void link(ChatPointsTTV plugin, CommandSender p) {
        if (ChatPointsTTV.customCredentials) {
            // Try to log in using the provided client secret. Otherwise, proceed as normal using Implicit Grant Flow
            plugin.linkToTwitch(p, plugin.config.getString("CUSTOM_ACCESS_TOKEN"));
        } else {
            CompletableFuture<String> future = ImplicitGrantFlow.getAccessToken(p);
            future.thenAccept(token -> {
                plugin.linkToTwitch(p, token);
            });
        }
    }

    private void reload(ChatPointsTTV plugin) {
        if (ImplicitGrantFlow.server != null) ImplicitGrantFlow.server.stop(); // Stop HTTP server if it is actve

        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();
    }

    private void help(CommandSender p, Command cmd) {
        ChatPointsTTV.utils.sendMessage(p, helpMsg);
    }

    private void status(CommandSender p, ChatPointsTTV plugin) {
        String msg = (
            "---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD  + "ChatPointsTTV status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" +plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Connected account: " + ChatColor.RESET + plugin.getConnectedUsername() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channel: " + ChatColor.RESET + plugin.getListenedChannel() + "\n" + 
            "\n" +
            ChatColor.LIGHT_PURPLE + "Connection status: " + (plugin.isAccountConnected() ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "DISCONNECTED")
        );

        ComponentBuilder formatted = new ComponentBuilder(msg);
        ChatPointsTTV.utils.sendMessage(p, formatted.create()[0]);
    }
}
