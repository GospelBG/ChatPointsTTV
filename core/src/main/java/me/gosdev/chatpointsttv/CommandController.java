package me.gosdev.chatpointsttv;

import me.gosdev.chatpointsttv.TwitchAuth.ImplicitGrantFlow;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.Utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

public class CommandController implements TabExecutor {
    Utils utils = ChatPointsTTV.getUtils();
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
                        utils.sendMessage(sender, new TextComponent("There is an account connected already!\nUnlink it before using another one."));
                        break;
                    }
                    if (ChatPointsTTV.configOk) {
                        link(plugin, sender, args.length == 2 ? args[1] : "default");
                    } else {
                        utils.sendMessage(sender, new TextComponent("Invalid configuration. Please check your config file."));
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
                        } catch (InterruptedException | NullPointerException e) {}
                        
                        plugin.unlink(sender);
                    });
                    return true;
                    
                case "status":
                    status(sender, plugin);
                    return true;

                default:
                    utils.sendMessage(sender, new TextComponent(ChatColor.RED + "Unknown command: /twitch " + args[0]));
                    help(sender);
                    return true;
            }
        }

        // If the sender (or console) uses our command correct, we can return true
        if (!ChatPointsTTV.configOk) ChatPointsTTV.getUtils().sendLogToPlayers(ChatColor.RED + "Config file is invalid or has been left at default. Please set it up correctly and reload the plugin.");
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
            utils.sendMessage(p, new TextComponent(ChatColor.RED + "Unknown command: /twitch link " + method));
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
        plugin.log.info("Reloading ChatPointsTTV...");

        if (ImplicitGrantFlow.server != null) ImplicitGrantFlow.server.stop(); // Stop HTTP server if it is actve

        OAuth2Credential cred = plugin.isAccountConnected() ? plugin.oauth : null;

        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();

        if (cred != null) plugin.linkToTwitch(Bukkit.getConsoleSender(), cred.getAccessToken());
    }

    private void help(CommandSender p) {
        utils.sendMessage(p, helpMsg);
    }

    private void status(CommandSender p, ChatPointsTTV plugin) {
        List<Channel> channels = plugin.getListenedChannels();
        String strChannels = "";

        for (Channel channel : channels) {
            ChatColor color = channel.isLive() ? ChatColor.DARK_RED : ChatColor.GRAY;
            strChannels += color + channel.getChannelUsername() + ChatColor.RESET + ", ";
        }

        strChannels = strChannels.substring(0, strChannels.length() - 2); // Remove last comma

        String msg = (
            "---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD  + "ChatPointsTTV status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" +plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Connected account: " + ChatColor.RESET + plugin.getConnectedUsername() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" + 
            "\n" +
            ChatColor.LIGHT_PURPLE + "Connection status: " + (plugin.isAccountConnected() ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "DISCONNECTED")
        );

        ComponentBuilder formatted = new ComponentBuilder(msg);
        utils.sendMessage(p, formatted.create()[0]);
    }
}
