package me.gosdev.chatpointsttv;

import me.gosdev.chatpointsttv.TwitchAuth.ImplicitGrantFlow;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bstats.charts.SimplePie;
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

        if (args.length == 0) {
            help(sender);
            return true;
        
        } else {
            switch (args[0]) {
                case "link":
                    if (plugin.isAccountConnected()) {
                        sender.sendMessage("There is an account connected already!\nUnlink your account before linking another one.");
                        break;
                    }
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
                    help(sender);
                    return true;

                case "unlink":
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        try {
                            plugin.linkThread.join();
                        } catch (InterruptedException e) {}
                        
                        plugin.unlink(sender);
                    });
                    return true;
                    
                case "status":
                    status(sender, plugin);
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /twitch " + args[0]);
                    help(sender);
                    return true;
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

        if (method.equalsIgnoreCase("browser"))  ChatPointsTTV.customCredentials = false;
        else if (method.equalsIgnoreCase("key")) ChatPointsTTV.customCredentials = true;
        else if (method.equals("default")) {
            ChatPointsTTV.customCredentials = (plugin.config.getString("CUSTOM_CLIENT_ID") != null || plugin.config.getString("CUSTOM_CLIENT_SECRET") != null);
        } else {
            ChatPointsTTV.getUtils().sendMessage(p, new ComponentBuilder(ChatColor.RED + "Unknown command: /twitch link " + method).create()[0]);
            help(p);
            return;
        }
        if (ChatPointsTTV.customCredentials) {
            // Try to log in using the provided client secret. Otherwise, proceed as normal using Implicit Grant Flow
            plugin.linkToTwitch(p, plugin.config.getString("CUSTOM_ACCESS_TOKEN"));
        } else {
            CompletableFuture<String> future = ImplicitGrantFlow.getAccessToken(p);
            future.thenAccept(token -> {
                plugin.linkToTwitch(p, token);
            });
        }
        plugin.metrics.addCustomChart(new SimplePie("authentication_method", () -> {
            return ChatPointsTTV.customCredentials ? "OAuth Keys" : "Browser Login";
        }));
    }

    private void reload(ChatPointsTTV plugin) {
        if (ImplicitGrantFlow.server != null) ImplicitGrantFlow.server.stop(); // Stop HTTP server if it is actve

        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();
    }

    private void help(CommandSender p) {
        ChatPointsTTV.getUtils().sendMessage(p, helpMsg);
    }

    private void status(CommandSender p, ChatPointsTTV plugin) {
        List<String> channels = plugin.getListenedChannels();
        String strChannels = "";
        for (int i = 0; i < channels.size(); i++) {
            strChannels += channels.get(i);
            if (i != channels.size() - 1) strChannels += ", ";
        }
        String msg = (
            "---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD  + "ChatPointsTTV status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" +plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Connected account: " + ChatColor.RESET + plugin.getConnectedUsername() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" + 
            "\n" +
            ChatColor.LIGHT_PURPLE + "Connection status: " + (plugin.isAccountConnected() ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "DISCONNECTED")
        );

        ComponentBuilder formatted = new ComponentBuilder(msg);
        ChatPointsTTV.getUtils().sendMessage(p, formatted.create()[0]);
    }
}
