package me.gosdev.chatpointsttv;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.twitch4j.common.enums.SubscriptionPlan;

import me.gosdev.chatpointsttv.Tests.TestCommand;
import me.gosdev.chatpointsttv.Twitch.Auth.ImplicitGrantFlow;
import me.gosdev.chatpointsttv.Twitch.TwitchClient;
import me.gosdev.chatpointsttv.Utils.Channel;
import me.gosdev.chatpointsttv.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class CommandController implements TabExecutor {
    Utils utils = ChatPointsTTV.getUtils();
    private final BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("twitch").getUsage() + ChatColor.RESET + "\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch link [method]: " + ChatColor.RESET + "Use this command to link your Twitch account and enable the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch unlink: " + ChatColor.RESET + "Use this command to unlink your account and disable the plugin.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch status: " + ChatColor.RESET + "Displays information about the plugin and the Twitch connection.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files. You will need to link again your Twitch account.\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch test <type> <...>: " + ChatColor.RESET + "Summons a test event.\n" +         
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
                    if (plugin.getTwitch().isAccountConnected()) {
                        utils.sendMessage(sender, "There is an account connected already!\nUnlink it before using another one.");
                        break;
                    }
                    if (ChatPointsTTV.configOk) {
                        link(plugin, sender, args.length == 2 ? args[1] : "default");
                    } else {
                        utils.sendMessage(sender, "Invalid configuration. Please check your config file.");
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
                            plugin.getTwitch().linkThread.join();
                        } catch (InterruptedException | NullPointerException e) {}
                        
                        plugin.getTwitch().unlink(sender);
                    });
                    return true;
                    
                case "status":
                    status(sender, plugin);
                    return true;

                case "test":
                    if (!plugin.getTwitch().isAccountConnected()) {
                        utils.sendMessage(sender, ChatColor.RED + "You need to link your account first.");
                        return true;
                    }
                    TestCommand.test(sender, args);
                    return true;

                default:
                    utils.sendMessage(sender, ChatColor.RED + "Unknown command: /twitch " + args[0]);
                    help(sender);
                    return true;
            }
        }

        if (!ChatPointsTTV.configOk) ChatPointsTTV.getUtils().sendLogToPlayers(ChatColor.RED + "Config file has errors or has been left at default. Please set it up correctly and reload the plugin.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        ArrayList<String> available = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        if (args.length == 1) {
            if (!ChatPointsTTV.getPlugin().getTwitch().isAccountConnected()) available.add("link");
            else available.add("unlink");
            available.add("reload");
            available.add("status");
            if (ChatPointsTTV.getPlugin().getTwitch().isAccountConnected()) available.add("test");
            available.add("help");

        } else if (args.length == 2 && args[0].equalsIgnoreCase("link")) {
            available.add("key");
            available.add("browser");

        } else if (ChatPointsTTV.getPlugin().getTwitch().isAccountConnected() && args.length >= 2 && args[0].equalsIgnoreCase("test")) { // Test Command Arguments
            if (args.length == 2) {
                available.add("channelpoints");
                available.add("cheer");
                available.add("sub");
                available.add("follow");
                available.add("subgift");
                available.add("raid");
            } else if (args[1].equalsIgnoreCase("channelpoints")) {
                int rewardNameEnd = args.length - 1;
                if (args.length >= 5 && args[4].startsWith("\"")) { // Check if the reward name starts with a quote. If so, wait to find the closing quote
                    for (int i = 5; i < args.length; i++) {
                        if (args[i].endsWith("\"")) {
                            rewardNameEnd = i;
                        }
                    }
                } else rewardNameEnd = 4;
                switch (args.length) {
                    case 3:
                        result.add("<Redeemer Name>");
                        break;

                    case 4:
                        result.add("<Streamer Channel>");
                        break;

                    case 5:
                        result.add("<Reward Name>");
                        break;
                
                    default:
                        if (args.length > rewardNameEnd + 1) {
                            result.add("[User Input]");
                        } else {
                            result.add("<Reward Name>");
                        }
                        break;
                }
                return result;
            } else if (args[1].equalsIgnoreCase("cheer")) {
                switch (args.length) {
                    case 3:
                        result.add("<Chatter Name>");
                        break;

                    case 4:
                        result.add("<Streamer Channel>");
                        break;

                    case 5:
                        result.add("<Amount>");
                        break;
                }

                return result;
            } else if (args[1].equalsIgnoreCase("sub")) {
                switch (args.length) {
                    case 3:
                        result.add("<Chatter Name>");
                        break;

                    case 4:
                        result.add("<Streamer Channel>");
                        break;

                    case 5:
                        for (SubscriptionPlan plan : EnumSet.allOf(SubscriptionPlan.class)) {
                            if (plan.equals(SubscriptionPlan.NONE)) continue;
                            result.add(plan.name());
                        }
                        break;

                    case 6:
                        result.add("<Months>");
                        break;
                }
                return result;
            } else if (args[1].equalsIgnoreCase("follow")) {
                switch (args.length) {
                    case 3:
                        result.add("<Chatter Name>");
                        break;

                    case 4:
                        result.add("<Streamer Channel>");
                        break;
                }
                return result;
            } else if (args[1].equalsIgnoreCase("subgift")) {
                switch (args.length) {
                    case 3:
                        result.add("<Chatter Name>");
                        break;
                    
                    case 4:
                        result.add("<Streamer Channel>");
                        break;

                    case 5:
                        for (SubscriptionPlan plan : EnumSet.allOf(SubscriptionPlan.class)) {
                            if (plan.equals(SubscriptionPlan.NONE)) continue;
                            result.add(plan.name());
                        }
                        break;

                    case 6:
                        result.add("<Amount>");
                        break;
                }
                return result;
            } else if (args[1].equalsIgnoreCase("raid")) {
                switch (args.length) {
                    case 3:
                        result.add("<Raider Name>");
                        break;

                    case 4:
                        result.add("<Streamer Channel>");
                        break;

                    case 5:
                        result.add("<Viewers>");
                        break;
                }
                return result;
            }
        }
            
        for (String s : available) {
            if (s.startsWith(args[args.length - 1])) {
                result.add(s);
            }
        }

        return result;
    }

    private void link(ChatPointsTTV plugin, CommandSender p, String method) {
        TwitchClient twitch = plugin.getTwitch();
        if (method.equalsIgnoreCase("browser"))  twitch.customCredentialsFound = false;
        else if (method.equalsIgnoreCase("key")) twitch.customCredentialsFound = true;
        else if (method.equals("default")) {
            twitch.customCredentialsFound = (plugin.config.getString("CUSTOM_CLIENT_ID") != null || plugin.config.getString("CUSTOM_CLIENT_SECRET") != null);
        } else {
            utils.sendMessage(p, new TextComponent(ChatColor.RED + "Unknown command: /twitch link " + method));
            help(p);
            return;
        }
        if (twitch.customCredentialsFound) {
            // Try to log in using the provided client secret. Otherwise, proceed as normal using Implicit Grant Flow
            twitch.linkToTwitch(p, plugin.config.getString("CUSTOM_ACCESS_TOKEN"), plugin.config.getString("CUSTOM_ACCESS_TOKEN"));
        } else {
            if (!ImplicitGrantFlow.server.isRunning()) {
                CompletableFuture<String> future = ImplicitGrantFlow.getAccessToken(plugin, p, TwitchClient.getClientID());
                future.thenAccept(token -> {
                    twitch.linkToTwitch(p, TwitchClient.getClientID(), token);
                });
            }
        }
        plugin.metrics.addCustomChart(new SimplePie("authentication_method", () -> {
            return twitch.customCredentialsFound ? "OAuth Keys" : "Browser Login";
        }));
    }

    private void reload(ChatPointsTTV plugin) {
        plugin.log.info("Reloading ChatPointsTTV...");

        plugin.onDisable();
        plugin.onEnable();
    }

    private void help(CommandSender p) {
        utils.sendMessage(p, helpMsg);
    }

    private void status(CommandSender p, ChatPointsTTV plugin) {
        TwitchClient twitch = plugin.getTwitch();
        String strChannels = "";

        for (Channel channel : twitch.getListenedChannels().values()) {
            ChatColor color = channel.isLive() ? ChatColor.DARK_RED : ChatColor.GRAY;
            strChannels += color + channel.getChannelUsername() + ChatColor.RESET + ", ";
        }

        strChannels = !twitch.getListenedChannels().isEmpty() ? strChannels.substring(0, strChannels.length() - 2) : "None"; // Get a comma-separated list of channels. If empty, display "None"

        BaseComponent msg = new ComponentBuilder(
            "---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD  + "ChatPointsTTV status" + ChatColor.RESET + " ----------\n" + 
            ChatColor.LIGHT_PURPLE + "Plugin version: " + ChatColor.RESET + "v" +plugin.getDescription().getVersion() + "\n" +
            ChatColor.LIGHT_PURPLE + "Connected account: " + ChatColor.RESET + twitch.getConnectedUsername() + "\n" +
            ChatColor.LIGHT_PURPLE + "Listened channels: " + ChatColor.RESET + strChannels + "\n" + 
            "\n"
        ).create()[0];

        BaseComponent status = new ComponentBuilder(ChatColor.LIGHT_PURPLE + "Connection status: " + (twitch.isAccountConnected() ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "DISCONNECTED")).create()[0];
        status.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to toggle connection").create()));
        status.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, twitch.isAccountConnected() ? "/twitch unlink" : "/twitch link"));

        utils.sendMessage(p, new BaseComponent[] {msg, status});
    }
}
