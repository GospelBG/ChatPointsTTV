package me.gosdev.chatpointsttv;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.twitch4j.common.enums.SubscriptionPlan;

import me.gosdev.chatpointsttv.Commands.AccountsCommand;
import me.gosdev.chatpointsttv.Commands.LinkCommand;
import me.gosdev.chatpointsttv.Commands.StatusCommand;
import me.gosdev.chatpointsttv.Commands.TestCommand;
import me.gosdev.chatpointsttv.Utils.Channel;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CommandController implements TabExecutor {
    private final BaseComponent helpMsg = new ComponentBuilder("---------- " + ChatColor.DARK_PURPLE + ChatColor.BOLD + "ChatPointsTTV help" + ChatColor.RESET + " ----------\n" + 
        ChatColor.GRAY + "Usage: " + Bukkit.getPluginCommand("twitch").getUsage() + ChatColor.RESET + "\n" +
        ChatColor.LIGHT_PURPLE + "/twitch accounts: " + ChatColor.RESET + "Manage linked accounts.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch link: " + ChatColor.RESET + "Use this command to link a Twitch account.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch unlink [username]: " + ChatColor.RESET + "Removes an account and the stored credentials. If a username is not provided all accounts will be unlinked.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch status: " + ChatColor.RESET + "Displays information about the plugin and the Twitch client.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch start: " + ChatColor.RESET + "Starts the Twitch client and logs in to any saved accounts.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch stop: " + ChatColor.RESET + "Stops the Twitch client. All incoming events will be ignored.\n" +
        ChatColor.LIGHT_PURPLE + "/twitch reload: " + ChatColor.RESET + "Restarts the plugin and reloads configuration files.\n" + 
        ChatColor.LIGHT_PURPLE + "/twitch test <type> <...>: " + ChatColor.RESET + "Mocks an event.\n" +
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
                    LinkCommand.link(plugin, sender);                    
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
                            ChatPointsTTV.getTwitch().linkThread.join();
                        } catch (InterruptedException | NullPointerException e) {}
                        
                        LinkCommand.unlink(sender, args.length == 2 ? Optional.of(args[1]) : Optional.empty());
                    });
                    return true;

                case "accounts":
                    AccountsCommand.displayAccounts(sender);
                    return true;
                    
                case "status":
                    StatusCommand.status(sender, plugin);
                    return true;

                case "stop":
                    if (!ChatPointsTTV.getTwitch().isStarted()) {
                        sender.sendMessage(ChatColor.RED + "Twitch client is already stopped.");
                        return true;
                    }
                    ChatPointsTTV.getTwitch().stop(sender);
                    return true;

                case "start":
                    if (ChatPointsTTV.getTwitch().isStarted()) {
                        sender.sendMessage(ChatColor.RED + "Twitch client is already started.");
                        return true;
                    }
                    ChatPointsTTV.getTwitch().enable();
                    sender.sendMessage(ChatPointsTTV.msgPrefix + "Twitch client has started successfully!");
                    return true;

                case "test":
                    TestCommand.test(sender, args);
                    return true;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command: /twitch " + args[0]);
                    help(sender);
                    return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String arg, String[] args) {
        ArrayList<String> available = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();

        if (args.length == 1) {
            available.add("help");
            available.add("reload");
            available.add("status");
            if (ChatPointsTTV.getTwitch().isStarted()) {
                available.add("link");
                available.add("stop");
                available.add("accounts");
            } else {
                available.add("start");
            }
            if (ChatPointsTTV.getTwitch().isAccountConnected()) {
                available.add("test");
                available.add("accounts");
                available.add("unlink");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("link")) {
            available.add("browser");
            available.add("code");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("unlink")) {
            if (args[0].equalsIgnoreCase("unlink")) {
                for (Channel channel : ChatPointsTTV.getTwitch().getListenedChannels().values()) {
                    available.add(channel.getChannelUsername().toLowerCase());
                }
            }
        } else if (ChatPointsTTV.getTwitch().isAccountConnected() && args.length >= 2 && args[0].equalsIgnoreCase("test")) { // Test Command Arguments
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

    private void reload(ChatPointsTTV plugin) {
        ChatPointsTTV.log.info("Reloading ChatPointsTTV...");

        plugin.onDisable();
        plugin.onEnable();
    }

    private void help(CommandSender p) {
        p.spigot().sendMessage(helpMsg);
    }

}
